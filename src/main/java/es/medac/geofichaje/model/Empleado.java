package es.medac.geofichaje.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity  // Le decimos a Spring Data JPA que no es una clase cualquiera sino una Entidad y que tiene que tener su tabla en la base de datos
@Table(name = "empleados") 
public class Empleado {

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private EstadoFichaje estado;

    private LocalDateTime ultimoFichaje;

    // Constructor
    public Empleado(String nombre, String email, EstadoFichaje estado, LocalDateTime ultimoFichaje) {
        this.nombre = nombre;
        this.email = email;
        this.estado = estado;
        this.ultimoFichaje = ultimoFichaje;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public EstadoFichaje getEstado() {
        return estado;
    }
    public void setEstado(EstadoFichaje estado) {
        this.estado = estado;
    }

    public LocalDateTime getUltimoFichaje() {
        return ultimoFichaje;
    }
    public void setUltimoFichaje(LocalDateTime ultimoFichaje) {
        this.ultimoFichaje = ultimoFichaje;
    }

}