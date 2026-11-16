# Inversiones

La interfaz vigente presenta un registro de actividades, posiciones derivadas y seguimiento de cartera; no es un CRUD plano de activos con edición directa de cantidad/valor.

## Pantalla y áreas

[`pages/page.tsx`](../../frontend/src/features/investments/pages/page.tsx) consulta estado de seguimiento y catálogo, organiza pestañas de cartera, actividad, rendimiento, posiciones, activos y estado. Si el seguimiento no está configurado, ciertas pestañas muestran el flujo de configuración. La pantalla también ofrece actualización de datos de mercado y registro de actividad.

## Modelo de interacción

Los componentes de `components/` incluyen `RecordActivityDialog`, historial, posiciones, configuración del seguimiento, paneles de cartera/rendimiento y actualización de precios. Los servicios y hooks en `services/` y `hooks/` establecen los endpoints y datos usados por el cliente. Las operaciones de actividad y cálculos asociados no deben describirse como borrado/edición convencional de activos.

La actualización de datos de mercado es una acción explícita del usuario. El comportamiento observable y posibles estados de error deben consultarse en los componentes y servicios; esta guía no promete disponibilidad de fuentes externas ni refresco automático.

## Fuentes

[`frontend/src/features/investments`](../../frontend/src/features/investments), en particular `pages/page.tsx`, `types/index.ts`, `services/investments.service.ts`, `hooks/useInvestmentQueries.ts` y `hooks/useInvestmentMutations.ts`.

## Modelo y vistas

| Archivo | Responsabilidad visible |
|---|---|
| `pages/page.tsx` | Lee tracking, fija `asOf`, compone pestañas y abre configuración/registro/refresh. |
| `components/InvestmentTabs.tsx` | Pestañas `portfolio`, `activity`, `performance`, `holdings`, `assets` y `status`. |
| `PortfolioPanel.tsx`, `PortfolioSummary.tsx`, `CashBalancesCard.tsx` | Valoración a fecha, resumen, posiciones y saldos por moneda. |
| `ActivityHistory.tsx`, `ActivityDetailsDialog.tsx`, `EditActivityDialog.tsx` | Historial, detalle y edición de actividades admitidas. |
| `HoldingsPanel.tsx`, `PositionsTable.tsx`, `ClosedLotsTable.tsx` | Snapshots de holdings, posiciones calculadas y lotes cerrados. |
| `PerformancePanel.tsx` | Consulta rendimientos en intervalo; presenta estados de cálculo/no disponibilidad. |
| `AssetsPanel.tsx`, `AssetFormDialog.tsx`, `AssetPicker.tsx` | Catálogo compartido y activos propios para seleccionar o administrar. |
| `TrackingSetupDialog.tsx`, `StatusPanel.tsx`, `HealthBanner.tsx` | Inicio de seguimiento y estado de salud informado por el cliente. |

`AssetReference` identifica un activo mediante `kind: SHARED | USER` e `id`. `AssetType` contempla STOCK, CRYPTO, FUND, ETF, REAL_ESTATE y OTHER. Las actividades incluyen BUY, SELL, DIVIDEND, DEPOSIT, WITHDRAW, SPLIT, EXCHANGE, OPENING_CASH y OPENING_POSITION; las dos últimas son tipos de historial, no opciones creables en el diálogo. Dividendos admiten CASH e IN_KIND. Los campos específicos están en `ActivityDetails`; los importes monetarios usan `Money { amount, currency }`.

`Portfolio` presenta fecha `asOf`, moneda local, saldos, posiciones y total local. Cada `Position` incluye cantidad, coste, valor de mercado, ganancia no realizada y `calculationStatus` (`CALCULABLE`/`NOT_CALCULABLE`), además de `estimated`, `historyIncomplete` y motivo opcional. Holding distingue `ACTIVE` y `SUPERSEDED`; el listado incluye sus fechas de snapshot y sustitución. El ledger modela actividades y snapshots; las vistas derivadas no equivalen a editar un saldo manual.

## Servicio HTTP observado

Todos los paths se añaden a `/investments` por `investmentsService`; los nombres de campos siguientes son los usados por el cliente.

| Método y path | Operación / datos relevantes |
|---|---|
| GET `/tracking` | Devuelve `{ configured, trackingStartAt }`; respuesta 404 se normaliza en cliente como no configurado. |
| POST `/tracking` | `ConfigureTrackingRequest { trackingStartAt }`. |
| GET `/portfolio?asOf=` / GET `/cash?asOf=` | Portfolio a instante dado y saldos `{ balances }`. |
| GET `/activities` | Página filtrable por `page,size,type,currency,from,to,assetKind,assetId`; devuelve `ActivityPage`. |
| POST `/activities/{buy,sell,deposits,withdrawals,dividends,dividends/in-kind,exchanges,splits}` | Actividad tipada; BUY/SELL incluyen activo, cantidad y precio; depósitos/retiros/dividendo cash importe y moneda; exchange origen/monedas; split activo/ratio. Se envía `Idempotency-Key`. |
| PATCH `/activities/{id}` | `EditActivityRequest { occurredAt, details, reason }`; devuelve actividad. |
| GET `/holdings` / POST `/holdings` | Página con filtro activo o snapshot `assetReference,snapshotAt,quantity,acquisitionCost`. |
| POST `/holdings/{id}/history` | Reemplaza historial usando `{ history: [...] }`; los elementos pueden ser trades, dividendos, split con fecha, cantidades/precio/moneda según tipo. |
| GET `/lots/closed?from=&to=&page=&size=` | Página de lotes con base de coste, proceeds, ganancia y marcas estimadas/incompletas. |
| GET `/performance?from=&to=` | Métricas de período y `status` calculable/no calculable. |
| GET `/shared-assets?search=`; GET `/user-assets?search=` | Catálogos usados por AssetPicker y catálogo combinado. |
| POST `/user-assets`; PUT `/user-assets/{id}`; POST `/user-assets/{id}/prices` | Alta `{name,type,sector,origin,initialPrice}`, edición `{name,sector}`, precio `{unitPrice}`. |
| GET `/sectors` | Sectores seleccionables. |
| GET `/shared-assets/{id}/prices?from=&to=&page=&size=`; GET `/fx-rates?baseCurrency=&quoteCurrency=&from=&to=` | Históricos paginados de precios y cambio. |
| POST `/market-data/refresh` | `{ currencyPairs, from?, to? }`; el diálogo actual envía pares y deja el rango omitido. |
| GET `/health`; GET `/ledger/snapshot`; POST `/ledger/restore` | Reporte de issues, snapshot de ledger, y restauración explícita de snapshot. |

## Secuencia, validación y estados

El usuario configura seguimiento desde la pestaña/aviso correspondiente; la fecha requerida no puede estar en el futuro y se envía como instante ISO. En la pantalla, `portfolio`, `activity`, `performance` y `holdings` están bloqueadas si no hay configuración; `assets` y `status` siguen disponibles. Las consultas llevan `staleTime` y reintentos específicos en `useInvestmentQueries`; los paneles muestran carga, error con reintento o respuesta vacía según componente.

`RecordActivityDialog` valida fecha y campos por tipo: trades requieren activo, cantidad/precio positivos; cash requiere cantidad positiva y código de moneda de tres letras; dividendos en especie requieren activo y cantidad; exchange requiere cantidad y códigos distintos; split requiere ratio positivo. Las peticiones fallidas dejan el diálogo abierto y la mutación muestra el error. Crear/editar actividades, holdings, activos, precio propio, tracking, refresh y restore invalidan los prefijos definidos por `ALL_INVESTMENT_QUERY_PREFIXES` y notifican éxito/error.

El refresh de mercado forma pares de moneda iniciales a partir de divisas de activos y moneda local; admite agregar/quitar pares, valida formato/distinción y muestra estado de envío. Servicio usa timeout ampliado para la llamada, sin que esto prometa disponibilidad externa. Los datos de mercado e FX también pueden consultarse desde sus paneles.
