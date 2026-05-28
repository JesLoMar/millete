# Planned Transactions — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/PlannedTransactionService.java** — Servicio central con CRUD y procesamiento automático
- **domain/model/PlannedTransaction.java** — Entidad de dominio con validaciones
- **domain/ports/in/RegisterPlannedTransactionUseCase.java** — Interfaz de registro
- **domain/ports/in/UpdatePlannedTransactionUseCase.java** — Interfaz de actualización
- **domain/ports/in/DeletePlannedTransactionUseCase.java** — Interfaz de eliminación
- **domain/ports/in/ProcessPlannedTransactionsUseCase.java** — Interfaz de procesamiento automático
- **domain/ports/out/PlannedTransactionRepository.java** — Interfaz del repositorio
- **infrastructure/in/controller/PlannedTransactionController.java** — Controlador REST con 4 endpoints
- **infrastructure/in/controller/dto/PlannedTransactionResponseDTO.java** — DTO de respuesta
- **infrastructure/in/controller/dto/RegisterPlannedTransactionRequestDTO.java** — DTO de creación
- **infrastructure/in/controller/dto/UpdatePlannedTransactionRequestDTO.java** — DTO de actualización
- **infrastructure/out/persistence/postgresql/adapters/PlannedTransactionPostgresAdapter.java** — Adaptador
- **infrastructure/out/persistence/postgresql/entity/PlannedTransactionEntity.java** — Entidad JPA
- **infrastructure/out/persistence/postgresql/mappers/PlannedTransactionEntityMapper.java** — Mapper MapStruct
- **infrastructure/out/persistence/postgresql/repository/SpringDataPlannedTransactionRepository.java** — Repositorio

---

## PlannedTransactionController.java

Controlador REST mapeado a /api/v1/planned-transactions.

### POST /
Crea una plantilla de transacción recurrente. Valida que la categoría exista si se especifica. Responde 201 con PlannedTransactionResponseDTO.

### GET /
Lista las plantillas activas del usuario autenticado, ordenadas por fecha de inicio descendente.

### PUT /{id}
Actualiza una plantilla existente. Verifica que pertenezca al usuario. Valida la categoría si se especifica.

### DELETE /{id}
Elimina lógicamente una plantilla (soft delete). Verifica propiedad del usuario.

---

## PlannedTransactionService.java

Servicio central que implementa los 4 casos de uso.

### Registrar plantilla (register)
1. Si se especifica categoría, verifica que exista.
2. Crea la plantilla con UUID aleatorio y fechas automáticas.
3. Guarda en base de datos.

### Procesar tareas programadas (processScheduledTasks)
1. Obtiene todas las plantillas activas.
2. Para cada una, evalúa si debe ejecutarse hoy mediante shouldExecuteToday.
3. Si corresponde, crea una transacción real llamando a RegisterTransactionUseCase.
4. La transacción se crea con fecha de hoy a las 00:00 y descripción con sufijo "(Recurrente)".
5. Anotado con @Transactional para garantizar atomicidad.

### Actualizar plantilla (update)
1. Busca la plantilla por ID.
2. Verifica que pertenezca al usuario (anti-IDOR).
3. Valida la categoría si se especifica.
4. Actualiza todos los campos y modifiedAt.

### Eliminar plantilla (deleteByIdAndUserId)
1. Busca la plantilla por ID.
2. Verifica que pertenezca al usuario.
3. Marca active = false (soft delete).

### Lógica shouldExecuteToday
Actualmente compara la fecha de inicio con la fecha actual. Es un placeholder para una implementación futura que calculará intervalos (cada X días/semanas/meses/años).

---

## PlannedTransaction.java (Modelo de dominio)

### FrequencyType
Enum con valores: DAYS, WEEKS, MONTHS, YEARS.

### Constructor
Valida que el amount no sea cero. Valida que endDate no sea anterior a startDate si ambas están presentes.

### Atributos
id, userId, categoryId, amount, type (TransactionType), description, frequencyType, frequencyInterval, startDate, endDate, createdAt, modifiedAt, active.

---

## Puertos de entrada

- RegisterPlannedTransactionUseCase: register(RegisterPlannedTransactionCommand)
- UpdatePlannedTransactionUseCase: update(UUID, UpdatePlannedTransactionCommand)
- DeletePlannedTransactionUseCase: deleteByIdAndUserId(UUID, UUID)
- ProcessPlannedTransactionsUseCase: processScheduledTasks()

---

## Puerto de salida

### PlannedTransactionRepository
Define: save, findById, findAllByUserId, findAllActive.

---

## Adaptador de persistencia

### PlannedTransactionEntity
Entidad JPA mapeada a planned_transactions. Columnas: id, user_id, category_id, amount, type, description, frequency_type, frequency_interval, start_date, end_date, created_at, modified_at, active.

### PlannedTransactionEntityMapper
Mapper MapStruct con métodos cualificados para convertir entre String y Enum tanto para TransactionType como para FrequencyType.

### SpringDataPlannedTransactionRepository
Interfaz JpaRepository con métodos: findAllByUserIdOrderByStartDateDesc, findByActiveTrue.

### PlannedTransactionPostgresAdapter
Implementa PlannedTransactionRepository. findAllByUserId ordena por fecha de inicio descendente. findAllActive busca por active = true.

---

## Conexión con el scheduler

TransactionScheduler (en shared/infrastructure) ejecuta processScheduledTasks cada día a las 00:01 AM. Este método itera sobre todas las plantillas activas y crea transacciones reales para las que corresponda ejecutarse ese día.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/planned-transactions | Listar plantillas |
| POST | /api/v1/planned-transactions | Crear plantilla |
| PUT | /api/v1/planned-transactions/:id | Editar plantilla |
| DELETE | /api/v1/planned-transactions/:id | Eliminar plantilla |