# Transacciones

La feature presenta transacciones ordinarias y planificadas (recurrentes), con resumen, filtros, búsqueda y paginación desde el servidor.

## Listas

[`TransactionList.tsx`](../../frontend/src/features/transactions/components/TransactionList.tsx) comparte los datos entre vistas desktop y móvil, filtros, edición y borrado con diálogo de confirmación. `useTransactions` consulta por período, tipo y búsqueda. La lista recurrente tiene su propio hook y filtros. La paginación común reside en [`useServerPagination.ts`](../../frontend/src/shared/hooks/useServerPagination.ts); tamaños y parámetros los configuran los hooks consumidores.

## Formularios y mutaciones

Los diálogos permiten crear/editar transacciones y recurrencias. [`useTransactionMutation.ts`](../../frontend/src/features/transactions/hooks/useTransactionMutation.ts) agrupa las operaciones y sus invalidaciones. La creación exige categoría desde la interfaz; edición puede manejar categoría nula según su formulario. Mensajes y contratos se extraen de los componentes y tipos actuales.

El cálculo de próxima ejecución y etiquetas de frecuencia se implementa en [`utils.ts`](../../frontend/src/features/transactions/utils.ts). El cliente puede mostrar un aviso de límite si la respuesta incluye `limitExceeded`; no garantiza reglas de cálculo del servidor.

## Código

Componentes: [`frontend/src/features/transactions/components`](../../frontend/src/features/transactions/components). Tipos y constantes: `index.ts` y `constants.ts`. La confirmación actual usa `ConfirmDeletionDialog`, no `window.confirm`.

## Contratos de consulta y escritura

| Solicitud | Datos usados |
|---|---|
| GET `/transactions?page=&size=50&period=&search=&type=` | Página de `{ id, categoryId, categoryName, categoryColor, amount, date, type, description, active }`; `type` se omite para todos. Muestra 10 elementos por página. |
| GET `/transactions/metrics?period=` | `income`, `expenses`, `balance`, `count` y tendencias para resumen. |
| POST `/transactions`; PUT `/transactions/{id}`; DELETE `/transactions/{id}` | `RegisterTransactionRequest` tiene categoría, importe, tipo, descripción y fecha; PUT acepta payload parcial. |
| GET `/planned-transactions?page=&size=50&search=&type=` | Recurrentes paginadas; la lista muestra categoría, importe/tipo, frecuencia, intervalo y fechas. |
| POST `/planned-transactions`; PUT `/planned-transactions/{id}`; DELETE `/planned-transactions/{id}` | Alta/edición usa categoría, importe, tipo, descripción, frecuencia, intervalo, inicio y fin opcional. |

`NewTransactionDialog` valida descripción, categoría e importe positivo; traduce gasto/ingreso a signo de importe al formar payload. Edición permite categoría nula. Formularios recurrentes requieren los campos de frecuencia e inicio, con fin opcional. `CategorySelect` carga las categorías; las filas muestran color de categoría devuelto por API y alternativa visual si falta.

La consulta muestra skeleton durante carga y distingue lista vacía de ausencia de resultados filtrados. Filtros, búsqueda y período forman parte de la identidad de consulta; la página de visualización cambia entre bloques por hook compartido. Borrado abre confirmación y mantiene diálogo si la mutación falla. Las mutaciones normales invalidan `transactions`, `transactionMetrics`, `dashboardMetrics`, `historyChart`, `categoryStats`, `budgets`, `recentTransactions`; las recurrentes invalidan `plannedTransactions`. Errores se notifican con el mensaje de API si existe, o la traducción. La respuesta de crear/editar puede contener `limitExceeded`; solo en ese caso el hook muestra el aviso correspondiente.

El servicio de recurrencias se implementa mediante `usePlannedTransactions.ts`; tanto fechas como formato de periodicidad son presentación del cliente. No afirmar que el cliente ejecuta las transacciones programadas.
