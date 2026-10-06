// Tipos que reflejan los DTOs del backend (paquete reparacion.dto)

export type EstadoReparacion =
  | 'RECIBIDO'
  | 'DIAGNOSTICO'
  | 'ESPERANDO_APROBACION'
  | 'EN_REPARACION'
  | 'LISTO'
  | 'ENTREGADO';

export type TipoEquipo = 'PORTATIL' | 'SOBREMESA' | 'MOVIL' | 'TABLET' | 'OTRO';

export const ESTADOS: EstadoReparacion[] = [
  'RECIBIDO',
  'DIAGNOSTICO',
  'ESPERANDO_APROBACION',
  'EN_REPARACION',
  'LISTO',
  'ENTREGADO',
];

/**
 * Saltos permitidos entre estados. Es una copia de EstadoReparacion.siguientesPermitidos()
 * del backend: aquí solo sirve para que el Kanban no deje soltar una tarjeta donde no toca.
 * Quien manda de verdad es la API.
 */
export const TRANSICIONES: Record<EstadoReparacion, EstadoReparacion[]> = {
  RECIBIDO: ['DIAGNOSTICO'],
  DIAGNOSTICO: ['ESPERANDO_APROBACION'],
  ESPERANDO_APROBACION: ['EN_REPARACION', 'LISTO'],
  EN_REPARACION: ['LISTO'],
  LISTO: ['ENTREGADO'],
  ENTREGADO: [],
};

export const ETIQUETA_ESTADO: Record<EstadoReparacion, string> = {
  RECIBIDO: 'Recibido',
  DIAGNOSTICO: 'En diagnóstico',
  ESPERANDO_APROBACION: 'Esperando aprobación',
  EN_REPARACION: 'En reparación',
  LISTO: 'Listo para recoger',
  ENTREGADO: 'Entregado',
};

export const ETIQUETA_TIPO_EQUIPO: Record<TipoEquipo, string> = {
  PORTATIL: 'Portátil',
  SOBREMESA: 'Sobremesa',
  MOVIL: 'Móvil',
  TABLET: 'Tablet',
  OTRO: 'Otro',
};

export interface ReparacionResumen {
  id: number;
  codigo: string;
  estado: EstadoReparacion;
  clienteNombre: string;
  tipoEquipo: TipoEquipo;
  equipo: string;
  averiaDescrita: string;
  tecnicoNombre: string | null;
  fechaEntrada: string;
}

export interface HistorialEstado {
  estadoAnterior: EstadoReparacion | null;
  estadoNuevo: EstadoReparacion;
  usuarioNombre: string | null;
  comentario: string | null;
  fecha: string;
}

export interface ReparacionDetalle {
  id: number;
  codigo: string;
  estado: EstadoReparacion;
  siguientesEstados: EstadoReparacion[];
  cliente: { id: number; nombre: string; telefono: string; email: string | null };
  equipo: {
    id: number;
    tipo: TipoEquipo;
    marca: string;
    modelo: string | null;
    numeroSerie: string | null;
  };
  averiaDescrita: string;
  diagnostico: string | null;
  presupuesto: number | null;
  presupuestoAceptado: boolean | null;
  precioFinal: number | null;
  tecnicoNombre: string | null;
  fechaEntrada: string;
  fechaListo: string | null;
  fechaEntrega: string | null;
  historial: HistorialEstado[];
}

export interface NuevaReparacion {
  cliente: { nombre: string; telefono: string; email: string | null };
  equipo: { tipo: TipoEquipo; marca: string; modelo: string | null; numeroSerie: string | null };
  averiaDescrita: string;
}

export interface Tecnico {
  id: number;
  nombre: string;
}
