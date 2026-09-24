package com.juanfran.fixflow.cliente;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    protected Cliente() {
    }

    public Cliente(String nombre, String telefono, String email) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    /** Usado por el portal: el cliente se identifica con los 4 últimos dígitos de su teléfono. */
    public boolean telefonoTerminaEn(String ultimosDigitos) {
        String soloDigitos = telefono.replaceAll("\\D", "");
        return ultimosDigitos != null
                && ultimosDigitos.length() == 4
                && soloDigitos.endsWith(ultimosDigitos);
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public Instant getCreadoEn() { return creadoEn; }
}
