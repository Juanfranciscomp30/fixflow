import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EstadoReparacion,
  NuevaReparacion,
  ReparacionDetalle,
  ReparacionResumen,
  Tecnico,
} from './reparacion.model';

@Injectable({ providedIn: 'root' })
export class ReparacionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/reparaciones`;

  listar(estado?: EstadoReparacion | null): Observable<ReparacionResumen[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<ReparacionResumen[]>(this.url, { params });
  }

  obtener(id: number): Observable<ReparacionDetalle> {
    return this.http.get<ReparacionDetalle>(`${this.url}/${id}`);
  }

  recibir(datos: NuevaReparacion): Observable<ReparacionDetalle> {
    return this.http.post<ReparacionDetalle>(this.url, datos);
  }

  registrarDiagnostico(id: number, diagnostico: string, presupuesto: number): Observable<ReparacionDetalle> {
    return this.http.put<ReparacionDetalle>(`${this.url}/${id}/diagnostico`, { diagnostico, presupuesto });
  }

  cambiarEstado(
    id: number,
    estado: EstadoReparacion,
    extra: { comentario?: string | null; precioFinal?: number | null } = {},
  ): Observable<ReparacionDetalle> {
    return this.http.patch<ReparacionDetalle>(`${this.url}/${id}/estado`, { estado, ...extra });
  }

  responderPresupuesto(id: number, aceptado: boolean): Observable<ReparacionDetalle> {
    return this.http.post<ReparacionDetalle>(`${this.url}/${id}/respuesta-presupuesto`, { aceptado });
  }

  /** Solo admin: reparte la reparación a un técnico. */
  asignarTecnico(id: number, tecnicoId: number): Observable<ReparacionDetalle> {
    return this.http.patch<ReparacionDetalle>(`${this.url}/${id}/tecnico`, { tecnicoId });
  }

  tecnicos(): Observable<Tecnico[]> {
    return this.http.get<Tecnico[]>(`${environment.apiUrl}/usuarios/tecnicos`);
  }
}
