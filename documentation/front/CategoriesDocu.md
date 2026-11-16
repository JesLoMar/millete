# Categorías

La página combina el listado paginado del servidor con gastos por período y acciones de gestión.

## Listado y presupuestos

[`CategoryTable.tsx`](../../frontend/src/features/categories/components/CategoryTable.tsx) consulta categorías a través de `useCategories`, recibe gastos mediante `useCategoryExpenses(period)` y aplica `getPeriodBudgetLimit`. La tabla muestra búsqueda, estados de carga/error y navegación de páginas. Los gastos se asocian por ID de categoría, no solo por nombre.

La paginación se apoya en [`useServerPagination.ts`](../../frontend/src/shared/hooks/useServerPagination.ts); el tamaño de bloque y página se fijan en hooks del módulo, no en la tabla. La búsqueda usa valor de entrada y la lógica del hook correspondiente.

## Operaciones

`useCategoryMutations` centraliza crear, actualizar y borrar, así como invalidaciones ([`useCategoryMutations.ts`](../../frontend/src/features/categories/hooks/useCategoryMutations.ts)). Los formularios están en `AddCategoryDialog.tsx` y `EditCategoryDialog.tsx`; las filas y confirmación se presentan mediante `CategoryRow.tsx` y `ConfirmDeletionDialog` compartido. Colores disponibles están en `constants.ts`.

Los servicios y tipos del frontend son fuente para métodos, parámetros y payloads. Esta guía no atribuye efectos de persistencia o cascadas del servidor que el cliente no observa.

## Contrato y secuencia

| Solicitud del cliente | Campos / respuesta consumida |
|---|---|
| GET `/categories?page=&size=50&search=` | Página con `content`, totales y categoría (`id,name,description,color,budgetLimit,...`). `search` se omite vacío y se aplica con debounce de 300 ms. |
| GET `/dashboard/categories?period=` | Gastos por período; `CategoryTable` asocia `categoryId` con categoría y usa `amount`. |
| POST `/categories` | `{ name, description, color, budgetLimit }`; texto se recorta y presupuesto ausente es `null`. |
| PUT `/categories/{id}` | Los mismos campos editables; mutación devuelve el cuerpo al hook. |
| DELETE `/categories/{id}` | El cliente envía el ID y espera resolución; no documenta efectos adicionales del backend. |

La página fija un período y el listado permite buscar, ver gasto versus límite ajustado, abrir edición y pedir confirmación de borrado. Crear requiere nombre; editar valida nombre y presupuesto no negativo. `ColorPicker` ofrece los colores declarados en `constants.ts`. En carga aparece skeleton; errores de lista y gastos tienen mensajes/reintento independientes; lista vacía distingue búsqueda sin resultados.

Cada éxito invalida `categories`, `budgets`, `categoryBudgets`, `categoryExpenses`, `dashboardMetrics`, `transactionMetrics`, `categoryStats`, `historyChart` y `recentTransactions`; las mutaciones muestran toast. Fallos muestran mensaje de respuesta (`message`/`error`) o traducción y la tabla/dialog conserva estado de reintento según operación.
