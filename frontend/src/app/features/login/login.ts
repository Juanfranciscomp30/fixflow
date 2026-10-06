import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../core/auth/auth.service';
import { mensajeError } from '../../shared/mensaje-error';

/** Cuentas de demostración (las mismas que en V2__datos_demo.sql y el README). */
const DEMO = {
  admin: { email: 'admin@fixflow.demo', password: 'Demo1234!' },
  tecnico: { email: 'marta@fixflow.demo', password: 'Demo1234!' },
};

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;

  /** ?volver=/reparaciones/3 → tras entrar, vuelve a donde estaba (lo pone el authGuard). */
  readonly volver = input<string>();

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected readonly entrando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verPassword = signal(false);

  protected entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { email, password } = this.form.getRawValue();
    this.entrando.set(true);
    this.error.set(null);

    this.auth.login(email, password).subscribe({
      next: () => this.router.navigateByUrl(this.volver() || '/'),
      error: (e) => {
        this.entrando.set(false);
        this.error.set(mensajeError(e));
      },
    });
  }

  /** Rellena una cuenta de demo y entra: quien revisa el portfolio no tiene que copiar nada. */
  protected entrarComo(cuenta: keyof typeof DEMO): void {
    this.form.setValue(DEMO[cuenta]);
    this.entrar();
  }
}
