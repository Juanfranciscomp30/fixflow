import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { EstadoChip } from '../../../shared/components/estado-chip/estado-chip';
import { mensajeError } from '../../../shared/mensaje-error';
import {
  ESTADOS,
  ETIQUETA_ESTADO,
  ETIQUETA_TIPO_EQUIPO,
  EstadoReparacion,
  ReparacionResumen,
} from '../reparacion.model';
import { ReparacionService } from '../reparacion.service';

@Component({
  selector: 'app-reparaciones-lista',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    EstadoChip,
  ],
  templateUrl: './reparaciones-lista.html',
  styleUrl: './reparaciones-lista.scss',
})
export class ReparacionesLista {
  private readonly service = inject(ReparacionService);
  private readonly router = inject(Router);

  protected readonly estados = ESTADOS;
  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly columnas = ['codigo', 'cliente', 'equipo', 'averia', 'estado', 'entrada'];

  protected readonly filtro = signal<EstadoReparacion | null>(null);
  protected readonly reparaciones = signal<ReparacionResumen[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.cargar();
  }

  protected filtrar(estado: EstadoReparacion | null): void {
    this.filtro.set(estado);
    this.cargar();
  }

  // Las filas de mat-table llegan sin tipo a la plantilla; este método lo recupera
  protected tipoDe(reparacion: ReparacionResumen): string {
    return ETIQUETA_TIPO_EQUIPO[reparacion.tipoEquipo];
  }

  protected abrir(reparacion: ReparacionResumen): void {
    this.router.navigate(['/reparaciones', reparacion.id]);
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.service.listar(this.filtro()).subscribe({
      next: (lista) => {
        this.reparaciones.set(lista);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeError(e));
        this.cargando.set(false);
      },
    });
  }
}
