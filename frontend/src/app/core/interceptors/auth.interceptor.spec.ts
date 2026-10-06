import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'login', children: [] }]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => backend.verify());

  function iniciarSesion(): void {
    auth.login('admin@fixflow.demo', 'Demo1234!').subscribe();
    backend.expectOne((r) => r.url.endsWith('/auth/login')).flush({
      token: 'abc.def.ghi',
      tipo: 'Bearer',
      expiraEnSegundos: 3600,
      nombre: 'Carmen Ortiz',
      rol: 'ADMIN',
    });
  }

  it('sin sesión no añade cabecera', () => {
    http.get('/api/reparaciones').subscribe();
    const req = backend.expectOne('/api/reparaciones');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('con sesión envía el token como Bearer', () => {
    iniciarSesion();
    expect(auth.esAdmin()).toBe(true);

    http.get('/api/reparaciones').subscribe();
    const req = backend.expectOne('/api/reparaciones');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc.def.ghi');
    req.flush([]);
  });

  it('un 401 cierra la sesión', () => {
    iniciarSesion();

    http.get('/api/reparaciones').subscribe({ error: () => undefined });
    backend.expectOne('/api/reparaciones').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(auth.autenticado()).toBe(false);
    expect(localStorage.getItem('fixflow.sesion')).toBeNull();
  });
});
