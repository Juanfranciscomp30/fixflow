# FixFlow

Gestor de reparaciones para talleres informáticos: recepción de equipos, flujo de estados,
tablero Kanban para técnicos, portal de seguimiento para clientes y métricas del taller.

> 🚧 En desarrollo

## Por qué este proyecto

<!-- Escríbelo con tus palabras: tu experiencia como técnico en la tienda y en Geo PC,
     qué problema veías en el día a día y qué querías resolver. Este párrafo es lo que
     hace que el proyecto sea tuyo. -->

## Stack

| Parte | Tecnologías |
|---|---|
| Frontend | Angular, Angular Material, TypeScript |
| Backend | Java 21, Spring Boot 3, Spring Data JPA, Spring Security (JWT), Flyway |
| Base de datos | PostgreSQL (Supabase) |
| Despliegue | Vercel (frontend) · Docker (backend) |

## Modelo de datos

```mermaid
erDiagram
    CLIENTE ||--o{ EQUIPO : trae
    EQUIPO ||--o{ REPARACION : tiene
    USUARIO |o--o{ REPARACION : "asignada a"
    REPARACION ||--o{ HISTORIAL_ESTADO : registra
    USUARIO |o--o{ HISTORIAL_ESTADO : "hecho por"

    CLIENTE {
        bigint id PK
        string nombre
        string telefono
        string email
    }
    EQUIPO {
        bigint id PK
        bigint cliente_id FK
        string tipo
        string marca
        string modelo
        string numero_serie
    }
    REPARACION {
        bigint id PK
        string codigo UK
        bigint equipo_id FK
        bigint tecnico_id FK
        text averia_descrita
        text diagnostico
        string estado
        decimal presupuesto
        boolean presupuesto_aceptado
        decimal precio_final
        timestamp fecha_entrada
        timestamp fecha_listo
        timestamp fecha_entrega
    }
    HISTORIAL_ESTADO {
        bigint id PK
        bigint reparacion_id FK
        string estado_anterior
        string estado_nuevo
        bigint usuario_id FK
        timestamp fecha
    }
    USUARIO {
        bigint id PK
        string nombre
        string email UK
        string rol
        boolean activo
    }
```

## Flujo de estados

```mermaid
stateDiagram-v2
    [*] --> RECIBIDO
    RECIBIDO --> DIAGNOSTICO
    DIAGNOSTICO --> ESPERANDO_APROBACION
    ESPERANDO_APROBACION --> EN_REPARACION : presupuesto aceptado
    ESPERANDO_APROBACION --> LISTO : presupuesto rechazado
    EN_REPARACION --> LISTO
    LISTO --> ENTREGADO
    ENTREGADO --> [*]
```

## Demo

| Rol | Email | Contraseña |
|---|---|---|
| Administrador | admin@fixflow.demo | Demo1234! |
| Técnico | marta@fixflow.demo | Demo1234! |

## Decisiones técnicas

- **JWT en lugar de sesiones**: el frontend (Vercel) y la API (otro servidor) están en dominios distintos,
  y un token en la cabecera `Authorization` evita depender de cookies entre dominios. La API no guarda
  estado, así que puede escalar o reiniciarse sin cerrar la sesión de nadie. Se valida con el
  *resource server* de Spring Security en lugar de un filtro hecho a mano.
- **Mismo error para email inexistente y contraseña incorrecta**, para no revelar qué usuarios existen.
- **Las reglas del taller viven en la entidad `Reparacion`** (`cambiarEstado`): no se puede pedir
  aprobación sin presupuesto ni reparar sin que el cliente acepte, llegue la petición de donde llegue.
- **Esquema gestionado con Flyway** y Hibernate en modo `validate`: la base de datos solo cambia
  mediante migraciones versionadas.
- **El cliente no tiene cuenta**: consulta su reparación con el código del resguardo y los últimos
  4 dígitos de su teléfono. En un taller real nadie se registra para recoger un portátil.
- **Historial de estados** para saber quién cambió qué y cuándo.
- **Bloqueo optimista** (`version`) para que dos técnicos no pisen la misma reparación.

## Limitaciones y próximos pasos

<!-- Ve rellenándolo según avances -->

## Ejecutar en local

Requisitos: Java 21, Node 24, Docker (para la base de datos local).

```bash
# 1. Base de datos
docker compose up -d

# 2. Backend  →  http://localhost:8080  (Swagger: /swagger-ui.html)
cd backend
mvn spring-boot:run

# 3. Frontend  →  http://localhost:4200
cd frontend
npm install
npm start
```

## Estructura

```
fixflow/
├── backend/    API REST · Spring Boot (organizado por funcionalidad)
├── frontend/   Angular · standalone components + signals
└── docker-compose.yml
```
