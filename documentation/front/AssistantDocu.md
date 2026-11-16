# Asistente del dashboard

El asistente acepta texto libre, solicita una interpretación y presenta acciones interpretadas para revisión en un diálogo. La UI no ejecuta automáticamente una acción solo por recibir texto.

## Flujo

[`AssistantCard.tsx`](../../frontend/src/features/assistant/components/AssistantCard.tsx) valida entrada no vacía, muestra ejemplos y estados de envío/error. Envía texto a `POST /assistant/interpret` mediante [`assistant.service.ts`](../../frontend/src/features/assistant/services/assistant.service.ts), con timeout específico y sin notificación global duplicada.

La respuesta de tipo `UNKNOWN`, ausencia de acción/datos o acción `EDIT_CATEGORY` se trata como no admitida en la interfaz. Las interpretaciones restantes abren `AssistantActionDialog` para revisión. Tipos en [`types/index.ts`](../../frontend/src/features/assistant/types/index.ts). El comportamiento del modelo/servidor no se infiere desde el cliente.

## Acciones interpretables y aplicación

| Acción del contrato | Interfaz que se abre |
|---|---|
| `ADD_EXPENSE_TRANSACTION`, `ADD_INCOME_TRANSACTION` | `NewTransactionDialog`, tipo fijado y descripción/categoría/importe precargados. |
| `ADD_RECURRING_EXPENSE_TRANSACTION`, `ADD_RECURRING_INCOME_TRANSACTION` | `NewRecurringTransactionDialog`, con frecuencia, intervalo y fechas cuando se especifican. |
| `ADD_CATEGORY` | `AddCategoryDialog`, precarga nombre, descripción y presupuesto. |
| `ADD_SAVING_GOAL` | `SavingsGoalDialog`, precarga nombre, objetivo, prioridad, fecha límite y enlace. |
| `ADD_SAVING_GOAL_CONTRIBUTION` | `ContributionModal`; confirma con `useAddContribution` para la meta identificada. |
| `EDIT_TRANSACTION` | `EditTransactionDialog`, resolviendo cada cambio especificado frente al valor objetivo. |
| `EDIT_RECURRING_TRANSACTION` | `EditRecurringTransactionDialog`, usando target y cambios indicados. |
| `EDIT_SAVING_GOAL` | `SavingsGoalEditDialog`, con cambios y atributos actuales de la meta. |

Los tipos también declaran `EDIT_CATEGORY`, pero el componente lo rechaza expresamente como no soportado. `NEEDS_INFORMATION`, campos faltantes, entidades no resueltas, defaults aplicados, probabilidad y abstención forman parte del contrato de interpretación/noticia; no crean una acción automática.

La revisión termina cuando el usuario cierra el diálogo o envía el formulario. Las altas/ediciones usan los hooks de las features correspondientes; por eso las mutaciones e invalidaciones son las de categorías, transacciones/recurrentes y metas, no una mutación genérica del asistente. La aportación llama a `useAddContribution` y cierra en éxito. Los formularios subordinados conservan sus validaciones y estados de envío/error; el asistente no persiste la interpretación por sí mismo.
