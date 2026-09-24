import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensajeError } from '../../../shared/mensaje-error';
import { ETIQUETA_TIPO_EQUIPO, NuevaReparacion, TipoEquipo } from '../reparacion.model';
import { ReparacionService } from '../reparacion.service';

@Component({
  selector: 'app-reparacion-nueva',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './reparacion-nueva.html',
  styleUrl: './reparacion-nueva.scss',
})
export class ReparacionNueva {
  private readonly service = inject(ReparacionService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly tipos = Object.entries(ETIQUETA_TIPO_EQUIPO) as [TipoEquipo, string][];
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    cliente: this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      telefono: ['', [Validators.required, Validators.pattern(/^[+0-9 ]{9,20}$/)]],
      email: ['', [Validators.email, Validators.maxLength(150)]],
    }),
    equipo: this.fb.group({
      tipo: this.fb.control<TipoEquipo | null>(null, Validators.required),
      marca: ['', [Validators.required, Validators.maxLength(50)]],
      modelo: ['', Validators.maxLength(100)],
      numeroSerie: ['', Validators.maxLength(100)],
    }),
    averiaDescrita: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { cliente, equipo, averiaDescrita } = this.form.getRawValue();
    const datos: NuevaReparacion = {
      cliente: { nombre: cliente.nombre, telefono: cliente.telefono, email: cliente.email || null },
      equipo: {
        tipo: equipo.tipo!,
        marca: equipo.marca,
        modelo: equipo.modelo || null,
        numeroSerie: equipo.numeroSerie || null,
      },
      averiaDescrita,
    };

    this.guardando.set(true);
    this.service.recibir(datos).subscribe({
      next: (creada) => {
        this.snackBar.open(`Reparación ${creada.codigo} registrada`, 'OK', { duration: 4000 });
        this.router.navigate(['/reparaciones', creada.id]);
      },
      error: (e) => {
        this.guardando.set(false);
        this.snackBar.open(mensajeError(e), 'Cerrar');
      },
    });
  }
}
