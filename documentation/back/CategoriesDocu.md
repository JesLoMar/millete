# Categories — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/CategoryService.java** — Servicio central que implementa todos los casos de uso
- **domain/model/Category.java** — Entidad de dominio con lógica de negocio
- **domain/ports/in/GetCategoryUseCase.java** — Interfaz de consulta de categorías
- **domain/ports/in/RegisterCategoryUseCase.java** — Interfaz de creación de categoría
- **domain/ports/in/UpdateCategoryUseCase.java** — Interfaz de actualización de categoría
- **domain/ports/out/CategoryRepository.java** — Interfaz del repositorio
- **infrastructure/in/controller/CategoryController.java** — Controlador REST
- **infrastructure/in/controller/dto/CategoryResponseDTO.java** — DTO de respuesta
- **infrastructure/in/controller/dto/RegisterCategoryRequestDTO.java** — DTO de petición
- **infrastructure/out/persistence/postgresql/adapters/CategoryPostgresAdapter.java** — Adaptador de persistencia
- **infrastructure/out/persistence/postgresql/entity/CategoryEntity.java** — Entidad JPA
- **infrastructure/out/persistence/postgresql/mappers/CategoryMapper.java** — Mapper manual
- **infrastructure/out/persistence/postgresql/repository/JpaCategoryRepository.java** — Repositorio Spring Data

---

## Arquitectura hexagonal

El módulo sigue el patrón de Puertos y Adaptadores:

- **Puertos de entrada (in):** RegisterCategoryUseCase, UpdateCategoryUseCase y GetCategoryUseCase.
- **Núcleo de dominio:** CategoryService implementa todos los casos de uso. Category.java contiene la lógica de negocio.
- **Puertos de salida (out):** CategoryRepository.
- **Adaptador:** CategoryPostgresAdapter implementa el repositorio.

El controlador inyecta tanto los puertos de entrada como el servicio directamente (para delete y findAll, que no tienen caso de uso independiente).

---

## CategoryController.java

Controlador REST mapeado a /api/v1/categories.

### POST /

Crea una nueva categoría.

1. Extrae el userId del token JWT.
2. Traduce RegisterCategoryRequestDTO a RegisterCategoryCommand.
3. Ejecuta registerCategoryUseCase.register().
4. Mapea el resultado a CategoryResponseDTO.
5. Responde 201 Created.

### GET /

Lista las categorías del usuario autenticado.

1. Extrae el userId del token.
2. Llama a categoryService.findByUserId().
3. Filtra solo las activas (isActive = true).
4. Mapea cada una a CategoryResponseDTO.
5. Responde 200 OK.

### PUT /{id}

Actualiza una categoría existente.

1. Recibe el ID por path variable y RegisterCategoryRequestDTO en el body.
2. Traduce a UpdateCategoryCommand.
3. Ejecuta updateCategoryUseCase.update().
4. Responde 200 con CategoryResponseDTO.

### DELETE /{id}

Elimina lógicamente una categoría.

1. Recibe el ID por path variable.
2. Llama a categoryService.delete().
3. Responde 204 No Content.

---

## DTOs

### RegisterCategoryRequestDTO

Campos validados: name con @NotBlank, color con @NotBlank, budgetLimit opcional (BigDecimal).

### CategoryResponseDTO

Campos: id, userId, name, color, budgetLimit, createdAt, active.

El mapeo de dominio a DTO se hace en el método privado mapToResponse del controlador.

---

## CategoryService.java

Servicio central que implementa RegisterCategoryUseCase, UpdateCategoryUseCase y GetCategoryUseCase.

También contiene el método delete, que no tiene caso de uso propio.

### Crear categoría

1. Crea un nuevo objeto Category con userId, nombre, color y budgetLimit.
2. El modelo genera automáticamente UUID, fechas createdAt/modifiedAt y marca active = true.
3. Si no se envía color, usa "#FFFFFF" por defecto.
4. Si no se envía budgetLimit, usa BigDecimal.ZERO.
5. Guarda mediante CategoryRepository.save y devuelve la categoría creada.

### Listar categorías del usuario

findByUserId obtiene las categorías del usuario y filtra solo las activas.

### Listar todas

findAll obtiene todas las categorías y filtra solo las activas.

### Actualizar categoría

1. Busca la categoría por ID. Si no existe, lanza excepción.
2. Llama a category.updateDetails() con los nuevos valores.
3. Guarda los cambios.

### Eliminar categoría

1. Busca la categoría por ID. Si no existe, lanza excepción.
2. Llama a category.deactivate() que marca active = false y actualiza modifiedAt.
3. Guarda la categoría desactivada.
4. Busca todas las transacciones asociadas a esa categoría.
5. Para cada transacción, desvincula la categoría (categoryId = null) y actualiza modifiedAt.
6. Guarda cada transacción modificada.

Esto asegura que al eliminar una categoría no se borren las transacciones históricas, solo se desvinculan.

---

## Category.java (Modelo de dominio)

Entidad rica con lógica de negocio. Usa @Getter y @NoArgsConstructor de Lombok.

### Constructores

- **Constructor de creación:** recibe userId, name, color, budgetLimit. Genera UUID aleatorio, fechas automáticas y marca active = true. Si color es nulo usa "#FFFFFF". Si budgetLimit es nulo usa BigDecimal.ZERO. Valida que el nombre no esté vacío.
- **Constructor de reconstrucción:** recibe todos los campos. Usado por el mapper al leer de base de datos.

### Métodos de dominio

- **updateDetails(nombre, color, budgetLimit):** actualiza nombre, color y presupuesto. Valida que el nombre no esté vacío. Valida que el presupuesto no sea negativo. Actualiza modifiedAt.
- **deactivate():** marca active = false y actualiza modifiedAt. Es un borrado lógico, no físico.

---

## Puertos de entrada

### RegisterCategoryUseCase

Define register(RegisterCategoryCommand) que devuelve Category. RegisterCategoryCommand contiene: userId, nombre, color, budgetLimit.

### UpdateCategoryUseCase

Define update(UUID, UpdateCategoryCommand) que devuelve Category. UpdateCategoryCommand contiene: nombre, color, budgetLimit.

### GetCategoryUseCase

Define findAll() y findByUserId(UUID), ambas devuelven List<Category>.

---

## Puerto de salida

### CategoryRepository

Define: save, findById, findAll, findByIdUsuario, findCategoriesWithBudgetByUserId.

---

## Adaptador de persistencia

### CategoryEntity

Entidad JPA mapeada a la tabla categories. Columnas: id (UUID, PK), user_id, name, color, budget_limit, created_at, modified_at, active.

Usa Lombok para getters, setters y constructores.

### JpaCategoryRepository

Interfaz que extiende JpaRepository. Spring Data genera la implementación automáticamente. Métodos personalizados:

- findByUserId: busca por columna user_id.
- findCategoriesWithBudgetByUserId: query JPQL que busca categorías del usuario con budgetLimit no nulo y mayor que cero.

### CategoryMapper

Mapper manual (sin MapStruct). Métodos:

- toEntity: Category → CategoryEntity. Mapea todos los campos uno a uno.
- toDomain: CategoryEntity → Category. Usa el constructor de reconstrucción.

### CategoryPostgresAdapter

Implementa CategoryRepository. Traduce entre dominio y entidad usando CategoryMapper. Delega en JpaCategoryRepository.

Flujo de save: dominio → toEntity → jpaRepository.save → toDomain → retorna dominio.

---

## Conexión con el frontend

El módulo categories del frontend se comunica con estos endpoints:

- GET /api/v1/categories → lista de categorías del usuario (solo activas)
- POST /api/v1/categories → crear categoría (name, color, budgetLimit)
- PUT /api/v1/categories/:id → editar categoría
- DELETE /api/v1/categories/:id → eliminar (desactivar) categoría

---

## Flujo completo de creación

1. Frontend envía RegisterCategoryRequestDTO con name, color y budgetLimit.
2. CategoryController extrae el userId del token JWT.
3. Traduce a RegisterCategoryCommand con userId, nombre, color, budgetLimit.
4. CategoryService crea el modelo Category (UUID, fechas y active automáticos).
5. Valida que el nombre no esté vacío.
6. CategoryPostgresAdapter traduce a CategoryEntity y guarda en PostgreSQL.
7. Devuelve el dominio y el controlador lo mapea a CategoryResponseDTO.
8. Responde 201 Created.

---

## Flujo completo de eliminación

1. Frontend envía DELETE /api/v1/categories/:id.
2. CategoryService busca la categoría por ID.
3. Marca la categoría como inactiva (active = false).
4. Guarda la categoría actualizada.
5. Busca todas las transacciones con ese categoryId.
6. Para cada transacción, desvincula la categoría (categoryId = null) y actualiza modifiedAt.
7. Guarda cada transacción modificada.
8. Responde 204 No Content.

Las transacciones no se eliminan, solo pierden la referencia a la categoría. Así se preserva el historial financiero. 