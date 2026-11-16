# Base de datos y migraciones

Flyway mantiene el esquema relacional; Hibernate usa validación del esquema y no lo crea ni lo actualiza. La autoridad es [`backend/src/main/resources/db/migration`](../../backend/src/main/resources/db/migration) junto con las entidades JPA.

## Historial

| Migración | Cambio representativo |
|---|---|
| [`V1__initial_schema.sql`](../../backend/src/main/resources/db/migration/V1__initial_schema.sql) | Esquema inicial |
| [`V2__v0.1.0.sql`](../../backend/src/main/resources/db/migration/V2__v0.1.0.sql) | Preferencias, sesiones, metas personales y notificaciones; cambios del modelo de metas compartidas |
| [`V3__v0.1.3.sql`](../../backend/src/main/resources/db/migration/V3__v0.1.3.sql) | Cambios de sesiones |
| [`V4__remove_telegram_integration.sql`](../../backend/src/main/resources/db/migration/V4__remove_telegram_integration.sql) | Retira estructuras de Telegram |
| [`V5__v0.2.0.sql`](../../backend/src/main/resources/db/migration/V5__v0.2.0.sql) | Ajustes generales y nuevo ledger de inversiones |

El historial real contiene V1–V5. V5 retira el modelo antiguo de inversiones y crea activos compartidos y personales, actividades, snapshots de holdings, lotes y consumos FIFO, posiciones derivadas, precios, FX, tracking e idempotencia. También introduce `failure_count` para transacciones planificadas y tipos de transferencia para enlazar transacciones financieras con actividades de inversión.

## Tablas y relaciones vigentes

| Área | Tablas representativas | Relaciones e invariantes |
|---|---|---|
| Identidad | `users`, `user_sessions`, `user_preferences` | Sesiones y preferencias pertenecen al usuario; preferencias de idioma/zona horaria se almacenan aparte |
| Finanzas | `categories`, `transactions` | Categoría opcional; categorías activas únicas por usuario y nombre sin distinguir mayúsculas; transacciones pertenecen al usuario |
| Recurrencia | `planned_transactions` | Plantilla de usuario, categoría opcional, estado activo y contador de fallos consecutivos |
| Metas | `savings_goals` | Objetivos personales con estado y avance |
| Metas grupales | `goal_units`, `goal_members`, `goal_invitations`, `goal_contributions` | Unidad con miembros, invitaciones y aportaciones; contribuciones restringidas a `DEPOSIT`/`WITHDRAWAL` |
| Notificaciones | `notifications` | Notificación asociada a usuario, con acción y expiración |
| Inversiones | `shared_assets`, `user_assets`, `investment_activities`, `activity_audit`, `holdings`, `lots`, `lot_consumptions`, `positions` | Actividades son fuente; lotes/posiciones proyecciones; consumos vinculan venta y lote del mismo usuario/activo |
| Mercado | `asset_prices`, `user_asset_prices`, `fx_rates`, `user_local_currency_history` | Precios globales compartidos o ligados a activo propio; periodos de divisa local no se solapan |
| Integridad del ledger | `investment_activity_requests`, `investment_tracking_settings` | Idempotencia por usuario/clave; tracking único por usuario |

En V5, `investment_activities` tiene identidad inmutable, orden único `(user_id, occurred_at, ordering_key)` y JSONB `details`; una actividad no se elimina físicamente. `activity_audit` enlaza actividad/usuario con `before_data`, `after_data`, `reason` y `changed_at`. `holdings` conserva snapshots `ACTIVE`/`SUPERSEDED` y un índice parcial limita a uno activo por usuario/activo. `lots` registra origen, cantidades original/restante, coste y moneda; `lot_consumptions` restringe que las ventas no excedan la cantidad original del lote. `positions` guarda una fila por usuario/activo con cantidad, coste, moneda, `history_incomplete` y `estimated`.

Los precios compartidos guardan OHLC, adjusted close, volumen, moneda, fuente, instante de precio y obtención. Los precios personales guardan precio unitario, moneda e instante. FX restringe tasa positiva y monedas distintas. Las transacciones vinculadas a inversión deben ser `TRANSFER_IN`/`TRANSFER_OUT` con actividad, moneda y zona horaria; un índice parcial asegura una sola transacción enlazada por actividad. Triggers impiden modificar/eliminar esos movimientos y una actividad ya enlazada.

Las migraciones V5 normalizan timestamps heredados interpretándolos en `Europe/Madrid` y convierten `transactions.date` en `DATE`; las fechas de negocio de las transacciones no equivalen a timestamps de creación/modificación, que son `TIMESTAMPTZ`.

## Mantenimiento

No se deben editar migraciones ya aplicadas: los cambios de esquema se representan en una nueva migración versionada. V5 incluye restricciones y triggers para proteger los invariantes descritos. Una discrepancia entre entidades y esquema puede impedir el arranque porque Hibernate valida el mapeo.
