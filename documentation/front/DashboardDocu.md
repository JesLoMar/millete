# Dashboard

[`pages/page.tsx`](../../frontend/src/features/dashboard/pages/page.tsx) compone métricas, gráficos, presupuestos, transacciones recientes, acciones rápidas y asistente. Los datos se obtienen con los hooks y servicios de `features/dashboard`; consultar [`useDashboardQueries.ts`](../../frontend/src/features/dashboard/hooks/useDashboardQueries.ts) para endpoints, claves y período actuales.

## Funciones disponibles

Los componentes del dashboard presentan métricas, historial, distribución por categoría, presupuestos y actividad reciente. Importar/exportar se implementa mediante los modales y [`useExport.ts`](../../frontend/src/features/dashboard/hooks/useExport.ts). Los formatos disponibles y parámetros son los que declara ese servicio; los detalles internos del formato del servidor quedan fuera del alcance.

Las acciones rápidas abren las operaciones existentes de transacciones/categorías o los modales de datos. Las mutaciones del resto de features invalidan claves relevantes según cada hook; no hay una regla universal de invalidación única para cualquier acción.

## Asistente

El dashboard integra `features/assistant`; interpretación de texto y confirmación se documentan en [AssistantDocu.md](AssistantDocu.md).

## Fuentes

Componentes: [`frontend/src/features/dashboard/components`](../../frontend/src/features/dashboard/components). Tipos y constantes: [`types/index.ts`](../../frontend/src/features/dashboard/types/index.ts) y `constants.ts`. Los contratos descritos aquí son los consumidos por el cliente.

## Consultas

`useDashboardQueries(period)` hace cinco queries, todas con `staleTime: 30 s` y `retry: 1` en este hook:

| Clave | Solicitud | Datos mostrados |
|---|---|---|
| `dashboardMetrics, period` | GET `/dashboard/metrics?period=` | balance, income, expenses, savings y tendencias. |
| `historyChart, period` | GET `/dashboard/history?period=` | labels y data para gráfico de historial. |
| `categoryStats, period` | GET `/dashboard/categories?period=` | nombres, amounts y porcentajes para donut. |
| `budgets, period` | GET `/dashboard/budgets?period=` | presupuesto, gasto, límite y porcentaje. |
| `recentTransactions` | GET `/dashboard/recent-transactions?limit=5` | lista de transacciones recientes y categoría/color. |

Cambiar período vuelve a consultar las cuatro queries de período; recientes no lo usan. Cada gráfico/tarjeta recibe sus estados de carga y presenta skeleton o estado sin datos definido en su componente.

## Importar y exportar

Import modal acepta archivo JSON, selección o arrastrar/soltar. La página envía el archivo como multipart a POST `/data/import`; errores son tratados por el callback de la página y se invalida la caché en éxito.

`useExport.performExport(format, configValue?)` solicita blob y descarga archivo local: JSON por GET `/data/export`, ZIP por `/data/export/zip`, CSV por `/data/export/csv/{entityType}` y PDF por `/data/export/pdf?period=`. CSV/PDF requieren la opción asociada; el hook devuelve resultado false si falla y siempre restablece `isExporting`. La selección de formato/opciones está en `ExportModal`.
