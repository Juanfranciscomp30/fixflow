import { Component, computed, input } from '@angular/core';
import { EstadoReparacion, ETIQUETA_ESTADO } from '../../../features/reparaciones/reparacion.model';

/** Etiqueta de color para un estado. Se reutilizará en el Kanban y en el portal. */
@Component({
  selector: 'app-estado-chip',
  template: `<span class="chip" [class]="'estado-' + estado()">{{ etiqueta() }}</span>`,
  styles: `
    .chip {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      white-space: nowrap;
    }
    .estado-RECIBIDO { background: #e3e8ef; color: #37474f; }
    .estado-DIAGNOSTICO { background: #dbeafe; color: #1e40af; }
    .estado-ESPERANDO_APROBACION { background: #fef3c7; color: #92400e; }
    .estado-EN_REPARACION { background: #ede9fe; color: #5b21b6; }
    .estado-LISTO { background: #dcfce7; color: #166534; }
    .estado-ENTREGADO { background: #f1f5f9; color: #64748b; }
  `,
})
export class EstadoChip {
  readonly estado = input.required<EstadoReparacion>();
  protected readonly etiqueta = computed(() => ETIQUETA_ESTADO[this.estado()]);
}
