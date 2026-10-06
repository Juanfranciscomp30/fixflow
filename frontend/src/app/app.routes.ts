import { Routes } from '@angular/router';
import { authGuard, invitadoGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Entrar · FixFlow',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    // Todo lo que hay aquí dentro necesita sesión y se pinta dentro del menú lateral
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./core/layout/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tablero' },
      {
        path: 'tablero',
        title: 'Tablero · FixFlow',
        loadComponent: () => import('./features/kanban/kanban').then((m) => m.Kanban),
      },
      {
        path: 'reparaciones',
        loadChildren: () =>
          import('./features/reparaciones/reparaciones.routes').then((m) => m.REPARACIONES_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
