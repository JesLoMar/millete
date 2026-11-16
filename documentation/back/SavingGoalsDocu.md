# Metas de ahorro personales

El módulo administra objetivos propios con importe objetivo, avance, fecha límite opcional, prioridad y estado. Controlador: [`SavingsGoalController.java`](../../backend/src/main/java/com/puntomartinez/millete/savingsgoals/infrastructure/in/controller/SavingsGoalController.java).

## API

Base `/api/v1/savings-goals`; el usuario se obtiene de la autenticación.

| Método y ruta | Comportamiento |
|---|---|
| `POST /` | Crea; `201` |
| `GET /?page=&size=&search=&status=` | Lista paginada y filtrable |
| `GET /{id}` | Obtiene un objetivo propio |
| `PUT /{id}` | Actualiza campos enviados |
| `PATCH /{id}/contribute` | Añade contribución |
| `DELETE /{id}` | Desactiva; `204` |

Alta recibe `name` obligatorio (máximo 100), `targetAmount` obligatorio y mayor que cero, `deadline` opcional, `priority` y `link` opcional (máximo 500). El estado inicial y avance los establece el dominio; el cliente no puede asignar propietario. Actualización usa campos del DTO de update; contribución recibe importe positivo y no puede superar el saldo objetivo sin validación de dominio. Los filtros de listado permiten estado y búsqueda; `page` empieza en 0, `size` por defecto 50 y máximo 200. El detalle devuelve objetivo, progreso, pendiente, prioridad, fecha límite, estado y metadatos.

El servicio comprueba propiedad y estado antes de mutar. Al alcanzar el importe objetivo, el dominio marca el objetivo como completado; contribuir a un objetivo completado se rechaza. La eliminación es lógica y los registros no se borran físicamente. La fecha límite no implica por sí misma una tarea automática. Los importes se persisten como decimales, sin conversión a `double`.

Referencias: [`SavingsGoalService.java`](../../backend/src/main/java/com/puntomartinez/millete/savingsgoals/application/services/SavingsGoalService.java), [`SavingsGoal.java`](../../backend/src/main/java/com/puntomartinez/millete/savingsgoals/domain/model/SavingsGoal.java), [`SavingsGoalPostgresAdapter.java`](../../backend/src/main/java/com/puntomartinez/millete/savingsgoals/infrastructure/out/persistence/postgresql/adapters/SavingsGoalPostgresAdapter.java).

El dashboard consulta este módulo mediante un puerto de salida; su vista no reemplaza el listado paginado de esta API.
