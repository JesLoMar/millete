# Metas grupales

El módulo `/api/v1/goals` administra metas compartidas, miembros, invitaciones y movimientos de aportación. Su controlador actual es [`GroupGoalController.java`](../../backend/src/main/java/com/puntomartinez/millete/groupgoals/infrastructure/in/controller/GroupGoalController.java).

## API actual

Todas las rutas requieren autenticación y derivan el usuario de `JwtUser`.

| Método y ruta | Respuesta |
|---|---|
| `GET /` | Lista simple (`List<GoalListItemResponseDTO>`), no paginada |
| `GET /{goalId}` | Detalle |
| `POST /` | Crea; `201` con resumen |
| `PUT /{goalId}` | Actualiza; `200` sin cuerpo |
| `DELETE /{goalId}` | Elimina; `204` |
| `PUT /{goalId}/members/{memberId}` | Actualiza miembro; `200` sin cuerpo |
| `DELETE /{goalId}/members/{memberId}` | Elimina miembro; `204` |
| `GET /{goalId}/contributions?page=0&size=60` | Historial paginado |
| `POST /{goalId}/contributions` | Registra aportación; `201` |
| `POST /{goalId}/contributions/withdraw` | Registra retirada; `200` |
| `POST /{goalId}/leave` | Sale de la meta; `204` |
| `GET /{goalId}/contributions/calculation` | Calcula distribución |
| `POST /{goalId}/invitations` | Invita; `201` |
| `GET /invitations/pending` | Lista sin paginar |
| `POST /invitations/{invitationId}/accept` | Acepta; `200` |
| `POST /invitations/{invitationId}/reject` | Rechaza; `200` |

La ruta de salir es `POST /{goalId}/leave`; no se expone como GET. La autorización de administrador y pertenencia se aplica en los casos de uso/servicios. Las invitaciones son internas y se dirigen a identificadores de usuarios; no se describe envío de correo.

## Modelo

Los modelos principales son `GoalUnit`, `GoalMember`, `GoalInvitation` y `GoalContribution`. `GoalUnit` mantiene nombre (máximo 100), objetivo mensual mayor que cero, modo de distribución, timestamps y estado activo. Los modos admitidos son `EQUITATIVE`, `PROPORTIONAL` y `CUSTOM`: el primero divide por igual; el segundo pondera por salarios (nulos cuentan como cero; si todos suman cero, no calcula); el tercero aplica porcentajes personalizados y exige todos presentes y suma exacta de 100. Los importes sugeridos se redondean a dos decimales con HALF_UP. No confundir objetivo mensual compartido con saldo ya aportado.

La persona creadora obtiene rol `ADMIN`. Actualizar/eliminar meta, actualizar/eliminar miembros e invitar requieren administrador activo; consultar detalle, calcular distribución, ver historial y aportar/retirar requieren pertenencia activa. El detalle solo muestra salario/porcentaje al administrador o al miembro correspondiente. `UpdateMemberRequestDTO` acepta `role`, `salary` no negativo y `customPercentage` de 0 a 100; en modo `CUSTOM` el servicio exige porcentaje al actualizar miembro. Un administrador no puede eliminarse ni salir si es el último administrador. La invitación recibe `identifier` obligatorio y se resuelve mediante el port de usuarios; aceptar/rechazar utiliza al usuario autenticado. V5 elimina token de invitación.

La creación acepta `name` (obligatorio, máximo 100), `monthlyTarget` (obligatorio, mínimo 0.01) y `distributionMode` obligatorio. `POST /contributions` recibe importe y registra tipo `DEPOSIT`, responde `201`; `/contributions/withdraw` recibe importe mínimo 0.01, exige saldo neto suficiente, registra `WITHDRAWAL` y responde `200`. La historia pagina con tamaño por defecto 60. `POST /leave` desactiva la membresía, no borra meta ni historial, y aplica regla del último administrador.

Las retiradas serializan la validación de saldo por miembro mediante un bloqueo de escritura sobre su fila activa de membresía. El bloqueo se obtiene antes de consultar las aportaciones y se mantiene hasta completar la transacción; así, una retirada concurrente vuelve a calcular el saldo después de que la anterior confirme.

Los adaptadores persisten meta, miembros, invitaciones y movimientos de contribución en tablas separadas; V5 hace `monthly_target` obligatorio, elimina tokens de invitación y restringe `goal_contributions.type` a `DEPOSIT` y `WITHDRAWAL`.
