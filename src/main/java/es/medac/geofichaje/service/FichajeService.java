package es.medac.geofichaje.service;

import es.medac.geofichaje.model.Empleado;
import es.medac.geofichaje.model.EstadoFichaje;
import es.medac.geofichaje.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FichajeService {

    @Autowired // Inyecta automáticamente el Repositorio que creamos antes.
    private EmpleadoRepository empleadoRepository;

    // Coordenadas de la oficina (puedes poner las de tu centro MEDAC)
    private final double LAT_OFICINA = 40.416775; 
    private final double LON_OFICINA = -3.703790;
    private final double RADIO_MAXIMO_METROS = 200.0;

    // Implementación de nuestra Interface Funcional usando Lambda
    private final ValidadorDistancia validador = (latE, lonE, latO, lonO) -> {
        double distancia = calcularDistanciaHaversine(latE, lonE, latO, lonO);
        return distancia <= RADIO_MAXIMO_METROS;
    };

    public String registrarFichaje(String email, double latitud, double longitud) {
        return empleadoRepository.findByEmail(email)
            .map(empleado -> {
                if (validador.esDistanciaValida(latitud, longitud, LAT_OFICINA, LON_OFICINA)) {
                    // Cambiamos el estado (si estaba FUERA pasa a DENTRO y viceversa)
                    EstadoFichaje nuevoEstado;

                    if (empleado.getEstado() == EstadoFichaje.FUERA) {
                        nuevoEstado = EstadoFichaje.DENTRO;
                    } else {
                        nuevoEstado = EstadoFichaje.FUERA;
                    }
                    
                    empleado.setEstado(nuevoEstado);
                    empleado.setUltimoFichaje(LocalDateTime.now());
                    empleadoRepository.save(empleado);
                    
                    return "Fichaje realizado con éxito. Estado actual: " + nuevoEstado;
                } else {
                    return "Error: Estás demasiado lejos de la oficina para fichar.";
                }
            })
            .orElse("Error: Empleado no encontrado.");
    }

    // Fórmula matemática para calcular distancia entre dos coordenadas (Haversine)
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        return dist * 60 * 1.1515 * 1609.344; // Convertido a metros
    }

    public String generarReporteEstado() {
        // Traemos a todos los empleados de la base de datos y los metemos en una Lista
        List<Empleado> todosLosEmpleados = empleadoRepository.findAll();

        // Filtramos la lista para contar cuántos están DENTRO
        long presentes = todosLosEmpleados.stream()
                .filter(e -> e.getEstado() == EstadoFichaje.DENTRO)
                .count();

        // Filtramos la lista para contar cuántos están FUERA
        long ausentes = todosLosEmpleados.stream()
                .filter(e -> e.getEstado() == EstadoFichaje.FUERA)
                .count();

        return "Reporte de hoy: " + presentes + " empleados trabajando y " + ausentes + " fuera.";
    }
}