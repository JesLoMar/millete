# Dashboard

El dashboard ofrece seis consultas GET; no expone endpoints de inversión. El controlador está en [`DashboardController.java`](../../backend/src/main/java/com/puntomartinez/millete/dashboard/infrastructure/in/controller/DashboardController.java), y la lógica en `dashboard/application/services`.

## API

Base: `/api/v1/dashboard`. Todas las rutas requieren autenticación; el propietario se obtiene del principal `JwtUser`.

| Ruta | Consulta y respuesta |
|---|---|
| `GET /metrics?period=month` | `DashboardMetricsResponseDTO` |
| `GET /history?period=month` | `DashboardHistoryResponseDTO` |
| `GET /categories?period=month` | `DashboardCategoriesResponseDTO` |
| `GET /budgets?period=month` | `DashboardBudgetsResponseDTO` |
| `GET /recent-transactions?limit=5` | `DashboardTransactionsResponseDTO`; límite acotado entre 1 y 50 |
| `GET /savings-goals` | `DashboardGoalsResponseDTO` |

Las cuatro rutas con período usan `period=month` por defecto. Los valores admitidos, sin distinguir mayúsculas, son `week`, `month` y `year`; métricas, categorías y presupuestos usan fechas civiles locales. `week` va de lunes a domingo, `month` es mes natural y `year` año natural. Un valor distinto produce `InvalidInputException`. Historial construye series distintas: días de la semana, bloques de siete días desde el día 1 del mes, o meses desde enero hasta el mes actual. No hay en el controlador un endpoint para `investments/metrics`, `investments/evolution` ni `investments/distribution`; la API de cartera pertenece al módulo inversiones.

## Datos consultados

Las métricas y el historial agregan transacciones. Las categorías agrupan gastos y permiten movimientos sin categoría. Los presupuestos se construyen a partir de categorías con límite y transacciones del período. Las transacciones recientes incluyen información de categoría cuando está disponible. La consulta de metas personales usa el puerto de lectura del módulo `savingsgoals`; el controlador documenta su respuesta, no expone una lectura directa de entidades.

`DashboardMetricsResponseDTO` contiene `balance`, `income`, `expenses`, `savings` (igual a balance), transferencias entrantes/salientes y conteos, más tendencias porcentuales; `savingsTrend` equivale a `balanceTrend`. Balance resta gastos de ingresos y no incorpora transferencias. `DashboardHistoryResponseDTO` contiene `period`, `labels` y `data`, con gastos agregados por día/semana/mes según período. `DashboardCategoriesResponseDTO` contiene `totalExpenses` y categorías con ID, nombre, importe, porcentaje y conteo; movimientos huérfanos se agrupan como «Sin categoría» y elementos inferiores al 5% como «Otros». `DashboardBudgetsResponseDTO` contiene período y hasta cinco presupuestos con categoría, gasto, límite y porcentaje; ordena primero los que alcanzan al menos 100%, luego porcentaje descendente. `DashboardTransactionsResponseDTO` contiene movimientos recientes con ID, descripción, nombre/color/ID de categoría, importe, fecha y tipo. `DashboardGoalsResponseDTO` contiene hasta 20 metas ordenadas por prioridad y fecha de creación, con importe actual/objetivo, porcentaje, icono y deadline.

El historial suma solo gastos (no ingresos ni transferencias): semana devuelve siete etiquetas, mes cuatro/cinco bloques de siete días desde el día 1 y año los meses transcurridos del año. Categorías agrega solo gastos; presupuestos incluyen categorías con límite y gastos asociados. Los gastos sin categoría cuentan en total de categorías y se agrupan, pero no pertenecen a presupuesto. El servicio no implementa caché y calcula métricas en memoria.

Referencias: [`DashboardService.java`](../../backend/src/main/java/com/puntomartinez/millete/dashboard/application/services/DashboardService.java), [`DashboardHistoryService.java`](../../backend/src/main/java/com/puntomartinez/millete/dashboard/application/services/DashboardHistoryService.java), [`DashboardCategoryService.java`](../../backend/src/main/java/com/puntomartinez/millete/dashboard/application/services/DashboardCategoryService.java), [`DashboardBudgetService.java`](../../backend/src/main/java/com/puntomartinez/millete/dashboard/application/services/DashboardBudgetService.java).
