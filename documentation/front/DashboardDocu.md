# Dashboard — Documentación técnica (Frontend)

## Estructura de archivos

- **components/BudgetBars.tsx** — Barras de presupuesto con paginación (5 por página)
- **components/CategoryDonut.tsx** — Gráfico de donut de gastos por categoría
- **components/HistoryChart.tsx** — Gráfico de barras de historial de gastos
- **components/ImportModal.tsx** — Modal para importar datos desde archivo JSON
- **components/QuickActions.tsx** — Botones de acciones rápidas
- **components/RecentTransactions.tsx** — Lista de últimas transacciones
- **constants.ts** — Colores e iconos compartidos
- **hooks/useDashboardQueries.ts** — Hook centralizado con todas las queries
- **hooks/index.ts** — Re-exportación del hook
- **pages/page.tsx** — Página principal del dashboard
- **types/index.ts** — Tipos y contratos de datos
- **utils.ts** — Función de formateo de fechas

---

## pages/page.tsx

Página principal del dashboard. Orquesta todos los componentes y centraliza las llamadas a la API mediante el hook `useDashboardQueries`.

### Estado local

- `period`: "week", "month" o "year". Controla el período de todos los gráficos y métricas.
- `isImportOpen`: controla la visibilidad del modal de importación.
- `isExporting` / `isImporting`: estados de carga para los botones de exportación/importación.
- `isAddOpen`: controla el diálogo de nueva transacción.
- `isAddCategoryOpen`: controla el diálogo de nueva categoría.

### Hook useDashboardQueries

Centraliza las 5 queries del dashboard: metrics, history, categories, budgets y recentTransactions. Cada query usa el período como parte de la query key, provocando recarga automática al cambiar el período. Las funciones de mapeo (mapHistoryToChart, mapCategoriesToDonut, mapBudgets) están dentro del hook.

### Funciones de importación/exportación

- **handleImport:** recibe un File, lo envía como FormData a `POST /data/import` con header `Content-Type: multipart/form-data` y opción `skipGlobalErrorNotify: true` para evitar notificaciones duplicadas. Maneja errores 403 (propiedad), 400 (formato) y genéricos con `notify.error`. Al finalizar ejecuta `queryClient.invalidateQueries()` para refrescar todos los datos y cierra el modal.
- **handleExport:** `GET /data/export` con `responseType: 'blob'`. Crea un blob JSON, genera un enlace temporal con `window.URL.createObjectURL`, lo descarga como `familybudget_export.json` y lo limpia con `revokeObjectURL`. Maneja errores con `notify.error`.

### Layout

Estructura de grid responsiva:

- Fila 1: Header con selector de período.
- Fila 2: QuickActions (grid 2x2 en móvil, 4x1 en escritorio).
- Fila 3: 4 tarjetas de métricas (FormattedMetricCard) con iconos Wallet, TrendingUp, TrendingDown y PiggyBank. La tarjeta de gastos usa `invertedTrend`.
- Fila 4: HistoryChart (8 columnas) + CategoryDonut (4 columnas).
- Fila 5: BudgetBars (5 columnas) + RecentTransactions (7 columnas).
- Modales: ImportModal, NewTransactionDialog (controlado), AddCategoryDialog (controlado).

### periodLabel

Texto dinámico para el tooltip de tendencias: `dashboard.metrics.vsLastMonth`, `vsLastWeek` o `vsLastYear` según el período.

---

## hooks/useDashboardQueries.ts

Hook que centraliza todas las llamadas a la API del dashboard. Recibe `period` como parámetro.

### Configuración común

Todas las queries comparten:
- `retry: 1`: para no dejar placeholders infinitos si falla la API.
- `staleTime: 30_000` (30 segundos): evita recargas innecesarias al cambiar de pestaña o reenfocar la ventana.

### Queries

- **dashboardMetrics:** `GET /dashboard/metrics?period=`. Query key: `['dashboardMetrics', period]`. Devuelve `DashboardMetrics` directamente.
- **history:** `GET /dashboard/history?period=`. Query key: `['historyChart', period]`. Mapea la respuesta con `mapHistoryToChart`.
- **categories:** `GET /dashboard/categories?period=`. Query key: `['categoryStats', period]`. Mapea la respuesta con `mapCategoriesToDonut`.
- **budgets:** `GET /dashboard/budgets?period=`. Query key: `['budgets', period]`. Mapea la respuesta con `mapBudgets`.
- **recentTransactions:** `GET /dashboard/recent-transactions?limit=5`. Query key: `['recentTransactions']` (sin dependencia del período). Devuelve `response.data.transactions`.

### Funciones de mapeo internas

- **mapHistoryToChart:** convierte `HistoryResponse` en `ChartDataPoint[]`. Si no hay labels o todos los valores son 0/null, devuelve array vacío.
- **mapCategoriesToDonut:** convierte `CategoriesResponse` en `CategoryData[]`. Si no hay categorías con porcentaje mayor que 0, devuelve array vacío. Asigna colores de `CHART_COLORS` cíclicamente.
- **mapBudgets:** convierte `BudgetsResponse` en `BudgetItem[]`. Asigna colores de `BUDGET_COLORS` usando un hash del nombre de la categoría (`hashCode`) para consistencia entre refrescos.

### hashCode

Función auxiliar que calcula un hash numérico a partir del string de categoría. El resultado se usa como índice en `BUDGET_COLORS` mediante módulo, garantizando que la misma categoría siempre tenga el mismo color.

---

## constants.ts

Constantes compartidas entre componentes:

- **CHART_COLORS:** 5 colores HSL usando variables CSS (`var(--chart-1)` a `var(--chart-5)`).
- **BUDGET_COLORS:** 6 colores de fondo Tailwind para barras de presupuesto (emerald, blue, pink, amber, purple, cyan).
- **CATEGORY_ICONS:** mapeo de nombre de categoría a icono de Lucide (Alimentación → Utensils, Hogar → Home, Transporte → Car, Suministros → Zap, Ocio → ShoppingCart).
- **CATEGORY_COLORS:** mapeo de nombre de categoría a clases Tailwind de color (texto + fondo).

---

## utils.ts

Funciones de utilidad:

- **formatDate:** formatea una fecha ISO a formato español con `toLocaleDateString("es-ES")`: día con 2 dígitos, mes abreviado y año numérico.

---

## types/index.ts

Define todos los contratos de datos entre el dashboard y el backend:

- **ChartDataPoint:** label, amount, fill (opcional).
- **CategoryData:** category, value, color.
- **BudgetItem:** category, spent, limit, percentage, color.
- **TransactionItem:** id, description, category, amount, date, type ("INCOME" | "EXPENSE"), icon (opcional).
- **DashboardMetrics:** balance, income, expenses, savings, balanceTrend, incomeTrend, expensesTrend, savingsTrend.
- **HistoryResponse:** period, labels, data.
- **CategoryItemResponse:** name, amount, percentage, transactionCount.
- **CategoriesResponse:** totalExpenses, categories.
- **BudgetItemResponse:** categoryId, category, spent, limit, percentage.
- **BudgetsResponse:** period, budgets.
- **TransactionResponse:** id, description, category, categoryId, amount, date, type.
- **TransactionsResponse:** transactions.

---

## components/QuickActions.tsx

Botones de acciones rápidas en grid 2x2 (móvil) o 4x1 (escritorio).

### Props

- `onImportClick`, `onExportClick`, `onAddClick?`, `onAddCategoryClick?`
- `isExporting?`, `isImporting?`

### Acciones definidas

- **Añadir gasto:** icono PlusCircle, color primary. Llama a `onAddClick`.
- **Crear categoría:** icono FolderPlus, color esmeralda. Llama a `onAddCategoryClick`.
- **Importar datos:** icono FileUp (o Loader2 animado si `isImporting`), color ámbar. Se deshabilita si `isAnyLoading`.
- **Exportar datos:** icono FileDown (o Loader2 animado si `isExporting`), color púrpura. Se deshabilita si `isAnyLoading`.

### Filtrado de acciones

Las acciones cuyo `onClick` sea `undefined` se filtran antes de renderizar, permitiendo ocultar botones según contexto.

### Estilo

Cada botón ocupa una altura fija (`h-28`) con icono centrado en contenedor redondeado y etiqueta debajo. Efecto hover con cambio de color de fondo del icono.

---

## components/HistoryChart.tsx

Gráfico de barras que muestra la evolución del gasto en el período seleccionado.

### Props

- `period?`: período activo (para el badge).
- `data?`: array de `ChartDataPoint`.
- `loading?`: estado de carga.

### Constante MAX_BARS

Máximo de 12 barras visibles. Si hay más de 12, se muestran solo las últimas y se muestra un badge "Mostrando últimos 12 períodos" en ámbar.

### Adaptaciones visuales

- `barSize` se ajusta según la cantidad de barras: 40px para hasta 7, 30px para hasta 12, 20px para más (aunque el máximo es 12).
- Si hay más de 7 barras, las etiquetas del eje X se giran -45 grados con `textAnchor="end"` y altura aumentada a 60px.
- Tooltip con valor formateado en euros mediante `formatCurrency`.
- Margen inferior aumentado a 20px para evitar corte de etiquetas.

### Estados

- **Carga:** placeholder con título y gráfico simulados con `animate-pulse`.
- **Sin datos:** mensaje "Sin gastos en este período" centrado.
- **Con datos:** gráfico de barras con grid, tooltip, etiquetas y badge de período + aviso de truncado si aplica.

---

## components/CategoryDonut.tsx

Gráfico de donut que muestra la distribución porcentual de gastos por categoría.

### Props

- `data?`: array de `CategoryData`.
- `loading?`: estado de carga.
- `title?`: título opcional (por defecto usa i18n `dashboard.donut.title`).

### Funcionalidad

- Gráfico de donut con `innerRadius={55}` y `outerRadius={75}`.
- `paddingAngle={3}` y `cornerRadius={3}` para separación visual entre segmentos.
- Efecto hover en cada segmento: `hover:opacity-80`.
- Tooltip con porcentaje y nombre de categoría mediante `ChartTooltipContent`.
- Leyenda debajo del gráfico en grid 2x2 con indicador de color, nombre y porcentaje. Efecto hover: escala 125% en el indicador y cambio de color en el texto.
- Altura del gráfico: `h-55` (220px).
- `ResponsiveContainer` para adaptación a móvil.

### Estados

- **Carga:** círculo animado con 4 items de leyenda simulados y altura fija `h-75`.
- **Sin datos:** mensaje "Sin gastos en este período" centrado con altura fija `h-75`.
- **Con datos:** donut interactivo con leyenda.

---

## components/BudgetBars.tsx

Barras de progreso horizontales que muestran el gasto respecto al presupuesto de cada categoría.

### Props

- `data?`: array de `BudgetItem`.
- `loading?`: estado de carga.

### Constante ITEMS_PER_PAGE

5 presupuestos por página.

### Paginación

- Estado local `currentPage` con `useState(1)`.
- `totalPages` calculado con `Math.ceil(budgets.length / ITEMS_PER_PAGE)`.
- Controles de navegación: flechas anterior/siguiente con `ChevronLeft` y `ChevronRight`.
- Los controles solo se muestran si `totalPages > 1`.
- La paginación se ancla abajo con `mt-auto` dentro de un contenedor flex.

### Altura fija

- `min-h-85` (340px) en todos los estados (carga, vacío, con datos) para evitar saltos de layout.

### Colores según estado

- **Normal (menos del 80%):** usa el color asignado a la categoría (de `BUDGET_COLORS` vía hash).
- **Cerca del límite (80%-99%):** `bg-amber-500`.
- **Sobrepasado (100% o más):** `bg-rose-500`.

### Validación de porcentaje

Protección contra NaN, Infinity o valores nulos: si el porcentaje no es válido, se fuerza a 0.

### Texto de estado

- **Excedido:** mensaje "Excedido por X €" en `text-rose-400` usando `formatNumber` con 0 decimales.
- **Normal:** mensaje "Quedan X €" en `text-muted-foreground` usando `formatNumber` con 0 decimales.

### Estados

- **Carga:** 5 barras simuladas (mismo número que ITEMS_PER_PAGE) con `animate-pulse` y altura fija.
- **Vacío:** mensaje centrado "No hay presupuestos configurados" con altura fija.
- **Con datos:** lista paginada de barras con formato de moneda (`formatCurrency`).

---

## components/RecentTransactions.tsx

Lista de las últimas transacciones del usuario.

### Props

- `data?`: array de `TransactionItem`.
- `loading?`: estado de carga.
- `limit?`: número máximo de transacciones a mostrar (default 5).

### Iconos y colores por categoría

Usa `CATEGORY_ICONS` y `CATEGORY_COLORS` de constants.ts. Si la categoría no coincide con ninguna clave, usa `ShoppingCart` y color por defecto (`text-muted-foreground bg-muted/10`).

### Formato

- **Fecha:** día numérico + mes abreviado + año mediante `formatDate` de utils.ts.
- **Importe:** formateado con `formatCurrency`. Verde con `ArrowUpRight` y signo "+" para ingresos, color normal con `ArrowDownRight` para gastos.
- **Badge:** "Gasto" (opacidad reducida) o "Ingreso" (color esmeralda) según `tx.type === "EXPENSE"`.

### Navegación

- El botón "Ver todas" redirige a `/transactions`.
- Al hacer clic en una transacción, redirige a `/transactions?id={tx.id}`.
- Cada fila tiene `cursor-pointer` y efecto hover.

### Estados

- **Carga:** N filas simuladas (según `limit`) con `animate-pulse`, incluyendo círculo, texto y badge.
- **Vacío:** mensaje "No hay transacciones recientes" centrado.
- **Con datos:** lista de transacciones con icono, descripción, fecha, categoría, importe y badge.

---

## components/ImportModal.tsx

Modal para seleccionar y cargar un archivo JSON de importación.

### Props

- `isOpen`: controla visibilidad.
- `onClose`: callback al cerrar.
- `onImport`: callback que recibe el archivo seleccionado.

### Funcionalidad

- Acepta solo archivos `.json` (tipo MIME `application/json` o extensión `.json`).
- Soporta drag and drop con feedback visual: borde y fondo cambian al arrastrar (`border-primary bg-primary/5`), ligero escalado (`scale-[0.99]`).
- Soporte de clic para abrir selector de archivos mediante input oculto y ref.
- Al seleccionar archivo válido: muestra nombre con icono `FileJson` y botón X para eliminar.
- Validación: si el archivo no es JSON, muestra error en banner rojo con borde y fondo semitransparente.
- Botón de aceptar deshabilitado hasta que haya un archivo seleccionado.
- Al hacer submit, llama a `onImport` con el archivo y resetea el estado interno.

---

## Actualización automática del dashboard

Cuando se crea una transacción desde NewTransactionDialog o una categoría desde AddCategoryDialog, se invalidan todas las queries del dashboard para reflejar los cambios al instante:

- transactions
- transactionMetrics
- dashboardMetrics
- historyChart
- categoryStats
- budgets
- recentTransactions
- categoryExpenses

La importación de datos ejecuta `queryClient.invalidateQueries()` sin filtro para refrescar toda la caché.

---

## Conexión con el backend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /dashboard/metrics?period= | Métricas principales (balance, income, expenses, savings + tendencias) |
| GET | /dashboard/history?period= | Historial de gastos (labels + data) |
| GET | /dashboard/categories?period= | Gastos por categoría (porcentajes) |
| GET | /dashboard/budgets?period= | Estado de presupuestos (spent, limit, percentage) |
| GET | /dashboard/recent-transactions?limit=5 | Últimas transacciones |
| POST | /data/import | Importar datos (multipart/form-data) |
| GET | /data/export | Exportar datos (blob JSON) |

---

## Mejoras implementadas (v0.0.1)

- **ImportModal con feedback de estados:** muestra zona de drop interactiva con validación de tipo de archivo. Banner de error estilizado para archivos no JSON. Botón de aceptar deshabilitado hasta seleccionar archivo.
- **handleImport con skipGlobalErrorNotify:** evita notificaciones duplicadas usando la opción `skipGlobalErrorNotify: true` en apiClient. Invalida toda la caché tras importación exitosa.
- **handleExport con blob:** descarga directa de archivo JSON con nombre `familybudget_export.json` usando `createObjectURL` y limpieza con `revokeObjectURL`.
- **CategoryDonut simplificado:** eliminado totalAmount del centro (no coincidía con los porcentajes del donut). Añadido `ResponsiveContainer` para adaptarse a móvil. Altura fija en todos los estados. Mensaje "Sin gastos en este período" cuando no hay datos.
- **BudgetBars mejorado:** texto "Excedido por X €" en rosa cuando el presupuesto se supera, "Quedan X €" en normal. Colores consistentes entre refrescos mediante hash de categoría. Skeleton usa 5 items (mismo número que ITEMS_PER_PAGE). Validación de porcentaje contra NaN/Infinity. Formateo con `formatCurrency` y `formatNumber`.
- **RecentTransactions corregido:** usa solo `tx.type === "EXPENSE"` para determinar icono y color. Skeleton dinámico según `limit`. Clic en transacción redirige a `/transactions?id={tx.id}`. Badge diferenciado para gasto/ingreso.
- **HistoryChart con aviso de truncado:** badge "Mostrando últimos 12 períodos" en ámbar cuando hay más de 12 barras. Mensaje "Sin gastos en este período" cuando no hay datos. Margen inferior aumentado a 20px. `barSize` adaptativo.
- **useDashboardQueries con manejo de errores:** `retry: 1` en todas las queries para no dejar placeholders infinitos. `staleTime: 30s` para evitar recargas innecesarias. Colores de presupuesto asignados por hash de categoría (consistentes entre refrescos).
- **QuickActions robusto:** filtra acciones cuyo `onClick` sea undefined antes de renderizar. Animación de spinner en iconos durante carga. Deshabilita import/export cuando cualquiera está en curso.
- **DashboardPage con 6 estados booleanos:** control granular de modales y estados de carga para importación/exportación.
- **Código muerto eliminado:** funciones mock eliminadas de utils.ts (solo queda formatDate).