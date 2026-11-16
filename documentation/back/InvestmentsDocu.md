# Inversiones: libro de actividades, replay y valoración

Investments modela un libro de actividades; las posiciones no son registros CRUD con precio de compra y precio actual. Las actividades son la fuente para reconstruir caja, lotes y posiciones. La base de datos conserva auditoría y proyecciones. Referencias: [`PortfolioReplay.java`](../../backend/src/main/java/com/puntomartinez/millete/investments/domain/model/PortfolioReplay.java), [`V5__v0.2.0.sql`](../../backend/src/main/resources/db/migration/V5__v0.2.0.sql).

## Organización y responsabilidades

| Clase / puerto | Función |
|---|---|
| `Activity`, `ActivityType`, detalles de actividad | Representar eventos financieros y sus datos tipados |
| `PortfolioReplay` | Reconstruir lotes, consumos FIFO, caja y posiciones |
| `TradeActivityService`, `CashActivityService`, `CorporateActionActivityService` | Casos de uso de compraventa, caja y eventos corporativos |
| `EditActivityService` | Editar datos permitidos, registrar auditoría y reconstruir |
| `PortfolioRebuildService` | Persistir proyecciones derivadas después de mutaciones |
| `PortfolioQueryService` | Consultar cartera/caja a instante, valorar activos y cerrar lotes |
| `PerformanceService` | Calcular atribución de performance para intervalo |
| `InvestmentTrackingService` | Consultar y configurar el instante de tracking |
| `MarketDataRefreshService`, `MarketDataQueryService` | Actualizar y consultar precios/FX |
| `InvestmentLedgerSnapshotService`, `InvestmentLedgerRestoreService` | Exportar/restaurar el ledger |
| `InvestmentHealthService` | Comprobar integridad/datos y reconciliar notificaciones |

Los puertos de entrada y salida están en `investments/domain/ports`; los controladores REST en `investments/infrastructure/in/controller`; adaptadores PostgreSQL en `investments/infrastructure/out/persistence/postgresql`.

## Activos y referencias

Una `AssetReference` contiene `kind` (`SHARED` o `USER`) e ID.

| Tipo | Propiedad/valoración | Registro |
|---|---|---|
| `SHARED` | Catálogo común y precios globales `asset_prices` | API de consulta `GET /shared-assets` y `GET /shared-assets/{id}` |
| `USER` | Activo del usuario, precio manual en `user_asset_prices` | `GET/POST /user-assets`, `GET/PUT /user-assets/{id}`, `POST /user-assets/{id}/prices` |

Tipos de activos personales incluyen `STOCK`, `CRYPTO`, `FUND`, `ETF`, `REAL_ESTATE` y `OTHER`. Los proveedores de mercado no necesariamente soportan todos esos tipos. El controlador no expone creación de `SharedAsset`; V5 crea el catálogo de sectores, pero no carga filas de activos compartidos.

## Actividades y API

Todas las rutas usan `/api/v1/investments`; los controladores obtienen el usuario de `JwtUser`, no de un parámetro del cliente.

| Método / ruta | Campos de entrada principales y resultado |
|---|---|
| `POST /activities/buy` | `occurredAt`, `assetReference`, `quantity`, `unitPrice`, `comment`; cabecera `Idempotency-Key`; `201 ActivityResponseDTO` |
| `POST /activities/sell` | Mismos campos; venta consume unidades FIFO |
| `POST /activities/dividends` | `occurredAt`, referencia opcional, `amount`, `currency`, `comment` |
| `POST /activities/dividends/in-kind` | `occurredAt`, referencia, `quantity`, `comment` |
| `POST /activities/deposits`, `/activities/withdrawals` | `occurredAt`, `amount`, `currency`, `comment`; cabecera idempotencia |
| `POST /activities/exchanges` | `occurredAt`, `amountOrigin`, `currencyOrigin`, `currencyDestination`, `comment` |
| `POST /activities/splits` | `occurredAt`, referencia, `ratio`, `comment` |
| `GET /activities` | Filtros opcionales `assetKind`+`assetId`, `type`, `currency`, `from`, `to`; `page=0`, `size=50` |
| `PATCH /activities/{id}` | `occurredAt`, detalles tipados, `reason`; devuelve actividad |

La cabecera `Idempotency-Key` es requerida en las altas anteriores. La referencia de activo del DTO contiene `kind` e `id`. Cantidades y precios de operación son positivos; moneda son tres letras; los comentarios tienen máximo 500 caracteres. El tamaño de página debe estar entre 1 y 200 y `page >= 0`. Una referencia de filtro exige ambos campos. El listado filtra por fechas, tipo y monedas presentes en los detalles, y devuelve `PaginatedResponseDTO`.

`ActivityResponseDTO` contiene ID, tipo, referencia, fechas de ocurrencia/creación/modificación, `orderingKey`, detalles, comentario y `linkedTransactionId`. Detalles de trade exponen cantidad, precio unitario, importe liquidado y FX aplicado; intercambio muestra dinero de origen/destino y FX; caja muestra importe/moneda; dividendos en especie exponen cantidad y precio unitario de referencia. La edición acepta `BUY_SELL`, `CASH_DIVIDEND`, `IN_KIND_DIVIDEND`, `EXCHANGE` o `SPLIT`, y exige razón no vacía de hasta 500 caracteres; depósitos/retiros no son tipos de edición en el DTO.

## Tracking y holdings iniciales

`GET /tracking` devuelve `configured` y `trackingStartAt`. `POST /tracking` recibe `{ "trackingStartAt": "<Instant>" }` y responde `204`. El servicio exige instante no nulo y configuración única; no hay ruta para editar o borrar el valor.

`POST /holdings` crea una posición inicial con `assetReference`, `snapshotAt`, `quantity` positiva y `acquisitionCost` no negativo. `GET /holdings` lista páginas (`page=0`, `size=50`, máximo 200), opcionalmente por referencia; `GET /holdings/active?assetKind=&assetId=` busca el holding activo. `POST /holdings/{id}/history` recibe una lista no vacía de eventos históricos (compra, venta, dividendo en efectivo/en especie, split) y reemplaza esa historia.

Un holding activo representa un punto inicial con historia incompleta, no una operación normal del ledger. Solo puede haber uno activo por activo; sus actividades no pueden coexistir en o antes de `snapshotAt`. La reconstrucción genera lote inicial. La posición resultante puede tener `historyIncomplete=true` (también expuesto en lotes cerrados), por lo que no representa un historial completo anterior al snapshot.

## Replay, orden, FIFO y caja

El replay ordena actividades por `occurredAt`, `orderingKey` e ID. Holdings activos se incorporan como lotes iniciales. BUY añade lote y, desde el tracking, descuenta efectivo de liquidación; SELL consume lotes FIFO (fecha de adquisición, orden, ID) y acredita liquidación. Dividendos pueden acreditar caja o crear lote de unidades con coste cero; SPLIT ajusta lotes abiertos; DEPOSIT/WITHDRAW/OPENING_CASH y EXCHANGE afectan efectivo cuando su fecha está dentro del período de tracking. Actividades anteriores al tracking aún reconstruyen lotes, pero no caja.

Compras/ventas crean coste de lote como cantidad × importe unitario en moneda del activo; el movimiento de caja usa `settlement.amount`. La liquidación puede expresarse en moneda del activo o moneda local; cuando difieren, `AppliedFxRate` conserva tipo/dirección. El replay impide que una salida de efectivo deje saldo negativo: lanza `CASH_NEGATIVE` y «El valor a pagar es mayor que el saldo disponible». Investment enlaza depósitos/retiros con movimientos de tipo opuesto en transactions: depósito de inversiones crea `TRANSFER_OUT`; retiro crea `TRANSFER_IN`. El importe de la transferencia se convierte a moneda local con el FX en la dirección solicitada, invirtiendo la cotización histórica cuando es necesario (por ejemplo, 100 USD con EUR/USD 1,1 produce aproximadamente 90,91 EUR). Esta integración no forma parte del DTO ordinario de transacciones.

Los resultados persistidos (`lots`, `lot_consumptions`, `positions`) son proyecciones derivadas. Mutaciones del ledger reconstruyen esas tablas; consultas temporales vuelven a ejecutar replay con actividades y holdings aplicables a la fecha.

Errores tipados del replay incluyen `POSITION_REQUIRED`, `POSITION_NEGATIVE`, `INCONSISTENT_CURRENCY`, `CASH_NEGATIVE`, `USER_MISMATCH`, `MULTIPLE_ACTIVE_HOLDINGS` y `HOLDING_ACTIVITY_CONFLICT`. V5 protege identidad de actividad, evita borrado físico, restringe vínculos de consumos a venta/lote del mismo usuario y limita consumos a la cantidad original.

## Edición y auditoría

`PATCH /activities/{id}` actualiza el instante, detalle compatible con tipo y razón. El servicio conserva el antes/después y la razón en `activity_audit`, cambia la actividad y reconstruye la cartera. Las activities no se eliminan físicamente. Las transacciones financieras que V5 marca vinculadas a inversiones están protegidas contra actualización/eliminación por restricciones de base.

## Consultas de cartera, efectivo y performance

| Método / ruta | Parámetros y respuesta |
|---|---|
| `GET /cash?asOf=<Instant>` | `CashBalancesResponseDTO`: mapa de moneda a saldo |
| `GET /portfolio?asOf=<Instant>` | Instante, divisa local, saldos, posiciones, total local y `estimated` |
| `GET /lots/closed?from=<Instant>&to=<Instant>&page=0&size=50` | Lotes cerrados, coste, proceeds, ganancia realizada y fechas |
| `GET /performance?from=<Instant>&to=<Instant>` | Atribución de performance del intervalo |

`asOf`, `from` y `to` son obligatorios en las operaciones correspondientes; los casos de uso de intervalo rechazan `from > to`. Para cada posición el servicio reproduce actividades/holdings no posteriores al instante; selecciona el precio más reciente no posterior al `asOf`, exige coincidencia de moneda con el activo y convierte mediante FX directo o inverso. El precio de `SHARED` proviene de `asset_prices`, el de `USER` de `user_asset_prices`.

Sin precio aplicable, precio de valoración, coincidencia de moneda o FX, una posición se marca `NOT_CALCULABLE` con razón y `marketValue`/`unrealizedGain` nulos. El total en moneda local no incluye valores de posiciones no calculables. El mapa de caja conserva el saldo original por divisa; caja sin conversión no suma al total local y marca el resultado `estimated`. El coste local puede ser nulo si no hay FX. El resultado de posición incluye cantidad, coste local, valor de mercado, ganancia no realizada, moneda local, estado de cálculo, estimación, incompletitud histórica y motivo de indisponibilidad. El precio compartido para valoración es `close`, o `adjustedClose` si `close` es nulo.

## Market data

`POST /market-data/refresh` recibe `from?`, `to?`, `currencyPairs` (requerido, hasta 100 elementos de 20 caracteres) y responde `204`. `from`/`to`, si están presentes, deben formar intervalo válido. Una excepción de proveedor se traduce a `502 Bad Gateway`; validación de entrada a `400`. `GET /shared-assets/{sharedAssetId}/prices?from=&to=&page=0&size=50` y `GET /fx-rates?baseCurrency=&quoteCurrency=&from=&to=&page=0&size=50` requieren fechas y aceptan páginas de hasta 200.

El servicio programa precios globales, no por usuario. El scheduler predeterminado espera 60 s y se repite cada 21 600 000 ms; su `to` se toma de `TimeProvider` y toma la última marca temporal guardada como inicio de la serie, con recuperación acotada por petición. Si el proveedor falla, el scheduler registra la excepción. El servicio de refresco configura proveedores y límites; sus adapters están bajo `investments/infrastructure/out/marketdata`. La selección actual del composite admite Yahoo para `STOCK`/`ETF` y FX, CoinGecko para `CRYPTO` cuando está configurado; Finnhub no anuncia soporte al composite. La clave/mapa CoinGecko se configuran externamente mediante `COINGECKO_API_KEY` y `COINGECKO_ID_MAP`.

Precio `asset_prices` incluye OHLC, adjusted close opcional, volumen, moneda, fuente, timestamp y fecha de obtención; FX contiene base, cotizada, tasa, fuente y timestamps. La consulta de valoración usa `close`, o `adjustedClose` cuando close es nulo. Las series se consultan al último instante no posterior a la fecha pedida.

## Estado de salud y tareas programadas

`GET /health` ejecuta `InvestmentHealthService.check` y responde `{checkedAt, issues}`; issue incluye `code`, `resourceId`, `severity`, `message`. La comprobación también reconcilia notificaciones. Comprueba tracking y moneda local, referencias a activos, sector, replay, disponibilidad/antigüedad/moneda de precio y FX, caja negativa y transferencias enlazadas.

El scheduler predeterminado inicia después de 120 s y repite cada 21 600 000 ms. Recorre usuarios devueltos por `InvestmentHealthTargetPort`; errores individuales se registran y no detienen el recorrido restante. La antigüedad máxima de datos usa `app.investments.market-data.max-age` (por defecto `PT72H`). El scheduler de market data usa `app.investments.market-data.refresh-interval-ms`, `initial-delay-ms` y `currency-pairs`; las parejas se separan por coma.

## Snapshot y restauración

`GET /ledger/snapshot` devuelve versión, `trackingStartAt`, activos propios, precios manuales, holdings, actividades y auditorías. Referencias compartidas se serializan mediante `stableCatalogId`; referencias propias usan ID del activo propio. `POST /ledger/restore` acepta el mismo formato y responde `204`. El destino debe resolver referencias estables existentes. El servicio valida versión, IDs únicos, referencias, monedas y detalles compatibles; reproduce el ledger combinado antes de guardar y reconstruye las proyecciones. Si ya existe tracking, debe coincidir con el snapshot. Restaura recursos con IDs nuevos y no borra previamente el ledger existente; depósitos/retiros restaurados generan transacciones enlazadas.

## Persistencia V5

V5 define `asset_sectors`, `shared_assets`, `user_assets`, `investment_activities`, `activity_audit`, `holdings`, `lots`, `lot_consumptions`, `positions`, `asset_prices`, `user_asset_prices`, `fx_rates`, `user_local_currency_history`, `investment_activity_requests` y `investment_tracking_settings`. `lots` y `positions` son proyecciones, `holdings` son snapshots iniciales y `activity_audit` conserva cambios. Las restricciones cubren orden único por usuario/instante, claves de idempotencia, relación usuario-lote/venta, períodos de moneda local no solapados e identidad/inmutabilidad de movimientos.
