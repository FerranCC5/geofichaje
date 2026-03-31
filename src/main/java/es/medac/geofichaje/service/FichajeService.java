package es.medac.geofichaje.service;

import es.medac.geofichaje.model.Empleado;
import es.medac.geofichaje.model.EstadoFichaje;
import es.medac.geofichaje.model.RegistroFichaje;
import es.medac.geofichaje.model.TipoFichaje;
import es.medac.geofichaje.repository.EmpleadoRepository;
import es.medac.geofichaje.repository.RegistroFichajeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
// 1. REQUISITO PDF: Implementamos la interfaz genérica <Empleado, Long>
public class FichajeService implements GenericService<Empleado, Long> {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private RegistroFichajeRepository registroFichajeRepository;

    private final double LAT_OFICINA = 40.416775; 
    private final double LON_OFICINA = -3.703790;
    private final double RADIO_MAXIMO_METROS = 200.0;

    private final ValidadorDistancia validador = (latE, lonE, latO, lonO) -> {
        double distancia = calcularDistanciaHaversine(latE, lonE, latO, lonO);
        return distancia <= RADIO_MAXIMO_METROS;
    };

    private final ValidadorHorario reglaHorario = (hora) -> {
        return hora.isAfter(LocalTime.of(8, 0)) && hora.isBefore(LocalTime.of(23, 0));
    };

    // --- MÉTODOS OBLIGATORIOS POR LA INTERFAZ GENÉRICA ---
    @Override
    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    @Override
    public Empleado guardar(Empleado entidad) {
        return empleadoRepository.save(entidad);
    }

    @Override
    public Optional<Empleado> buscarPorId(Long id) {
        return empleadoRepository.findById(id);
    }

    // --- REQUISITO PDF: CLASE ANIDADA (Nested Class) ---
    // La creamos dentro de FichajeService porque solo tiene sentido aquí.
    public static class ReporteResumen {
        public long totalDentro;
        public long totalFuera;
        public String horasTrabajadasHoy;
        public String mensaje;

        public ReporteResumen(long dentro, long fuera, String horas) {
            this.totalDentro = dentro;
            this.totalFuera = fuera;
            this.horasTrabajadasHoy = horas;
            this.mensaje = "Resumen: " + dentro + " en oficina, " + fuera + " fuera.";
        }
    }

    public String calcularHorasTrabajadas(String email) {
        Empleado empleado = empleadoRepository.findByEmail(email).orElse(null);
        if (empleado == null) {
            return "00:00";
        } 

        List<RegistroFichaje> registros = registroFichajeRepository.findByEmpleado(empleado);

        // USAMOS STREAMS (Requisito PDF)
        long minutosTotales = 0;
        LocalDateTime inicio = null;

        // Ordenamos por fecha para no liarnos
        List<RegistroFichaje> ordenados = registros.stream()
                .filter(r -> r.getFechaHora().toLocalDate().equals(LocalDate.now())) // Solo hoy
                .sorted(Comparator.comparing(RegistroFichaje::getFechaHora))
                .toList();

        for (RegistroFichaje r : ordenados) {
            if (r.getTipo() == TipoFichaje.ENTRADA) {
                inicio = r.getFechaHora();
            } else if (r.getTipo() == TipoFichaje.SALIDA && inicio != null) {
                minutosTotales += Duration.between(inicio, r.getFechaHora()).toMinutes();
                inicio = null; // Reseteamos para la siguiente pareja
            }
        }

        long horas = minutosTotales / 60;
        long minutos = minutosTotales % 60;
        return String.format("%02d:%02d", horas, minutos);
    }
    

    // Método que usa la CLASE ANIDADA y STREAMS
    public ReporteResumen generarReporte(String email) {
        List<Empleado> todos = empleadoRepository.findAll();
        
        long dentro = todos.stream()
                .filter(e -> e.getEstado() == EstadoFichaje.DENTRO)
                .count();
        
        long fuera = todos.stream()
                .filter(e -> e.getEstado() == EstadoFichaje.FUERA)
                .count();

        // Calculamos las horas del empleado que consulta
        String horas = calcularHorasTrabajadas(email);

        return new ReporteResumen(dentro, fuera, horas);
    }

    public String registrarFichaje(String email, double latitud, double longitud) {
        return empleadoRepository.findByEmail(email)
            .map(empleado -> {

                boolean distanciaOk = validador.esDistanciaValida(latitud, longitud, LAT_OFICINA, LON_OFICINA);
                boolean horarioOk = reglaHorario.esHoraPermitida(LocalTime.now());

                if (distanciaOk && horarioOk) {
                    // 1. Decidimos el nuevo estado y el TIPO de registro
                    EstadoFichaje nuevoEstado;
                    TipoFichaje tipoAccion;

                    if (empleado.getEstado() == EstadoFichaje.FUERA) {
                        nuevoEstado = EstadoFichaje.DENTRO;
                        tipoAccion = TipoFichaje.ENTRADA;
                    } else {
                        nuevoEstado = EstadoFichaje.FUERA;
                        tipoAccion = TipoFichaje.SALIDA;
                    }
                    
                    // 2. Actualizamos la ficha del empleado (Persistencia en tabla empleados)
                    empleado.setEstado(nuevoEstado);
                    empleado.setUltimoFichaje(LocalDateTime.now());
                    empleadoRepository.save(empleado);

                    // 3. ¡Novedad!: Creamos y guardamos el registro en el historial (Persistencia en tabla registros)
                    RegistroFichaje nuevoRegistro = new RegistroFichaje(empleado, tipoAccion, LocalDateTime.now());
                    registroFichajeRepository.save(nuevoRegistro);
                    
                    return "Fichaje OK. Registrada " + tipoAccion + " a las " + LocalTime.now().withNano(0);
                } else if (!horarioOk) {
                    return "Error: Estás fuera del horario laboral permitido.";
                } else {
                    return "Error: Estás demasiado lejos de la oficina (" + RADIO_MAXIMO_METROS + "m max).";
                }
            })
            .orElse("Error: Empleado no encontrado.");
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.1515 * 1609.344;
    }
}