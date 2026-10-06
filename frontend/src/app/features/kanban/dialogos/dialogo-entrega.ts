import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface DatosEntrega {
  codigo: string;
  clienteNombre: string;
  precioSugerido: number | null;
}

/** Al entregar el equipo se apunta lo que se ha cobrado (alimenta los ingresos del dashboard). */
@Component({
  selector: 'app-dialogo-entrega',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Entregar <span class="codigo-resguardo">{{ datos.codigo }}</span></h2>
    <mat-dialog-content>
      <p class="ayuda">{{ datos.clienteNombre }} recoge su equipo. ¿Cuánto se le ha cobrado?</p>
      <mat-form-field appearance="outline">
        <mat-label>Precio final</mat-label>
        <input matInput type="number" min="0" step="0.01" [formControl]="precio" cdkFocusInitial />
        <span matTextSuffix>€</span>
        <mat-error>Indica un importe válido</mat-error>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-flat-button (click)="entregar()">Marcar como entregado</button>
    </mat-dialog-actions>
  `,
  styles: `
    mat-form-field { width: 100%; }
    .ayuda { margin-top: 0; color: var(--ff-tinta-suave); }
  `,
})
export class DialogoEntrega {
  protected readonly datos = inject<DatosEntrega>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<DialogoEntrega, number>);

  protected readonly precio = new FormControl<number | null>(this.datos.precioSugerido ?? 0, [
    Validators.required,
    Validators.min(0),
  ]);

  protected entregar(): void {
    if (this.precio.invalid) {
      this.precio.markAsTouched();
      return;
    }
    this.ref.close(this.precio.value!);
  }
}
