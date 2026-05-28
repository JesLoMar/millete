# V1__initial_schema.sql — Esquema inicial de base de datos

## Descripción

Migración única que crea el esquema completo de la base de datos para la versión 0.0.1 de Millete. Incluye todas las tablas, índices, restricciones CHECK e integridad referencial necesarias para el funcionamiento de la aplicación.

## Tablas creadas (9)

| # | Tabla | Módulo | Descripción |
|---|-------|--------|-------------|
| 1 | users | Users | Usuarios registrados. username o email obligatorio, pero no ambos. |
| 2 | family_units | Family | Unidades familiares con objetivo mensual y modo de distribución. |
| 3 | family_members | Family | Miembros de cada familia con rol, salario y porcentaje personalizado. |
| 4 | family_invitations | Family | Invitaciones pendientes con token único y fecha de expiración. |
| 5 | family_contributions | Family | Aportaciones individuales al fondo familiar. |
| 6 | categories | Categories | Categorías de gasto/ingreso con presupuesto y color. |
| 7 | transactions | Transactions | Transacciones financieras con tipo, importe y categoría opcional. |
| 8 | planned_transactions | Planned Transactions | Transacciones recurrentes con frecuencia, intervalo y rango de fechas. |
| 9 | investments | Investments | Inversiones con cantidad, precio de compra, precio actual y tipo de activo. |

## Índices creados (10)

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

## Restricciones CHECK (9)

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

## Estrategia de borrado (ON DELETE)

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

## Restricciones UNIQUE

| Tabla | Columnas | Propósito |
|-------|----------|-----------|
| users | username | No puede haber dos usuarios con el mismo nombre |
| users | email | No puede haber dos usuarios con el mismo email |
| family_members | family_id, user_id | Un usuario no puede unirse dos veces a la misma familia |
| family_invitations | token | Cada invitación tiene un token único |