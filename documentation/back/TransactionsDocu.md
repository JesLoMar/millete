# Transactions — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/TransactionService.java** — CRUD de transacciones con guardián de presupuesto
- **application/services/TransactionMetricsService.java** — Métricas de transacciones por período
- **domain/model/Transaction.java** — Entidad de dominio con lógica de negocio
- **domain/ports/in/RegisterTransactionUseCase.java** — Interfaz de registro de transacción
- **domain/ports/in/UpdateTransactionUseCase.java** — Interfaz de actualización
- **domain/ports/in/DeleteTransactionUseCase.java** — Interfaz de eliminación
- **domain/ports/in/GetTransactionUseCase.java** — Interfaz de consulta por ID
- **domain/ports/in/ListTransactionsUseCase.java** — Interfaz de listado
- **domain/ports/in/GetTransactionMetricsUseCase.java** — Interfaz de métricas
- **domain/ports/out/TransactionRepository.java** — Interfaz del repositorio
- **infrastructure/in/controller/TransactionController.java** — Controlador REST con 6 endpoints
- **infrastructure/in/controller/dto/RegisterTransactionRequestDTO.java** — DTO de creación
- **infrastructure/in/controller/dto/UpdateTransactionRequestDTO.java** — DTO de actualización
- **infrastructure/in/controller/dto/TransactionResponseDTO.java** — DTO de respuesta
- **infrastructure/in/controller/dto/TransactionMetricsResponseDTO.java** — DTO de métricas
- **infrastructure/out/persistence/postgresql/adapters/TransactionPostgresAdapter.java** — Adaptador de persistencia
- **infrastructure/out/persistence/postgresql/entity/TransactionEntity.java** — Entidad JPA
- **infrastructure/out/persistence/postgresql/mappers/TransactionEntityMapper.java** — Mapper MapStruct
- **infrastructure/out/persistence/postgresql/repository/SpringDataTransactionRepository.java** — Repositorio Spring Data

---

## TransactionController.java

Controlador REST mapeado a /api/v1/transactions.

### GET /metrics?period=

Obtiene métricas de transacciones para el período especificado.

1. Extrae el userId del token JWT.
2. Crea MetricsCommand con userId y period.
3. Ejecuta transactionMetricsUseCase.getMetrics().
4. Responde 200 con TransactionMetricsResponseDTO.

### GET /

Lista todas las transacciones del usuario autenticado.

1. Extrae el userId del token.
2. Llama a listTransactionsUseCase.findAllByUserId().
3. Mapea cada Transaction a TransactionResponseDTO.
4. Las transacciones se devuelven ordenadas por fecha descendente.

### POST /

Crea una nueva transacción con guardián de presupuesto.

1. Extrae el userId del token.
2. Traduce RegisterTransactionRequestDTO a RegisterTransactionCommand.
3. Ejecuta registerTransactionUseCase.register().
4. Responde 201 Created con TransactionResponseDTO.

### GET /{id}

Obtiene una transacción específica. Valida que pertenezca al usuario.

### PUT /{id}

Actualiza una transacción existente. Valida propiedad y existencia de categoría.

### DELETE /{id}

Elimina lógicamente (soft delete) una transacción. La marca como inactiva.

---

## DTOs

### RegisterTransactionRequestDTO

Campos validados: categoryId (opcional), amount (@NotNull), date (@NotNull), type (@NotNull), description (@NotBlank).

### UpdateTransactionRequestDTO

Campos validados: amount (@NotNull), date (@NotNull), type (@NotNull), description (@NotBlank), categoryId (opcional).

### TransactionResponseDTO

id, categoryId, amount, date, type, description, active.

El campo `limitExceeded` no se incluye en este DTO. Solo está presente en la respuesta del POST /transactions como parte del resultado del guardián de presupuesto.

### TransactionMetricsResponseDTO

income, expenses, balance, count, incomeTrend, expensesTrend, balanceTrend, countTrend.

---

## TransactionService.java

Servicio central que implementa los 5 casos de uso de transacciones.

### Registrar transacción (register)

1. Si se especifica categoría, verifica que exista en CategoryRepository.
2. Crea la transacción con UUID aleatorio, fechas automáticas y active = true.
3. Guarda en base de datos.
4. Guardián de presupuesto: si la transacción es de tipo EXPENSE, verifica el 70% de ingresos del mes.
    - Obtiene todas las transacciones del mes actual.
    - Calcula ingresos totales y gastos totales (incluyendo la nueva transacción).
    - Si los gastos superan el 70% de los ingresos, marca limitExceeded = true.
5. Devuelve RegisterTransactionResult con la transacción y el flag limitExceeded.

### Listar transacciones (findAllByUserId)

Devuelve todas las transacciones del usuario. El repositorio las ordena por fecha descendente.

### Obtener por ID (getByIdAndUserId)

Busca por ID y verifica que el userId coincida. Si no, lanza excepción (protección anti-IDOR).

### Actualizar (update)

1. Obtiene la transacción validando propiedad.
2. Si se especifica categoría nueva, verifica que exista.
3. Actualiza los campos mediante transaction.updateDetails().
4. Guarda los cambios.

### Eliminar (deleteByIdAndUserId)

1. Obtiene la transacción validando propiedad.
2. Llama a transaction.deactivate() que marca active = false.
3. Guarda (soft delete).

---

## TransactionMetricsService.java

Servicio de métricas que implementa GetTransactionMetricsUseCase.

### getMetrics

1. Calcula el rango de fechas actual y el período anterior mediante getDateRange y getPreviousPeriod.
2. Obtiene transacciones activas de ambos períodos.
3. Calcula income, expenses, balance y count para ambos períodos.
4. Calcula tendencias comparando valores actuales con anteriores.
5. Para countTrend usa fórmula específica con redondeo a un decimal.

### Métodos auxiliares

- getDateRange: devuelve inicio y fin del período (semana, mes o año).
- getPreviousPeriod: devuelve el rango del período anterior.
- sumByType: suma importes de transacciones filtradas por tipo.
- calculateTrend: tendencia porcentual entre valor actual y anterior.
- calculateCountTrend: tendencia del número de transacciones.

---

## Transaction.java (Modelo de dominio)

Entidad rica con lógica de negocio. Usa @Getter y @Setter de Lombok.

### Atributos

id, userId, categoryId, amount, date, type (INCOME/EXPENSE), description, createdAt, modifiedAt, active.

### TransactionType

Enum con valores INCOME y EXPENSE.

### Constructor

Valida que el amount no sea cero ni nulo. Si no cumple, lanza IllegalArgumentException.

### updateDetails

Actualiza amount, date, type, description y categoryId. Valida que el amount no sea cero. Actualiza modifiedAt automáticamente.

### deactivate

Marca active = false y actualiza modifiedAt. Es un borrado lógico.

---

## Puertos de entrada

### RegisterTransactionUseCase

- register(RegisterTransactionCommand) → RegisterTransactionResult
- RegisterTransactionCommand: userId, categoryId, amount, date, type, description
- RegisterTransactionResult: transaction, limitExceeded

### ListTransactionsUseCase

- findAllByUserId(UUID) → List<Transaction>

### GetTransactionUseCase

- getByIdAndUserId(UUID, UUID) → Transaction

### UpdateTransactionUseCase

- update(UUID, UpdateTransactionCommand) → Transaction
- UpdateTransactionCommand: userId, amount, date, type, description, categoryId

### DeleteTransactionUseCase

- deleteByIdAndUserId(UUID, UUID)

### GetTransactionMetricsUseCase

- getMetrics(MetricsCommand) → TransactionMetricsResponseDTO
- MetricsCommand: userId, period

---

## Puerto de salida

### TransactionRepository

Define: save, findById, findAllByUserId, findByUserIdAndDateBetween, findRecentByUserId, findAllByCategoryId.

---

## Adaptador de persistencia

### TransactionEntity

Entidad JPA mapeada a la tabla transactions. Columnas: id, user_id, category_id, amount, date, type, description, created_at, modified_at, active. Usa Lombok.

### TransactionEntityMapper

Interfaz MapStruct anotada con @Mapper(componentModel = "spring"). Métodos: toEntity (Transaction → TransactionEntity), toDomain (TransactionEntity → Transaction).

### SpringDataTransactionRepository

Interfaz que extiende JpaRepository. Métodos:

- findAllByUserIdOrderByDateDesc: ordenadas por fecha descendente.
- findByUserIdAndDateBetween: query JPQL para filtrar por rango de fechas.
- findTop5ByUserIdOrderByDateDesc: 5 más recientes.
- findAllByCategoryId: transacciones por categoría.

### TransactionPostgresAdapter

Implementa TransactionRepository. Traduce entre dominio y entidad usando TransactionEntityMapper. Delega en SpringDataTransactionRepository.

---

## Guardián de presupuesto

Al registrar una transacción de tipo EXPENSE, el sistema verifica:

1. Obtiene todas las transacciones del mes actual del usuario.
2. Calcula ingresos totales y gastos totales (incluyendo la nueva).
3. Si los gastos superan el 70% de los ingresos, marca limitExceeded = true.

Este flag se devuelve en la respuesta para que el frontend pueda mostrar una alerta al usuario.

---

## Ordenación

Las transacciones se devuelven ordenadas por fecha descendente (más recientes primero) gracias a findAllByUserIdOrderByDateDesc en el repositorio.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/transactions | Listar transacciones |
| POST | /api/v1/transactions | Crear transacción |
| GET | /api/v1/transactions/:id | Obtener transacción |
| PUT | /api/v1/transactions/:id | Actualizar transacción |
| DELETE | /api/v1/transactions/:id | Eliminar transacción |
| GET | /api/v1/transactions/metrics?period= | Métricas de transacciones |