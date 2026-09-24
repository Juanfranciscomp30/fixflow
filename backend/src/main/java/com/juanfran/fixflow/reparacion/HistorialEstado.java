package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.usuario.Usuario;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "historial_estado")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reparacion_id", nullable = false)
    private Reparacion reparacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoReparacion estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private EstadoReparacion estadoNuevo;

    /** Null cuando el cambio lo provoca el cliente desde el portal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String comentario;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    protected HistorialEstado() {
    }

    public HistorialEstado(Reparacion reparacion, EstadoReparacion estadoAnterior,
                           EstadoReparacion estadoNuevo, Usuario usuario,
                           String comentario, Instant fecha) {
        this.reparacion = reparacion;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Reparacion getReparacion() { return reparacion; }
    public EstadoReparacion getEstadoAnterior() { return estadoAnterior; }
    public EstadoReparacion getEstadoNuevo() { return estadoNuevo; }
    public Usuario getUsuario() { return usuario; }
    public String getComentario() { return comentario; }
    public Instant getFecha() { return fecha; }
}
