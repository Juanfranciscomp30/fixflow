export type Rol = 'ADMIN' | 'TECNICO';

/** Respuesta de POST /api/auth/login (record LoginResponse del backend). */
export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  nombre: string;
  rol: Rol;
}

/** Lo que guardamos en el navegador para no pedir login en cada recarga. */
export interface Sesion {
  token: string;
  nombre: string;
  rol: Rol;
  /** Momento (ms desde 1970) en que caduca el token. */
  expiraEn: number;
}
