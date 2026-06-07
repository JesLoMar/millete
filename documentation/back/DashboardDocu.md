# Dashboard — Documentación técnica (Backend)

## Estructura de archivos

- application/services/DashboardService.java — Servicio central con todas las métricas y gráficos
- domain/ports/in/GetDashboardDataUseCase.java — Interfaz del caso de uso del dashboard
- infrastructure/in/controller/DashboardController.java — Controlador REST con 9 endpoints
- infrastructure/in/controller/dto/DashboardMetricsResponseDTO.java — DTO de métricas principales
- infrastructure/in/controller/dto/DashboardHistoryResponseDTO.java — DTO de historial de gastos
- infrastructure/in/controller/dto/DashboardCategoriesResponseDTO.java — DTO de gastos por categoría
- infrastructure/in/controller/dto/DashboardBudgetsResponseDTO.java — DTO de presupuestos
- infrastructure/in/controller/dto/DashboardTransactionsResponseDTO.java — DTO de transacciones recientes
- infrastructure/in/controller/dto/DashboardGoalsResponseDTO.java — DTO de metas de ahorro
- infrastructure/in/controller/dto/InvestmentMetricsResponseDTO.java — DTO de métricas de inversiones
- infrastructure/in/controller/dto/InvestmentEvolutionResponseDTO.java — DTO de evolución del patrimonio
- infrastructure/in/controller/dto/InvestmentDistributionResponseDTO.java — DTO de distribución de cartera
- infrastructure/in/controller/dto/CategoryExpenseItemResponseDTO.java — DTO de item de gasto por categoría
- infrastructure/in/controller/dto/BudgetItemResponseDTO.java — DTO de item de presupuesto
- infrastructure/in/controller/dto/RecentTransactionResponseDTO.java — DTO de transacción reciente
- infrastructure/in/controller/dto/SavingsGoalResponseDTO.java — DTO de meta de ahorro
- infrastructure/in/controller/dto/InvestmentDistributionItemDTO.java — DTO de item de distribución

---

## DashboardController.java

Controlador REST mapeado a /api/v1/dashboard. Todos los endpoints extraen el userId del token JWT mediante un método auxiliar getUserId().

### GET /metrics?period=

Obtiene las métricas principales del dashboard: balance, ingresos, gastos, ahorros y tendencias respecto al período anterior. Responde 200 con DashboardMetricsResponseDTO.

### GET /history?period=

Obtiene el historial de gastos para gráficos de evolución. El período puede ser week, month o year. Responde 200 con DashboardHistoryResponseDTO.

### GET /categories?period=

Obtiene los gastos agrupados por categoría para el gráfico de donut. Responde 200 con DashboardCategoriesResponseDTO.

### GET /budgets?period=

Obtiene el estado de los presupuestos por categoría. Responde 200 con DashboardBudgetsResponseDTO.

### GET /recent-transactions?limit=

Obtiene las transacciones más recientes. El límite por defecto es 5. Responde 200 con DashboardTransactionsResponseDTO.

### GET /savings-goals

Obtiene las metas de ahorro calculadas automáticamente. Responde 200 con DashboardGoalsResponseDTO.

### GET /investments/metrics?period=

Obtiene métricas de inversiones: valor actual, retorno y tendencias. Responde 200 con InvestmentMetricsResponseDTO.

### GET /investments/evolution?period=

Obtiene la evolución del patrimonio en el tiempo. Responde 200 con InvestmentEvolutionResponseDTO.

### GET /investments/distribution?period=

Obtiene la distribución de la cartera por tipo de activo. Responde 200 con InvestmentDistributionResponseDTO.

---

## DashboardService.java

Servicio central que implementa GetDashboardDataUseCase. Inyecta TransactionRepository, CategoryRepository e InvestmentRepository. Contiene toda la lógica de agregación, cálculo de tendencias y generación de datos para gráficos.

---

## 1. Métricas del dashboard (getMetrics)

Calcula las métricas financieras principales comparando el período actual con el anterior.

### Funcionamiento

1. Obtiene el rango de fechas actual y el del período anterior.
2. Consulta las transacciones de ambos períodos mediante findByUserIdAndDateBetween.
3. Calcula ingresos, gastos y balance para cada período con sumByType.
4. Calcula tendencias porcentuales comparando valores actuales con anteriores.
5. Las transacciones inactivas no aparecen porque el repositorio aplica @SQLRestriction.

### DashboardMetricsResponseDTO

Campos: balance, income, expenses, savings (income - expenses), balanceTrend, incomeTrend, expensesTrend, savingsTrend.

---

## 2. Historial de gastos (getHistory)

Genera datos para gráficos de evolución de gastos en el tiempo.

### Períodos soportados

- Week: gastos diarios de lunes a domingo de la semana actual. Labels: Lun, Mar, Mié, Jue, Vie, Sáb, Dom.
- Month: gastos semanales del mes actual (Sem 1 a Sem 5). Labels: Sem 1, Sem 2, etc.
- Year: gastos mensuales del año actual (Ene a Dic). Labels: Ene, Feb, Mar, etc.

### Funcionamiento

Para cada intervalo, consulta las transacciones de tipo EXPENSE en ese rango de fechas y suma sus importes. Filtra solo las de tipo EXPENSE. No aplica filtro manual de isActive porque el repositorio ya lo hace con @SQLRestriction.

### DashboardHistoryResponseDTO

Campos: period, labels (Lista de String), data (Lista de BigDecimal).

---

## 3. Gastos por categoría (getCategories)

Genera datos para el gráfico de donut de gastos por categoría. Soporta transacciones huérfanas (sin categoría).

### Funcionamiento

1. Obtiene las transacciones de tipo EXPENSE en el rango de fechas.
2. Calcula el total de gastos.
3. Agrupa las transacciones por categoryId manualmente para evitar NullPointerException con groupingBy. Las transacciones con categoryId null se separan en una lista de huérfanas.
4. Para cada categoría con ID, busca su nombre con categoryRepository.findById(). Si la categoría fue eliminada, devuelve "Sin categoría".
5. Procesa las transacciones huérfanas: calcula su total y las combina con una entrada "Sin categoría" existente o crea una nueva.
6. Ordena por importe descendente.
7. Agrupa las categorías con menos del 5% en "Otros" mediante groupSmallCategories.

### Manejo de huérfanas

Las transacciones con categoryId = null (porque su categoría fue eliminada) se agrupan bajo "Sin categoría". Si ya existe un grupo "Sin categoría" (porque se encontró una categoría borrada), se combinan sus importes para evitar entradas duplicadas en el gráfico.

### DashboardCategoriesResponseDTO

Campos: totalExpenses, categories (Lista de CategoryExpenseItemResponseDTO con name, amount, percentage, transactionCount).

---

## 4. Presupuestos (getBudgets)

Muestra el estado de los presupuestos por categoría.

### Funcionamiento

1. Obtiene las categorías que tienen presupuesto definido con findCategoriesWithBudgetByUserId.
2. Obtiene las transacciones del período.
3. Para cada categoría con presupuesto, calcula el gasto real filtrando transacciones de tipo EXPENSE con esa categoría. Usa filter(t -> t.getCategoryId() != null && t.getCategoryId().equals(category.getId())) para evitar NullPointerException con categorías huérfanas.
4. Calcula el porcentaje de gasto sobre el límite.
5. Filtra solo las que tienen gasto mayor que cero.
6. Ordena: primero las que superan el 100%, luego por porcentaje descendente.

### DashboardBudgetsResponseDTO

Campos: period, budgets (Lista de BudgetItemResponseDTO con categoryId, category, spent, limit, percentage).

---

## 5. Transacciones recientes (getRecentTransactions)

Obtiene las últimas transacciones para mostrar en el dashboard.

### Funcionamiento

1. Obtiene las transacciones más recientes con findRecentByUserId.
2. Para cada transacción, resuelve el nombre y color de la categoría:
   - Si tiene categoryId, busca la categoría con categoryRepository.findById().
   - Si la categoría existe, obtiene su nombre y color.
   - Si no existe o no tiene categoryId, asigna "Sin categoría" y color null.
3. Mapea a RecentTransactionResponseDTO incluyendo categoryName y categoryColor.

### RecentTransactionResponseDTO

Campos: id, description, category (nombre), categoryColor (hexadecimal), categoryId, amount, date, type.

El campo categoryColor permite al frontend mostrar cada transacción con el color personalizado de su categoría en el dashboard.

---

## 6. Metas de ahorro (getSavingsGoals)

Genera metas de ahorro automáticas basadas en los ingresos del último año.

### Metas generadas

- Fondo de Emergencia: 3 meses de ingresos promedio mensuales. Target = monthlyAverageIncome x 3.
- Vacaciones: 20% del ingreso mensual durante 6 meses. Target = monthlyAverageIncome x 0.2 x 6.

### DashboardGoalsResponseDTO

Campos: goals (Lista de SavingsGoalResponseDTO con id, name, current, target, percentage, icon, targetDate).

---

## 7. Métricas de inversiones (getInvestmentMetrics)

Calcula el valor actual de la cartera, retorno y capital invertido.

### Funcionamiento

1. Obtiene todas las inversiones activas del usuario filtrando con Investment::isActive.
2. Suma currentValue, profitOrLoss e investedCapital de todas las inversiones.
3. Calcula la tendencia de retorno como porcentaje del capital invertido.

### InvestmentMetricsResponseDTO

Campos: portfolioValue, monthlyReturn, yearlyReturn, valueTrend, returnTrend, yearlyTrend.

---

## 8. Evolución del patrimonio (getInvestmentEvolution)

Genera datos para el gráfico de evolución del valor de la cartera en el tiempo.

### Períodos soportados

- Week: valor diario de la cartera de lunes a domingo.
- Month: valor mensual de los últimos 6 meses.
- Year: valor mensual de enero a diciembre del año actual.

### Funcionamiento

Para cada punto en el tiempo, calcula el valor del portfolio sumando el currentValue de las inversiones activas cuya fecha de compra es anterior a esa fecha.

### InvestmentEvolutionResponseDTO

Campos: period, labels (Lista de String), data (Lista de BigDecimal).

---

## 9. Distribución de cartera (getInvestmentDistribution)

Genera datos para el gráfico de distribución de inversiones por tipo de activo.

### Funcionamiento

1. Obtiene todas las inversiones activas del usuario.
2. Agrupa por tipo de inversión (STOCK, ETF, CRYPTO, etc.) usando groupingBy.
3. Calcula el valor total y el porcentaje de cada tipo.
4. Asigna colores del tema para el gráfico.

### InvestmentDistributionResponseDTO

Campos: totalValue, distribution (Lista de InvestmentDistributionItemDTO con type, value, percentage, color).

---

## Métodos auxiliares

### getDateRange

Devuelve el inicio y fin de un período (week, month, year). Para week: lunes 00:00 a domingo 23:59. Para month: día 1 00:00 a último día 23:59. Para year: 1 de enero 00:00 a 31 de diciembre 23:59.

### getPreviousPeriod

Calcula el rango del período anterior al actual. Para week: resta 7 días. Para month: resta 1 mes. Para year: resta 1 año.

### sumByType

Suma los importes absolutos de una lista de transacciones filtradas por tipo (INCOME o EXPENSE).

### calculateTrend

Calcula la tendencia porcentual entre un valor actual y uno anterior. Si el valor anterior es cero, devuelve 100% si el actual es positivo, o 0% si no.

### calculatePercentage

Calcula el porcentaje de una parte sobre un total. Si el total es cero, devuelve 0%.

### groupSmallCategories

Agrupa las categorías con menos del 5% del total en una categoría "Otros". Mantiene las categorías principales con 5% o más.

### generateSavingsGoals

Genera dos metas de ahorro: Fondo de Emergencia (3 meses de ingresos) y Vacaciones (6 meses al 20% de ingresos).

---

## Optimización de consultas

Todas las consultas de transacciones usan findByUserIdAndDateBetween del repositorio, que aplica @SQLRestriction("active = true") a nivel de Hibernate y además incluye AND t.active = true en la JPQL como doble seguridad. No se realizan filtrados manuales en memoria para active, lo que mejora el rendimiento al delegar el filtrado a la base de datos.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/dashboard/metrics?period= | Métricas principales |
| GET | /api/v1/dashboard/history?period= | Historial de gastos |
| GET | /api/v1/dashboard/categories?period= | Gastos por categoría |
| GET | /api/v1/dashboard/budgets?period= | Presupuestos |
| GET | /api/v1/dashboard/recent-transactions?limit= | Transacciones recientes |
| GET | /api/v1/dashboard/savings-goals | Metas de ahorro |
| GET | /api/v1/dashboard/investments/metrics?period= | Métricas de inversiones |
| GET | /api/v1/dashboard/investments/evolution?period= | Evolución del patrimonio |
| GET | /api/v1/dashboard/investments/distribution?period= | Distribución de cartera |