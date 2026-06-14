# Planned Transactions — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/PlannedTransactionService.java** — Servicio central con CRUD, procesamiento automático, lógica de recurrencia y sistema de recuperación ante fallos (Catch-up)
- **domain/model/PlannedTransaction.java** — Entidad de dominio con validaciones y control de última ejecución
- **domain/ports/in/RegisterPlannedTransactionUseCase.java** — Interfaz de registro
- **domain/ports/in/UpdatePlannedTransactionUseCase.java** — Interfaz de actualización
- **domain/ports/in/DeletePlannedTransactionUseCase.java** — Interfaz de eliminación
- **domain/ports/in/ProcessPlannedTransactionsUseCase.java** — Interfaz de procesamiento automático
- **domain/ports/out/PlannedTransactionRepository.java** — Interfaz del repositorio
- **infrastructure/in/controller/PlannedTransactionController.java** — Controlador REST con 4 endpoints
- **infrastructure/in/controller/dto/PlannedTransactionResponseDTO.java** — DTO de respuesta
- **infrastructure/in/controller/dto/RegisterPlannedTransactionRequestDTO.java** — DTO de creación
- **infrastructure/in/controller/dto/UpdatePlannedTransactionRequestDTO.java** — DTO de actualización
- **infrastructure/out/persistence/postgresql/adapters/PlannedTransactionPostgresAdapter.java** — Adaptador de persistencia
- **infrastructure/out/persistence/postgresql/entity/PlannedTransactionEntity.java** — Entidad JPA mapeada a PostgreSQL
- **infrastructure/out/persistence/postgresql/mappers/PlannedTransactionEntityMapper.java** — Mapper MapStruct automático
- **infrastructure/out/persistence/postgresql/repository/SpringDataPlannedTransactionRepository.java** — Repositorio Spring Data JPA

---

## PlannedTransactionController.java

Controlador REST mapeado a /api/v1/planned-transactions.

### POST /
Crea una plantilla de transacción recurrente. Valida que la categoría exista si se especifica. Responde 201 con PlannedTransactionResponseDTO. Inicializa el control de ejecuciones en nulo.

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
2. Crea la plantilla con UUID aleatorio, fechas de auditoría automáticas y lastExecutedDate establecido en null.
3. Guarda en base de datos.

### Procesar tareas programadas (processScheduledTasks - Modo Catch-up)
1. Obtiene todas las plantillas activas del sistema.
2. Para cada una, calcula la primera ocurrencia pendiente usando getNextPendingExecutionDate pasándole la fecha actual como límite superior.
3. Ejecuta un bucle iterativo (while) por plantilla: mientras exista una fecha de ejecución pendiente válida anterior o igual a hoy, se procesará. Esto garantiza que si el servidor se cae durante días, se ejecuten en orden cronológico todas las transacciones acumuladas perdidas.
4. Registra mediante log.info los datos relevantes incluyendo la fecha planificada exacta a procesar.
5. Emite la transacción real llamando a RegisterTransactionUseCase. RegisterTransactionCommand recibe la fecha exacta de la ocurrencia original (atStartOfDay) y la descripción concatenada con el sufijo " (Recurring)".
6. Actualiza el atributo lastExecutedDate de la plantilla con la fecha que se acaba de generar con éxito.
7. Persiste la plantilla actualizada llamando inmediatamente al repositorio.
8. Vuelve a evaluar getNextPendingExecutionDate para determinar si restan más ejecuciones pendientes en el periodo de tiempo evaluado.
9. Todo el proceso está cubierto por la anotación @Transactional para asegurar la atomicidad operativa de las inserciones y actualizaciones.

### Actualizar plantilla (update)
1. Busca la plantilla por ID.
2. Verifica la autoría del registro con el userId del comando (mecanismo anti-IDOR).
3. Valida la categoría en caso de haberse proporcionado.
4. Detecta si hubo modificaciones en el patrón de recurrencia evaluando cambios comparativos en startDate, frequencyType o frequencyInterval.
5. Si el patrón ha cambiado, se restablece el valor de lastExecutedDate a null para evitar bloqueos matemáticos e inconsistencias de fechas, recalculando el calendario desde cero.
6. Actualiza el resto de campos correspondientes y el sello modifiedAt.
7. Guarda los cambios a través del puerto de salida.

### Eliminar plantilla (deleteByIdAndUserId)
1. Busca la plantilla por ID.
2. Verifica los permisos de propiedad del usuario.
3. Actualiza el flag active a false (soft delete) e introduce la estampa modifiedAt.

### Lógica de recurrencia y tolerancia a fallos (getNextPendingExecutionDate)

Implementación matemática optimizada que localiza y calcula la fecha cronológica de la próxima ejecución pendiente basándose en la persistencia del estado histórico (lastExecutedDate), evitando bucles de simulación día por día de alto impacto en rendimiento.

**Validaciones y cortocircuitos:**
- Si la fecha de inicio (startDate) de la plantilla es posterior a la fecha actual (today), retorna null ya que el ciclo aún no ha comenzado.
- Si la fecha calculada excede la fecha actual o supera el límite de endDate (si está configurado), retorna null.

**Algoritmo matemático de cálculo:**
1. Si lastExecutedDate es null, toma como primera fecha candidata el startDate de la plantilla.
2. Si existe un registro de lastExecutedDate previo, calcula matemáticamente cuántos periodos transcurrieron desde el startDate original hasta dicha última ejecución mediante calculatePeriodsPassed.
3. Obtiene la proyección de la fecha base sumando los periodos correspondientes (addPeriods).
4. Si la proyección matemática calculada se solapa o es menor/igual al valor de lastExecutedDate, avanza incrementalmente los periodos necesarios en un ciclo controlado hasta situar el puntero en la primera fecha estrictamente posterior a la última ejecución registrada.
5. Valida finalmente que dicha fecha no sea futura con respecto a hoy ni superior a endDate. Si cumple el criterio, la devuelve para su procesamiento.

**Soporte para tipos de frecuencia (ChronoUnit e intervalos):**
- DAYS: divide y añade unidades basándose en ChronoUnit.DAYS.
- WEEKS: divide y añade unidades basándose en ChronoUnit.WEEKS.
- MONTHS: divide y añade unidades basándose en ChronoUnit.MONTHS.
- YEARS: divide y añade unidades basándose en ChronoUnit.YEARS.

---

## PlannedTransaction.java (Modelo de dominio)

### FrequencyType
Enum con valores: DAYS, WEEKS, MONTHS, YEARS.

### Constructor
Valida que amount no sea nulo ni cero. Controla que la propiedad endDate no sea anterior de forma cronológica a startDate. Asigna todas las propiedades nativas de la plantilla de transacciones incluyendo el nuevo parámetro lastExecutedDate.

### Atributos
id, userId, categoryId, amount, type (TransactionType), description, frequencyType, frequencyInterval, startDate, endDate, createdAt, modifiedAt, active, lastExecutedDate.

---

## Puertos de entrada

- RegisterPlannedTransactionUseCase: register(RegisterPlannedTransactionCommand)
- UpdatePlannedTransactionUseCase: update(UUID, UpdatePlannedTransactionCommand)
- DeletePlannedTransactionUseCase: deleteByIdAndUserId(UUID, UUID)
- ProcessPlannedTransactionsUseCase: processScheduledTasks()

---

## Puerto de salida

### PlannedTransactionRepository
Define las firmas del contrato: save, findById, findAllByUserId, findAllActive.

---

## Adaptador de persistencia

### PlannedTransactionEntity
Entidad JPA decorada con anotaciones de Hibernate mapeada explícitamente a la tabla planned_transactions. Columnas reflejadas: id, user_id, category_id, amount, type, description, frequency_type, frequency_interval, start_date, end_date, created_at, modified_at, active, last_executed_date.

### PlannedTransactionEntityMapper
Interfaz de mapeo basada en MapStruct con estrategias cualificadas personalizadas (Named) para realizar transformaciones bidireccionales seguras entre los Enums del dominio puro y las cadenas String guardadas dentro de la persistencia relacional PostgreSQL.

### SpringDataPlannedTransactionRepository
Extensión directa de JpaRepository encargado de proveer los métodos nativos: findAllByUserIdOrderByStartDateDesc y findByActiveTrue.

### PlannedTransactionPostgresAdapter
Implementación física del puerto PlannedTransactionRepository. Delega los ciclos de persistencia en SpringDataPlannedTransactionRepository transformando las entidades mediante el mapper a objetos entendibles por las capas superiores del dominio.

---

## Conexión con el scheduler

La clase TransactionScheduler (ubicada dentro de shared/infrastructure/config/scheduler) activa el método processScheduledTasks de forma automática diariamente a las 00:01 AM. El proceso recupera todas las plantillas activas sin importar fallos anteriores, asegurando que ninguna transacción recurrente quede en el olvido si ocurriese una indisponibilidad de infraestructura.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/planned-transactions | Listar todas las plantillas del usuario |
| POST | /api/v1/planned-transactions | Registrar una nueva plantilla de recurrencia |
| PUT | /api/v1/planned-transactions/:id | Modificar datos estructurales o el patrón de la plantilla |
| DELETE | /api/v1/planned-transactions/:id | Desactivar lógicamente la plantilla (soft delete) |