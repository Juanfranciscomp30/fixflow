import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'reparaciones' },
  {
    path: 'reparaciones',
    loadChildren: () =>
      import('./features/reparaciones/reparaciones.routes').then((m) => m.REPARACIONES_ROUTES),
  },
  { path: '**', redirectTo: 'reparaciones' },
];
