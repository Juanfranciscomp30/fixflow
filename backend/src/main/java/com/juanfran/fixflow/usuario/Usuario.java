package com.juanfran.fixflow.usuario;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    protected Usuario() {
        // Requerido por JPA
    }

    public Usuario(String nombre, String email, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public Instant getCreadoEn() { return creadoEn; }
}
