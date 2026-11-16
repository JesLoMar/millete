# Transacciones

Las transacciones financieras pertenecen a un usuario y pueden referenciar una categoría. Los endpoints están en [`TransactionController.java`](../../backend/src/main/java/com/puntomartinez/millete/transactions/infrastructure/in/controller/TransactionController.java).

## API

Base `/api/v1/transactions`; el usuario se deriva de `JwtUser`.

| Método y ruta | Contrato |
|---|---|
| `GET /?page=0&size=50&search=&type=&period=month` | Lista paginada y filtrada; se limita página solicitada al último índice disponible |
| `GET /metrics?period=month` | Métricas por período |
| `POST /` | Crea y responde `201` |
| `GET /{id}` | Obtiene movimiento propio |
| `PUT /{id}` | Actualiza movimiento propio |
| `DELETE /{id}` | Desactiva y responde `204` |

El listado usa `page=0`, `size=50`, y `period=month`; el tipo es opcional y debe coincidir con un valor de `TransactionType` (se parsea sin distinguir mayúsculas). Los períodos válidos son `week`, `month` y `year`, sin distinguir mayúsculas; la fecha de referencia es el día local del usuario. Página negativa, tamaño no positivo, tipo o período inválido producen error de entrada; una página mayor al último índice se limita al último índice. Alta recibe `categoryId` opcional, `amount` obligatorio positivo, `date` obligatorio (`LocalDate` ISO), `type` obligatorio y `description` obligatoria de hasta 50 caracteres. Actualización recibe esos campos mutables, con `categoryId` opcional; ninguno de los DTOs contiene moneda. La categoría, cuando se indica, debe resolverse dentro del contexto del usuario. Las respuestas incluyen nombre/color de categoría resueltos por el controlador; si la categoría no se puede resolver aparece «Sin categoría». La métrica contempla ingresos y salidas por transferencia además de movimientos ordinarios.

## Reglas observables

La eliminación es lógica (`active=false`). La categoría no es necesaria para un movimiento. Las métricas y el listado consultan transacciones activas según los repositorios. La creación puede devolver el indicador `alertLimitExceeded`; la lógica se encuentra en [`TransactionService.java`](../../backend/src/main/java/com/puntomartinez/millete/transactions/application/services/TransactionService.java). Tipos, fecha y campos concretos están definidos en los DTO [`RegisterTransactionRequestDTO.java`](../../backend/src/main/java/com/puntomartinez/millete/transactions/infrastructure/in/controller/dto/RegisterTransactionRequestDTO.java) y [`UpdateTransactionRequestDTO.java`](../../backend/src/main/java/com/puntomartinez/millete/transactions/infrastructure/in/controller/dto/UpdateTransactionRequestDTO.java).

V5 añade tipos de transferencia asociados a inversiones. Esos movimientos vinculados no deben tratarse como CRUD ordinario; las restricciones están en [`V5__v0.2.0.sql`](../../backend/src/main/resources/db/migration/V5__v0.2.0.sql).

Los tipos son `INCOME`, `EXPENSE`, `TRANSFER_IN` y `TRANSFER_OUT`. Para transacciones ordinarias el servicio permite alta, modificación y borrado lógico. La integración de Investments añade actividad, moneda y zona horaria a la persistencia; esos campos no forman parte de los DTOs públicos ordinarios. V5 exige metadatos de vínculo para transferencias vinculadas y bloquea cambios/eliminación de movimientos enlazados. La API financiera no edita por separado una transferencia del ledger.

La fecha de una transacción es día civil; creación/modificación son timestamps. Los filtros de período se interpretan mediante los servicios, considerando la zona horaria del usuario. `alertLimitExceeded` es un indicador calculado al crear, no un rechazo de la transacción.
