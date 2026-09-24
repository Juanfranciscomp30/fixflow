import { Component, OnInit, inject, input, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EstadoChip } from '../../../shared/components/estado-chip/estado-chip';
import { mensajeError } from '../../../shared/mensaje-error';
import {
  ETIQUETA_ESTADO,
  ETIQUETA_TIPO_EQUIPO,
  EstadoReparacion,
  ReparacionDetalle as Detalle,
} from '../reparacion.model';
import { ReparacionService } from '../reparacion.service';

@Component({
  selector: 'app-reparacion-detalle',
  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    EstadoChip,
  ],
  templateUrl: './reparacion-detalle.html',
  styleUrl: './reparacion-detalle.scss',
})
export class ReparacionDetalle implements OnInit {
  private readonly service = inject(ReparacionService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder).nonNullable;

  /** Viene del parámetro :id de la ruta (withComponentInputBinding). */
  readonly id = input.required<string>();

  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly etiquetaTipo = ETIQUETA_TIPO_EQUIPO;

  protected readonly reparacion = signal<Detalle | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly procesando = signal(false);

  protected readonly diagnosticoForm = this.fb.group({
    diagnostico: ['', [Validators.required, Validators.maxLength(4000)]],
    presupuesto: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
  });

  protected readonly precioFinal = this.fb.control<number | null>(null, Validators.min(0));

  ngOnInit(): void {
    this.service.obtener(Number(this.id())).subscribe({
      next: (r) => this.mostrar(r),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  protected avanzar(estado: EstadoReparacion): void {
    const r = this.reparacion()!;
    this.ejecutar(this.service.cambiarEstado(r.id, estado), `Pasa a «${ETIQUETA_ESTADO[estado]}»`);
  }

  protected guardarDiagnostico(): void {
    if (this.diagnosticoForm.invalid) {
      this.diagnosticoForm.markAllAsTouched();
      return;
    }
    const { diagnostico, presupuesto } = this.diagnosticoForm.getRawValue();
    const r = this.reparacion()!;
    this.ejecutar(this.service.registrarDiagnostico(r.id, diagnostico, presupuesto!), 'Diagnóstico guardado');
  }

  protected responderPresupuesto(aceptado: boolean): void {
    const r = this.reparacion()!;
    this.ejecutar(
      this.service.responderPresupuesto(r.id, aceptado),
      aceptado ? 'Presupuesto aceptado: a reparar' : 'Presupuesto rechazado: listo para devolver',
    );
  }

  protected entregar(): void {
    if (this.precioFinal.invalid) {
      return;
    }
    const r = this.reparacion()!;
    this.ejecutar(
      this.service.cambiarEstado(r.id, 'ENTREGADO', { precioFinal: this.precioFinal.value }),
      'Equipo entregado al cliente',
    );
  }

  private ejecutar(peticion: Observable<Detalle>, mensajeOk: string): void {
    this.procesando.set(true);
    peticion.subscribe({
      next: (r) => {
        this.mostrar(r);
        this.procesando.set(false);
        this.snackBar.open(mensajeOk, undefined, { duration: 3000 });
      },
      error: (e) => {
        this.procesando.set(false);
        this.snackBar.open(mensajeError(e), 'Cerrar');
      },
    });
  }

  private mostrar(r: Detalle): void {
    this.reparacion.set(r);
    this.diagnosticoForm.reset({ diagnostico: r.diagnostico ?? '', presupuesto: r.presupuesto });
    // Si el cliente aceptó, lo normal es cobrar lo presupuestado; si rechazó, 0 (o lo que cobre el taller)
    this.precioFinal.setValue(r.precioFinal ?? (r.presupuestoAceptado ? r.presupuesto : 0));
  }
}
