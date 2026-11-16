# Metas personales de ahorro

La feature lista metas personales y permite crearlas, editarlas, eliminarlas, añadir aportaciones y retirar importes.

## Página y paginación

[`pages/page.tsx`](../../frontend/src/features/savingsgoals/pages/page.tsx) compone tarjetas, formularios y diálogos de contribución/retiro. `useSavingsGoals.ts` consulta páginas del servidor; usa bloques de 45 elementos y muestra páginas de 9, con búsqueda por nombre. No hay filtro de estado en este hook actual.

## Operaciones

[`savingsGoals.service.ts`](../../frontend/src/features/savingsgoals/services/savingsGoals.service.ts) define la API usada por los hooks. Crear, actualizar, borrar, aportar y retirar invalidan las metas y las métricas del dashboard. Los links se normalizan antes de crear o actualizar. Los modelos actuales están en [`types/index.ts`](../../frontend/src/features/savingsgoals/types/index.ts).

Los campos y estados visibles se detallan en `SavingsGoalForm.tsx`, `SavingsGoalCard.tsx` y los diálogos de `components/`. La documentación se limita al comportamiento del cliente.

## Servicio, campos y estados

| Método | Ruta | Payload/uso |
|---|---|---|
| GET | `/savings-goals?page=&size=45&search=` | Busca por texto recortado; respuesta paginada de metas. |
| POST | `/savings-goals` | `name,targetAmount,deadline?,priority,link?`. |
| PUT | `/savings-goals/{id}` | Campos editables más `status`. |
| DELETE | `/savings-goals/{id}` | Elimina desde la acción confirmada de UI. |
| PATCH | `/savings-goals/{id}/contribute` | Añade importe. |
| POST | `/savings-goals/{id}/withdraw` | Retira importe. |

Los tipos vigentes incluyen `targetAmount`, `currentAmount`, prioridad y estado; la fecha límite y enlace son opcionales. La UI ofrece estados ACTIVE, PAUSED, COMPLETED y CANCELLED. Los formularios validan nombre y objetivo positivo; links pasan por `normalizeHttpLink`. Tarjeta presenta progreso y acciones, y los diálogos cubren alta, edición, contribución y retiro. Mientras se carga se usa estado de carga; lista vacía tiene EmptyState. Hooks notifican éxito/error e invalidan tanto `savings-goals` como `dashboardMetrics` en las operaciones.
