package com.juanfran.fixflow.equipo;

import com.juanfran.fixflow.cliente.Cliente;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEquipo tipo;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(length = 100)
    private String modelo;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    protected Equipo() {
    }

    public Equipo(Cliente cliente, TipoEquipo tipo, String marca, String modelo, String numeroSerie) {
        this.cliente = cliente;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public TipoEquipo getTipo() { return tipo; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getNumeroSerie() { return numeroSerie; }
    public Instant getCreadoEn() { return creadoEn; }
}
