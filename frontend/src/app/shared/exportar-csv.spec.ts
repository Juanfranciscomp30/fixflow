import { exportarCsv } from './exportar-csv';

describe('exportarCsv', () => {
  it('genera un CSV con ; y escapa los textos con separadores', async () => {
    let contenido: Blob | undefined;
    URL.createObjectURL = (b: Blob) => ((contenido = b), 'blob:x');
    URL.revokeObjectURL = () => undefined;

    exportarCsv('prueba.csv', [
      { codigo: 'FX-2026-00001', averia: 'No enciende; huele a quemado' },
      { codigo: 'FX-2026-00002', averia: 'Pantalla "rota"' },
    ]);

    const texto = await contenido!.text();
    expect(texto).toContain('codigo;averia');
    expect(texto).toContain('FX-2026-00001;"No enciende; huele a quemado"');
    expect(texto).toContain('FX-2026-00002;"Pantalla ""rota"""');
  });
});
