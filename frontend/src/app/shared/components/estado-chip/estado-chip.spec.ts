import { TestBed } from '@angular/core/testing';
import { EstadoChip } from './estado-chip';

describe('EstadoChip', () => {
  it('muestra la etiqueta legible y la clase de color del estado', async () => {
    const fixture = TestBed.createComponent(EstadoChip);
    fixture.componentRef.setInput('estado', 'ESPERANDO_APROBACION');
    await fixture.whenStable();

    const chip = (fixture.nativeElement as HTMLElement).querySelector('.chip')!;
    expect(chip.textContent).toBe('Esperando aprobación');
    expect(chip.classList).toContain('estado-ESPERANDO_APROBACION');
  });
});
