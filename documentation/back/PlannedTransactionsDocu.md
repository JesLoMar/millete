# Transacciones planificadas

Una plantilla define el calendario y los datos para crear transacciones financieras recurrentes. El controlador es [`PlannedTransactionController.java`](../../backend/src/main/java/com/puntomartinez/millete/plannedtransactions/infrastructure/in/controller/PlannedTransactionController.java).

## API

Base `/api/v1/planned-transactions`; la identidad del usuario proviene de autenticación.

| Método y ruta | Comportamiento |
|---|---|
| `GET /?page=0&size=50&search=&type=` | Listado paginado |
| `POST /` | Crea plantilla |
| `PUT /{id}` | Actualiza plantilla propia |
| `DELETE /{id}` | Desactiva plantilla |

La plantilla almacena tipo, importe, frecuencia e intervalo, fecha inicial/final, categoría opcional y estado de ejecución. El DTO define las validaciones HTTP; [`PlannedTransaction.java`](../../backend/src/main/java/com/puntomartinez/millete/plannedtransactions/domain/model/PlannedTransaction.java) contiene las reglas de dominio. El servicio valida paginación y propiedad; consulta una página fuera de rango con el último índice válido.

El contrato incluye descripción, importe, tipo, frecuencia, intervalo, `startDate`, `endDate` opcional y categoría opcional. El importe debe ser positivo; frecuencia/intervalo y rango de fechas se validan al crear y editar. El listado permite búsqueda y tipo, con página 0 y tamaño 50 predeterminados y máximo 200. Las plantillas pertenecen al usuario; una categoría inválida o ajena no concede acceso.

## Ejecución

[`TransactionScheduler.java`](../../backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/scheduler/TransactionScheduler.java) invoca el caso de uso diariamente a las 00:01. El servicio calcula ocurrencias debidas considerando la fecha y el estado persistido; el catch-up procesa ocurrencias pendientes. [`PlannedTransactionExecutionService.java`](../../backend/src/main/java/com/puntomartinez/millete/plannedtransactions/application/services/PlannedTransactionExecutionService.java) crea el movimiento con la fecha programada y añade el sufijo ` (Recurring)` a la descripción, truncando la base para ajustarla al máximo. Si una categoría ya no pertenece al usuario, la ejecución se realiza sin categoría.

V5 incorpora un contador de fallos consecutivos. Cada error de ejecución incrementa el contador; al llegar a tres, el servicio desactiva la plantilla. Una ejecución correcta actualiza la última fecha procesada y restablece el contador. La excepción se registra y el procesamiento de esa plantilla se detiene en ese fallo. Véanse [`PlannedTransactionService.java`](../../backend/src/main/java/com/puntomartinez/millete/plannedtransactions/application/services/PlannedTransactionService.java) y [`V5__v0.2.0.sql`](../../backend/src/main/resources/db/migration/V5__v0.2.0.sql).

El catch-up avanza desde la última fecha procesada (o el inicio si aún no hubo ejecución) y genera las ocurrencias pendientes hasta el día actual o la fecha final. Cada movimiento conserva la fecha programada y añade ` (Recurring)` a la descripción respetando el límite del campo. Al tercer fallo consecutivo la plantilla se desactiva para evitar reintentos diarios indefinidos.
