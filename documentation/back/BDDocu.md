# Base de datos — Esquema y migraciones

El esquema de la base de datos se gestiona mediante Flyway con migraciones evolutivas. Spring Boot usa `ddl-auto: validate`, por lo que Hibernate nunca modifica el esquema: todas las estructuras deben reflejarse en los archivos de migración.

## Archivos de migración

| Archivo | Descripción |
|---------|-------------|
| `V1__initial_schema.sql` | Esquema inicial con 9 tablas base |
| `V2__v0.1.0.sql` | Sesiones, preferencias, metas de ahorro, campos premium/Telegram, refactor `family_* → goal_*`, soporte multi-sesión y tabla de notificaciones |

---

## V1__initial_schema.sql

Migración inicial que crea el esquema base para la versión 0.0.1.

### Tablas creadas (9)

| # | Tabla | Módulo | Descripción |
|---|-------|--------|-------------|
| 1 | users | Users | Usuarios registrados. username o email obligatorio, pero no ambos. |
| 2 | family_units | Family/Goal | Unidades familiares con objetivo mensual y modo de distribución. |
| 3 | family_members | Family/Goal | Miembros de cada familia con rol, salario y porcentaje personalizado. |
| 4 | family_invitations | Family/Goal | Invitaciones pendientes con token único y fecha de expiración. |
| 5 | family_contributions | Family/Goal | Aportaciones individuales al fondo familiar. |
| 6 | categories | Categories | Categorías de gasto/ingreso con presupuesto y color. |
| 7 | transactions | Transactions | Transacciones financieras con tipo, importe y categoría opcional. |
| 8 | planned_transactions | Planned Transactions | Transacciones recurrentes con frecuencia, intervalo y rango de fechas. |
| 9 | investments | Investments | Inversiones con cantidad, precio de compra, precio actual y tipo de activo. |

### Índices creados (10)

| Índice | Tabla | Columnas | Propósito |
|--------|-------|----------|-----------|
| idx_family_members_family | family_members | family_id | Buscar miembros por familia |
| idx_family_members_user | family_members | user_id | Buscar familias de un usuario |
| idx_family_invitations_token | family_invitations | token | Buscar invitación por token |
| idx_family_invitations_family | family_invitations | family_id | Buscar invitaciones por familia |
| idx_family_contributions_family | family_contributions | family_id | Buscar aportaciones por familia |
| idx_categories_user | categories | user_id | Buscar categorías por usuario |
| idx_transactions_user_date | transactions | user_id, date | Buscar transacciones por usuario ordenadas por fecha |
| idx_transactions_category | transactions | category_id | Buscar transacciones por categoría |
| idx_planned_transactions_user | planned_transactions | user_id | Buscar transacciones programadas por usuario |
| idx_investments_user | investments | user_id | Buscar inversiones por usuario |

### Restricciones CHECK (9)

| Restricción | Tabla | Valores permitidos |
|-------------|-------|-------------------|
| chk_user_identity | users | username IS NOT NULL OR email IS NOT NULL |
| chk_distribution_mode | family_units | EQUITATIVE, PROPORTIONAL, CUSTOM |
| chk_member_role | family_members | ADMIN, MEMBER |
| chk_invitation_status | family_invitations | PENDING, ACCEPTED, EXPIRED |
| chk_budget_limit | categories | budget_limit >= 0 |
| chk_transaction_type | transactions | INCOME, EXPENSE |
| chk_planned_type | planned_transactions | INCOME, EXPENSE |
| chk_frequency_type | planned_transactions | DAYS, WEEKS, MONTHS, YEARS |
| chk_frequency_interval | planned_transactions | frequency_interval > 0 |
| chk_investment_type | investments | STOCK, CRYPTO, FUND, REAL_ESTATE, OTHER |
| chk_quantity_positive | investments | quantity > 0 |

### Estrategia de borrado (ON DELETE)

| Relación | Comportamiento | Justificación |
|----------|---------------|---------------|
| users → categories | CASCADE | Al eliminar usuario, sus categorías desaparecen |
| users → transactions | CASCADE | Al eliminar usuario, sus transacciones desaparecen |
| users → investments | CASCADE | Al eliminar usuario, sus inversiones desaparecen |
| users → family_members | CASCADE | Al eliminar usuario, sale de sus familias |
| users → family_contributions | CASCADE | Al eliminar usuario, sus aportaciones desaparecen |
| categories → transactions | SET NULL | La transacción sobrevive sin categoría |
| categories → planned_transactions | SET NULL | La transacción programada sobrevive sin categoría |
| family_units → members | CASCADE | Al eliminar familia, sus miembros desaparecen |
| family_units → invitations | CASCADE | Al eliminar familia, sus invitaciones desaparecen |
| family_units → contributions | CASCADE | Al eliminar familia, sus aportaciones desaparecen |
| family_units → categories | SET NULL | La categoría pierde el vínculo familiar |

### Restricciones UNIQUE

| Tabla | Columnas | Propósito |
|-------|----------|-----------|
| users | username | No puede haber dos usuarios con el mismo nombre |
| users | email | No puede haber dos usuarios con el mismo email |
| family_members | family_id, user_id | Un usuario no puede unirse dos veces a la misma familia |
| family_invitations | token | Cada invitación tiene un token único |

---

## V2__v0.1.0.sql

Migración principal de la versión 0.1.0. Añade nuevas tablas, campos y renombra el dominio de familias a metas grupales. También incluye los cambios de las antiguas V3 (multi-sesión) y V4 (notificaciones), fusionados en esta migración para mantener un historial limpio.

### Nuevas tablas

| # | Tabla | Descripción |
|---|-------|-------------|
| 10 | user_preferences | Preferencias de usuario en formato JSONB |
| 11 | user_sessions | Sesiones de usuario por canal (WEB/TELEGRAM) con control de intentos fallidos |
| 12 | telegram_fsm_context | Contexto de la máquina de estados del bot de Telegram |
| 13 | savings_goals | Metas de ahorro personales |
| 14 | notifications | Notificaciones internas persistentes (GOAL_INVITATION, SYSTEM) |

### user_preferences

- id (UUID, PK)
- user_id (UUID, UNIQUE, FK → users ON DELETE CASCADE)
- preferences (JSONB, NOT NULL DEFAULT '{}')
- created_at, modified_at

### user_sessions

- id (UUID, PK)
- user_id (UUID, FK → users ON DELETE CASCADE)
- channel (VARCHAR(20), CHECK 'WEB' | 'TELEGRAM')
- telegram_chat_id (BIGINT)
- login_attempts (INT, DEFAULT 0)
- blocked_until (TIMESTAMP)
- last_attempt_at (TIMESTAMP)
- active (BOOLEAN, DEFAULT TRUE) — permite cerrar sesiones remotamente
- created_at, modified_at
- Sin restricción UNIQUE por canal (eliminada para soporte multi-sesión)

### telegram_fsm_context

- id (UUID, PK)
- user_id (UUID, UNIQUE, FK → users ON DELETE CASCADE)
- current_state (VARCHAR(50))
- context_data (JSONB, DEFAULT '{}')
- created_at, modified_at

### savings_goals

- id (UUID, PK)
- user_id (UUID, FK → users ON DELETE CASCADE)
- name (VARCHAR(100))
- target_amount (DECIMAL(12,2))
- current_amount (DECIMAL(12,2), DEFAULT 0.00)
- deadline (DATE)
- priority (VARCHAR(10), CHECK 'LOW' | 'MEDIUM' | 'HIGH')
- status (VARCHAR(20), CHECK 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED')
- link (VARCHAR(500))
- active (BOOLEAN, DEFAULT TRUE)
- created_at, modified_at

### Cambios en users

- `is_premium` (BOOLEAN, DEFAULT FALSE)
- `license` (VARCHAR(100))
- `premium_tier` (VARCHAR(20), DEFAULT 'FREE', CHECK 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE')
- `telegram_chat_id` (BIGINT, UNIQUE cuando no es null)

### Cambios en planned_transactions

- `last_executed_date` (DATE) — permite el cálculo del catch-up de recurrentes.

### Cambios en family_invitations

- `email` y `token` pasan a opcionales.
- Añadidos `inviter_user_id` y `invited_user_id` (FK → users).
- Estado `EXPIRED` añadido al CHECK.

### Cambios en user_sessions (multi-sesión)

- Elimina la restricción `uq_user_channel` que impedía múltiples sesiones en el mismo canal.
- Añade la columna `active` (BOOLEAN, DEFAULT TRUE) para poder cerrar sesiones remotamente.
- Añade índice `idx_sessions_user_active` para filtrar sesiones activas por usuario.

### Notificaciones

Tabla `notifications` con soporte para notificaciones internas persistentes:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador único |
| user_id | UUID FK → users ON DELETE CASCADE | Destinatario |
| type | VARCHAR(50) | `GOAL_INVITATION` o `SYSTEM` |
| title | VARCHAR(255) | Título de la notificación |
| message | TEXT | Cuerpo del mensaje |
| metadata | JSONB DEFAULT '{}' | Datos estructurados (ej: goalId, invitationId) |
| read | BOOLEAN DEFAULT FALSE | Estado de lectura |
| action_required | BOOLEAN DEFAULT FALSE | Requiere acción del usuario |
| actioned_at | TIMESTAMP | Fecha en que se ejecutó la acción |
| created_at | TIMESTAMP | Fecha de creación |
| expires_at | TIMESTAMP | Fecha de expiración |
| active | BOOLEAN DEFAULT TRUE | Soft delete |

Índices:
- `idx_notifications_user_active` — notificaciones activas por usuario
- `idx_notifications_user_read` — no leídas y activas
- `idx_notifications_user_created` — ordenadas por fecha descendente

### Refactor family_* → goal_*

Tablas y columnas renombradas:

- `family_units` → `goal_units`
- `family_members` → `goal_members` (columna `family_id` → `goal_id`)
- `family_invitations` → `goal_invitations` (columna `family_id` → `goal_id`)
- `family_contributions` → `goal_contributions` (columna `family_id` → `goal_id`)

Se actualizan constraints, índices y foreign keys para reflejar los nuevos nombres.

### Eliminaciones

- FK `fk_categories_family` eliminada de `categories`.

---

## V3__update_sessions.sql

Migración de la versión 0.2.0 para soporte de múltiples sesiones.

### Cambios en user_sessions

- Elimina la restricción `uq_user_channel` que impedía múltiples sesiones en el mismo canal.
- Añade la columna `active` (BOOLEAN, DEFAULT TRUE).
- Añade índice `idx_sessions_user_active` para filtrar sesiones activas por usuario.

Esto permite que un usuario tenga varias sesiones WEB abiertas simultáneamente y pueda cerrarlas remotamente desde el perfil.

---

## Notas de mantenimiento

- **Nunca modificar migraciones ya ejecutadas en producción.** Si se necesita un cambio, crear una nueva migración `V<N>__description.sql`.
- Hibernate valida el esquema al arrancar. Cualquier discrepancia entre entidades JPA y tablas lanzará un error de validación.
- Los índices parciales (con `WHERE`) se usan para optimizar consultas frecuentes sobre subconjuntos pequeños (sesiones activas, chatId no nulos, notificaciones no leídas).
- Las migraciones V3 y V4 fueron fusionadas en V2 para mantener un historial limpio. En entornos de desarrollo con bases de datos limpias, solo se ejecutan V1 y V2.
