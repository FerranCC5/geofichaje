package es.medac.geofichaje.controller;

import es.medac.geofichaje.service.FichajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fichajes")
public class FichajeController {

    @Autowired
    private FichajeService fichajeService;

    @GetMapping("/registrar")
    public String registrar(@RequestParam String email, 
                            @RequestParam double latitud, 
                            @RequestParam double longitud) {
        
        return fichajeService.registrarFichaje(email, latitud, longitud);
    }
    
    @GetMapping("/reporte")
    public FichajeService.ReporteResumen verReporte() {
        return fichajeService.generarReporte();
    }
}
