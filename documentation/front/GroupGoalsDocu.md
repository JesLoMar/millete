# Metas grupales

La feature permite consultar metas compartidas, gestionar miembros e invitaciones y registrar o retirar aportaciones desde la interfaz.

## Vistas y consultas

[`pages/page.tsx`](../../frontend/src/features/groupgoals/pages/page.tsx) presenta selector y detalle de una meta. `useGroupGoalQueries.ts` carga listas, detalle y contribuciones mediante la API configurada; `useServerPagination` administra los bloques y páginas visibles. `JoinGroupGoalPage.tsx` es la página para procesar una invitación, y la ruta está protegida según [`App.tsx`](../../frontend/src/App.tsx).

## Acciones y cálculos

Los hooks de mutación contemplan crear/eliminar meta, invitar, editar meta/miembro, añadir y retirar contribuciones. Los modos de distribución son equitativo, proporcional y personalizado; [`utils.ts`](../../frontend/src/features/groupgoals/utils.ts) calcula importes esperados y contribuidos para la presentación. El cliente muestra validación de porcentaje personalizado. No asumir reglas de autorización o persistencia más allá del contrato que invoca la UI.

Los diálogos están en `components/dialogs/`; tipos en `types/index.ts`. Las invitaciones también pueden atenderse en el perfil y el centro de notificaciones.

## Lecturas y escrituras HTTP

Los hooks actuales llaman a `/goals` (no `/group-goals`) mediante `apiClient`.

| Método y path | Datos que usa la interfaz |
|---|---|
| GET `/goals?page=&size=12` | Lista `{id,name,monthlyTarget,memberCount/activeMembers,isAdmin}`; muestra 4 por página en UI. |
| GET `/goals/{id}` | Detalle con nombre/objetivo/modo/admin, miembros y contribuciones; el mapper normaliza rol y tipo de contribución. |
| GET `/goals/{id}/contributions?page=&size=60` | Historial con aportante, importe, tipo y fecha; UI muestra 20 por página. |
| POST `/goals` | `{name,monthlyTarget,distributionMode}`. |
| POST `/goals/{id}/invitations` | `{identifier}`. |
| PUT `/goals/{id}` | Solo campos cambiados entre `name`, `monthlyTarget`, `distributionMode`. |
| PUT/DELETE `/goals/{id}/members/{memberId}` | Edita rol/salario/porcentaje o elimina miembro. |
| POST `/goals/{id}/contributions` y `/contributions/withdraw` | `{amount}` para aportar o retirar. |
| DELETE `/goals/{id}/contributions` | Reinicia los movimientos de esa meta tras una confirmación explícita. Solo los administradores ven el botón. El borrado es definitivo y el progreso vuelve a cero. |
| POST `/invitations/{token}/accept` o `/reject` | Acciones usadas por página de invitación/notificaciones. |

## Validación, resultado y caché

El selector permite crear meta; detalle ofrece gestión de miembros, modo, objetivo, historial y aportes. Los modos son `EQUITATIVE`, `PROPORTIONAL` y `CUSTOM`; el cálculo visual reparte objetivo por partes iguales, proporción de salarios o porcentaje definido. En CUSTOM el componente advierte si la suma difiere de 100% (tolerancia UI 0.01). El formulario de invitación acepta identificador de texto no vacío, no exige formato email en cliente.

Lista/detalle/contribuciones usan estados de consulta y paginación compartida. Cada mutación notifica resultado y refresca `group-goals`; invitar además invalida `notifications`. Errores de mutaciones usan mensaje de API si está presente o texto traducido. La UI separa aceptación y rechazo; el contrato observado está en `useInvitations.ts` y `JoinGroupGoalPage.tsx`.
