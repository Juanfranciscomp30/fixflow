-- =========================================================
-- FixFlow · V1 · Esquema inicial
-- =========================================================

-- Usuarios internos del taller (admin y técnicos)
CREATE TABLE usuario (
    id             BIGSERIAL    PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    rol            VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'TECNICO')),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Clientes del taller (no tienen cuenta, consultan con código + teléfono)
CREATE TABLE cliente (
    id         BIGSERIAL    PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    telefono   VARCHAR(20)  NOT NULL,
    email      VARCHAR(150),
    creado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Equipos que trae cada cliente (un cliente puede traer varios)
CREATE TABLE equipo (
    id            BIGSERIAL    PRIMARY KEY,
    cliente_id    BIGINT       NOT NULL REFERENCES cliente (id),
    tipo          VARCHAR(20)  NOT NULL CHECK (tipo IN ('PORTATIL', 'SOBREMESA', 'MOVIL', 'TABLET', 'OTRO')),
    marca         VARCHAR(50)  NOT NULL,
    modelo        VARCHAR(100),
    numero_serie  VARCHAR(100),
    creado_en     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Secuencia para generar códigos legibles tipo FX-2026-00042
CREATE SEQUENCE reparacion_codigo_seq START 1;

-- Cada entrada de un equipo en el taller
CREATE TABLE reparacion (
    id                    BIGSERIAL      PRIMARY KEY,
    codigo                VARCHAR(20)    NOT NULL UNIQUE,
    equipo_id             BIGINT         NOT NULL REFERENCES equipo (id),
    tecnico_id            BIGINT         REFERENCES usuario (id),
    averia_descrita       TEXT           NOT NULL,
    diagnostico           TEXT,
    estado                VARCHAR(30)    NOT NULL DEFAULT 'RECIBIDO'
                          CHECK (estado IN ('RECIBIDO', 'DIAGNOSTICO', 'ESPERANDO_APROBACION',
                                            'EN_REPARACION', 'LISTO', 'ENTREGADO')),
    presupuesto           NUMERIC(10, 2) CHECK (presupuesto >= 0),
    presupuesto_aceptado  BOOLEAN,       -- NULL = el cliente aún no ha respondido
    precio_final          NUMERIC(10, 2) CHECK (precio_final >= 0),
    fecha_entrada         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    fecha_listo           TIMESTAMPTZ,   -- para calcular el tiempo medio de reparación
    fecha_entrega         TIMESTAMPTZ,
    version               BIGINT         NOT NULL DEFAULT 0  -- bloqueo optimista (@Version)
);

-- Auditoría: cada cambio de estado queda registrado
CREATE TABLE historial_estado (
    id               BIGSERIAL    PRIMARY KEY,
    reparacion_id    BIGINT       NOT NULL REFERENCES reparacion (id) ON DELETE CASCADE,
    estado_anterior  VARCHAR(30),               -- NULL en el alta
    estado_nuevo     VARCHAR(30)  NOT NULL,
    usuario_id       BIGINT       REFERENCES usuario (id),  -- NULL si lo hizo el cliente desde el portal
    comentario       TEXT,
    fecha            TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Índices para las consultas más habituales
CREATE INDEX idx_equipo_cliente        ON equipo (cliente_id);
CREATE INDEX idx_reparacion_estado     ON reparacion (estado);
CREATE INDEX idx_reparacion_tecnico    ON reparacion (tecnico_id);
CREATE INDEX idx_historial_reparacion  ON historial_estado (reparacion_id);
