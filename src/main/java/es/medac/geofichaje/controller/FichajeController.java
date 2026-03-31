package es.medac.geofichaje.controller;

import es.medac.geofichaje.model.RegistroFichaje;
import es.medac.geofichaje.repository.EmpleadoRepository;
import es.medac.geofichaje.repository.RegistroFichajeRepository;
import es.medac.geofichaje.service.FichajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/fichajes")
public class FichajeController {

    @Autowired
    private FichajeService fichajeService;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private RegistroFichajeRepository registroFichajeRepository;

    @GetMapping("/registrar")
    public String registrar(@RequestParam String email, 
                            @RequestParam double latitud, 
                            @RequestParam double longitud) {
        
        return fichajeService.registrarFichaje(email, latitud, longitud);
    }
    
    @GetMapping("/reporte")
    public FichajeService.ReporteResumen verReporte(@RequestParam String email) {
        return fichajeService.generarReporte(email);
    }

    @GetMapping("/historial")
    public List<RegistroFichaje> verHistorial(@RequestParam String email) {
        return empleadoRepository.findByEmail(email)
                .map(empleado -> registroFichajeRepository.findByEmpleado(empleado))
                .orElse(Collections.emptyList());
    }
}
