# Transactions — Documentación técnica (Frontend)

## Estructura de archivos

- **components/CategorySelect.tsx** — Selector de categoría reutilizable
- **components/TypeToggle.tsx** — Selector de tipo gasto/ingreso reutilizable
- **components/RecurringTransactionRow.tsx** — Fila individual de transacción recurrente
- **components/RecurringTransactionsList.tsx** — Lista de transacciones recurrentes con filtros
- **components/TransactionActions.tsx** — Botones de acciones (nueva normal + nueva recurrente)
- **components/TransactionList.tsx** — Lista paginada de transacciones con filtros
- **components/TransactionRow.tsx** — Fila individual de transacción
- **components/TransactionSkeleton.tsx** — Esqueleto de carga reutilizable
- **components/TransactionSummary.tsx** — Tarjetas de métricas de transacciones
- **components/dialogs/NewTransactionDialog.tsx** — Diálogo de creación de transacción
- **components/dialogs/EditTransactionDialog.tsx** — Diálogo de edición de transacción
- **components/dialogs/NewRecurringTransactionDialog.tsx** — Diálogo de creación de transacción recurrente
- **components/dialogs/EditRecurringTransactionDialog.tsx** — Diálogo de edición de transacción recurrente
- **constants.ts** — Constantes compartidas (filtros, colores, frecuencias)
- **hooks/useTransactionMutation.ts** — Hook centralizado con las 6 mutaciones
- **index.ts** — Tipos y contratos de datos del módulo
- **utils.ts** — Funciones de utilidad (formateo de fechas, frecuencias y próxima ejecución)
- **pages/page.tsx** — Página principal de transacciones

---

## pages/page.tsx

Página principal de transacciones. Estructura:

- Sidebar izquierdo permanente.
- TopNav superior.
- Cabecera con Header (con `hidePeriodSelector` para ocultar su propio selector), PeriodSelector centrado y TransactionActions a la derecha.
- TransactionSummary con 4 tarjetas de métricas (recibe `period`).
- TransactionList con filtros, buscador y paginación (recibe `period`).
- RecurringTransactionsList con sus propios filtros (sin dependencia del período).

El período se gestiona con `useState<PeriodFilter>("month")` y se pasa a TransactionSummary y TransactionList.

---

## index.ts — Tipos

Define los contratos de datos para el módulo:

- **TransactionType:** "INCOME" | "EXPENSE".
- **TransactionResponse:** id, userId, categoryId, amount, type, description, date, createdAt, limitExceeded (opcional).
- **RegisterTransactionRequest:** categoryId, amount, type, description, date.
- **UpdateTransactionRequest:** Partial<RegisterTransactionRequest>.

---

## constants.ts

Constantes compartidas entre todos los componentes del módulo:

- **FREQUENCY_TYPES:** array de objetos con value ("DAYS", "WEEKS", "MONTHS", "YEARS") y labelKey (claves i18n).
- **FREQUENCY_LABELS:** mapeo de tipo de frecuencia a claves i18n plurales.
- **FREQUENCY_SINGULAR:** mapeo de tipo de frecuencia a claves i18n singulares (everyDay, everyWeek, everyMonth, everyYear).
- **CATEGORY_COLORS:** mapeo de nombre de categoría a clases Tailwind (Alimentación, Hogar, Transporte, Suministros, Ocio).
- **FILTERS:** array "all" | "income" | "expense" con `as const`.
- **FILTER_LABELS:** mapeo de Filter a claves i18n.
- **Filter:** tipo extraído de FILTERS.

---

## utils.ts

Funciones de utilidad:

- **formatCurrency / formatNumber:** re-exportados desde `@/shared/utils/i18nFormat.ts`.
- **formatDate:** formatea una fecha ISO usando `toLocaleDateString` con el idioma actual de i18n (día 2 dígitos, mes abreviado, año numérico).
- **formatFrequency:** formatea la frecuencia de una transacción recurrente. Si el intervalo es 1 usa clave singular (`FREQUENCY_SINGULAR`), si no usa clave plural con el intervalo (`transactions.recurring.every`). Recibe la función `t` de i18n como parámetro.
- **calculateNextExecution:** calcula la próxima fecha de ejecución de una transacción recurrente basada en `startDate`, `frequencyType` y `frequencyInterval`. Itera sumando intervalos hasta superar la fecha actual. Si hay `endDate` y la próxima ejecución la supera, devuelve "—". Si `startDate` es futura, devuelve la fecha de inicio formateada.

---

## components/TransactionActions.tsx

Agrupa los botones de creación:

- NewRecurringTransactionDialog: botón outline con icono RefreshCcw y texto "Transacción recurrente".
- NewTransactionDialog: botón primary con icono Plus y texto "Nueva transacción".

Ambos son diálogos con su propio Trigger, por lo que TransactionActions no necesita estado. Acepta prop `className` opcional para estilos adicionales.

---

## components/TransactionSummary.tsx

Muestra 4 tarjetas de métricas usando FormattedMetricCard:

- **Ingresos del período:** icono ArrowUpRight, color esmeralda.
- **Gastos del período:** icono ArrowDownLeft, color rosa, tendencia invertida (`invertedTrend`).
- **Balance:** icono Scale, color primario.
- **Total de transacciones:** icono Activity, color ámbar, formato `number`.

### Datos

Obtiene los datos de `GET /transactions/metrics?period=` mediante `useQuery` con query key `['transactionMetrics', period]`.

### Tipos internos

- **TransactionMetrics:** income, expenses, balance, count, incomeTrend, expensesTrend, balanceTrend, countTrend.

### periodLabel

Texto dinámico para el tooltip de tendencias: `dashboard.metrics.vsLastMonth`, `vsLastWeek` o `vsLastYear` según el período.

---

## components/TransactionList.tsx

Lista principal de transacciones con filtros, buscador y paginación.

### Datos

- Obtiene transacciones de `GET /transactions` mediante `useQuery` con query key `['transactions']` (sin dependencia del período).
- Filtra solo las activas (`active !== false`).

### Filtros

Tres botones: Todos, Ingresos, Gastos. Al cambiar el filtro se resetea la página actual a 1 mediante `useEffect` con `goToPage(1)`.

### Buscador

Input con icono Search. Filtra por texto en la descripción. Búsqueda en tiempo real sin distinguir mayúsculas/minúsculas.

### Paginación

- Usa `usePagination` desde `@/features/categories/hooks/usePagination` con 10 items por página.
- Controles de navegación: flechas ChevronLeft/ChevronRight y contador "X / Y".
- Muestra texto "Mostrando X-Y de Z transacciones".

### Fila de transacción

- Icono de flecha arriba (ingreso, verde esmeralda) o abajo (gasto, rosa).
- Descripción (truncada), fecha formateada con `formatDate` y categoría en badge coloreado.
- Importe formateado con signo +/- y 2 decimales con separadores españoles.
- Menú desplegable (visible al hover) con opciones de editar y eliminar.

### Edición

Abre EditTransactionDialog con los datos de la transacción seleccionada. El diálogo se controla con estado `editingTransaction`.

### Eliminación

`DELETE /transactions/:id` con confirmación nativa `window.confirm`. Invalida las queries de transactions, transactionMetrics y dashboardMetrics.

### Estados

- **Carga:** skeleton inline con 10 filas simuladas.
- **Vacío:** mensaje "No hay transacciones" centrado.

---

## components/TransactionRow.tsx

Componente independiente para cada fila de la lista de transacciones (usado por TransactionList).

### Props

- **transaction:** objeto con id, description, category, categoryId, amount, date, type.
- **onEdit:** callback que recibe la transacción.
- **onDelete:** callback que recibe la transacción.

### Estructura

- Icono circular con flecha según tipo (ArrowUpRight verde para ingreso, ArrowDownLeft rosa para gasto).
- Descripción, fecha formateada y badge de categoría con color de `CATEGORY_COLORS`.
- Importe con signo +/- y 2 decimales en formato español.
- Menú desplegable visible al hover con opciones de editar y eliminar (usando claves i18n).

---

## components/RecurringTransactionsList.tsx

Lista de transacciones recurrentes con sus propios filtros y buscador.

### Datos

- Obtiene transacciones recurrentes mediante `usePlannedTransactions` (hook compartido).
- Usa `useQueryClient` para invalidar queries tras eliminar.

### Filtros y buscador

Misma mecánica que TransactionList: tres botones de filtro y buscador por texto con icono Search.

### Eliminación

Usa `ConfirmDeletionDialog` (reutilizado desde Categories) en lugar de `window.confirm`. Al confirmar hace `DELETE /planned-transactions/:id` e invalida `['plannedTransactions']`.

### Edición

Abre `EditRecurringTransactionDialog` con los datos de la transacción seleccionada.

### Estados

- **Carga:** TransactionSkeleton con 5 filas.
- **Sin transacciones:** no muestra nada (`return null`).
- **Sin resultados tras filtrar:** mensaje "No se encontraron transacciones recurrentes".

### Badge

Muestra contador de resultados filtrados junto a los botones de filtro.

---

## components/RecurringTransactionRow.tsx

Componente independiente para cada fila de transacción recurrente.

### Props

- **transaction:** PlannedTransaction (de `usePlannedTransactions`).
- **onEdit:** callback que recibe la transacción.
- **onDelete:** callback que recibe la transacción.

### Estructura

- Icono circular con flecha según tipo.
- Descripción.
- Frecuencia formateada con `formatFrequency` (pasando `t` como parámetro), icono Calendar, fecha de inicio y fecha de fin (si existe, separadas por →).
- Próxima ejecución calculada con `calculateNextExecution` (si hay endDate).
- Importe formateado con signo y 2 decimales.
- Menú desplegable visible al hover con opciones de editar y eliminar.

---

## components/TransactionSkeleton.tsx

Componente de esqueleto reutilizable para listas de transacciones.

### Props

- **rows?:** número de filas simuladas (default 5).

### Estructura

Contenedor con borde y fondo de card. N filas con círculo, dos barras de texto y un valor simulado, todo con `animate-pulse`.

Usado por TransactionList (10 filas), RecurringTransactionsList (5 filas) y potencialmente otros componentes.

---

## components/CategorySelect.tsx

Selector de categoría reutilizable usado por los 4 diálogos.

### Props

- **value:** id de categoría seleccionada.
- **onValueChange:** callback al cambiar.
- **className?:** clases adicionales.

### Comportamiento

- Obtiene categorías mediante `useCategories` (hook compartido).
- Muestra Loader2 animado mientras carga con texto "Cargando...".
- Si no hay categorías, muestra mensaje de "No hay categorías".
- Renderiza cada categoría como SelectItem con name.

---

## components/TypeToggle.tsx

Selector de tipo gasto/ingreso reutilizable usado por los 4 diálogos.

### Props

- **value:** "INCOME" | "EXPENSE".
- **onChange:** callback al cambiar.

### Estructura

Dos botones en contenedor con borde:
- **Gasto:** fondo rosa semitransparente cuando activo (`bg-rose-500/20 text-rose-400`).
- **Ingreso:** fondo verde semitransparente cuando activo (`bg-emerald-500/20 text-emerald-400`).

---

## hooks/useTransactionMutation.ts

Hook centralizado con las 6 mutaciones del módulo.

### invalidateAll

Función asíncrona que ejecuta `Promise.all` con 9 invalidaciones:
- `['transactions']`
- `['transactionMetrics']`
- `['dashboardMetrics']`
- `['historyChart']`
- `['categoryStats']`
- `['budgets']`
- `['recentTransactions']`
- `['categoryExpenses']`
- `['plannedTransactions']`

### checkBudgetLimit

Función que verifica si la respuesta de crear/actualizar transacción incluye `limitExceeded === true`. Si es así, muestra una notificación warning con duración de 6 segundos avisando que se ha superado el 70% del presupuesto mensual.

### createTransaction

- **mutationFn:** POST a `/transactions` con los datos de la transacción.
- **onSuccess:** ejecuta `invalidateAll`, muestra notificación de éxito y llama a `checkBudgetLimit` con la respuesta.

### updateTransaction

- **mutationFn:** PUT a `/transactions/:id` con datos parciales.
- **onSuccess:** ejecuta `invalidateAll`, muestra notificación de éxito y llama a `checkBudgetLimit`.

### deleteTransaction

- **mutationFn:** DELETE a `/transactions/:id`.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### createRecurring

- **mutationFn:** POST a `/planned-transactions`.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### updateRecurring

- **mutationFn:** PUT a `/planned-transactions/:id`.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### deleteRecurring

- **mutationFn:** DELETE a `/planned-transactions/:id`.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### Estados de carga combinados

- **isCreating:** `createTransaction.isPending || createRecurring.isPending`
- **isUpdating:** `updateTransaction.isPending || updateRecurring.isPending`
- **isDeleting:** `deleteTransaction.isPending || deleteRecurring.isPending`

---

## Diálogos (dialogs/)

### NewTransactionDialog.tsx

Diálogo de creación de transacción. Soporta modo controlado (desde dashboard) y no controlado (con Trigger propio).

#### Props

- **open?:** controla apertura desde padre.
- **onOpenChange?:** callback de cambio de apertura.

Si no se pasan props, el diálogo usa su propio estado interno y muestra un botón Trigger con icono Plus y texto "Nueva transacción".

#### Campos

- **Descripción:** texto obligatorio con placeholder y foco automático.
- **Tipo:** TypeToggle (default "EXPENSE").
- **Importe:** numérico obligatorio con min 0.01 y step 0.01.
- **Categoría:** CategorySelect.

#### Flujo

1. Llama a `createTransaction.mutateAsync` con description (trim), categoryId, amount (negativo para gastos, positivo para ingresos), type y date (fecha actual ISO sin milisegundos: `new Date().toISOString().split('.')[0]`).
2. `onSuccess` de la mutación invalida 9 queries, muestra notificación y verifica límite de presupuesto.
3. Cierra y resetea el formulario.
4. Si hay error, muestra el mensaje del backend (message, error o clave por defecto).

#### Validaciones

- Descripción, categoría e importe obligatorios.
- Importe mayor que 0.
- Botón deshabilitado si `!isValid` o `isCreating`.
- Inputs deshabilitados durante `isCreating`.

---

### EditTransactionDialog.tsx

Diálogo de edición de transacción. Siempre controlado: se abre desde TransactionList al seleccionar una transacción.

#### Props

- **transaction:** Transaction | null.
- **open:** boolean.
- **onOpenChange:** callback al cerrar.

#### Campos

Mismos campos que NewTransactionDialog: descripción, tipo, importe y categoría.

#### Flujo

1. Al abrirse, los campos se inicializan con los valores de la transacción (usa `key={transaction?.id ?? "new"}` en Dialog para forzar reinicio al cambiar de transacción).
2. Al guardar, hace `PUT /transactions/:id` con description, categoryId, amount, type y date (mantiene la fecha original).
3. Invalida 8 queries manualmente (transactions, transactionMetrics, dashboardMetrics, historyChart, categoryStats, budgets, recentTransactions, categoryExpenses).
4. Cierra el diálogo.

#### Validaciones

- Descripción e importe obligatorios.
- Importe mayor que 0.
- Categoría opcional (a diferencia de creación, permite `categoryId: null`).
- Botón deshabilitado si `!isValid` o `isSubmitting`.
- Estado `isSubmitting` local (no usa el hook centralizado).

---

### NewRecurringTransactionDialog.tsx

Diálogo de creación de transacción recurrente con su propio Trigger (botón outline con icono RefreshCcw).

#### Campos

- **Descripción:** texto obligatorio con placeholder y foco automático.
- **Tipo:** TypeToggle (default "EXPENSE").
- **Importe:** numérico obligatorio.
- **Categoría:** CategorySelect.
- **Frecuencia:** Select con opciones de FREQUENCY_TYPES.
- **Intervalo:** numérico, mínimo 1.
- **Fecha de inicio:** date picker con mínimo today.
- **Fecha de fin:** opcional, date picker con mínimo startDate o today. Etiqueta con badge "(opcional)".

#### Resumen

Si frecuencia y fecha de inicio están completos, muestra un panel con texto descriptivo de la recurrencia (descripción, importe, frecuencia, fecha inicio y fin o "indefinido").

#### Flujo

1. Construye payload con categoryId, amount (negativo para gastos), type, description, frequencyType, frequencyInterval, startDate y endDate (solo si tiene valor).
2. Llama a `createRecurring.mutateAsync(payload)`.
3. `onSuccess` de la mutación invalida 9 queries y muestra notificación.
4. Cierra y resetea el formulario.

#### Validaciones

- Todos los campos obligatorios excepto endDate.
- Importe mayor que 0.
- Botón deshabilitado si `!isValid` o `isCreating`.
- Inputs deshabilitados durante `isCreating`.

---

### EditRecurringTransactionDialog.tsx

Diálogo de edición de transacción recurrente. Siempre controlado.

#### Props

- **transaction:** PlannedTransaction | null.
- **open:** boolean.
- **onOpenChange:** callback al cerrar.

#### Campos

Mismos campos que NewRecurringTransactionDialog. Los valores se inicializan desde la transacción al montar el componente (no usa useEffect, se inicializan directamente en useState con `transaction?.description || ""`).

#### Flujo

1. Al guardar, llama a `updateRecurring.mutateAsync` con id y data (description, categoryId, amount, type, frequencyType, frequencyInterval, startDate, endDate).
2. `onSuccess` de la mutación invalida 9 queries y muestra notificación.
3. Cierra el diálogo.

#### Validaciones

- Mismas que NewRecurringTransactionDialog.
- Botón deshabilitado si `!isValid` o `isUpdating`.
- Inputs deshabilitados durante `isUpdating`.

---

## Conexión con el backend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /transactions | Listar transacciones del usuario |
| POST | /transactions | Crear transacción |
| PUT | /transactions/:id | Editar transacción |
| DELETE | /transactions/:id | Eliminar transacción |
| GET | /transactions/metrics?period= | Métricas de transacciones |
| GET | /planned-transactions | Listar transacciones recurrentes |
| POST | /planned-transactions | Crear transacción recurrente |
| PUT | /planned-transactions/:id | Editar transacción recurrente |
| DELETE | /planned-transactions/:id | Eliminar transacción recurrente |

---

## Mejoras implementadas (v0.0.1)

- **useTransactionMutations centralizado:** hook con createTransaction, updateTransaction, deleteTransaction, createRecurring, updateRecurring, deleteRecurring y estados combinados isCreating, isUpdating, isDeleting. Los 4 diálogos lo usan eliminando lógica duplicada de apiClient.
- **Invalidación masiva de queries:** `invalidateAll` con Promise.all de 9 queries (transactions, transactionMetrics, dashboardMetrics, historyChart, categoryStats, budgets, recentTransactions, categoryExpenses, plannedTransactions).
- **Notificación de límite de presupuesto:** `checkBudgetLimit` muestra warning con duración de 6s si la respuesta del backend incluye `limitExceeded: true` tras crear o actualizar transacción.
- **Signo del monto corregido en recurrentes:** NewRecurringTransactionDialog y EditRecurringTransactionDialog usan `-Math.abs()` para gastos y `Math.abs()` para ingresos.
- **queryKey simplificado en TransactionList:** eliminado period de la queryKey de transactions (el endpoint no lo usa, solo recibe período en métricas).
- **usePagination compartido:** TransactionList usa el hook de paginación desde `@/features/categories/hooks/usePagination` con currentPage, totalPages, goToPage, prevPage, nextPage. La página se resetea al buscar o filtrar.
- **TransactionSkeleton reutilizable:** componente con prop rows para usar en TransactionList (10 filas) y RecurringTransactionsList (5 filas).
- **TransactionRow extraído:** componente independiente para cada fila de la lista de transacciones con icono, descripción, fecha, categoría, importe y menú contextual.
- **RecurringTransactionRow extraído:** componente independiente con frecuencia formateada, fechas (inicio → fin), próxima ejecución calculada y menú contextual.
- **Próxima fecha de ejecución en recurrentes:** `calculateNextExecution` en utils.ts muestra la fecha estimada de la siguiente ejecución basada en frecuencia e intervalo. Si la fecha supera endDate o no hay startDate, muestra "—".
- **EditTransactionDialog con reinicio por key:** Dialog usa `key={transaction?.id ?? "new"}` para forzar el reinicio de estado interno al cambiar de transacción.
- **Date sin zona horaria en NewTransactionDialog:** `new Date().toISOString().split('.')[0]` para evitar problemas de interpretación en el backend.
- **Categoría obligatoria en creación, opcional en edición:** NewTransactionDialog y NewRecurringTransactionDialog requieren categoría. EditTransactionDialog permite `categoryId: null`.
- **Inputs deshabilitados durante envío:** todos los campos de los 4 diálogos usan `disabled={isCreating/isUpdating/isSubmitting}`.
- **Diálogo de confirmación al eliminar en recurrentes:** RecurringTransactionsList usa ConfirmDeletionDialog reutilizado desde Categories. TransactionList mantiene window.confirm por simplicidad.
- **TransactionActions simplificado:** solo orquesta los dos diálogos con sus Triggers, sin estado interno.
- **formatDate con i18n dinámico:** usa `i18n.language` para formatear fechas en el idioma actual del usuario.
- **TransactionSummary con tipos internos:** define interfaz TransactionMetrics local con 8 campos (4 valores + 4 tendencias). Query key incluye período para recargar al cambiar.