# Planned Transactions — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/PlannedTransactionService.java** — Servicio central con CRUD, procesamiento automático y lógica de recurrencia completa
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

Servicio central que implementa los 4 casos de uso. Utiliza @Slf4j de Lombok para logging estructurado con niveles de severidad (INFO, DEBUG, ERROR).

### Registrar plantilla (register)
1. Si se especifica categoría, verifica que exista.
2. Crea la plantilla con UUID aleatorio y fechas automáticas.
3. Guarda en base de datos.

### Procesar tareas programadas (processScheduledTasks)
1. Obtiene todas las plantillas activas.
2. Para cada una, evalúa si debe ejecutarse hoy mediante shouldExecuteToday.
3. Si corresponde, registra la ejecución mediante log.info con los datos relevantes (descripción, importe, usuario).
4. Crea una transacción real llamando a RegisterTransactionUseCase.
5. La transacción se crea con fecha de hoy a las 00:00 y descripción con sufijo "(Recurring)".
6. Anotado con @Transactional para garantizar atomicidad.

### Actualizar plantilla (update)
1. Busca la plantilla por ID.
2. Verifica que pertenezca al usuario (anti-IDOR).
3. Valida la categoría si se especifica.
4. Actualiza todos los campos y modifiedAt.

### Eliminar plantilla (deleteByIdAndUserId)
1. Busca la plantilla por ID.
2. Verifica que pertenezca al usuario.
3. Marca active = false (soft delete).

### Lógica de recurrencia (shouldExecuteToday)

Implementación completa del cálculo de recurrencia que determina si una plantilla debe ejecutarse en una fecha concreta.

**Validaciones previas:**
- Si la fecha actual es anterior a startDate, retorna false.
- Si existe endDate y la fecha actual es posterior, retorna false.

**Cálculo de ocurrencias:**
1. Calcula cuántos períodos han pasado desde startDate hasta hoy usando el método calculatePeriodsPassed. Divide la diferencia temporal por el intervalo de frecuencia para obtener el número de períodos completos transcurridos.
2. Calcula la fecha de la última ocurrencia con addPeriods, multiplicando los períodos por el intervalo y sumándolos a startDate.
3. Si la fecha resultante coincide exactamente con hoy, retorna true.

**Soporte para todos los tipos de frecuencia:**
- DAYS: calcula días transcurridos y suma días.
- WEEKS: calcula semanas transcurridas y suma semanas.
- MONTHS: calcula meses transcurridos y suma meses.
- YEARS: calcula años transcurridos y suma años.

**Ejemplo de funcionamiento:**
Una plantilla con startDate = 2026-01-15, frequencyType = MONTHS y frequencyInterval = 1 se ejecutará los días 15 de enero, 15 de febrero, 15 de marzo, etc. Con frequencyInterval = 3 se ejecutaría cada 3 meses: 15 de enero, 15 de abril, 15 de julio, etc.

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

TransactionScheduler (en shared/infrastructure) ejecuta processScheduledTasks cada día a las 00:01 AM. Este método itera sobre todas las plantillas activas y crea transacciones reales para las que corresponda ejecutarse ese día según la lógica de recurrencia implementada.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/planned-transactions | Listar plantillas |
| POST | /api/v1/planned-transactions | Crear plantilla |
| PUT | /api/v1/planned-transactions/:id | Editar plantilla |
| DELETE | /api/v1/planned-transactions/:id | Eliminar plantilla |