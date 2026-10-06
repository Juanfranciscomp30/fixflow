import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ReparacionResumen } from '../reparaciones/reparacion.model';
import { Kanban } from './kanban';

function tarjeta(id: number, estado: ReparacionResumen['estado']): ReparacionResumen {
  return {
    id,
    codigo: `FX-2026-0000${id}`,
    estado,
    clienteNombre: 'Ana López',
    tipoEquipo: 'PORTATIL',
    equipo: 'HP Pavilion',
    averiaDescrita: 'No enciende',
    tecnicoNombre: null,
    fechaEntrada: new Date().toISOString(),
  };
}

describe('Kanban', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [Kanban],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  it('pinta cada reparación en la columna de su estado', async () => {
    const fixture = TestBed.createComponent(Kanban);
    http
      .expectOne((r) => r.url.endsWith('/reparaciones'))
      .flush([tarjeta(1, 'RECIBIDO'), tarjeta(2, 'RECIBIDO'), tarjeta(3, 'LISTO')]);
    await fixture.whenStable();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.columna.estado-RECIBIDO .tarjeta').length).toBe(2);
    expect(el.querySelectorAll('.columna.estado-LISTO .tarjeta').length).toBe(1);
    expect(el.querySelector('.aviso-recoger')?.textContent).toContain('1 listo');
  });

  it('solo deja soltar en los estados permitidos', () => {
    const fixture = TestBed.createComponent(Kanban);
    const kanban = fixture.componentInstance as unknown as {
      reparaciones: () => ReparacionResumen[];
      puedeEntrar: (drag: { data: ReparacionResumen }, drop: { data: string }) => boolean;
    };
    http.expectOne((r) => r.url.endsWith('/reparaciones')).flush([tarjeta(1, 'RECIBIDO')]);

    // Un equipo recibido solo puede pasar a diagnóstico
    const r = kanban.reparaciones()[0];
    expect(kanban.puedeEntrar({ data: r }, { data: 'DIAGNOSTICO' })).toBe(true);
    expect(kanban.puedeEntrar({ data: r }, { data: 'LISTO' })).toBe(false);
  });
});
