package es.medac.geofichaje.service;

import java.time.LocalTime;

@FunctionalInterface
public interface ValidadorHorario {
    boolean esHoraPermitida(LocalTime horaActual);
}