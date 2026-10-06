import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../environments/environment';
import { exportarCsv } from '../../shared/exportar-csv';
import { mensajeError } from '../../shared/mensaje-error';
import {
  ETIQUETA_ESTADO,
  ETIQUETA_TIPO_EQUIPO,
  EstadoReparacion,
} from '../reparaciones/reparacion.model';
import { ReparacionService } from '../reparaciones/reparacion.service';
import { Metricas } from './metricas.model';

const EUROS = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' });
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

/** Colores de identidad de cada estado (los mismos que en el tablero y en los chips). */
const COLOR_ESTADO: Record<EstadoReparacion, string> = {
  RECIBIDO: '#64748b',
  DIAGNOSTICO: '#2563eb',
  ESPERANDO_APROBACION: '#b98200',
  EN_REPARACION: '#7c3aed',
  LISTO: '#16a34a',
  ENTREGADO: '#94a3b8',
};

/** Hueco: qué barra tiene el ratón encima, para el tooltip. */
interface Hover {
  texto: string;
  x: number;
  y: number;
}

@Component({
  selector: 'app-metricas',
  imports: [CurrencyPipe, DecimalPipe, MatButtonModule, MatIconModule, MatProgressBarModule],
  templateUrl: './metricas.html',
  styleUrl: './metricas.scss',
})
export class MetricasPagina {
  private readonly http = inject(HttpClient);
  private readonly reparaciones = inject(ReparacionService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly datos = signal<Metricas | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly hover = signal<Hover | null>(null);
  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly colorEstado = COLOR_ESTADO;

  /** Tiempo medio legible: "2,7 días" mejor que "66 horas". */
  protected readonly tiempoMedioDias = computed(() => (this.datos()?.tiempoMedioHoras ?? 0) / 24);

  protected readonly maxEstado = computed(() =>
    Math.max(1, ...(this.datos()?.porEstado.map((e) => e.total) ?? [])),
  );

  /** Barras de ingresos: altura en % respecto al mes con más ingresos. */
  protected readonly barrasIngresos = computed(() => {
    const meses = this.datos()?.ingresosPorMes ?? [];
    const max = Math.max(1, ...meses.map((m) => m.total));
    return meses.map((m) => {
      const [anio, mes] = m.mes.split('-').map(Number);
      return {
        etiqueta: MESES[mes - 1],
        etiquetaLarga: `${MESES[mes - 1]} ${anio}`,
        total: m.total,
        altura: (m.total / max) * 100,
        esMax: m.total === max,
        textoHover: `${MESES[mes - 1]} ${anio}: ${EUROS.format(m.total)}`,
      };
    });
  });

  protected readonly maxCarga = computed(() =>
    Math.max(1, ...(this.datos()?.cargaPorTecnico.map((t) => t.abiertas + t.entregadas) ?? [])),
  );

  constructor() {
    this.http.get<Metricas>(`${environment.apiUrl}/metricas`).subscribe({
      next: (m) => this.datos.set(m),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  protected mostrar(evento: MouseEvent, texto: string): void {
    const marco = (evento.currentTarget as HTMLElement).closest('.grafico')!.getBoundingClientRect();
    this.hover.set({ texto, x: evento.clientX - marco.left, y: evento.clientY - marco.top });
  }

  /** Exporta todas las reparaciones (con los nombres de columna en castellano). */
  protected exportar(): void {
    this.reparaciones.listar().subscribe({
      next: (lista) => {
        exportarCsv(
          `fixflow-reparaciones-${new Date().toISOString().slice(0, 10)}.csv`,
          lista.map((r) => ({
            Código: r.codigo,
            Estado: ETIQUETA_ESTADO[r.estado],
            Cliente: r.clienteNombre,
            Tipo: ETIQUETA_TIPO_EQUIPO[r.tipoEquipo],
            Equipo: r.equipo,
            Avería: r.averiaDescrita,
            Técnico: r.tecnicoNombre ?? '',
            Entrada: new Date(r.fechaEntrada).toLocaleString('es-ES'),
          })),
        );
      },
      error: (e) => this.snackBar.open(mensajeError(e), 'Cerrar'),
    });
  }
}
