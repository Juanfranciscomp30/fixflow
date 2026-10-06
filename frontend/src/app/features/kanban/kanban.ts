import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragPlaceholder,
  CdkDropList,
  CdkDropListGroup,
} from '@angular/cdk/drag-drop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Observable, of, switchMap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { mensajeError } from '../../shared/mensaje-error';
import {
  ESTADOS,
  ETIQUETA_ESTADO,
  ETIQUETA_TIPO_EQUIPO,
  EstadoReparacion,
  ReparacionDetalle,
  ReparacionResumen,
  TRANSICIONES,
  Tecnico,
} from '../reparaciones/reparacion.model';
import { ReparacionService } from '../reparaciones/reparacion.service';
import { DatosEntrega, DialogoEntrega } from './dialogos/dialogo-entrega';
import {
  DatosPresupuesto,
  DialogoPresupuesto,
  ResultadoPresupuesto,
} from './dialogos/dialogo-presupuesto';

/** En la columna «Entregado» solo enseñamos las últimas; el resto está en el listado. */
const MAX_ENTREGADAS = 5;
const DIAS_AVISO = 7;
const MS_DIA = 86_400_000;

@Component({
  selector: 'app-kanban',
  imports: [
    RouterLink,
    CdkDropListGroup,
    CdkDropList,
    CdkDrag,
    CdkDragPlaceholder,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatProgressBarModule,
    MatTooltipModule,
  ],
  templateUrl: './kanban.html',
  styleUrl: './kanban.scss',
})
export class Kanban {
  private readonly service = inject(ReparacionService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly estados = ESTADOS;
  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly etiquetaTipo = ETIQUETA_TIPO_EQUIPO;

  protected readonly reparaciones = signal<ReparacionResumen[]>([]);
  protected readonly tecnicos = signal<Tecnico[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  /** Estado de la tarjeta que se está arrastrando (para iluminar las columnas válidas). */
  protected readonly arrastrandoDesde = signal<EstadoReparacion | null>(null);

  /** Agrupa las tarjetas por columna. Se recalcula solo cuando cambia reparaciones(). */
  protected readonly columnas = computed(() => {
    const grupos = Object.fromEntries(ESTADOS.map((e) => [e, [] as ReparacionResumen[]])) as Record<
      EstadoReparacion,
      ReparacionResumen[]
    >;
    for (const r of this.reparaciones()) grupos[r.estado].push(r);
    // Lo que más tiempo lleva esperando, arriba
    for (const e of ESTADOS) grupos[e].sort((a, b) => a.fechaEntrada.localeCompare(b.fechaEntrada));
    grupos.ENTREGADO = grupos.ENTREGADO.reverse().slice(0, MAX_ENTREGADAS);
    return grupos;
  });

  protected readonly enTaller = computed(
    () => this.reparaciones().filter((r) => r.estado !== 'ENTREGADO').length,
  );
  protected readonly listas = computed(
    () => this.reparaciones().filter((r) => r.estado === 'LISTO').length,
  );

  constructor() {
    this.service.listar().subscribe({
      next: (lista) => {
        this.reparaciones.set(lista);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeError(e));
        this.cargando.set(false);
      },
    });
    if (this.auth.esAdmin()) {
      this.service.tecnicos().subscribe((t) => this.tecnicos.set(t));
    }
  }

  // Arrow function: el CDK la llama sin "this", así que la definimos como propiedad
  protected readonly puedeEntrar = (
    drag: CdkDrag<ReparacionResumen>,
    drop: CdkDropList<EstadoReparacion>,
  ): boolean => TRANSICIONES[drag.data.estado].includes(drop.data);

  protected esDestinoValido(destino: EstadoReparacion): boolean {
    const origen = this.arrastrandoDesde();
    return origen !== null && TRANSICIONES[origen].includes(destino);
  }

  protected soltar(evento: CdkDragDrop<EstadoReparacion, EstadoReparacion, ReparacionResumen>): void {
    if (evento.previousContainer === evento.container) return;
    this.mover(evento.item.data, evento.container.data);
  }

  protected abrir(r: ReparacionResumen): void {
    this.router.navigate(['/reparaciones', r.id]);
  }

  protected asignar(r: ReparacionResumen, tecnico: Tecnico): void {
    this.service.asignarTecnico(r.id, tecnico.id).subscribe({
      next: (d) => {
        this.actualizar(r.id, { tecnicoNombre: d.tecnicoNombre });
        this.snackBar.open(`${r.codigo} asignada a ${tecnico.nombre}`, undefined, { duration: 3000 });
      },
      error: (e) => this.snackBar.open(mensajeError(e), 'Cerrar'),
    });
  }

  protected diasEnTaller(r: ReparacionResumen): number {
    return Math.floor((Date.now() - new Date(r.fechaEntrada).getTime()) / MS_DIA);
  }

  protected vaRetrasada(r: ReparacionResumen): boolean {
    return r.estado !== 'LISTO' && r.estado !== 'ENTREGADO' && this.diasEnTaller(r) >= DIAS_AVISO;
  }

  protected iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .slice(0, 2)
      .map((p) => p[0])
      .join('');
  }

  /**
   * Mueve la tarjeta al momento (UI optimista) y llama a la API.
   * Si la API dice que no, o se cancela el diálogo, la tarjeta vuelve a su sitio.
   */
  private mover(r: ReparacionResumen, destino: EstadoReparacion): void {
    const origen = r.estado;
    this.actualizar(r.id, { estado: destino });

    this.accionPara(r, origen, destino).subscribe({
      next: (detalle) => {
        if (detalle === null) {
          this.actualizar(r.id, { estado: origen }); // diálogo cancelado
          return;
        }
        this.actualizar(r.id, { estado: detalle.estado, tecnicoNombre: detalle.tecnicoNombre });
        this.snackBar.open(`${r.codigo} → ${ETIQUETA_ESTADO[detalle.estado]}`, undefined, {
          duration: 2500,
        });
      },
      error: (e) => {
        this.actualizar(r.id, { estado: origen });
        this.snackBar.open(mensajeError(e), 'Cerrar', { duration: 6000 });
      },
    });
  }

  /** Cada movimiento del tablero equivale a una operación de negocio distinta en la API. */
  private accionPara(
    r: ReparacionResumen,
    origen: EstadoReparacion,
    destino: EstadoReparacion,
  ): Observable<ReparacionDetalle | null> {
    // Desde «Esperando aprobación», mover la tarjeta es apuntar la respuesta del cliente
    if (origen === 'ESPERANDO_APROBACION') {
      return this.service.responderPresupuesto(r.id, destino === 'EN_REPARACION');
    }

    if (destino === 'ESPERANDO_APROBACION') {
      return this.service.obtener(r.id).pipe(
        switchMap((d) =>
          this.dialog
            .open<DialogoPresupuesto, DatosPresupuesto, ResultadoPresupuesto>(DialogoPresupuesto, {
              data: { codigo: d.codigo, diagnostico: d.diagnostico, presupuesto: d.presupuesto },
              width: '480px',
            })
            .afterClosed(),
        ),
        switchMap((res) =>
          res
            ? this.service
                .registrarDiagnostico(r.id, res.diagnostico, res.presupuesto)
                .pipe(switchMap(() => this.service.cambiarEstado(r.id, 'ESPERANDO_APROBACION')))
            : of(null),
        ),
      );
    }

    if (destino === 'ENTREGADO') {
      return this.service.obtener(r.id).pipe(
        switchMap((d) =>
          this.dialog
            .open<DialogoEntrega, DatosEntrega, number>(DialogoEntrega, {
              data: {
                codigo: d.codigo,
                clienteNombre: d.cliente.nombre,
                // Si aceptó, lo normal es cobrar lo presupuestado; si no, nada
                precioSugerido: d.presupuestoAceptado ? d.presupuesto : 0,
              },
              width: '420px',
            })
            .afterClosed(),
        ),
        switchMap((precio) =>
          precio === undefined
            ? of(null)
            : this.service.cambiarEstado(r.id, 'ENTREGADO', { precioFinal: precio }),
        ),
      );
    }

    return this.service.cambiarEstado(r.id, destino);
  }

  private actualizar(id: number, cambios: Partial<ReparacionResumen>): void {
    this.reparaciones.update((lista) => lista.map((r) => (r.id === id ? { ...r, ...cambios } : r)));
  }
}
