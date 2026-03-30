package es.medac.geofichaje.service;

import es.medac.geofichaje.model.Empleado;
import es.medac.geofichaje.model.EstadoFichaje;
import es.medac.geofichaje.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
// 1. REQUISITO PDF: Implementamos la interfaz genérica <Empleado, Long>
public class FichajeService implements GenericService<Empleado, Long> {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    private final double LAT_OFICINA = 40.416775; 
    private final double LON_OFICINA = -3.703790;
    private final double RADIO_MAXIMO_METROS = 200.0;

    private final ValidadorDistancia validador = (latE, lonE, latO, lonO) -> {
        double distancia = calcularDistanciaHaversine(latE, lonE, latO, lonO);
        return distancia <= RADIO_MAXIMO_METROS;
    };

    private final ValidadorHorario reglaHorario = (hora) -> {
        return hora.isAfter(LocalTime.of(8, 0)) && hora.isBefore(LocalTime.of(20, 0));
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
        public String mensaje;

        public ReporteResumen(long dentro, long fuera) {
            this.totalDentro = dentro;
            this.totalFuera = fuera;
            this.mensaje = "Resumen: " + dentro + " en oficina, " + fuera + " fuera.";
        }
    }

    // Método que usa la CLASE ANIDADA y STREAMS
    public ReporteResumen generarReporte() {
        List<Empleado> todos = empleadoRepository.findAll();
        
        long dentro = todos.stream()
                .filter(e -> e.getEstado() == EstadoFichaje.DENTRO)
                .count();
        
        long fuera = todos.stream()
                .filter(e -> e.getEstado() == EstadoFichaje.FUERA)
                .count();

        return new ReporteResumen(dentro, fuera);
    }

    public String registrarFichaje(String email, double latitud, double longitud) {
        return empleadoRepository.findByEmail(email)
            .map(empleado -> {

                boolean distanciaOk = validador.esDistanciaValida(latitud, longitud, LAT_OFICINA, LON_OFICINA);
                boolean horarioOk = reglaHorario.esHoraPermitida(LocalTime.now());

                if (distanciaOk && horarioOk) {

                    EstadoFichaje nuevoEstado;
                    if (empleado.getEstado() == EstadoFichaje.FUERA) {
                        nuevoEstado = EstadoFichaje.DENTRO;
                    } else {
                        nuevoEstado = EstadoFichaje.FUERA;
                    }
                    
                    empleado.setEstado(nuevoEstado);
                    empleado.setUltimoFichaje(LocalDateTime.now());
                    empleadoRepository.save(empleado);
                    
                    return "Fichaje OK. Estado: " + nuevoEstado;
                } else if (!horarioOk) {
                    return "Error: Estás fuera del horario laboral permitido.";
                } else {
                    return "Error: Estás demasiado lejos de la oficina.";
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