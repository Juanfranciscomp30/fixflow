/**
 * Descarga un CSV que Excel en español abre bien a la primera:
 * separador ";" (la coma es el separador decimal) y BOM para que respete las tildes.
 */
export function exportarCsv(nombreArchivo: string, filas: Record<string, unknown>[]): void {
  if (filas.length === 0) return;
  const columnas = Object.keys(filas[0]);
  const celda = (valor: unknown): string => {
    const texto = valor === null || valor === undefined ? '' : String(valor);
    // Si el texto lleva ; comillas o saltos de línea, va entre comillas (y las comillas se duplican)
    return /[";\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
  };
  const lineas = [columnas.join(';'), ...filas.map((f) => columnas.map((c) => celda(f[c])).join(';'))];

  const blob = new Blob(['﻿' + lineas.join('\r\n')], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  URL.revokeObjectURL(url);
}
