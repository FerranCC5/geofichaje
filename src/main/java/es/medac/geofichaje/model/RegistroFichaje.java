package es.medac.geofichaje.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_fichajes")
public class RegistroFichaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private TipoFichaje tipo;

    // Relación: Muchos registros pertenecen a un solo empleado
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    // Constructores
    public RegistroFichaje() {

    }

    public RegistroFichaje(Empleado empleado, TipoFichaje tipo, LocalDateTime fechaHora) {
        this.empleado = empleado;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoFichaje getTipo() {
        return tipo;
    }
    public void setTipo(TipoFichaje tipo) {
        this.tipo = tipo;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
}
