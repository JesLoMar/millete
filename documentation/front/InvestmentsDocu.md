# Investments — Documentación técnica (Frontend)

## Estructura de archivos

- **components/AssetList.tsx** — Lista de activos con buscador y filtro por tipo
- **components/AssetListSkeleton.tsx** — Esqueleto de carga de la lista
- **components/AssetRow.tsx** — Fila individual de activo
- **components/AssetSkeletonRow.tsx** — Esqueleto de fila individual reutilizable
- **components/DistributionChart.tsx** — Gráfico de donut de distribución por tipo
- **components/EvolutionChart.tsx** — Gráfico de barras de evolución del patrimonio
- **components/InvestmentMetrics.tsx** — Tarjetas de métricas de inversiones
- **components/NewInvestmentDialog.tsx** — Diálogo de creación de inversión
- **components/UpdatePriceDialog.tsx** — Diálogo de actualización de precio con confirmación
- **constants.ts** — Colores, tipos de inversión y constantes compartidas
- **hooks/useInvestmentQueries.ts** — Hook centralizado con todas las queries
- **hooks/useInvestmentMutations.ts** — Hook centralizado con las 3 mutaciones
- **hooks/index.ts** — Re-exportación de hooks
- **pages/page.tsx** — Página principal de inversiones
- **types/index.ts** — Tipos y contratos de datos

---

## pages/page.tsx

Página principal de inversiones. Orquesta todos los componentes y centraliza las llamadas a la API mediante los hooks `useInvestmentQueries` y `useInvestmentMutations`.

### Estado local

- `period`: "week", "month" o "year". Controla el período de todos los gráficos y métricas.
- `deletingInvestment`: inversión seleccionada para eliminar (controla ConfirmDeletionDialog).

### Hooks utilizados

- `useInvestmentQueries(period)`: centraliza las 4 queries del módulo (investments, metrics, evolution, distribution).
- `useInvestmentMutations()`: expone `deleteInvestment` y estados de carga.

### Función handleDelete

Ejecuta `deleteInvestment.mutateAsync` con el id de la inversión seleccionada. Al completar, cierra el diálogo de confirmación. La mutación invalida 4 queries automáticamente.

### Layout

- Cabecera con Header (con `hidePeriodSelector`), PeriodSelector centrado y NewInvestmentDialog a la derecha.
- InvestmentMetrics con 3 tarjetas (2 activas + 1 placeholder de dividendos).
- Grid de gráficos: EvolutionChart (8 columnas) + DistributionChart (4 columnas).
- AssetList con buscador, filtro por tipo y acciones por fila.
- ConfirmDeletionDialog reutilizado desde Categories para eliminar inversiones.

---

## types/index.ts

Define todos los contratos de datos:

- **InvestmentAssetType:** "STOCK" | "CRYPTO" | "REAL_ESTATE" | "OTHER".
- **InvestmentResponse:** id, userId, assetName, ticker (opcional), type, quantity, purchasePrice, currentPrice, currentValue, investedCapital, profitOrLoss, roiPercentage, purchaseDate, active.
- **RegisterInvestmentRequest:** name, tickerSymbol (opcional), assetType, quantity, purchasePrice, purchaseDate.
- **UpdateInvestmentPriceRequest:** currentPrice.
- **InvestmentMetricsData:** portfolioValue, monthlyReturn, dividends, portfolioTrend, returnTrend, dividendsTrend.
- **EvolutionResponse:** period, labels, data.
- **DistributionData:** name, value, percentage, color.
- **DistributionResponse:** totalValue, distribution.

---

## constants.ts

Constantes compartidas entre componentes:

- **TYPE_COLORS:** mapeo de tipo de inversión a clase Tailwind de fondo (STOCK → bg-blue-500, CRYPTO → bg-amber-500, FUND → bg-emerald-500, REAL_ESTATE → bg-rose-500, OTHER → bg-slate-500).
- **INVESTMENT_TYPES:** array de 5 objetos con value, labelKey (clave i18n), icon (LucideIcon: BarChart3, Bitcoin, Wallet, Building2, HelpCircle) y color (clase Tailwind de texto). Usado en NewInvestmentDialog y filtros de AssetList.

---

## hooks/useInvestmentQueries.ts

Hook que centraliza las 4 llamadas a la API del módulo. Recibe `period` como parámetro.

### Configuración común

Todas las queries comparten:
- `retry: 1`: para no dejar placeholders infinitos si falla la API.
- `staleTime: 30_000` (30 segundos): evita recargas innecesarias al cambiar de pestaña.

### Queries

- **investments:** `GET /investments`. Query key: `['investments']` (sin período). Filtra solo activas (`active !== false`).
- **metrics:** `GET /dashboard/investments/metrics?period=`. Query key: `['investmentMetrics', period]`.
- **evolution:** `GET /dashboard/investments/evolution?period=`. Query key: `['investmentEvolution', period]`.
- **distribution:** `GET /dashboard/investments/distribution?period=`. Query key: `['investmentDistribution', period]`.

Devuelve un objeto con las 4 queries para que page.tsx las distribuya a los componentes.

---

## hooks/useInvestmentMutations.ts

Hook centralizado con las 3 mutaciones del módulo.

### invalidateAll

Función asíncrona que ejecuta `Promise.all` con 4 invalidaciones:
- `['investments']`
- `['investmentMetrics']`
- `['investmentEvolution']`
- `['investmentDistribution']`

### createInvestment

- **mutationFn:** POST a `/investments`. Sanitiza `purchaseDate`: si no incluye "T", añade `T00:00:00` para evitar problemas de zona horaria.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### updatePrice

- **mutationFn:** PATCH a `/investments/:id/price` con `{ newPrice: price }`.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### deleteInvestment

- **mutationFn:** DELETE a `/investments/:id`.
- **onSuccess:** ejecuta `invalidateAll` y muestra notificación de éxito.

### Estados de carga

- **isCreating:** `createInvestment.isPending`
- **isUpdating:** `updatePrice.isPending`
- **isDeleting:** `deleteInvestment.isPending`

---

## components/InvestmentMetrics.tsx

Muestra tarjetas de métricas de inversiones. Recibe datos por props.

### Props

- **data:** InvestmentMetricsData | undefined
- **isLoading:** boolean
- **period:** PeriodFilter

### Tarjetas

- **Valor del portafolio:** icono Wallet, color índigo (`bg-indigo-500/10 text-indigo-400`). Muestra `portfolioValue` y `portfolioTrend`.
- **Retorno mensual:** icono TrendingUp, color esmeralda (`bg-emerald-500/10 text-emerald-400`). Muestra `monthlyReturn` y `returnTrend`.
- **Dividendos:** tarjeta placeholder deshabilitada con opacidad reducida (`opacity-60`), icono Coins en ámbar, valor fijo "0,00 €" y texto "Próximamente". Tooltip con `investments.comingSoon`. No usa FormattedMetricCard.

### periodLabel

Texto dinámico para el tooltip de tendencias: `dashboard.metrics.vsLastMonth`, `vsLastWeek` o `vsLastYear` según el período.

---

## components/EvolutionChart.tsx

Gráfico de barras que muestra la evolución del patrimonio en el período seleccionado.

### Props

- **data:** EvolutionResponse | undefined
- **isLoading:** boolean

### Datos

Transforma `response.labels` y `response.data` en `ChartDataPoint[]` con `useMemo`. Si no hay datos, devuelve array vacío.

### Visual

- BarChart con barras de radio superior 4px y `barSize` dinámico: 40px para ≤7 barras, 30px para ≤12, 20px para más.
- Eje X sin línea ni ticks, con fuente `muted-foreground` y desplazamiento `dy={5}`.
- Tooltip sin label, con valor formateado en euros según locale.
- Margen inferior de 20px para evitar corte de etiquetas.
- Badge superior derecho con el número de meses mostrados: `investments.lastMonths`.

### Estados

- **Carga:** placeholder animado con pulso que ocupa todo el espacio (`h-95`).

---

## components/DistributionChart.tsx

Gráfico de donut que muestra la distribución del portafolio por tipo de activo.

### Props

- **data:** DistributionResponse | undefined
- **isLoading:** boolean

### Datos

Extrae `distribution` y `totalValue` del response. Si no hay datos, usa arrays vacíos y 0.

### Visual

- Donut con `innerRadius={55}`, `outerRadius={75}`, `paddingAngle={5}` y sin stroke.
- Valor total en el centro del donut formateado condicionalmente: valores ≥1M se muestran en millones (ej: "2.5M €"), valores ≥1000 en miles (ej: "1,2k €"), menores enteros. Incluye etiqueta "TOTAL" debajo.
- Tooltip con porcentaje formateado según locale.
- Leyenda inferior con scroll: indicador de color, nombre (truncado a 120px), valor en euros y porcentaje.

### formatCurrency

Función auxiliar que formatea valores monetarios: ≥1M → "X.XM", ≥1000 → "X.Xk", resto → formato locale.

### Estados

- **Carga:** círculo animado con pulso de 48px centrado (`h-95`).

---

## components/AssetList.tsx

Lista de activos del usuario con buscador y filtro por tipo.

### Props

- **investments:** InvestmentResponse[]
- **isLoading:** boolean
- **onDelete:** callback que recibe InvestmentResponse

### Buscador

Input con icono Search. Filtra por `assetName` o `ticker`. Sin distinción de mayúsculas/minúsculas.

### Filtro por tipo

Botones para cada tipo de inversión (con icono y texto) + opción "Todos". Los botones usan `variant="secondary"` cuando están activos. Badge con contador de resultados filtrados alineado a la derecha.

### Delegación

Cada fila se renderiza con AssetRow pasando `investment` y `onDelete`. La carga se muestra con AssetListSkeleton.

### Estados

- **Carga:** AssetListSkeleton (buscador + 5 AssetSkeletonRow).
- **Vacío:** mensaje "No se encontraron activos" centrado.

---

## components/AssetRow.tsx

Fila individual de un activo.

### Props

- **investment:** InvestmentResponse
- **onDelete:** callback que recibe InvestmentResponse

### Estructura

- **Icono:** cuadrado redondeado (`rounded-xl`) con color de fondo según tipo (`TYPE_COLORS`) y abreviatura (ticker o 3 primeras letras de assetName en mayúsculas).
- **Nombre y detalle:** assetName en negrita, cantidad + tipo en texto secundario.
- **Actualizar precio:** botón UpdatePriceDialog con estilo ghost y texto "Actualizar precio".
- **Valor actual:** formateado con 2 decimales en español, con icono TrendingUp (verde) o TrendingDown (rojo) según `profitOrLoss`.
- **Porcentaje de retorno:** badge redondeado con fondo semitransparente y borde. Verde para positivo, rojo para negativo. Si `roiPercentage` es null/undefined, muestra "—".
- **Menú desplegable:** visible al hover, con opción de eliminar (icono Trash2, texto destructivo).

---

## components/AssetSkeletonRow.tsx

Componente de esqueleto para una fila individual de activo.

### Estructura

- Icono cuadrado de 32px con animación pulse.
- Dos barras de texto simuladas (nombre y detalle).
- Botón simulados de 80px.
- Bloque de valor con dos barras (valor y porcentaje).
- Botón circular de 32px.

Usado por AssetListSkeleton (5 filas).

---

## components/AssetListSkeleton.tsx

Placeholder de carga completo: barra de búsqueda simulada + 5 AssetSkeletonRow con efecto pulse. Incluye buscador y filtros simulados con barras animadas.

---

## components/NewInvestmentDialog.tsx

Diálogo para crear una nueva inversión con Trigger propio (botón primary con icono Plus y texto "Nueva inversión").

### Campos

- **Nombre del activo:** texto obligatorio con placeholder y foco automático. Ocupa 2/3 del ancho en grid de 3 columnas.
- **Ticker:** texto opcional, se convierte a mayúsculas automáticamente con `toUpperCase()` en onChange. Ocupa 1/3 del ancho.
- **Tipo:** Select con opciones de INVESTMENT_TYPES (icono + texto traducido). Default "STOCK".
- **Cantidad:** numérico con min 0.0001 y step "any".
- **Precio de compra:** numérico con min 0.01 y step 0.01.
- **Fecha de compra:** date picker con fecha actual por defecto (`new Date().toISOString().split('T')[0]`).

### Flujo

1. Construye payload con assetName (trim), ticker (uppercase trim o null), quantity, purchasePrice, type y purchaseDate.
2. Llama a `createInvestment.mutateAsync(payload)`.
3. La mutación sanitiza purchaseDate añadiendo `T00:00:00` si no lo tiene.
4. `onSuccess` invalida 4 queries y muestra notificación.
5. Cierra y resetea el formulario.
6. Si hay error, muestra notificación de error con `notify.error`.

### Validaciones

- Nombre, cantidad y precio de compra obligatorios.
- Cantidad y precio mayores que 0.
- Botón deshabilitado si `!isValid` o `isCreating`.
- Inputs deshabilitados durante `isCreating`.

---

## components/UpdatePriceDialog.tsx

Diálogo para actualizar el precio de mercado de un activo. Se abre desde el botón "Actualizar precio" en cada AssetRow.

### Props

- **investmentId:** string
- **assetName:** string
- **currentPrice:** number

### Funcionalidad

- Campo numérico con el precio actual como valor inicial.
- **Detección de desviación crítica:** si la diferencia entre el nuevo precio y el actual supera el 50% (`deviationRatio > 0.5`), requiere doble confirmación.
- **Primera confirmación:** al hacer clic en actualizar con desviación crítica, cambia el botón a variante destructive con texto "Confirmar actualización" y muestra banner de advertencia con icono AlertTriangle.
- **Segunda confirmación:** al volver a hacer clic, ejecuta la mutación.
- **Validación:** botón deshabilitado si el precio es igual al actual o menor o igual a 0.
- Durante el envío muestra Loader2 animado.
- Al cerrar el diálogo se resetea el estado de confirmación.

### Flujo

1. Usuario introduce nuevo precio.
2. Si la desviación es ≤50%, ejecuta `updatePrice.mutateAsync` directamente.
3. Si la desviación es >50%, muestra confirmación adicional.
4. Al confirmar, PATCH a `/investments/:id/price` con `{ newPrice: targetPrice }`.
5. `onSuccess` invalida 4 queries y cierra el diálogo.
6. Si hay error, muestra notificación con `notify.error`.

---

## Conexión con el backend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /investments | Listar inversiones del usuario |
| POST | /investments | Crear nueva inversión |
| DELETE | /investments/:id | Eliminar inversión |
| PATCH | /investments/:id/price | Actualizar precio de mercado |
| GET | /dashboard/investments/metrics?period= | Métricas de inversiones |
| GET | /dashboard/investments/evolution?period= | Evolución del patrimonio |
| GET | /dashboard/investments/distribution?period= | Distribución por tipo |

---

## Mejoras implementadas (v0.0.1)

- **Diálogo de confirmación al eliminar:** modal ConfirmDeletionDialog reutilizado desde Categories, con soporte i18n y botones Cancelar/Eliminar. La eliminación se gestiona desde page.tsx con estado `deletingInvestment`.
- **Invalidación completa de queries al eliminar/actualizar:** `invalidateAll` con Promise.all de 4 queries (investments, investmentMetrics, investmentEvolution, investmentDistribution).
- **useInvestmentMutations centralizado:** hook con createInvestment, updatePrice, deleteInvestment y estados isCreating, isUpdating, isDeleting. NewInvestmentDialog, UpdatePriceDialog y page.tsx lo usan eliminando lógica duplicada de apiClient.
- **useInvestmentQueries mejorado:** queryKey de investments sin period (el endpoint no lo usa). retry: 1 y staleTime: 30s en todas las queries para manejo de errores consistente.
- **Filtro por tipo de inversión en AssetList:** botones de filtro con iconos para cada tipo (STOCK, CRYPTO, FUND, REAL_ESTATE, OTHER) + opción "Todos". Badge con contador de resultados. Los iconos usan colores distintivos de INVESTMENT_TYPES.
- **AssetRow usa useTranslation directamente:** el componente obtiene las traducciones internamente sin recibir t por props.
- **Formato de moneda condicional en DistributionChart:** función `formatCurrency` que muestra valores ≥1M en millones (ej: "2.5M €"), ≥1000 en miles (ej: "1,2k €"), y menores en formato completo. Se muestra en el centro del donut con etiqueta "TOTAL".
- **EvolutionChart con barSize dinámico:** 40px para ≤7 barras, 30px para ≤12, 20px para más. Tooltip con valor formateado en euros según locale.
- **UpdatePriceDialog con doble confirmación:** detecta desviaciones superiores al 50% y solicita confirmación adicional con botón destructive y banner de advertencia. Previene cambios accidentales drásticos.
- **Porcentaje undefined en AssetRow:** muestra "—" cuando el ROI no está disponible, en lugar de "undefined%".
- **AssetListSkeleton mejorado:** 5 filas con estructura visual que coincide con las filas reales usando AssetSkeletonRow reutilizable.
- **AssetSkeletonRow extraído:** componente independiente para el skeleton de cada fila, reutilizable en toda la aplicación.
- **Sanitización de fecha en createInvestment:** si purchaseDate no incluye "T", se añade `T00:00:00` para evitar problemas de interpretación de zona horaria en el backend.
- **NewInvestmentDialog con foco automático:** usa `onOpenAutoFocus` con `preventDefault` y ref para enfocar el input de nombre. Ticker se convierte a mayúsculas automáticamente. Tipo con iconos en el Select.
- **InvestmentMetrics con tarjeta de dividendos placeholder:** tercera tarjeta con opacidad reducida, valor fijo y texto "Próximamente" para funcionalidad futura.
- **Notificaciones toast en mutaciones:** createInvestment y updatePrice usan `notify.error` para mostrar errores del backend.