package es.medac.geofichaje.repository;

import es.medac.geofichaje.model.Empleado;
import es.medac.geofichaje.model.RegistroFichaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroFichajeRepository extends JpaRepository<RegistroFichaje, Long> {
    
    /*
        Recupera el histórico completo de eventos de fichaje asociados a un empleado.
        Esta colección constituye la base de datos para el cálculo de cómputo horario 
        mediante el procesamiento de flujos (Streams).
    */
    List<RegistroFichaje> findByEmpleado(Empleado empleado);
}