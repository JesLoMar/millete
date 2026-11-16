# Notificaciones

La feature incluye una página protegida, lista paginada y contador no leído, además de una campana de acceso rápido en la interfaz compartida.

## Consultas y acciones

[`useNotifications.ts`](../../frontend/src/features/notifications/hooks/useNotifications.ts) ofrece consulta completa, recientes, paginada (bloques de 25 y páginas visibles de 5) y contador no leído. Las acciones permiten marcar una o todas como leídas y eliminar; las mutaciones invalidan las queries de notificaciones. La actualización optimista del contador al marcar todo restaura el valor anterior si falla.

[`notifications.service.ts`](../../frontend/src/features/notifications/services/notifications.service.ts) define las solicitudes del cliente: `GET /notifications`, `GET /notifications/paginated`, `GET /notifications/unread-count`, `POST /notifications/{id}/read` y `DELETE /notifications/{id}`.

## Interfaz

`NotificationList` y `NotificationItem` componen la pantalla; `NotificationBell`, `NotificationBellList` y `NotificationBellItem` proporcionan el acceso compacto. La ruta `/notifications` está montada como protegida en [`App.tsx`](../../frontend/src/App.tsx). Invitaciones de metas grupales también aparecen en el perfil; ver [ProfileDocu.md](ProfileDocu.md).

## Comportamiento visible

La página principal usa `useNotifications` (GET completo): muestra tres placeholders durante carga, vacío si no hay elementos y una tarjeta por notificación. Cada elemento presenta título, mensaje y fecha localizable; las no leídas se resaltan. El botón de lectura se ofrece solo para no leídas y el borrado para todas. La campana consulta recientes y contador, con acciones según sus componentes; el hook mantiene `staleTime` de un minuto.

Para notificaciones `GOAL_INVITATION` no leídas con ID de invitación, la tarjeta ofrece aceptar, rechazar o ver perfil en la sección de notificaciones. Aceptar/rechazar ejecuta los hooks de invitación de metas grupales; en éxito marca la notificación como leída, y en error muestra aviso. Los botones se bloquean durante la acción.

| Hook | Efecto |
|---|---|
| `useMarkNotificationAsRead` | POST read y luego invalida `notifications`. |
| `useMarkAllNotificationsAsRead` | Lee todas y marca las no leídas; optimiza contador a cero, restaura contador anterior si falla y al terminar invalida. |
| `useDeleteNotification` | DELETE y posterior invalidación de consultas relacionadas. |
| `usePaginatedNotifications` | Consulta `/paginated` en bloques de 25 y páginas de 5 mediante paginador compartido. |

Los endpoints y campos se limitan al cliente: `Notification` incluye `id,type,title,message,createdAt,read,metadata`; `metadata` puede incluir `invitationId`/`id`.
