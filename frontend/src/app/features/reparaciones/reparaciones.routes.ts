import { Routes } from '@angular/router';

export const REPARACIONES_ROUTES: Routes = [
  {
    path: '',
    title: 'Reparaciones · FixFlow',
    loadComponent: () => import('./lista/reparaciones-lista').then((m) => m.ReparacionesLista),
  },
  {
    path: 'nueva',
    title: 'Nueva reparación · FixFlow',
    loadComponent: () => import('./nueva/reparacion-nueva').then((m) => m.ReparacionNueva),
  },
  {
    path: ':id',
    title: 'Reparación · FixFlow',
    loadComponent: () => import('./detalle/reparacion-detalle').then((m) => m.ReparacionDetalle),
  },
];
