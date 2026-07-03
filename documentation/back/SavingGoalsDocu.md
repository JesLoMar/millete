# Savings Goals — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/SavingsGoalService.java** — CRUD completo de objetivos de ahorro con lógica de negocio y contribuciones
- **domain/model/SavingsGoal.java** — Entidad de dominio con validaciones y cálculo automático de estado
- **domain/utils/GoalPriority.java** — Enum con los valores de prioridad (LOW, MEDIUM, HIGH)
- **domain/ports/in/CreateSavingsGoalCommand.java** — Record con los datos para crear un objetivo
- **domain/ports/in/CreateSavingsGoalUseCase.java** — Interfaz de creación de objetivo
- **domain/ports/in/UpdateSavingsGoalCommand.java** — Record con los datos para actualizar un objetivo
- **domain/ports/in/UpdateSavingsGoalUseCase.java** — Interfaz de actualización de objetivo
- **domain/ports/in/AddContributionToGoalCommand.java** — Record con los datos para añadir una contribución
- **domain/ports/in/AddContributionToGoalUseCase.java** — Interfaz de contribución a un objetivo
- **domain/ports/in/ListSavingsGoalsUseCase.java** — Interfaz de listado con filtro opcional por estado
- **domain/ports/in/GetSavingsGoalUseCase.java** — Interfaz de obtención de un objetivo por ID
- **domain/ports/in/DeleteSavingsGoalUseCase.java** — Interfaz de eliminación lógica
- **domain/ports/out/SavingsGoalRepository.java** — Interfaz del repositorio con métodos de consulta por usuario y estado
- **infrastructure/in/controller/SavingsGoalController.java** — Controlador REST con 6 endpoints protegidos
- **infrastructure/in/controller/dto/CreateSavingsGoalRequestDTO.java** — DTO de creación con validaciones Jakarta
- **infrastructure/in/controller/dto/UpdateSavingsGoalRequestDTO.java** — DTO de actualización con campos opcionales
- **infrastructure/in/controller/dto/AddContributionRequestDTO.java** — DTO de contribución con validación de cantidad
- **infrastructure/in/controller/dto/SavingsGoalResponseDTO.java** — DTO de respuesta con todos los campos
- **infrastructure/out/persistence/postgresql/adapters/SavingsGoalPostgresAdapter.java** — Adaptador de persistencia
- **infrastructure/out/persistence/postgresql/entity/SavingsGoalEntity.java** — Entidad JPA mapeada a la tabla savings_goals
- **infrastructure/out/persistence/postgresql/mappers/SavingsGoalEntityMapper.java** — Mapper MapStruct entre dominio y entidad
- **infrastructure/out/persistence/postgresql/repository/JpaSavingsGoalRepository.java** — Repositorio Spring Data con queries personalizadas

---

## SavingsGoalController.java

Controlador REST mapeado a `/api/v1/savings-goals`. Todos los endpoints requieren autenticación JWT.

### POST /

Crea un nuevo objetivo de ahorro.

1. Extrae el `userId` del token JWT mediante `Authentication.getName()`.
2. Construye un `CreateSavingsGoalCommand` con los datos del `CreateSavingsGoalRequestDTO`.
3. Inicializa `currentAmount` a cero, `status` a `ACTIVE` y `priority` a `MEDIUM` si no se especifica.
4. Responde `201 Created` con `SavingsGoalResponseDTO`.

### GET /

Lista los objetivos de ahorro activos del usuario autenticado.

1. Extrae el `userId` del token JWT.
2. Soporta un query param opcional `status` para filtrar por estado (ACTIVE, PAUSED, COMPLETED, CANCELLED).
3. Si no se especifica `status`, devuelve todos los activos.
4. Filtra solo goals con `active = true` (realizado en el servicio).
5. Responde `200 OK` con una lista de `SavingsGoalResponseDTO`.

### GET /{id}

Obtiene un objetivo de ahorro específico por su ID.

1. Extrae el `userId` del token JWT.
2. Busca el goal mediante `getByIdAndUserId` — solo lo encuentra si el ID y el `userId` coinciden.
3. Si no pertenece al usuario o no está activo, lanza excepción (anti-IDOR).
4. Responde `200 OK` con `SavingsGoalResponseDTO`.

### PUT /{id}

Actualiza los datos de un objetivo de ahorro existente.

1. Extrae el `userId` del token JWT.
2. Construye un `UpdateSavingsGoalCommand` con el ID de la URL, el `userId` y los campos del `UpdateSavingsGoalRequestDTO`.
3. El servicio busca el goal por `id` + `userId` — si no coincide, lanza excepción.
4. Solo se puede actualizar si el goal está activo.
5. Los campos no enviados se ignoran (actualización parcial).
6. Responde `200 OK` con `SavingsGoalResponseDTO` actualizado.

### PATCH /{id}/contribute

Añade una contribución monetaria al objetivo, aumentando `currentAmount`.

1. Extrae el `userId` del token JWT.
2. Construye un `AddContributionToGoalCommand` con el ID de la URL, el `userId` y la cantidad del `AddContributionRequestDTO`.
3. El servicio busca el goal por `id` + `userId` — si no coincide, lanza excepción.
4. Solo se puede contribuir si el goal está activo y en estado `ACTIVE`.
5. Si tras la contribución `currentAmount >= targetAmount`, el estado cambia automáticamente a `COMPLETED`.
6. Responde `200 OK` con `SavingsGoalResponseDTO` actualizado.

### DELETE /{id}

Elimina lógicamente un objetivo de ahorro (soft delete).

1. Extrae el `userId` del token JWT.
2. El servicio busca el goal por `id` + `userId` — si no coincide, lanza excepción.
3. Marca `active = false`, actualiza `modifiedAt` y cambia el estado a `CANCELLED` si no estaba ya `COMPLETED` o `CANCELLED`.
4. Responde `204 No Content`.

---

## DTOs

### CreateSavingsGoalRequestDTO

Campos validados con Jakarta Validation:

- `name` — `@NotBlank`, `@Size(max = 100)`. Nombre del objetivo.
- `targetAmount` — `@NotNull`, `@DecimalMin(value = "0.01")`. Monto objetivo a alcanzar.
- `deadline` — `LocalDate` opcional. Fecha límite del objetivo.
- `priority` — `@Pattern(regexp = "^(LOW|MEDIUM|HIGH)$")` opcional. Prioridad del objetivo.
- `link` — `@Size(max = 500)` opcional. Enlace relacionado con el objetivo.

### UpdateSavingsGoalRequestDTO

Todos los campos son opcionales (actualización parcial):

- `name` — `@Size(max = 100)`. Nuevo nombre del objetivo.
- `targetAmount` — `@DecimalMin(value = "0.01")`. Nuevo monto objetivo.
- `deadline` — `LocalDate` opcional. Nueva fecha límite.
- `priority` — `@Pattern(regexp = "^(LOW|MEDIUM|HIGH)$")`. Nueva prioridad.
- `status` — `@Pattern(regexp = "^(ACTIVE|PAUSED|COMPLETED|CANCELLED)$")`. El usuario puede cambiar manualmente a ACTIVE, PAUSED o CANCELLED. COMPLETED se asigna automáticamente.
- `link` — `@Size(max = 500)`. Nuevo enlace.

### AddContributionRequestDTO

- `amount` — `@NotNull`, `@DecimalMin(value = "0.01")`. Cantidad a contribuir (debe ser positiva).

### SavingsGoalResponseDTO

Incluye todos los campos del objetivo: `id`, `userId`, `name`, `targetAmount`, `currentAmount`, `deadline`, `priority`, `status`, `link`, `createdAt`, `modifiedAt`, `active`.

---

## SavingsGoalService.java

Servicio central que implementa los 6 casos de uso de savings goals.

### Crear objetivo (create)

1. Genera un UUID aleatorio y timestamps automáticos.
2. Establece `currentAmount` a cero, `status` a `ACTIVE`, `priority` a `MEDIUM` (por defecto) y `active` a `true`.
3. Guarda mediante `SavingsGoalRepository`.

### Actualizar objetivo (update)

1. Busca el goal por `id` y `userId` — si no existe o no pertenece al usuario, lanza excepción.
2. Verifica que el goal esté activo.
3. Llama a `goal.updateDetails()` con los campos proporcionados. Los campos nulos no se modifican.
4. Si se cambia el estado a uno que no permite recalcular, se respeta. Si el nuevo `targetAmount` hace que `currentAmount >= targetAmount`, se marca `COMPLETED` automáticamente.
5. Guarda los cambios.

### Añadir contribución (addContribution)

1. Busca el goal por `id` y `userId` — si no existe o no pertenece al usuario, lanza excepción.
2. Verifica que el goal esté activo y en estado `ACTIVE`.
3. Llama a `goal.addContribution(amount)`, que suma la cantidad a `currentAmount` y recalcula el estado.
4. Si `currentAmount >= targetAmount`, el estado cambia automáticamente a `COMPLETED`.
5. Guarda los cambios.

### Listar objetivos (findByUserId / findByUserIdAndStatus)

1. `findByUserId`: devuelve todos los goals activos del usuario, ordenados por fecha de creación descendente.
2. `findByUserIdAndStatus`: filtra adicionalmente por el estado especificado.

### Obtener objetivo por ID (getByIdAndUserId)

1. Busca el goal por `id` y `userId`.
2. Solo devuelve goals activos.
3. Si no existe o no pertenece al usuario, lanza excepción.

### Eliminar objetivo (deleteByIdAndUserId)

1. Busca el goal por `id` y `userId`.
2. Verifica que esté activo.
3. Llama a `goal.deactivate()`, que marca `active = false`, actualiza `modifiedAt` y establece `status = CANCELLED` (salvo que ya fuera `COMPLETED` o `CANCELLED`).
4. Guarda los cambios.

---

## SavingsGoal.java (Modelo de dominio)

Entidad rica con lógica de negocio encapsulada y validaciones en el constructor y métodos de actualización.

### Constructor

- Valida que `name` no sea nulo ni vacío.
- Valida que `targetAmount` sea mayor que cero.
- Valida que `currentAmount` no sea negativo (por defecto es cero).
- Valida que `deadline`, si se proporciona, sea una fecha posterior al día actual.
- Valida que `priority` sea uno de: LOW, MEDIUM, HIGH (por defecto MEDIUM).
- Valida que `status` sea uno de: ACTIVE, PAUSED, COMPLETED, CANCELLED (por defecto ACTIVE).
- Si no se proporciona `id`, genera uno aleatorio.
- Si no se proporcionan fechas de creación/modificación, usa el instante actual.

### Método updateDetails

Actualiza los campos del objetivo solo si el valor proporcionado no es nulo. Valida cada campo antes de asignarlo. Tras actualizar, recalcula el estado: si `currentAmount >= targetAmount` y el estado es `ACTIVE` o `PAUSED`, cambia a `COMPLETED`.

### Método addContribution

1. Valida que la cantidad sea mayor que cero.
2. Suma la cantidad a `currentAmount`.
3. Actualiza `modifiedAt`.
4. Recalcula el estado automáticamente: si `currentAmount >= targetAmount`, cambia a `COMPLETED`.

### Método deactivate

1. Marca `active = false`.
2. Actualiza `modifiedAt`.
3. Si el estado no es `COMPLETED` ni `CANCELLED`, lo cambia a `CANCELLED`.

### Validaciones internas

Cada setter de validación lanza `IllegalArgumentException` con un mensaje descriptivo en español si el valor no cumple las reglas de negocio.

---

## Puertos de entrada

### CreateSavingsGoalUseCase

- `create(CreateSavingsGoalCommand)` → `SavingsGoal`
- `CreateSavingsGoalCommand`: `userId`, `name`, `targetAmount`, `deadline`, `priority`, `link`

### UpdateSavingsGoalUseCase

- `update(UpdateSavingsGoalCommand)` → `SavingsGoal`
- `UpdateSavingsGoalCommand`: `id`, `userId`, `name`, `targetAmount`, `deadline`, `priority`, `status`, `link`

### AddContributionToGoalUseCase

- `addContribution(AddContributionToGoalCommand)` → `SavingsGoal`
- `AddContributionToGoalCommand`: `goalId`, `userId`, `amount`

### ListSavingsGoalsUseCase

- `findByUserId(UUID)` → `List<SavingsGoal>`
- `findByUserIdAndStatus(UUID, String)` → `List<SavingsGoal>`

### GetSavingsGoalUseCase

- `getByIdAndUserId(UUID, UUID)` → `SavingsGoal`
- Parámetros: `id` del goal, `userId`

### DeleteSavingsGoalUseCase

- `deleteByIdAndUserId(UUID, UUID)` → `void`
- Parámetros: `id` del goal, `userId`

---

## Puerto de salida

### SavingsGoalRepository

Define los siguientes métodos:

- `save(SavingsGoal)` → `SavingsGoal`
- `findById(UUID)` → `Optional<SavingsGoal>`
- `findByIdAndUserId(UUID, UUID)` → `Optional<SavingsGoal>` — búsqueda segura por ID y propietario
- `findAllByUserId(UUID)` → `List<SavingsGoal>` — todos los goals activos del usuario
- `findAllByUserIdAndStatus(UUID, String)` → `List<SavingsGoal>` — goals activos del usuario filtrados por estado

---

## Adaptador de persistencia

### SavingsGoalEntity

Entidad JPA mapeada a la tabla `savings_goals`. Columnas:

- `id` — `UUID`, clave primaria.
- `user_id` — `UUID`, no nulo. Clave foránea a `users(id)` con `ON DELETE CASCADE`.
- `name` — `VARCHAR(100)`, no nulo.
- `target_amount` — `DECIMAL(12, 2)`, no nulo. Restricción: mayor que cero.
- `current_amount` — `DECIMAL(12, 2)`, no nulo, por defecto `0.00`. Restricción: no negativo.
- `deadline` — `DATE`, nullable.
- `priority` — `VARCHAR(10)`, no nulo, por defecto `MEDIUM`. Restricción: LOW, MEDIUM o HIGH.
- `status` — `VARCHAR(20)`, no nulo, por defecto `ACTIVE`. Restricción: ACTIVE, PAUSED, COMPLETED o CANCELLED.
- `link` — `VARCHAR(500)`, nullable.
- `created_at` — `TIMESTAMP`, no nulo.
- `modified_at` — `TIMESTAMP`, no nulo.
- `active` — `BOOLEAN`, no nulo, por defecto `TRUE`.

Usa Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`).

### SavingsGoalEntityMapper

Interfaz MapStruct anotada con `@Mapper(componentModel = "spring")`. Métodos:

- `toEntity(SavingsGoal)` → `SavingsGoalEntity`
- `toDomain(SavingsGoalEntity)` → `SavingsGoal`

### JpaSavingsGoalRepository

Interfaz que extiende `JpaRepository<SavingsGoalEntity, UUID>`. Métodos personalizados:

- `findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID)` — goals activos del usuario ordenados por fecha de creación.
- `findByUserIdAndStatusAndActiveTrue(UUID, String)` — query JPQL que filtra por usuario, estado y activo.
- `findByIdAndUserId(UUID, UUID)` — búsqueda por ID y propietario para operaciones seguras.
- `findAllByUserIdAndActiveTrue(UUID)` — todos los goals activos del usuario.

### SavingsGoalPostgresAdapter

Implementa `SavingsGoalRepository`. Traduce entre dominio y entidad usando `SavingsGoalEntityMapper`. Delega todas las operaciones en `JpaSavingsGoalRepository`.

---

## Seguridad

Todas las operaciones verifican que el `userId` extraído del token JWT coincida con el propietario del recurso:

- **Listado y creación**: solo se accede a goals del `userId` autenticado.
- **Obtención por ID, actualización, contribución y eliminación**: usan `findByIdAndUserId(id, userId)`, que solo devuelve resultado si el goal pertenece al usuario. Si no coincide, se lanza una excepción y se devuelve un error 404 (recurso no encontrado), evitando así ataques IDOR (Insecure Direct Object Reference).
- **Contribuciones**: adicionalmente validan que el goal esté en estado `ACTIVE`, impidiendo contribuciones a goals pausados, completados o cancelados.
- **Actualizaciones y eliminaciones**: validan que el goal esté activo antes de operar.

---

## Reglas de negocio

### Cambio automático de estado a COMPLETED

Cada vez que se añade una contribución o se actualiza el objetivo (cambiando `targetAmount`), el sistema recalcula el estado:

- Si `currentAmount >= targetAmount` y el estado actual es `ACTIVE` o `PAUSED`, se cambia automáticamente a `COMPLETED`.
- Este cambio es irreversible desde el punto de vista del usuario (no se puede volver a ACTIVE manualmente).

### Estados gestionables por el usuario

- El usuario puede cambiar manualmente entre `ACTIVE`, `PAUSED` y `CANCELLED` mediante el endpoint de actualización.
- `COMPLETED` solo se asigna automáticamente.
- Un goal cancelado (`CANCELLED`) no se puede reactivar — se debe crear uno nuevo.

### Validación de deadline

- Si se proporciona una fecha límite, debe ser estrictamente posterior a la fecha actual.
- No hay comportamiento automático asociado al vencimiento de la deadline en esta versión.

### Soft delete

- La eliminación es lógica: marca `active = false` y cambia el estado a `CANCELLED` (si no estaba ya `COMPLETED` o `CANCELLED`).
- Los goals inactivos no aparecen en los listados ni se pueden modificar.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| POST | /api/v1/savings-goals | Crear nuevo objetivo de ahorro |
| GET | /api/v1/savings-goals | Listar objetivos activos (opcional: ?status=ACTIVE) |
| GET | /api/v1/savings-goals/:id | Obtener detalle de un objetivo |
| PUT | /api/v1/savings-goals/:id | Actualizar datos del objetivo |
| PATCH | /api/v1/savings-goals/:id/contribute | Añadir una contribución al objetivo |
| DELETE | /api/v1/savings-goals/:id | Eliminar (desactivar) objetivo |

---

## Conexión con el Dashboard

El módulo de dashboard (`DashboardService`) lee las metas de ahorro reales del usuario desde la tabla `savings_goals` mediante `SavingsGoalRepository.findAllByUserId`. El dashboard muestra el progreso actual de cada meta (currentAmount / targetAmount) sin metas estimadas.
