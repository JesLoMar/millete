# Savings Goals — Documentación técnica (Frontend)

## Estructura de archivos

- **components/ContributionModal.tsx** — Modal para añadir contribución a una meta
- **components/EmptyState.tsx** — Estado vacío cuando no hay metas
- **components/SavingsGoalCard.tsx** — Tarjeta de meta de ahorro individual
- **components/SavingsGoalDialog.tsx** — Diálogo para crear/editar meta
- **components/SavingsGoalEditDialog.tsx** — Diálogo específico de edición
- **hooks/useSavingsGoals.ts** — Hooks de query y mutaciones
- **pages/page.tsx** — Página principal de metas de ahorro
- **services/savingsGoals.service.ts** — Servicio de API
- **types/index.ts** — Tipos y contratos de datos

---

## pages/page.tsx

Página principal de metas de ahorro personales.

### Estructura

- Cabecera con título y botón para crear nueva meta.
- Lista de `SavingsGoalCard` en grid responsive.
- `EmptyState` cuando no hay metas.
- Modales controlados para crear, editar y añadir contribuciones.

### Estado local

- `isCreateOpen`: controla el diálogo de creación.
- `editingGoal`: meta seleccionada para edición (null si no hay).
- `contributingGoal`: meta seleccionada para añadir contribución (null si no hay).

---

## services/savingsGoals.service.ts

Servicio de API para metas de ahorro. Base URL: `/savings-goals`.

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /savings-goals | Listar metas del usuario |
| GET | /savings-goals/:id | Obtener meta por ID |
| POST | /savings-goals | Crear meta |
| PUT | /savings-goals/:id | Actualizar meta |
| DELETE | /savings-goals/:id | Eliminar meta |
| PATCH | /savings-goals/:id/contribute | Añadir contribución |

---

## hooks/useSavingsGoals.ts

Centraliza las queries y mutaciones de metas de ahorro.

### useSavingsGoals

- **queryKey:** `['savings-goals']`
- **queryFn:** `savingsGoalsService.getAll`

### useCreateSavingsGoal

- Crea una meta y invalida `['savings-goals']`.
- Toast de éxito/error con claves `savingsGoals:alerts.createSuccess` / `createError`.

### useUpdateSavingsGoal

- Actualiza una meta y invalida `['savings-goals']`.
- Toast de éxito/error con claves `savingsGoals:alerts.updateSuccess` / `updateError`.

### useDeleteSavingsGoal

- Elimina una meta y invalida `['savings-goals']`.
- Toast de éxito/error con claves `savingsGoals:alerts.deleteSuccess` / `deleteError`.

### useAddContribution

- Añade una contribución a una meta (`PATCH /savings-goals/:id/contribute`).
- Invalida `['savings-goals']`.
- Toast de éxito/error con claves `savingsGoals:alerts.contributionSuccess` / `contributionError`.

---

## components/SavingsGoalCard.tsx

Tarjeta de meta de ahorro individual.

### Props

- **goal:** SavingsGoal
- **onEdit:** callback al editar
- **onDelete:** callback al eliminar
- **onContribute:** callback al añadir contribución

### Secciones

- Nombre de la meta y badge de prioridad (LOW/MEDIUM/HIGH).
- Progreso visual con porcentaje completado.
- Cantidad actual / objetivo.
- Fecha límite (si existe).
- Botones de acción: editar, eliminar, aportar.
- Enlace externo (si la meta tiene `link`).

---

## components/SavingsGoalDialog.tsx

Diálogo para crear una nueva meta de ahorro.

### Campos

- **Nombre:** texto obligatorio.
- **Objetivo:** numérico > 0.
- **Fecha límite:** fecha opcional.
- **Prioridad:** Select con LOW/MEDIUM/HIGH.
- **Link:** enlace opcional.

### Validación

Botón deshabilitado si nombre vacío o objetivo ≤ 0.

---

## components/SavingsGoalEditDialog.tsx

Diálogo para editar una meta existente.

### Campos

Incluye los campos de creación más estado (`ACTIVE`, `PAUSED`, `COMPLETED`, `CANCELLED`).

### Reinicio de estado (v0.2.0)

El formulario se inicializa directamente desde la prop `goal` mediante `getInitialForm(goal)`. El componente padre (`page.tsx`) fuerza el reset completo pasando `key={selectedGoal?.id}`. Esto elimina el `useEffect` de sincronización de estado que causaba renders extra (problema reportado por react-doctor).

---

## components/ContributionModal.tsx

Modal para añadir una contribución a una meta.

### Campos

- **Cantidad:** numérico > 0.

### Flujo

Llama a `onSave(amount)`, invalida la lista y cierra el modal.

---

## types/index.ts

- **SavingsGoal:** id, userId, name, targetAmount, currentAmount, deadline?, priority, status, link?.
- **CreateSavingsGoalDTO:** name, targetAmount, deadline?, priority, link?.
- **UpdateSavingsGoalDTO:** name, targetAmount, deadline?, priority, status, link?.

---

## Notas de implementación (v0.1.0)

- Las metas de ahorro son personales (no compartidas).
- Las contribuciones se acumulan en `currentAmount`.
- El estado permite pausar, completar o cancelar metas.
