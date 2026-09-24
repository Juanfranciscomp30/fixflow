import { HttpErrorResponse } from '@angular/common/http';
import { mensajeError } from './mensaje-error';

describe('mensajeError', () => {
  it('usa el detail del ProblemDetail del backend', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Operación no permitida', detail: 'No se puede pasar de RECIBIDO a LISTO' },
    });
    expect(mensajeError(error)).toBe('No se puede pasar de RECIBIDO a LISTO');
  });

  it('avisa cuando el backend no responde', () => {
    expect(mensajeError(new HttpErrorResponse({ status: 0 }))).toContain('No se puede conectar');
  });

  it('da un mensaje genérico para errores desconocidos', () => {
    expect(mensajeError(new Error('boom'))).toBe('Ha ocurrido un error inesperado');
  });
});
