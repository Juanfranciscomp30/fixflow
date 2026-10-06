import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../auth/auth.service';

interface Enlace {
  ruta: string;
  texto: string;
  icono: string;
  soloAdmin?: boolean;
}

const ENLACES: Enlace[] = [
  { ruta: '/tablero', texto: 'Tablero', icono: 'view_kanban' },
  { ruta: '/reparaciones', texto: 'Reparaciones', icono: 'list_alt' },
  { ruta: '/reparaciones/nueva', texto: 'Nueva recepción', icono: 'add_box' },
  { ruta: '/metricas', texto: 'Métricas', icono: 'monitoring', soloAdmin: true },
];

/** Marco de la aplicación con sesión iniciada: menú lateral + contenido. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  protected readonly auth = inject(AuthService);

  // computed: si cambia el rol (otro login), el menú se recalcula solo
  protected readonly enlaces = computed(() =>
    ENLACES.filter((e) => !e.soloAdmin || this.auth.esAdmin()),
  );

  protected readonly iniciales = computed(() =>
    (this.auth.usuario()?.nombre ?? '')
      .split(' ')
      .slice(0, 2)
      .map((p) => p[0])
      .join(''),
  );
}
