package es.medac.geofichaje.controller;

import es.medac.geofichaje.service.FichajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fichajes")
public class FichajeController {

    @Autowired
    private FichajeService fichajeService;

    // Ruta para realizar un fichaje (POST porque enviamos datos)
    // He usado GET para facilitar las pruebas, aunque en un entorno de producción real usaríamos POST por seguridad
    @GetMapping("/registrar")
    public String registrar(@RequestParam String email, 
                            @RequestParam double latitud, 
                            @RequestParam double longitud) {
        
        return fichajeService.registrarFichaje(email, latitud, longitud);
    }

    // Ruta para ver el reporte (GET porque solo pedimos información)
    @GetMapping("/reporte")
    public String verReporte() {
        return fichajeService.generarReporteEstado();
    }
}
