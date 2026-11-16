# Categorías

El módulo mantiene categorías por usuario, con nombre, color y límite presupuestario opcional. Controlador: [`CategoryController.java`](../../backend/src/main/java/com/puntomartinez/millete/categories/infrastructure/in/controller/CategoryController.java); servicio: [`CategoryService.java`](../../backend/src/main/java/com/puntomartinez/millete/categories/application/services/CategoryService.java).

## API

Base `/api/v1/categories`; el usuario se toma de la autenticación, no de un parámetro del cliente.

| Método y ruta | Comportamiento |
|---|---|
| `POST /` | Crea categoría; responde `201` y `CategoryResponseDTO` |
| `GET /` | Lista categorías propias, paginadas, con búsqueda opcional |
| `PUT /{id}` | Actualiza detalles |
| `DELETE /{id}` | Desactiva y responde `204` |

El listado usa `page=0`, `size=50` por defecto, admite `search` y limita `size` a 200; una página fuera de rango se rechaza. El controlador no expone `GET /{id}`. El DTO de escritura valida los campos definidos en `RegisterCategoryRequestDTO`/`UpdateCategoryRequestDTO`; la lógica de dominio vuelve a proteger las invariantes. Un ID ajeno o inexistente no concede acceso.

El cuerpo de alta/actualización tiene `name` (obligatorio, máximo 20), `description` (opcional, máximo 500), `color` (obligatorio, hexadecimal `#RRGGBB`) y `budgetLimit` opcional no negativo. `CategoryResponseDTO` expone identidad, nombre, descripción, color y límite, junto con estado/fechas de auditoría. El nombre activo es único por usuario sin distinguir mayúsculas/minúsculas; nombres de categorías inactivas pueden reutilizarse.

## Persistencia y vínculos

La categoría se marca inactiva al borrarla. La operación de servicio coordina la desvinculación de transacciones asociadas del mismo usuario, conservando los movimientos. La entidad JPA filtra categorías inactivas en consultas normales; ver [`CategoryEntity.java`](../../backend/src/main/java/com/puntomartinez/millete/categories/infrastructure/out/persistence/postgresql/entity/CategoryEntity.java) y [`V5__v0.2.0.sql`](../../backend/src/main/resources/db/migration/V5__v0.2.0.sql).

Una transacción sin categoría sigue existiendo y sus importes no se borran. Borrar una categoría no borra transacciones; las deja sin esa asociación para que métricas/listados no atribuyan movimientos a una categoría inactiva.
