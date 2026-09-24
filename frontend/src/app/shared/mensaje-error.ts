import { HttpErrorResponse } from '@angular/common/http';

/**
 * Saca un texto legible de un error HTTP. El backend responde con ProblemDetail
 * ({ title, detail }), así que casi siempre basta con el campo detail.
 */
export function mensajeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se puede conectar con el servidor. ¿Está arrancado el backend?';
    }
    const detalle = error.error?.detail;
    if (typeof detalle === 'string' && detalle.length > 0) {
      return detalle;
    }
  }
  return 'Ha ocurrido un error inesperado';
}
