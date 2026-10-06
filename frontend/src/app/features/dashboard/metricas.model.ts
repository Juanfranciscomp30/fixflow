import { EstadoReparacion } from '../reparaciones/reparacion.model';

/** Refleja el record MetricasDto del backend (GET /api/metricas). */
export interface Metricas {
  tiempoMedioHoras: number;
  abiertas: number;
  listasParaRecoger: number;
  ingresosUltimos30Dias: number;
  porEstado: { estado: EstadoReparacion; total: number }[];
  ingresosPorMes: { mes: string; total: number }[];
  cargaPorTecnico: { tecnico: string; abiertas: number; entregadas: number }[];
}
