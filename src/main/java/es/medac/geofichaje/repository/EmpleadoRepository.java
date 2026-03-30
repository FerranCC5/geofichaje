package es.medac.geofichaje.repository;

import es.medac.geofichaje.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/*
    Al heredar de JpaRepository, esta interfaz gana automáticamente métodos como save(), findAll(), findById() y delete().
    <Empleado, Long> le dice que vamos a gestionar objetos de tipo Empleado y que su ID (su clave primaria) es de tipo Long.
*/

@Repository // Es una etiqueta de Spring que sirve para que el sistema sepa que esta clase se encarga de hablar con la base de datos.
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    
    /*
        Al usar Optional<Empleado>, el método nos devuelve una "caja". Nosotros preguntamos: ¿Está la caja vacía? Si no lo está, sacamos al empleado.
        Es la forma profesional de evitar que tu aplicación se cierre de golpe por errores como intentar trabajar con un null si no existiera el empleado.
    */
    Optional<Empleado> findByEmail(String email);
}