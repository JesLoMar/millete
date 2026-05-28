# Categories — Documentación técnica (Frontend)

## Estructura de archivos

- **components/AddCategoryDialog.tsx** — Diálogo para crear nueva categoría
- **components/CategoryRow.tsx** — Fila individual de la tabla de categorías
- **components/CategoryTable.tsx** — Tabla principal con buscador, paginación y presupuestos
- **components/CategoryTableSkeleton.tsx** — Esqueleto de carga de la tabla
- **components/ColorPicker.tsx** — Selector de color reutilizable con 32 colores
- **components/ConfirmDeletionDialog.tsx** — Diálogo de confirmación para eliminar categoría
- **components/EditCategoryDialog.tsx** — Diálogo para editar categoría existente
- **constants.ts** — 32 colores disponibles para categorías
- **hooks/useCategoryMutation.ts** — Mutaciones centralizadas de categorías
- **hooks/usePagination.ts** — Hook reutilizable de paginación
- **pages/page.tsx** — Página principal de categorías
- **types/index.ts** — Tipos y contratos de datos

---

## pages/page.tsx

Página principal de categorías. Estructura:

- Sidebar izquierdo permanente.
- TopNav superior.
- Cabecera con Header (con `hidePeriodSelector` para ocultar su propio selector), PeriodSelector independiente centrado y AddCategoryDialog a la derecha.
- CategoryTable que recibe el período seleccionado.

El estado del período se gestiona con `useState<PeriodFilter>("month")` y se pasa a CategoryTable. Al cambiar el período, CategoryTable ajusta los límites presupuestarios automáticamente.

---

## components/CategoryTable.tsx

Tabla principal que muestra todas las categorías con su información de gasto y presupuesto.

### Datos que consume

- **useCategories:** hook compartido que obtiene las categorías desde `GET /categories`.
- **useQuery:** obtiene los gastos por categoría desde `GET /dashboard/categories?period=` según el período activo. Query key: `['categoryExpenses', period]`.
- **useCategoryMutations:** hook centralizado con `deleteCategory` e `isDeleting`.
- **usePagination:** hook reutilizable con `currentPage`, `totalPages`, `paginatedRange`, `prevPage`, `nextPage`, `resetPage`.

### Estado local

- `searchTerm`: texto de búsqueda.
- `editingCategory`: categoría seleccionada para editar (controla EditCategoryDialog).
- `deletingCategory`: categoría seleccionada para eliminar (controla ConfirmDeletionDialog).

### Filtrado y paginación

- Filtra categorías por nombre con búsqueda en tiempo real sin distinguir mayúsculas/minúsculas.
- `usePagination` recibe `totalItems = filteredData.length` y 10 items por página.
- `paginatedRange` devuelve `{ start, end }` para hacer slice de los datos filtrados.
- La página se resetea automáticamente al cambiar el término de búsqueda mediante `useEffect`.

### Funciones principales

- **getAdjustedBudgetLimit:** ajusta el límite mensual según el período. Para semana divide entre 4, para año multiplica por 12, para mes lo deja igual. Si no hay límite devuelve null.
- **getSpentPercentage:** calcula el porcentaje gastado respecto al límite ajustado. Máximo 100%. Si no hay límite devuelve 0.
- **handleDelete:** ejecuta `deleteCategory.mutateAsync` y cierra el diálogo de confirmación.

### Estados

- **Carga:** muestra CategoryTableSkeleton con 5 filas de placeholder animadas que replican la estructura real (círculo, nombre, barra, importe, botón).
- **Vacío (sin categorías):** mensaje centrado "No hay categorías".
- **Con datos:** lista de CategoryRow con barra de progreso, porcentaje y gasto/límite.
- **Paginación:** si hay más de 10 categorías (`totalPages > 1`), muestra controles de página anterior/siguiente con contador y texto "Mostrando X-Y de Z transacciones".

### Buscador

Input con icono Search a la izquierda. Filtra categorías por nombre en tiempo real. Placeholder i18n `categories.search`.

### Cabecera de la tabla

Muestra badge con el período activo (etiqueta i18n `dashboard.header.period.{period}`) y contador de resultados.

---

## components/CategoryRow.tsx

Fila individual de la tabla. Muestra:

- Círculo de color de la categoría (con fallback `#3B82F6`).
- Nombre de la categoría (truncado con ellipsis).
- Barra de progreso con porcentaje:
  - Si no hay presupuesto (`budgetLimit` null o 0): muestra texto en cursiva "Asigna un presupuesto mensual para ver el progreso".
  - Si hay presupuesto: barra de progreso coloreada. Si el porcentaje supera o iguala el 100% (`isOverBudget`), la barra se vuelve roja (`bg-destructive`). Si no, usa el color de la categoría.
- Gasto actual y límite presupuestario en formato moneda española (`toLocaleString("es-ES")`). Si no hay presupuesto muestra "—".
- Menú desplegable (DropdownMenu) visible solo al hacer hover sobre la fila (`opacity-0 group-hover:opacity-100`). Opciones: Editar y Eliminar con iconos Edit2 y Trash2.

### Props

- **category:** datos de la categoría (tipo `Category` de shared).
- **spent:** gasto actual en euros.
- **budgetLimit:** límite ajustado al período (puede ser null).
- **percentage:** porcentaje gastado (0-100).
- **onEdit:** callback que recibe la categoría a editar.
- **onDelete:** callback que recibe la categoría a eliminar.

---

## components/AddCategoryDialog.tsx

Diálogo para crear una nueva categoría. Soporta modo controlado (desde un padre) y no controlado (con su propio Trigger).

### Props

- **open?:** controla apertura desde padre.
- **onOpenChange?:** callback de cambio de apertura.

Si no se pasan props, el diálogo usa su propio estado interno y muestra un botón Trigger con icono Plus y texto "Añadir categoría".

### Campos

- **Nombre:** campo de texto obligatorio con foco automático al abrir (`onOpenAutoFocus` previene default y enfoca el input via ref). Placeholder i18n.
- **Color:** selector visual con ColorPicker. Se inicializa con el primer color de CATEGORY_COLORS.
- **Presupuesto mensual:** campo numérico opcional con step 0.01 y min 0. Incluye hint explicativo debajo.

### Flujo

1. Usuario rellena nombre, elige color y opcionalmente presupuesto.
2. Al guardar, llama a `createCategory.mutateAsync` con name (trim), color y budgetLimit (number o null).
3. Si tiene éxito: cierra el diálogo, resetea el formulario y ejecuta `invalidateCategoryQueries`.
4. Si hay error: muestra el mensaje del backend o el texto por defecto de i18n.

### Validaciones

- Nombre obligatorio. El botón de guardar se deshabilita si está vacío.
- Inputs deshabilitados durante `isCreating`.
- Botón de guardar muestra spinner Loader2 animado mientras se crea.

---

## components/EditCategoryDialog.tsx

Diálogo para editar una categoría existente. Siempre controlado: se abre desde CategoryTable al seleccionar una categoría.

### Props

- **category:** Category | null — categoría a editar.
- **open:** boolean — controla visibilidad.
- **onOpenChange:** callback al cerrar.

### Campos

- **Nombre:** campo de texto editable (a diferencia de la versión anterior). Placeholder i18n. Máximo 50 caracteres.
- **Límite de presupuesto mensual:** campo numérico con step 0.01, min 0 y sufijo "EUR". Oculta spinners con CSS `[appearance:textfield]`.
- **Color:** selector visual con ColorPicker, inicializado al color actual.

### Efecto de reinicio

`useEffect` que se dispara al cambiar `category`. Reinicia nombre, color y presupuesto con los valores de la categoría seleccionada. Si no hay categoría, los deja vacíos.

### Flujo

1. Recibe la categoría a editar por props.
2. Al hacer submit (form onSubmit):
   - Valida que el nombre no esté vacío.
   - Valida que el presupuesto sea un número válido no negativo.
   - Llama a `updateCategory.mutateAsync` con id y data (name, color, budgetLimit).
3. Si tiene éxito: cierra el diálogo.
4. Si hay error: muestra mensaje de error en banner rojo con borde y fondo semitransparente.

### Estados de UI

- **isUpdating:** deshabilita todos los campos y muestra Loader2 animado en el botón con texto "Guardando...".
- **Error:** banner con icono y texto del error.

---

## components/ColorPicker.tsx

Componente reutilizable que muestra una cuadrícula de 32 colores (8 columnas x 4 filas).

### Props

- **value:** color actual seleccionado.
- **onChange:** callback al seleccionar color.
- **disabled?:** deshabilita interacción.

### Comportamiento

- El color seleccionado se resalta con `border-primary`, `scale-110` y `ring-2 ring-primary/30`.
- Hover: `scale-105` con borde más visible.
- Deshabilitado: opacidad reducida, cursor `not-allowed`, sin hover.
- Accesibilidad: soporte de teclado (Enter/Space), atributos `aria-label` con el código de color y `aria-pressed` para el estado seleccionado.

---

## components/CategoryTableSkeleton.tsx

Placeholder animado que se muestra mientras se cargan las categorías.

- Simula la cabecera con dos barras de placeholder (buscador y badge).
- 5 filas con efecto `animate-pulse` que replican la estructura real:
  - Círculo de color.
  - Barra de texto para el nombre.
  - Barra de progreso.
  - Barra de texto para el importe.
  - Botón circular.

---

## components/ConfirmDeletionDialog.tsx

Diálogo de confirmación para eliminar una categoría. Reutiliza el estilo de otros diálogos de confirmación del proyecto.

### Props

- **open:** controla visibilidad.
- **onOpenChange:** callback al cambiar apertura.
- **itemName:** nombre del elemento a eliminar.
- **onConfirm:** callback al confirmar eliminación.
- **isDeleting?:** estado de carga (deshabilita botones).

### Estructura

- Icono AlertTriangle centrado con fondo destructivo semitransparente.
- Título centrado: "Eliminar categoría".
- Descripción: "¿Estás seguro de que deseas eliminar la categoría {name}?".
- Botones: Cancelar (outline) y Eliminar (destructive). Si `isDeleting`, el botón muestra "Eliminando...".

---

## hooks/useCategoryMutation.ts

Hook centralizado con las tres mutaciones de categorías.

### invalidateCategoryQueries

Función asíncrona que ejecuta `Promise.all` con 5 invalidaciones:
- `['categories']`
- `['budgets']`
- `['categoryExpenses']`
- `['dashboardMetrics']`
- `['transactionMetrics']`

### createCategory

- **mutationFn:** POST a `/categories` con name (trim), color y budgetLimit (null si no se proporciona).
- **onSuccess:** invalida queries y muestra notificación de éxito.
- **onError:** extrae mensaje del backend o usa clave i18n por defecto. Lanza el error para que el diálogo lo capture.

### updateCategory

- **mutationFn:** PUT a `/categories/:id` con name (trim), color y budgetLimit (null si no se proporciona).
- **onSuccess:** invalida queries y muestra notificación de éxito.
- **onError:** igual que createCategory.

### deleteCategory

- **mutationFn:** DELETE a `/categories/:id`. Devuelve el id.
- **onSuccess:** invalida queries y muestra notificación de éxito.
- **onError:** muestra notificación de error con el mensaje del backend o clave por defecto.

### Retorno

- `createCategory`, `updateCategory`, `deleteCategory` (mutaciones).
- `isCreating`, `isUpdating`, `isDeleting` (estados de carga).
- `invalidateQueries` (función para invalidación manual).

---

## hooks/usePagination.ts

Hook reutilizable de paginación.

### Props

- **totalItems:** número total de elementos.
- **itemsPerPage:** elementos por página (default 10).
- **initialPage:** página inicial (default 1).

### Retorno

- **currentPage:** página actual.
- **totalPages:** total de páginas (calculado con `Math.ceil`).
- **paginatedRange:** objeto `{ start, end }` con el rango de índices para slice.
- **goToPage:** función para ir a una página específica (clampada entre 1 y totalPages).
- **nextPage:** página siguiente.
- **prevPage:** página anterior.
- **resetPage:** vuelve a la página 1.

---

## constants.ts

Exporta `CATEGORY_COLORS`: array de 32 colores hexadecimales agrupados por tonalidades (rojos, naranjas, amarillos, verdes esmeralda, verdes teal, azules, violetas).

---

## types/index.ts

Define los contratos de datos para el módulo:

- **Category:** id, userId, name, color, budgetLimit (number | null), createdAt, modifiedAt, active.
- **RegisterCategoryRequest:** name, color, budgetLimit (opcional, number | null).
- **UpdateCategoryRequest:** name, color, budgetLimit (number | null).
- **CategoryExpense:** name, amount, percentage.
- **CategoriesExpenseResponse:** totalExpenses, categories (array de CategoryExpense).

---

## Flujo de creación de categoría

1. Usuario hace clic en "Añadir categoría".
2. AddCategoryDialog se abre con el campo de nombre enfocado.
3. Usuario rellena nombre, elige color y opcionalmente presupuesto.
4. Al guardar, `createCategory.mutateAsync` hace POST a `/categories`.
5. Backend crea la categoría y devuelve 201.
6. `onSuccess` de la mutación invalida 5 queries (categories, budgets, categoryExpenses, dashboardMetrics, transactionMetrics).
7. CategoryTable se re-renderiza con la nueva categoría.

---

## Flujo de edición de categoría

1. Usuario hace hover sobre una fila y hace clic en "Editar".
2. EditCategoryDialog se abre con los datos actuales (nombre, color, presupuesto).
3. Usuario modifica nombre, color o presupuesto.
4. Al hacer submit, `updateCategory.mutateAsync` hace PUT a `/categories/:id`.
5. Backend actualiza y devuelve 200.
6. `onSuccess` invalida las 5 queries y cierra el diálogo.

---

## Flujo de eliminación

1. Usuario hace hover sobre una fila y hace clic en "Eliminar".
2. ConfirmDeletionDialog se abre mostrando el nombre de la categoría.
3. Usuario confirma.
4. `deleteCategory.mutateAsync` hace DELETE a `/categories/:id`.
5. Backend elimina (o desactiva) la categoría.
6. `onSuccess` invalida las 5 queries y cierra el diálogo.

---

## Ajuste de presupuestos por período

El límite presupuestario de cada categoría se define como valor mensual en base de datos. CategoryTable lo ajusta según el período seleccionado:

- **Semana:** límite dividido entre 4.
- **Mes:** límite sin cambios.
- **Año:** límite multiplicado por 12.

El porcentaje de gasto y la barra de progreso se recalculan con el límite ajustado. Si el gasto supera o iguala el límite, la barra se vuelve roja (destructive). Si no hay presupuesto asignado, se muestra texto en cursiva sin barra de progreso.

---

## Conexión con el backend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /categories | Obtener todas las categorías |
| POST | /categories | Crear nueva categoría |
| PUT | /categories/:id | Editar categoría existente |
| DELETE | /categories/:id | Eliminar categoría |
| GET | /dashboard/categories?period= | Gastos por categoría según período |

---

## Mejoras implementadas (v0.0.1)

- **Invalidación masiva de queries:** todas las mutaciones invalidan 5 queries en paralelo con Promise.all (categories, budgets, categoryExpenses, dashboardMetrics, transactionMetrics).
- **useCategoryMutations centralizado:** hook con createCategory, updateCategory, deleteCategory y estados isCreating, isUpdating, isDeleting. Elimina lógica duplicada de apiClient en los diálogos.
- **usePagination extraído:** hook reutilizable con currentPage, totalPages, paginatedRange (start/end), prevPage, nextPage, resetPage. La página se resetea automáticamente al buscar mediante useEffect.
- **CategoryTableSkeleton mejorado:** estructura visual que coincide con las columnas reales de la tabla (círculo de color, nombre, barra de progreso, importe, botón).
- **Texto visible sin presupuesto:** cuando budgetLimit es null o 0, CategoryRow muestra el mensaje "Asigna un presupuesto mensual para ver el progreso" en cursiva, sin barra de progreso.
- **ColorPicker accesible:** soporte de teclado (Enter/Space), atributos ARIA (aria-label con código de color, aria-pressed) y estilos de focus/hover.
- **EditCategoryDialog con nombre editable:** el campo de nombre ahora es modificable (a diferencia de la versión anterior que lo mostraba deshabilitado).
- **EditCategoryDialog con validación local:** comprueba nombre no vacío y presupuesto válido antes de enviar, mostrando errores en banner estilizado.
- **Diálogo de confirmación al eliminar:** modal ConfirmDeletionDialog con icono AlertTriangle, estilo consistente, soporte i18n y botones Cancelar/Eliminar. El botón muestra "Eliminando..." durante la petición.
- **AddCategoryDialog con modo controlado/no controlado:** soporta ambas modalidades mediante props opcionales open y onOpenChange.
- **Inputs deshabilitados durante envío:** todos los campos de AddCategoryDialog, EditCategoryDialog y ConfirmDeletionDialog se deshabilitan mientras se procesa la petición.
- **Foco automático en AddCategoryDialog:** usa onOpenAutoFocus con preventDefault y ref para enfocar el input de nombre.
- **i18n completo:** todos los textos del módulo usan claves de traducción, incluyendo menú contextual (Editar/Eliminar), tooltips, placeholders y notificaciones toast.