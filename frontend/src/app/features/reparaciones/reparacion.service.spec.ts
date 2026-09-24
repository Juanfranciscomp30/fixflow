import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { ReparacionService } from './reparacion.service';

describe('ReparacionService', () => {
  const url = `${environment.apiUrl}/reparaciones`;
  let service: ReparacionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReparacionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista todas las reparaciones sin filtro', () => {
    service.listar().subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('filtra por estado con un parámetro de consulta', () => {
    service.listar('LISTO').subscribe();
    http.expectOne(`${url}?estado=LISTO`).flush([]);
  });

  it('cambia el estado con PATCH incluyendo el precio final', () => {
    service.cambiarEstado(3, 'ENTREGADO', { precioFinal: 50 }).subscribe();
    const req = http.expectOne(`${url}/3/estado`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ estado: 'ENTREGADO', precioFinal: 50 });
    req.flush({});
  });

  it('envía la respuesta del cliente al presupuesto', () => {
    service.responderPresupuesto(3, false).subscribe();
    const req = http.expectOne(`${url}/3/respuesta-presupuesto`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ aceptado: false });
    req.flush({});
  });
});
