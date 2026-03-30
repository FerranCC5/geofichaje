package es.medac.geofichaje.service;

import java.util.List;
import java.util.Optional;

/*
    Interfaz genérica para cumplir con el requisito del PDF.
    <T> Representa el tipo de la Entidad (ej: Empleado)
    <ID> Representa el tipo del ID (ej: Long)
*/
public interface GenericService<T, ID> {
    List<T> listarTodos();
    T guardar(T entidad);
    Optional<T> buscarPorId(ID id);
}