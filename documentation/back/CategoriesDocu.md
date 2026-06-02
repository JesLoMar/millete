# Categories — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/CategoryService.java** — Servicio central que implementa todos los casos de uso
- **domain/model/Category.java** — Entidad de dominio con lógica de negocio y validaciones
- **domain/ports/in/GetCategoryUseCase.java** — Interfaz de consulta de categorías
- **domain/ports/in/RegisterCategoryUseCase.java** — Interfaz de creación de categoría
- **domain/ports/in/RegisterCategoryCommand.java** — Comando inmutable (record) para crear categoría
- **domain/ports/in/UpdateCategoryUseCase.java** — Interfaz de actualización de categoría
- **domain/ports/in/UpdateCategoryCommand.java** — Comando inmutable (record) para actualizar categoría
- **domain/ports/out/CategoryRepository.java** — Interfaz del repositorio (puerto de salida)
- **infrastructure/in/controller/CategoryController.java** — Controlador REST
- **infrastructure/in/controller/dto/CategoryResponseDTO.java** — DTO de respuesta (record)
- **infrastructure/in/controller/dto/RegisterCategoryRequestDTO.java** — DTO de petición con Bean Validation
- **infrastructure/out/persistence/postgresql/adapters/CategoryPostgresAdapter.java** — Adaptador de persistencia
- **infrastructure/out/persistence/postgresql/entity/CategoryEntity.java** — Entidad JPA
- **infrastructure/out/persistence/postgresql/mappers/CategoryMapper.java** — Mapper manual dominio ↔ entidad
- **infrastructure/out/persistence/postgresql/repository/JpaCategoryRepository.java** — Repositorio Spring Data

---

## Arquitectura hexagonal

El módulo sigue el patrón de Puertos y Adaptadores:

- **Puertos de entrada (in):** RegisterCategoryUseCase, UpdateCategoryUseCase y GetCategoryUseCase. Cada uno recibe un Command inmutable (record) como parámetro en lugar de parámetros sueltos, lo que facilita la evolución de la interfaz sin romper el contrato.
- **Núcleo de dominio:** CategoryService implementa todos los casos de uso. Category.java contiene la lógica de negocio y validaciones de invariantes.
- **Puertos de salida (out):** CategoryRepository define las operaciones de persistencia que necesita el dominio.
- **Adaptador de persistencia:** CategoryPostgresAdapter implementa el repositorio usando JPA, traduciendo entre entidades de dominio y entidades JPA mediante CategoryMapper.

El controlador inyecta los puertos de entrada para crear/actualizar/listar, y el servicio directamente para delete y findById.

---

## Seguridad

### Defensa en profundidad

El módulo aplica múltiples capas de validación:

**Capa Controller:** Bean Validation con la anotación @Valid en los DTOs de entrada, que devuelve 400 Bad Request con mensajes descriptivos si los datos no cumplen el formato esperado.

**Capa Controller:** El userId se extrae del token JWT mediante authentication.getName(), nunca se recibe del cuerpo de la petición, evitando suplantación de identidad.

**Capa Dominio:** Validaciones en el constructor y métodos de Category.java que protegen las invariantes del negocio. Si alguien intenta crear una categoría con datos inválidos directamente (sin pasar por el controller), el dominio lo rechaza.

**Capa Servicio:** Validación de propiedad mediante el binomio (id, userId) en todas las operaciones de modificación. La consulta findByIdAndUserId busca en base de datos por ambos campos simultáneamente.

**Capa Base de datos:** Constraints SQL como NOT NULL y CHECK que actúan como última barrera de integridad.

### Validación de propiedad (protección contra IDOR)

Todas las operaciones de modificación (update, delete) validan que el recurso pertenezca al usuario autenticado usando el binomio (id, userId). El servicio llama a categoryRepository.findByIdAndUserId(id, userId). Si el recurso no existe o no pertenece al usuario, se lanza una ResponseStatusException con HttpStatus.NOT_FOUND y el mensaje "Categoría no encontrada", sin revelar si el recurso existe realmente.

### Validaciones de dominio

Category.java protege sus invariantes mediante validaciones en el constructor y métodos:

- **Nombre:** Obligatorio, no puede estar vacío ni ser null. Se valida con isBlank().
- **Color:** Obligatorio, debe ser un hexadecimal válido que coincida con el patrón #RRGGBB (ej: #FF5733). Se valida con la expresión regular ^#[0-9A-Fa-f]{6}$.
- **Presupuesto (budgetLimit):** Opcional (null significa sin límite), pero si se especifica no puede ser negativo. Se valida con compareTo(BigDecimal.ZERO).
- **Constructor de creación:** Genera UUID aleatorio, establece createdAt y modifiedAt con la fecha actual, y marca active = true.
- **Método updateDetails():** Valida nombre, color (si se proporciona) y presupuesto (si se proporciona) antes de actualizar los campos. Actualiza modifiedAt.
- **Método deactivate():** Marca active = false y actualiza modifiedAt. Es un borrado lógico, nunca físico.

### Principio de mínimo privilegio

- Se eliminó el método findAll() del caso de uso GetCategoryUseCase y del servicio. La aplicación no puede listar categorías globales de todos los usuarios bajo ninguna circunstancia.
- Solo se expone findByUserId(userId) que devuelve únicamente las categorías del usuario autenticado, filtradas por active = true.
- En la eliminación, solo se desasignan transacciones del mismo propietario, filtrando por tx.getUserId().equals(userId). Las transacciones de otros usuarios no se modifican.

---

## CategoryController.java

Controlador REST mapeado a /api/v1/categories. Usa la anotación @Validated a nivel de clase para activar validaciones adicionales.

### POST /

Crea una nueva categoría. Extrae el userId del token JWT mediante authentication.getName(). Valida el cuerpo de la petición con @Valid y lo recibe como RegisterCategoryRequestDTO. Traduce el DTO a RegisterCategoryCommand (record con userId, nombre, color, budgetLimit). Ejecuta registerCategoryUseCase.register(command). Mapea el resultado a CategoryResponseDTO. Responde 201 Created.

### GET /

Lista las categorías activas del usuario autenticado. Extrae el userId del token. Llama a getCategoryUseCase.findByUserId(userId). El servicio ya filtra solo las activas. Mapea cada categoría a CategoryResponseDTO. Responde 200 OK.

### PUT /{id}

Actualiza una categoría existente. Recibe el ID por @PathVariable y RegisterCategoryRequestDTO en el cuerpo con @Valid. Extrae el userId del token. Traduce a UpdateCategoryCommand (record con nombre, color, budgetLimit). Ejecuta updateCategoryUseCase.update(id, userId, command). El servicio valida propiedad con findByIdAndUserId(id, userId) y lanza 404 si no es dueño. Responde 200 con CategoryResponseDTO.

### DELETE /{id}

Elimina lógicamente una categoría. Recibe el ID por @PathVariable. Extrae el userId del token. Llama a categoryService.delete(id, userId). El servicio valida propiedad con findByIdAndUserId(id, userId) y lanza 404 si no es dueño. Desactiva la categoría y desasigna solo las transacciones del propietario. Responde 204 No Content.

---

## DTOs

### RegisterCategoryRequestDTO

Record inmutable con Bean Validation. El campo name tiene @NotBlank y @Size(min=1, max=50). El campo color tiene @NotBlank y @Pattern con la expresión regular ^#[0-9A-Fa-f]{6}$ que exige un hexadecimal de 6 caracteres con # al inicio. El campo budgetLimit es opcional pero si se proporciona debe ser mayor que 0 según @DecimalMin(value="0.0", inclusive=false).

### CategoryResponseDTO

Record inmutable con campos: id (UUID), userId (UUID), name (String), color (String), budgetLimit (BigDecimal), createdAt (LocalDateTime), active (boolean). El mapeo desde el dominio se realiza en el método privado mapToResponse del controlador.

---

## CategoryService.java

Servicio central que implementa RegisterCategoryUseCase, UpdateCategoryUseCase y GetCategoryUseCase.

### Crear categoría

Recibe RegisterCategoryCommand (record con userId, nombre, color, budgetLimit). Crea un nuevo objeto Category cuyo constructor valida nombre (no vacío), color (hexadecimal #RRGGBB obligatorio) y presupuesto (no negativo si se especifica). El constructor genera UUID, fechas y marca active = true. Guarda mediante categoryRepository.save() y devuelve la categoría creada.

### Listar categorías del usuario

findByUserId(userId) obtiene las categorías del usuario mediante findByIdUsuario y filtra solo las activas con isActive().

### Actualizar categoría

Recibe id, userId y UpdateCategoryCommand. Busca con findByIdAndUserId(id, userId). Si no existe o no pertenece al usuario, lanza ResponseStatusException con NOT_FOUND. Llama a category.updateDetails() que valida los nuevos valores (nombre, color si se proporciona, presupuesto si se proporciona). Guarda los cambios.

### Eliminar categoría

Recibe id y userId. Busca con findByIdAndUserId(id, userId). Si no existe o no pertenece al usuario, lanza ResponseStatusException con NOT_FOUND. Llama a category.deactivate() que marca active = false. Guarda la categoría desactivada. Busca todas las transacciones asociadas a esa categoría mediante transactionRepository.findAllByCategoryId(id). Filtra solo las transacciones del propietario comprobando que tx.getUserId().equals(userId). Para cada transacción del propietario, desvincula la categoría estableciendo categoryId a null y actualiza modifiedAt con la fecha actual. Las transacciones de otros usuarios no se modifican.

---

## Category.java (Modelo de dominio)

Entidad de dominio con lógica de negocio. Usa @Getter de Lombok para generar los getters. No tiene setters públicos para proteger las invariantes del negocio.

### Constructores

**Constructor de creación:** Recibe userId, name, color, budgetLimit. Valida que el nombre no esté vacío. Valida que el color sea un hexadecimal válido con el patrón #RRGGBB (obligatorio). Valida que el presupuesto no sea negativo si se especifica. Genera UUID aleatorio, establece createdAt y modifiedAt con la fecha actual, y marca active = true.

**Constructor de reconstrucción:** Recibe todos los campos (id, userId, name, color, budgetLimit, createdAt, modifiedAt, active). Usado por el mapper al leer de base de datos. No aplica validaciones porque los datos ya fueron validados al crearse.

### Métodos de dominio

**updateDetails(nombre, color, budgetLimit):** Actualiza nombre, color y presupuesto. Valida que el nombre no esté vacío. Valida que el color sea hex válido si se proporciona (no es null). Valida que el presupuesto no sea negativo si se proporciona. Actualiza modifiedAt con la fecha actual.

**deactivate():** Marca active = false y actualiza modifiedAt con la fecha actual. Es un borrado lógico, nunca se elimina el registro físicamente.

### Validaciones

El nombre es obligatorio y no puede estar vacío ni ser null. El color es obligatorio y debe coincidir con el patrón # seguido de 6 caracteres hexadecimales. El presupuesto es opcional (null significa sin límite), pero si se especifica debe ser mayor o igual que 0.

---

## Commands (Puertos de entrada)

### RegisterCategoryCommand

Record inmutable ubicado en domain/ports/in/. Contiene: userId (UUID), nombre (String), color (String), budgetLimit (BigDecimal). Encapsula todos los datos necesarios para crear una categoría.

### UpdateCategoryCommand

Record inmutable ubicado en domain/ports/in/. Contiene: nombre (String), color (String), budgetLimit (BigDecimal). Encapsula los datos que se pueden actualizar.

---

## Puertos de entrada

### RegisterCategoryUseCase

Define el método register que recibe RegisterCategoryCommand y devuelve Category.

### UpdateCategoryUseCase

Define el método update que recibe UUID id, UUID userId y UpdateCategoryCommand, y devuelve Category. El userId se incluye para validar propiedad del recurso.

### GetCategoryUseCase

Define el método findByUserId que recibe UUID userId y devuelve List<Category>. Solo expone datos del usuario autenticado. Se eliminó el método findAll() por seguridad.

---

## Puerto de salida

### CategoryRepository

Define los métodos: save, findById, findByIdAndUserId, findByIdUsuario, findCategoriesWithBudgetByUserId.

El método findByIdAndUserId es la pieza clave de seguridad: recibe id y userId y busca en base de datos validando ambos campos simultáneamente, garantizando que solo el propietario pueda acceder al recurso.

---

## Adaptador de persistencia

### CategoryEntity

Entidad JPA mapeada a la tabla categories. Usa @Data de Lombok que genera getters, setters, equals, hashCode y toString.

Columnas: id (UUID, clave primaria), user_id (UUID, no nulo), name (VARCHAR 50, no nulo), color (VARCHAR 7, no nulo), budget_limit (DECIMAL, permite nulos), created_at (TIMESTAMP, no nulo), modified_at (TIMESTAMP, no nulo), active (BOOLEAN, no nulo).

### JpaCategoryRepository

Interfaz que extiende JpaRepository con la entidad CategoryEntity y clave UUID. Spring Data genera la implementación automáticamente.

Métodos personalizados:

- findByUserId(UUID userId): Busca categorías por la columna user_id.
- findByIdAndUserId(UUID id, UUID userId): Query JPQL personalizada que busca por id y userId simultáneamente.
- findCategoriesWithBudgetByUserId(UUID userId): Query JPQL que busca categorías del usuario que tengan budgetLimit no nulo.

### CategoryMapper

Mapper manual sin MapStruct. Anotado con @Component.

Método toEntity: Recibe un Category de dominio y devuelve un CategoryEntity. Mapea campo por campo: id, userId, name, color, budgetLimit, createdAt, modifiedAt, active.

Método toDomain: Recibe un CategoryEntity y devuelve un Category de dominio. Usa el constructor de reconstrucción que acepta todos los campos.

### CategoryPostgresAdapter

Implementa CategoryRepository. Anotado con @Repository. Traduce entre objetos de dominio y entidades JPA usando CategoryMapper. Delega todas las operaciones de base de datos en JpaCategoryRepository.

Flujo de save: recibe Category de dominio, lo convierte a CategoryEntity con mapper.toEntity(), lo guarda con jpaRepository.save(), convierte el resultado a dominio con mapper.toDomain() y lo devuelve.

Flujo de findByIdAndUserId: recibe id y userId, llama a jpaRepository.findByIdAndUserId(id, userId), si existe lo convierte a dominio con mapper.toDomain().

---

## Conexión con el frontend

El módulo categories del frontend se comunica con estos endpoints:

- GET /api/v1/categories — Obtiene la lista de categorías activas del usuario autenticado
- POST /api/v1/categories — Crea una nueva categoría enviando name, color y budgetLimit
- PUT /api/v1/categories/:id — Edita una categoría existente (solo el propietario puede hacerlo)
- DELETE /api/v1/categories/:id — Elimina (desactiva) una categoría (solo el propietario puede hacerlo)

---

## Flujo completo de creación

1. El frontend envía POST /api/v1/categories con el body conteniendo name, color y budgetLimit.
2. CategoryController valida el body con @Valid. Si los datos no cumplen el formato, responde 400 Bad Request con mensajes descriptivos.
3. Extrae el userId del token JWT mediante authentication.getName().
4. Crea un RegisterCategoryCommand (record inmutable) con userId, name, color y budgetLimit.
5. CategoryService.register() crea un nuevo objeto Category. El constructor del dominio valida que el nombre no esté vacío, que el color sea un hexadecimal válido y que el presupuesto no sea negativo.
6. El constructor genera automáticamente UUID, createdAt, modifiedAt y marca active = true.
7. CategoryPostgresAdapter traduce el dominio a CategoryEntity y lo guarda en PostgreSQL.
8. El controlador mapea el dominio resultante a CategoryResponseDTO.
9. Responde 201 Created con los datos de la categoría creada.

---

## Flujo completo de eliminación

1. El frontend envía DELETE /api/v1/categories/:id.
2. CategoryController extrae el userId del token JWT.
3. CategoryService.delete(id, userId) busca la categoría con findByIdAndUserId(id, userId).
4. Si la categoría no existe o no pertenece al usuario, lanza ResponseStatusException con 404 Not Found y mensaje genérico, sin revelar si el recurso existe.
5. Si la categoría pertenece al usuario, llama a category.deactivate() que marca active = false y actualiza modifiedAt.
6. Guarda la categoría desactivada mediante categoryRepository.save().
7. Busca todas las transacciones asociadas a esa categoría con transactionRepository.findAllByCategoryId(id).
8. Filtra solo las transacciones cuyo userId coincide con el del propietario (tx.getUserId().equals(userId)).
9. Para cada transacción del propietario, desvincula la categoría estableciendo categoryId = null y actualiza modifiedAt con la fecha actual.
10. Las transacciones de otros usuarios no se modifican en absoluto.
11. Responde 204 No Content.

Las transacciones históricas no se eliminan, solo pierden la referencia a la categoría desactivada. Así se preserva el historial financiero completo.

---

## Tests

El módulo tiene 21 tests unitarios que cubren todas las funcionalidades y casos de error:

### CategoryTest (12 tests)

- Creación con datos válidos: verifica que todos los campos se asignan correctamente.
- Aceptar budgetLimit nulo: verifica que una categoría sin presupuesto es válida.
- Rechazar nombre vacío: lanza IllegalArgumentException.
- Rechazar nombre nulo: lanza IllegalArgumentException.
- Rechazar color inválido (texto): lanza IllegalArgumentException al pasar "rojo".
- Rechazar color con formato incorrecto: lanza IllegalArgumentException al pasar "#FFF" (3 caracteres en vez de 6).
- Rechazar presupuesto negativo: lanza IllegalArgumentException.
- Actualizar detalles correctamente: verifica que name, color y budgetLimit se actualizan y modifiedAt cambia.
- Rechazar actualización con nombre vacío: lanza IllegalArgumentException.
- Rechazar actualización con color inválido: lanza IllegalArgumentException.
- Rechazar actualización con presupuesto negativo: lanza IllegalArgumentException.
- Desactivar categoría: verifica que active pasa a false y modifiedAt se actualiza.

### CategoryServiceTest (9 tests)

- Crear categoría: verifica que se guarda correctamente con todos los campos.
- Listar categorías por usuario: verifica que solo devuelve las activas, filtrando las inactivas.
- Actualizar categoría como propietario: verifica que se actualiza usando findByIdAndUserId.
- Rechazar actualización si no existe: lanza ResponseStatusException con NOT_FOUND.
- Rechazar actualización de otro usuario: simula que otro userId intenta actualizar y verifica que lanza NOT_FOUND y nunca se llama a save.
- Eliminar categoría como propietario: verifica que se desactiva y se desasignan las transacciones del propietario.
- Eliminar solo modifica transacciones del propietario: crea transacciones de dos usuarios distintos y verifica que solo se modifican las del propietario.
- Rechazar eliminación si no existe: lanza ResponseStatusException con NOT_FOUND.
- Rechazar eliminación de otro usuario: simula que otro userId intenta eliminar y verifica que lanza NOT_FOUND y nunca se modifican transacciones.