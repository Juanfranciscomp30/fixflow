import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginResponse, Sesion } from './auth.model';

const CLAVE = 'fixflow.sesion';

/**
 * Guarda quién ha iniciado sesión. La sesión vive en un signal: cualquier componente
 * que lea usuario() o esAdmin() se actualiza solo al hacer login o logout.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesion = signal<Sesion | null>(this.leerSesionGuardada());

  readonly usuario = this.sesion.asReadonly();
  readonly autenticado = computed(() => this.sesion() !== null);
  readonly esAdmin = computed(() => this.sesion()?.rol === 'ADMIN');

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(
        tap((r) => {
          const sesion: Sesion = {
            token: r.token,
            nombre: r.nombre,
            rol: r.rol,
            expiraEn: Date.now() + r.expiraEnSegundos * 1000,
          };
          localStorage.setItem(CLAVE, JSON.stringify(sesion));
          this.sesion.set(sesion);
        }),
      );
  }

  logout(): void {
    localStorage.removeItem(CLAVE);
    this.sesion.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.sesion()?.token ?? null;
  }

  // Si el token guardado ya caducó, es como si no hubiera sesión
  private leerSesionGuardada(): Sesion | null {
    try {
      const guardada = localStorage.getItem(CLAVE);
      if (!guardada) return null;
      const sesion = JSON.parse(guardada) as Sesion;
      return sesion.expiraEn > Date.now() ? sesion : null;
    } catch {
      return null;
    }
  }
}
