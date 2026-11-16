# Notificaciones

El módulo proporciona una bandeja persistida por usuario y operaciones para lectura, acción y eliminación. El controlador es [`NotificationController.java`](../../backend/src/main/java/com/puntomartinez/millete/notifications/infrastructure/in/controller/NotificationController.java).

## API

Base `/api/v1/notifications`; todas las rutas requieren autenticación y el usuario se toma de `JwtUser`.

| Método y ruta | Resultado |
|---|---|
| `GET /?limit=` | Lista de notificaciones del usuario; sin límite explícito consulta con `Integer.MAX_VALUE` |
| `GET /paginated?page=0&size=25` | Página en `PaginatedResponseDTO` |
| `GET /unread-count` | Objeto `{ "count": n }` |
| `POST /{notificationId}/read` | Marca como leída; `200` |
| `POST /{notificationId}/actioned` | Marca como atendida; `200` o `404` si el caso de uso no pudo hacerlo |
| `DELETE /{notificationId}` | Elimina lógicamente; `204` |

El DTO de respuesta incluye tipo, título, mensaje, metadata, estado de lectura, requisito/fecha de acción, creación y expiración. Las operaciones de modificación reciben el usuario autenticado y el ID de notificación.

El listado paginado exige página no negativa y tamaño positivo limitado por el servicio; el listado simple acepta `limit`. Las consultas solo devuelven registros del usuario autenticado. `actioned` solo tiene efecto cuando la notificación admite acción y el caso de uso la puede completar; un ID inexistente o de otro propietario no permite modificar el registro. `expiresAt` indica caducidad, pero no implica borrado físico inmediato.

Los tipos y metadata son datos de dominio, no instrucciones universales; el consumidor interpreta `requiresAction` y el estado según el tipo de notificación.

## Integraciones

Las metas grupales pueden crear notificaciones para invitaciones. Inversiones usa el puerto de notificaciones para publicar incidencias de salud. El servicio, modelo y adaptador de persistencia están bajo `notifications/application`, `notifications/domain` e `notifications/infrastructure`.
