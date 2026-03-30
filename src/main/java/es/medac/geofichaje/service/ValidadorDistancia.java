package es.medac.geofichaje.service;

@FunctionalInterface
public interface ValidadorDistancia {
    boolean esDistanciaValida(double latEmpleado, double lonEmpleado, double latOficina, double lonOficina);
}