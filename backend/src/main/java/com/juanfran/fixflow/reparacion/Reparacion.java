package com.juanfran.fixflow.reparacion;

import com.juanfran.fixflow.common.ReglaDeNegocioException;
import com.juanfran.fixflow.equipo.Equipo;
import com.juanfran.fixflow.usuario.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reparacion")
public class Reparacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Column(name = "averia_descrita", nullable = false)
    private String averiaDescrita;

    private String diagnostico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReparacion estado = EstadoReparacion.RECIBIDO;

    @Column(precision = 10, scale = 2)
    private BigDecimal presupuesto;

    @Column(name = "presupuesto_aceptado")
    private Boolean presupuestoAceptado;

    @Column(name = "precio_final", precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "fecha_entrada", nullable = false, updatable = false)
    private Instant fechaEntrada = Instant.now();

    @Column(name = "fecha_listo")
    private Instant fechaListo;

    @Column(name = "fecha_entrega")
    private Instant fechaEntrega;

    @Version
    private Long version;

    protected Reparacion() {
    }

    public Reparacion(String codigo, Equipo equipo, String averiaDescrita, BigDecimal presupuesto) {
        this.codigo = codigo;
        this.equipo = equipo;
        this.averiaDescrita = averiaDescrita;
        this.presupuesto = presupuesto;
    }

    /**
     * Único punto por el que cambia el estado. Aplica las transiciones permitidas
     * y las reglas del taller, y registra las fechas que usa el dashboard.
     */
    public void cambiarEstado(EstadoReparacion nuevo, Instant ahora) {
        if (!estado.puedePasarA(nuevo)) {
            throw new ReglaDeNegocioException(
                    "No se puede pasar de %s a %s".formatted(estado, nuevo));
        }
        if (nuevo == EstadoReparacion.ESPERANDO_APROBACION && presupuesto == null) {
            throw new ReglaDeNegocioException("Hay que indicar un presupuesto antes de pedir aprobación");
        }
        if (estado == EstadoReparacion.ESPERANDO_APROBACION) {
            if (nuevo == EstadoReparacion.EN_REPARACION && !Boolean.TRUE.equals(presupuestoAceptado)) {
                throw new ReglaDeNegocioException("El cliente aún no ha aceptado el presupuesto");
            }
            if (nuevo == EstadoReparacion.LISTO && !Boolean.FALSE.equals(presupuestoAceptado)) {
                throw new ReglaDeNegocioException("Solo se devuelve sin reparar si el cliente rechaza el presupuesto");
            }
        }
        if (nuevo == EstadoReparacion.LISTO) {
            fechaListo = ahora;
        }
        if (nuevo == EstadoReparacion.ENTREGADO) {
            fechaEntrega = ahora;
        }
        estado = nuevo;
    }

    public void responderPresupuesto(boolean aceptado) {
        if (estado != EstadoReparacion.ESPERANDO_APROBACION) {
            throw new ReglaDeNegocioException("Esta reparación no está esperando aprobación");
        }
        this.presupuestoAceptado = aceptado;
    }

    public void asignarTecnico(Usuario tecnico) { this.tecnico = tecnico; }
    public void registrarDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public void fijarPresupuesto(BigDecimal presupuesto) { this.presupuesto = presupuesto; }
    public void fijarPrecioFinal(BigDecimal precioFinal) { this.precioFinal = precioFinal; }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public Equipo getEquipo() { return equipo; }
    public Usuario getTecnico() { return tecnico; }
    public String getAveriaDescrita() { return averiaDescrita; }
    public String getDiagnostico() { return diagnostico; }
    public EstadoReparacion getEstado() { return estado; }
    public BigDecimal getPresupuesto() { return presupuesto; }
    public Boolean getPresupuestoAceptado() { return presupuestoAceptado; }
    public BigDecimal getPrecioFinal() { return precioFinal; }
    public Instant getFechaEntrada() { return fechaEntrada; }
    public Instant getFechaListo() { return fechaListo; }
    public Instant getFechaEntrega() { return fechaEntrega; }
    public Long getVersion() { return version; }
}
