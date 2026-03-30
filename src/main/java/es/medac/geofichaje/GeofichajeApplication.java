package es.medac.geofichaje;

import es.medac.geofichaje.model.Empleado;
import es.medac.geofichaje.model.EstadoFichaje;
import es.medac.geofichaje.repository.EmpleadoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.LocalDateTime;

@SpringBootApplication
public class GeofichajeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeofichajeApplication.class, args);
    }

    /*
    	Este es nuestro SEEDER.
    	Se ejecuta automáticamente justo después de que Spring Boot arranca.
    */
    @Bean
    CommandLineRunner initDatabase(EmpleadoRepository repository) {
        return args -> {
            // Solo creamos el usuario si la tabla está vacía
            if (repository.count() == 0) {
                Empleado ferran = new Empleado();
                ferran.setNombre("Ferran Test");
                ferran.setEmail("ferran@medac.es");
                ferran.setEstado(EstadoFichaje.FUERA);
                ferran.setUltimoFichaje(LocalDateTime.now());
                
                repository.save(ferran);
            }
        };
    }
}
