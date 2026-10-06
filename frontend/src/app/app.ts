import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/** Raíz de la aplicación: el login va a pantalla completa y el resto dentro de Shell. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
