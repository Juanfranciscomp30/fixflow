import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface DatosPresupuesto {
  codigo: string;
  diagnostico: string | null;
  presupuesto: number | null;
}

export interface ResultadoPresupuesto {
  diagnostico: string;
  presupuesto: number;
}

/** Al soltar en «Esperando aprobación» hace falta diagnóstico y presupuesto para el cliente. */
@Component({
  selector: 'app-dialogo-presupuesto',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Presupuesto para <span class="codigo-resguardo">{{ datos.codigo }}</span></h2>
    <form [formGroup]="form" (ngSubmit)="enviar()">
      <mat-dialog-content>
        <p class="ayuda">Esto es lo que verá el cliente para aceptar o rechazar la reparación.</p>
        <mat-form-field appearance="outline">
          <mat-label>Diagnóstico</mat-label>
          <textarea matInput formControlName="diagnostico" rows="4" cdkFocusInitial></textarea>
          <mat-error>Escribe el diagnóstico</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Presupuesto</mat-label>
          <input matInput type="number" min="0" step="0.01" formControlName="presupuesto" />
          <span matTextSuffix>€</span>
          <mat-error>Indica un importe válido</mat-error>
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancelar</button>
        <button mat-flat-button type="submit">Enviar presupuesto</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    mat-form-field { width: 100%; }
    .ayuda { margin-top: 0; color: var(--ff-tinta-suave); }
  `,
})
export class DialogoPresupuesto {
  protected readonly datos = inject<DatosPresupuesto>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<DialogoPresupuesto, ResultadoPresupuesto>);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    diagnostico: [this.datos.diagnostico ?? '', [Validators.required, Validators.maxLength(4000)]],
    presupuesto: [this.datos.presupuesto as number | null, [Validators.required, Validators.min(0)]],
  });

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { diagnostico, presupuesto } = this.form.getRawValue();
    this.ref.close({ diagnostico: diagnostico.trim(), presupuesto: presupuesto! });
  }
}
