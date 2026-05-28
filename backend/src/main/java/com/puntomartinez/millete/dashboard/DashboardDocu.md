# Dashboard — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/DashboardService.java** — Servicio central que implementa todos los casos de uso
- **domain/ports/in/GetDashboardDataUseCase.java** — Interfaz con todos los casos de uso del dashboard
- **infrastructure/in/controller/DashboardController.java** — Controlador REST con 9 endpoints
- **infrastructure/in/controller/dto/DashboardMetricsResponseDTO.java** — Métricas principales
- **infrastructure/in/controller/dto/DashboardHistoryResponseDTO.java** — Historial de gastos
- **infrastructure/in/controller/dto/DashboardCategoriesResponseDTO.java** — Gastos por categoría
- **infrastructure/in/controller/dto/CategoryExpenseItemResponseDTO.java** — Item de gasto por categoría
- **infrastructure/in/controller/dto/DashboardBudgetsResponseDTO.java** — Estado de presupuestos
- **infrastructure/in/controller/dto/BudgetItemResponseDTO.java** — Item de presupuesto
- **infrastructure/in/controller/dto/DashboardTransactionsResponseDTO.java** — Transacciones recientes
- **infrastructure/in/controller/dto/RecentTransactionResponseDTO.java** — Item de transacción reciente
- **infrastructure/in/controller/dto/DashboardGoalsResponseDTO.java** — Metas de ahorro
- **infrastructure/in/controller/dto/SavingsGoalResponseDTO.java** — Item de meta de ahorro
- **infrastructure/in/controller/dto/InvestmentMetricsResponseDTO.java** — Métricas de inversiones
- **infrastructure/in/controller/dto/InvestmentEvolutionResponseDTO.java** — Evolución del patrimonio
- **infrastructure/in/controller/dto/InvestmentDistributionResponseDTO.java** — Distribución de cartera
- **infrastructure/in/controller/dto/InvestmentDistributionItemDTO.java** — Item de distribución

---

## Arquitectura

El módulo sigue una arquitectura simple sin puertos de salida propios. Usa directamente los repositorios de otros módulos: TransactionRepository, CategoryRepository y InvestmentRepository. La interfaz GetDashboardDataUseCase define todos los casos de uso y DashboardService los implementa.

---

## DashboardController.java

Controlador REST mapeado a /api/v1/dashboard. Todos los endpoints requieren autenticación. El userId se extrae del token JWT.

### Endpoints

- GET /metrics?period= — Métricas principales (balance, income, expenses, savings y tendencias)
- GET /history?period= — Historial de gastos (labels y data)
- GET /categories?period= — Gastos por categoría (total y lista de categorías con amount, percentage, transactionCount)
- GET /budgets?period= — Estado de presupuestos (categorías con spent, limit y percentage)
- GET /recent-transactions?limit=5 — Últimas transacciones del usuario
- GET /savings-goals — Metas de ahorro generadas automáticamente
- GET /investments/metrics?period= — Métricas de inversiones (valor, retorno, dividendos)
- GET /investments/evolution?period= — Evolución del patrimonio en el período
- GET /investments/distribution?period= — Distribución de la cartera por tipo

---

## GetDashboardDataUseCase.java

Interfaz que define los 9 casos de uso del dashboard. Todos devuelven DTOs específicos. Está en el paquete de puertos de entrada (domain/ports/in).

---

## DashboardService.java

Servicio central que implementa GetDashboardDataUseCase. Inyecta TransactionRepository, CategoryRepository y InvestmentRepository.

### 1. Métricas (getMetrics)

- Calcula el rango de fechas actual y el período anterior mediante getDateRange y getPreviousPeriod.
- Obtiene transacciones activas de ambos períodos.
- Calcula income, expenses, balance y savings para ambos períodos.
- Calcula tendencias comparando el valor actual con el anterior (calculateTrend).
- Savings se calcula como income menos expenses.

### 2. Historial de gastos (getHistory)

Delega en tres métodos según el período:

- getWeeklyHistory: itera los 7 días de la semana actual. Para cada día calcula el gasto total filtrando por tipo EXPENSE. Labels: Lun, Mar, Mié, Jue, Vie, Sáb, Dom.
- getMonthlyHistory: divide el mes en hasta 5 semanas. Para cada semana calcula el gasto total. Labels: Sem 1, Sem 2, etc.
- getYearlyHistory: itera los meses del año actual hasta el mes actual. Para cada mes calcula el gasto total. Labels: Ene, Feb, Mar, etc.

### 3. Gastos por categoría (getCategories)

- Obtiene todas las transacciones de tipo EXPENSE del período.
- Agrupa por categoryId.
- Para cada grupo calcula amount total, percentage respecto al total y transactionCount.
- Obtiene el nombre de la categoría del CategoryRepository. Si no existe, asigna "Sin categoría".
- Ordena por amount de mayor a menor.
- Agrupa las categorías con menos del 5% en "Otros" mediante groupSmallCategories.

### 4. Presupuestos (getBudgets)

- Obtiene las categorías con presupuesto configurado (budgetLimit no nulo y mayor que cero) mediante findCategoriesWithBudgetByUserId.
- Filtra solo las activas.
- Obtiene las transacciones del período.
- Para cada categoría calcula el gasto total filtrando por tipo EXPENSE.
- Calcula el porcentaje de gasto respecto al límite.
- Filtra solo las que tienen gasto mayor que cero.
- Ordena: primero las excedidas (porcentaje >= 100), luego el resto por porcentaje de mayor a menor.

### 5. Transacciones recientes (getRecentTransactions)

- Obtiene las últimas transacciones del usuario mediante findRecentByUserId con el límite especificado.
- Filtra solo las activas.
- Para cada transacción obtiene el nombre de la categoría. Si no existe, asigna "Sin categoría".
- Mapea a RecentTransactionResponseDTO con id, description, category, categoryId, amount, date y type.

### 6. Metas de ahorro (getSavingsGoals)

- Obtiene las transacciones del último año.
- Calcula el ingreso mensual promedio (totalIncome / 12).
- Genera dos metas automáticas:
    - Fondo de Emergencia: objetivo de 3 meses de ingresos promedio.
    - Vacaciones: objetivo del 20% del ingreso mensual por 6 meses.
- El progreso actual se calcula sobre los ahorros totales (income - expenses).

### 7. Métricas de inversiones (getInvestmentMetrics)

- Obtiene las inversiones activas del usuario.
- Calcula valor actual total, retorno total y capital invertido.
- Calcula tendencia del portafolio y retorno.

### 8. Evolución del patrimonio (getInvestmentEvolution)

Delega en tres métodos según el período:

- getWeeklyInvestmentEvolution: itera los días de la semana, calculando el valor del portafolio al final de cada día.
- getMonthlyInvestmentEvolution: itera los últimos 6 meses, calculando el valor al final de cada mes.
- getYearlyInvestmentEvolution: itera los meses del año, calculando el valor al final de cada mes.

En cada punto usa getPortfolioValueAtDate que suma el currentValue de las inversiones activas cuya fecha de compra es anterior a la fecha consultada.

### 9. Distribución de cartera (getInvestmentDistribution)

- Agrupa las inversiones activas por tipo (STOCK, ETF, etc.).
- Para cada tipo calcula el valor total y el porcentaje respecto al total.
- Asigna colores de CHART_COLORS.

### Métodos auxiliares

- getDateRange: devuelve el rango de fechas (inicio y fin) según el período.
- getPreviousPeriod: devuelve el rango del período anterior para comparar tendencias.
- sumByType: suma los importes de transacciones filtradas por tipo.
- calculateTrend: calcula la tendencia porcentual entre valor actual y anterior.
- calculatePercentage: calcula el porcentaje de una parte respecto a un total.
- groupSmallCategories: agrupa categorías con menos del 5% en "Otros".

---

## DTOs

### DashboardMetricsResponseDTO

balance, income, expenses, savings (BigDecimal) y balanceTrend, incomeTrend, expensesTrend, savingsTrend (double).

### DashboardHistoryResponseDTO

period (String), labels (List<String>), data (List<BigDecimal>).

### DashboardCategoriesResponseDTO

totalExpenses (BigDecimal), categories (List<CategoryExpenseItemResponseDTO>).

### CategoryExpenseItemResponseDTO

name, amount, percentage, transactionCount.

### DashboardBudgetsResponseDTO

period (String), budgets (List<BudgetItemResponseDTO>).

### BudgetItemResponseDTO

categoryId (UUID), category, spent, limit (BigDecimal), percentage (double).

### DashboardTransactionsResponseDTO

transactions (List<RecentTransactionResponseDTO>).

### RecentTransactionResponseDTO

id, description, category, categoryId, amount, date, type.

### DashboardGoalsResponseDTO

goals (List<SavingsGoalResponseDTO>).

### SavingsGoalResponseDTO

id, name, current, target, percentage, icon, deadline.

### InvestmentMetricsResponseDTO

portfolioValue, monthlyReturn, dividends, portfolioTrend, returnTrend, dividendsTrend.

### InvestmentEvolutionResponseDTO

period, labels, data.

### InvestmentDistributionResponseDTO

totalValue, distribution (List<InvestmentDistributionItemDTO>).

### InvestmentDistributionItemDTO

name, value, percentage, color.

---

## Ordenación de presupuestos

Las categorías en el endpoint /budgets se ordenan con el siguiente criterio:

1. Primero las categorías con presupuesto excedido (porcentaje >= 100%).
2. Luego el resto de categorías por porcentaje de mayor a menor.

Esto se implementa en el método getBudgets del DashboardService mediante un comparador personalizado.

---

## Conexión con el frontend

| Endpoint | Uso en el dashboard |
|----------|-------------------|
| GET /metrics | FormattedMetricCard (4 tarjetas) |
| GET /history | HistoryChart (gráfico de barras) |
| GET /categories | CategoryDonut (gráfico de donut) |
| GET /budgets | BudgetBars (barras de presupuesto) |
| GET /recent-transactions | RecentTransactions (lista de últimas 5) |
| GET /savings-goals | Metas de ahorro |
| GET /investments/metrics | Métricas de inversiones |
| GET /investments/evolution | Evolución del patrimonio |
| GET /investments/distribution | Distribución de cartera |