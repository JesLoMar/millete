# Investments — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/InvestmentService.java** — CRUD de inversiones con lógica de negocio
- **domain/model/Investment.java** — Entidad de dominio con cálculos financieros
- **domain/ports/in/RegisterInvestmentUseCase.java** — Interfaz de registro de inversión
- **domain/ports/in/ListInvestmentsUseCase.java** — Interfaz de listado
- **domain/ports/in/UpdateInvestmentPriceUseCase.java** — Interfaz de actualización de precio
- **domain/ports/out/InvestmentRepository.java** — Interfaz del repositorio
- **infrastructure/in/controller/InvestmentController.java** — Controlador REST con 4 endpoints
- **infrastructure/in/controller/dto/InvestmentResponseDTO.java** — DTO de respuesta
- **infrastructure/in/controller/dto/RegisterInvestmentRequestDTO.java** — DTO de creación
- **infrastructure/in/controller/dto/UpdateInvestmentPriceRequestDTO.java** — DTO de actualización de precio
- **infrastructure/out/persistence/postgresql/adapters/InvestmentPostgresAdapter.java** — Adaptador de persistencia
- **infrastructure/out/persistence/postgresql/entity/InvestmentEntity.java** — Entidad JPA
- **infrastructure/out/persistence/postgresql/mappers/InvestmentEntityMapper.java** — Mapper MapStruct
- **infrastructure/out/persistence/postgresql/repository/SpringDataInvestmentRepository.java** — Repositorio Spring Data

---

## InvestmentController.java

Controlador REST mapeado a /api/v1/investments.

### POST /

Crea una nueva inversión.

1. Extrae el userId del token JWT.
2. Traduce RegisterInvestmentRequestDTO a RegisterInvestmentCommand.
3. Al crear, el currentPrice se inicializa con el purchasePrice.
4. Responde 201 Created con InvestmentResponseDTO.

### GET /

Lista las inversiones activas del usuario autenticado.

1. Obtiene todas las inversiones del usuario.
2. Filtra solo las activas.
3. Mapea cada una a InvestmentResponseDTO con todos los campos calculados.

### PATCH /{id}/price

Actualiza el precio de mercado de un activo.

1. Recibe UpdateInvestmentPriceRequestDTO con newPrice.
2. Valida que la inversión pertenezca al usuario (anti-IDOR).
3. Actualiza el precio mediante el modelo de dominio.
4. Responde 200 con InvestmentResponseDTO actualizado.

### DELETE /{id}

Elimina lógicamente una inversión (soft delete).

1. Busca la inversión por ID.
2. Verifica que pertenezca al usuario.
3. Marca active = false y actualiza modifiedAt.
4. Responde 204 No Content.

---

## DTOs

### RegisterInvestmentRequestDTO

Campos validados: assetName (@NotBlank), quantity (@NotNull, @Positive), purchasePrice (@NotNull, @PositiveOrZero), type (@NotNull), purchaseDate (@NotNull). ticker es opcional.

### UpdateInvestmentPriceRequestDTO

Campos validados: newPrice (@NotNull, @PositiveOrZero).

### InvestmentResponseDTO

id, assetName, ticker, quantity, purchasePrice, currentPrice, investedCapital, currentValue, profitOrLoss, roiPercentage, type, purchaseDate, active.

Los campos investedCapital, currentValue, profitOrLoss y roiPercentage son calculados por el modelo de dominio.

---

## InvestmentService.java

Servicio central que implementa los 3 casos de uso de inversiones.

### Registrar inversión (register)

1. Genera UUID aleatorio y fechas automáticas.
2. Crea la inversión con currentPrice igual al purchasePrice inicialmente.
3. Guarda mediante InvestmentRepository.

### Listar inversiones (findAllByUserId)

Devuelve todas las inversiones del usuario. El controlador se encarga de filtrar las activas.

### Actualizar precio (updatePrice)

1. Busca la inversión por ID.
2. Verifica que el userId coincida (protección anti-IDOR).
3. Llama a investment.updateCurrentPrice(newPrice) que actualiza el precio y modifiedAt.
4. Guarda los cambios.

---

## Investment.java (Modelo de dominio)

Entidad rica con lógica de negocio y cálculos financieros. Usa @Getter y @Setter de Lombok.

### InvestmentType

Enum con valores: STOCK, CRYPTO, FUND, REAL_ESTATE, OTHER.

### Constructor

Valida que la cantidad sea mayor que cero. Si no se proporciona currentPrice, usa el purchasePrice como valor inicial.

### Métodos de cálculo financiero

- getInvestedCapital(): cantidad × precio de compra.
- getCurrentValue(): cantidad × precio actual.
- getProfitOrLoss(): valor actual - capital invertido.
- getReturnOnInvestmentPercentage(): (beneficio / capital invertido) × 100, redondeado a 4 decimales.

### Métodos de actualización

- updateCurrentPrice(newPrice): actualiza el precio de mercado si es no negativo. Actualiza modifiedAt.
- deactivate(): marca active = false y actualiza modifiedAt (soft delete).

---

## Puertos de entrada

### RegisterInvestmentUseCase

- register(RegisterInvestmentCommand) → Investment
- RegisterInvestmentCommand: userId, assetName, ticker, quantity, purchasePrice, type, purchaseDate

### ListInvestmentsUseCase

- findAllByUserId(UUID) → List<Investment>

### UpdateInvestmentPriceUseCase

- updatePrice(UUID, UUID, BigDecimal) → Investment
- Parámetros: id de la inversión, userId, newPrice

---

## Puerto de salida

### InvestmentRepository

Define: save, findById, findAllByUserId.

---

## Adaptador de persistencia

### InvestmentEntity

Entidad JPA mapeada a la tabla investments. Columnas: id, user_id, asset_name, ticker, quantity (18,8), purchase_price (18,2), current_price (18,2), type, purchase_date, created_at, modified_at, active. Usa Lombok y @Builder.

### InvestmentEntityMapper

Interfaz MapStruct anotada con @Mapper(componentModel = SPRING). Métodos: toEntity (Investment → InvestmentEntity), toDomain (InvestmentEntity → Investment).

### SpringDataInvestmentRepository

Interfaz que extiende JpaRepository. Método personalizado: findAllByUserId.

### InvestmentPostgresAdapter

Implementa InvestmentRepository. Traduce entre dominio y entidad usando InvestmentEntityMapper. Delega en SpringDataInvestmentRepository.

---

## Seguridad

Tanto en updatePrice como en delete, el sistema verifica que el userId de la inversión coincida con el usuario autenticado. Esto previene ataques IDOR (Insecure Direct Object Reference).

---

## Cálculos financieros

El modelo de dominio encapsula toda la lógica de cálculo:

- Capital invertido = cantidad × precio de compra
- Valor actual = cantidad × precio de mercado
- Beneficio/Pérdida = valor actual - capital invertido
- ROI % = (beneficio / capital invertido) × 100

Estos valores se calculan bajo demanda y se incluyen en el DTO de respuesta.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/investments | Listar inversiones activas |
| POST | /api/v1/investments | Crear nueva inversión |
| PATCH | /api/v1/investments/:id/price | Actualizar precio de mercado |
| DELETE | /api/v1/investments/:id | Eliminar (desactivar) inversión |