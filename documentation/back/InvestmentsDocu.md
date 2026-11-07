# Investments — Tracking de acciones y criptomonedas

> **Ámbito:** rama `v0.2.0`, commit `167f47a`.
> Este documento describe el comportamiento **real del código actual**. El archivo
> `InvestmentsDocu.md` de esta misma carpeta describe el modelo antiguo
> (`Investment`, CRUD con `purchasePrice`/`currentPrice`) y **ya no representa**
> el módulo de inversiones actual.

---

## 1. Qué es el "tracking"

En este módulo, *tracking* no significa "seguir un ticker" ni una lista de seguimiento.
Es un **corte temporal por usuario**: la fecha y hora desde la que el libro de
inversiones empieza a contabilizar efectivo.

- Se guarda en `investment_tracking_settings` (`user_id`, `tracking_start_at`).
- Es **por usuario** e **inmutable** una vez configurado.
- Sin tracking configurado, el módulo deja usar **Activos** y **Estado**, pero
  bloquea Cartera, Actividad, Rentabilidad y Posiciones iniciales.
- Las operaciones anteriores a `trackingStartAt` **sí reconstruyen posiciones**
  (lotes), pero **no mueven efectivo**: `cashTracked = !occurredAt.isBefore(trackingStartAt)`
  en `PortfolioReplay`.

En otras palabras: el tracking separa "lo que ya tenías" de "lo que el módulo
empieza a gestionar como caja".

### Flujo general

```
                    ┌──────────────────────────────┐
                    │ POST /investments/tracking   │
                    │ trackingStartAt (una vez)    │
                    └──────────────┬───────────────┘
                                   │
   actividades + holdings ─────────┼─────────► PortfolioReplay
                                   │              │
                                   │              ├─► lotes / consumos
                                   │              ├─► posiciones
                                   │              └─► saldos de efectivo
                                   │
   shared_assets ──► refresh ──► asset_prices ──┐
   (catálogo)        (auto/manual)              ├─► valoración de cartera
                      └──────► fx_rates ────────┘
                                   │
                                   └─► InvestmentHealthService (incidencias)
```

---

## 2. Configuración del tracking

### 2.1 Persistencia

Migración: `backend/src/main/resources/db/migration/V5__v0.2.0.sql` (tabla
`investment_tracking_settings`, líneas ~1042).

El repositorio de salida es `InvestmentTrackingSettingsRepository`:

```java
Optional<Instant> findTrackingStartAt(UUID userId);
void saveTrackingStartAt(UUID userId, Instant trackingStartAt);
```

### 2.2 Reglas de negocio

`InvestmentTrackingService.configure(...)`:

1. Valida `userId` y `trackingStartAt` no nulos.
2. Adquiere un **lock de asesoría de PostgreSQL por usuario**
   (`pg_advisory_xact_lock`) mediante `InvestmentPortfolioLockPort`, para
   serializar la configuración con las reconstrucciones de cartera.
3. Si ya existe un `trackingStartAt`, lanza `InvalidInputException`
   ("El tracking de Investments ya está configurado.").
4. Guarda la fecha.

**No hay endpoint de actualización ni de borrado.** La inmutabilidad se
garantiza por ausencia de operación, no por una restricción de base de datos.

> **Punto débil:** el backend **no valida que la fecha no sea futura**. Solo la
> interfaz (`TrackingSetupDialog`) lo comprueba. Una llamada directa a la API
> puede configurar un tracking en el futuro y dejarlo bloqueado para siempre.

### 2.3 API

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/v1/investments/tracking` | `200 { configured, trackingStartAt }` (si no hay, `configured=false` y `trackingStartAt=null`) |
| `POST` | `/api/v1/investments/tracking` | `204 No Content` |

Body del `POST`:

```json
{ "trackingStartAt": "2026-01-01T00:00:00Z" }
```

El `userId` se obtiene siempre de `JwtUser`; nunca de parámetros.

### 2.4 Interfaz

- `TrackingSetupDialog` envía la fecha local a medianoche
  (`new Date(fecha + 'T00:00:00')`) y bloquea fechas futuras.
- El aviso es explícito: la fecha no se puede cambiar después.
- En `pages/page.tsx`, las pestañas bloqueadas son:

```ts
const GATED_TABS = ['portfolio', 'activity', 'performance', 'holdings'];
```

Si no hay tracking, la pestaña por defecto es `assets`. Las pestañas `assets` y
`status` siguen accesibles.

---

## 3. Activos: `SHARED` vs `USER`

Una posición se refiere a un activo mediante `AssetReference`:

| `kind` | Entidad | Ámbito | Precio | Refresco automático |
|---|---|---|---|---|
| `SHARED` | `SharedAsset` | Catálogo global | `asset_prices` | Sí (Yahoo / CoinGecko) |
| `USER` | `UserAsset` | Por usuario | `user_asset_prices` | No: se registra a mano |

### 3.1 `SharedAsset`

- Tipos permitidos por el dominio: **`STOCK`, `ETF`, `CRYPTO`**.
- Requiere `symbol` (se normaliza a mayúsculas), sector del catálogo (no custom)
  y divisa.
- Es global: todos los usuarios ven el mismo catálogo y los mismos precios.
- Tabla `shared_assets`; índices por nombre, símbolo, tipo y divisa.

### 3.2 `UserAsset`

- Tipos: `STOCK`, `CRYPTO`, `FUND`, `ETF`, `REAL_ESTATE`, `OTHER`.
- Su divisa es la divisa local del usuario en el momento de crearlo.
- Se valoran con `user_asset_prices`, que el usuario registra manualmente.
- Endpoints: `POST/PUT/GET /investments/user-assets`,
  `POST /investments/user-assets/{id}/prices`.

### 3.3 El catálogo compartido está vacío y no hay forma de poblarlo desde la app

Esto es importante y no es obvio al leer la interfaz:

- `GET /investments/shared-assets` es **solo lectura**. No existe
  `POST /investments/shared-assets` ni un caso de uso de creación expuesto.
- `SharedAssetCatalogService` únicamente busca y obtiene activos.
- `SharedAsset.create(...)` existe en el dominio, pero **no lo invoca ningún
  servicio ni controlador**.
- Las migraciones Flyway **no insertan filas** en `shared_assets`; solo siembran
  `asset_sectors`.
- La restauración de un snapshot **tampoco crea** los `SharedAsset`: los busca
  por `stableCatalogId` y falla si no existen.

**Consecuencia:** en una base de datos nueva, el catálogo compartido está vacío,
el panel "Catálogo de mercado" no muestra nada y el refresh de precios no tiene
nada que pedir. Solo se pueden usar activos `USER` con precios manuales.

Para probar el flujo de precios automáticos hay que insertar los `SharedAsset`
a mano (o mediante una migración/seed propia). Ejemplo:

```sql
INSERT INTO shared_assets (
    id, stable_catalog_id, name, symbol, type,
    sector_code, sector_display_name, sector_custom,
    currency, created_at, modified_at
) VALUES (
    gen_random_uuid(),
    'yahoo:AAPL',
    'Apple Inc.',
    'AAPL',
    'STOCK',
    'TECHNOLOGY',
    'Technology',
    FALSE,
    'USD',
    now(),
    now()
);
```

Los códigos de sector sembrados están en `V5__v0.2.0.sql` (`TECHNOLOGY`,
`HEALTHCARE`, `FINANCIALS`, `CONSUMER`, `INDUSTRIALS`, `ENERGY`, `UTILITIES`,
`MATERIALS`, `REAL_ESTATE`, `COMMUNICATIONS`, `OTHER`).

### 3.4 Restaurar un snapshot en una base vacía

`InvestmentLedgerSnapshot` **no incluye** los `SharedAsset` (solo referencias por
`stableCatalogId`). Por tanto, restaurar un libro con activos compartidos en una
base sin catálogo falla con:

```
No existe un SharedAsset con stableCatalogId <id>
```

Antes de restaurar hay que crear esos `SharedAsset` con el mismo
`stable_catalog_id`. El snapshot sí incluye `trackingStartAt`; si ya hay uno
configurado y no coincide, la restauración se rechaza.

---

## 4. Proveedores de market data

El puerto de salida es `MarketDataProviderPort`. La implementación activa es
`CompositeMarketDataProviderAdapter`, que reparte cada activo entre las fuentes
(`MarketDataSourcePort`) según `supports(asset)`.

### 4.1 Fuentes

| Fuente | `supports(asset)` | `supportsFxRates()` | Requisitos |
|---|---|---|---|
| `YahooFinanceMarketDataAdapter` | `STOCK`, `ETF` | `true` | Ninguno (endpoint no oficial) |
| `CoinGeckoMarketDataAdapter` | `CRYPTO` | `false` | `COINGECKO_API_KEY` + `COINGECKO_ID_MAP` |
| `FinnhubMarketDataAdapter` | **siempre `false`** | `false` | `FINNHUB_API_KEY` (sin uso práctico) |

#### Yahoo Finance

- `GET {base}/v8/finance/chart/{symbol}?period1=&period2=&interval=1d`
- Cabecera `User-Agent: Mozilla/5.0`.
- Sin API key. Endpoint **no oficial**: puede cambiar sin aviso.
- Guarda `close` como precio; no rellena `adjustedClose`.
- La divisa del precio se toma de la divisa declarada en el `SharedAsset`; Yahoo
  no convierte. Si el activo está mal declarado (p. ej. un valor en USD marcado
  como EUR), la valoración será incorrecta y la salud no lo detectará.
- **Tipos de cambio:** usa el símbolo `BASEQUOTE=X` (p. ej. `EURUSD=X`,
  `JPYUSD=X`). Yahoo resuelve el par directo, por lo que el `rate` guardado es
  "1 unidad de BASE = rate unidades de QUOTE". No necesita clave y sustituye a
  Finnhub como única fuente FX (`supportsFxRates() == true`).
- El cierre diario de Yahoo para divisas viene fechado a las 00:00 UTC; no hay
  velas de sábado ni domingo.

#### CoinGecko

- `GET {base}/coins/{coinId}/market_chart/range?vs_currency=<divisa>&from=&to=`
- Cabecera configurable; por defecto `x-cg-demo-api-key`.
- Exige `COINGECKO_API_KEY`; si falta, lanza `MarketDataProviderException`.
- Exige un mapa `COINGECKO_ID_MAP` con formato `TICKER=coingecko-id`, separado
  por comas. Ejemplo: `BTC=bitcoin,ETH=ethereum`.
- Si el ticker no está en el mapa, lanza excepción y **aborta el refresh**.
- Pide los precios en la divisa del activo (`vs_currency`), por lo que sí
  respeta la divisa declarada (si CoinGecko la soporta).

#### Finnhub

- Finnhub **no se usa**: `supports(asset)` y `supportsFxRates()` devuelven
  `false`. Su endpoint de FX (`/forex/candle`) es de pago y responde `403` con el
  plan gratuito, por eso los tipos de cambio los sirve Yahoo Finance.
- El código de velas de acciones (`/stock/candle`) queda como referencia para
  una futura fuente alternativa; hoy es inalcanzable desde el composite.

### 4.2 Selección y errores del composite

- Para cada `SharedAsset` debe existir **exactamente una** fuente que lo soporte.
  Si hay cero o más de una, lanza `IllegalStateException` y el refresh falla.
- Para FX debe existir **exactamente una** fuente con `supportsFxRates=true`.
  Hoy solo Yahoo Finance cumple.
- Si en `shared_assets` se inserta por SQL un tipo distinto de `STOCK`, `ETF` o
  `CRYPTO` (la tabla lo permite, el dominio no), ningún proveedor lo soportará y
  el refresh fallará.
- El cliente HTTP (`RetryingMarketDataHttpClient`) hace como máximo **3
  intentos**, con espera de `250 ms × intento`. Reintenta `429`, `5xx` y errores
  de red; un `4xx` distinto de `429` falla inmediatamente.
- Aplica rate limit secuencial: Yahoo `1 s`, CoinGecko `1 s`, Finnhub `250 ms`
  entre peticiones. Es un proceso **secuencial**, no paralelo.

---

## 5. Configuración

Todo se inyecta con `@Value`. **No hay sección `app.investments` en
`application.yml` ni variables de estas en `docker-compose.yml`**: si no se
definen, se usan los valores por defecto. En Spring Boot pueden pasarse como
variables de entorno con *relaxed binding*.

| Propiedad | Variable de entorno | Valor por defecto |
|---|---|---|
| `app.investments.market-data.refresh-interval-ms` | `APP_INVESTMENTS_MARKET_DATA_REFRESH_INTERVAL_MS` | `21600000` (6 h) |
| `app.investments.market-data.initial-delay-ms` | `APP_INVESTMENTS_MARKET_DATA_INITIAL_DELAY_MS` | `60000` (1 min) |
| `app.investments.market-data.currency-pairs` | `APP_INVESTMENTS_MARKET_DATA_CURRENCY_PAIRS` | vacío |
| `app.investments.health.check-interval-ms` | `APP_INVESTMENTS_HEALTH_CHECK_INTERVAL_MS` | `21600000` (6 h) |
| `app.investments.health.initial-delay-ms` | `APP_INVESTMENTS_HEALTH_INITIAL_DELAY_MS` | `120000` (2 min) |
| `app.investments.market-data.max-age` | `APP_INVESTMENTS_MARKET_DATA_MAX_AGE` | `PT72H` |
| `app.investments.market-data.yahoo.base-url` | — | `https://query1.finance.yahoo.com` |
| `app.investments.market-data.yahoo.minimum-interval` | — | `PT1S` |
| `app.investments.market-data.coingecko.base-url` | — | `https://api.coingecko.com/api/v3` |
| `app.investments.market-data.coingecko.api-key-header` | — | `x-cg-demo-api-key` |
| `app.investments.market-data.coingecko.minimum-interval` | — | `PT1S` |
| `app.investments.market-data.finnhub.base-url` | — | `https://finnhub.io/api/v1` |
| `app.investments.market-data.finnhub.minimum-interval` | — | `PT0.25S` |
| `FINNHUB_API_KEY` | `FINNHUB_API_KEY` | vacío |
| `COINGECKO_API_KEY` | `COINGECKO_API_KEY` | vacío |
| `COINGECKO_ID_MAP` | `COINGECKO_ID_MAP` | vacío |

`currency-pairs` se parsea separando por comas y limpiando espacios:
`USD/EUR,EUR/USD`. Si está vacío, el scheduler **no actualiza FX**.

---

## 6. Refresco de datos de mercado

### 6.1 Automático

`InvestmentMarketDataScheduler`:

- `@Scheduled(fixedDelay = refresh-interval-ms, initialDelay = initial-delay-ms)`.
- Ventana fija de **5 días** hacia atrás desde `TimeProvider.now()`.
- Usa los `currency-pairs` configurados.
- Captura cualquier `RuntimeException` y solo lo registra como error: **no
  relanza**, por lo que el scheduler sigue vivo.
- El refresh es **global**: recorre todos los `SharedAsset` y guarda precios
  globales, no por usuario.

`MarketDataRefreshService.refresh(command)`:

1. Valida `from < to`, `to` no nulo y `currencyPairs` no nulo.
2. Carga **todos** los `SharedAsset`.
3. Pide precios de todos ellos en una sola llamada al composite.
4. Valida que cada precio corresponda a un activo solicitado y que su divisa
   coincida con la del activo.
5. Guarda con upsert.
6. Si hay pares, pide y valida FX y los guarda.

Si un proveedor falla, **toda la operación de precios se aborta** y no se
intenta FX. No hay guardado parcial por activo.

### 6.2 Manual

| Método | Ruta | Respuesta |
|---|---|---|
| `POST` | `/api/v1/investments/market-data/refresh` | `204` |
| `GET` | `/api/v1/investments/shared-assets/{id}/prices?from=&to=&page=&size=` | página de precios |
| `GET` | `/api/v1/investments/fx-rates?baseCurrency=&quoteCurrency=&from=&to=&page=&size=` | página de tipos |

Body del refresh:

```json
{
  "from": "2026-01-01T00:00:00Z",
  "to": "2026-01-31T23:59:59Z",
  "currencyPairs": ["USD/EUR"]
}
```

- `from` y `to` son obligatorios; `currencyPairs` es obligatorio (puede ser
  `[]`), máximo 100 elementos y 20 caracteres por par.
- Un fallo de proveedor se traduce a **`502 Bad Gateway`** con el mensaje del
  proveedor. Una entrada inválida, a `400`.
- No hay límite de rango ni de número de peticiones por usuario.
- El endpoint está autenticado, pero **afecta al catálogo global**: cualquier
  usuario puede lanzar un refresh que impacta a todos.

En la interfaz, `MarketDataRefreshDialog`:

- Rango inicial: **últimos 30 días** (distinto de los 5 días del scheduler).
- Permite añadir pares `BASE/QUOTE`.
- Avisa de que la consulta a proveedores externos puede tardar varios minutos.

### 6.3 Idempotencia

El upsert de `JpaAssetPriceRepository` usa:

```sql
ON CONFLICT (shared_asset_id, price_timestamp, source)
DO UPDATE SET ...
```

Por tanto, repetir un refresh actualiza los mismos registros en lugar de
duplicarlos. Lo mismo aplica a FX en su repositorio.

### 6.4 Huecos históricos

El scheduler solo pide los últimos 5 días en cada ejecución. Si la aplicación
está apagada más de 5 días, al arrancar **no rellena automáticamente el hueco**;
solo cubrirá desde `ahora - 5 días`. Para backfill hay que usar el refresh manual
con un rango mayor. Tampoco se rellena automáticamente desde `trackingStartAt`.

---

## 7. Almacenamiento y selección de precio

### 7.1 Precios de activos

Tabla `asset_prices` (migración `V5__v0.2.0.sql`), clave única lógica
`(shared_asset_id, price_timestamp, source)`.

`AssetPrice` contiene: `timestamp`, `open`, `high`, `low`, `close`,
`adjustedClose` (nullable), `volume`, `currency`, `source`, `fetchedAt`.

Precio de valoración (`AssetPrice.valuationPrice()`):

```java
return close != null ? close : adjustedClose;
```

### 7.2 Selección del último precio

`JpaAssetPriceRepository.findLatestCandidates`:

```sql
WHERE shared_asset_id = :id
  AND timestamp <= :at
  AND (close IS NOT NULL OR adjusted_close IS NOT NULL)
ORDER BY timestamp DESC, fetched_at DESC, source ASC, id DESC
```

El adaptador se queda con el primero. Es decir: el precio **más reciente cuya
fecha no supere el instante consultado**; si hay empate, gana el `fetchedAt` más
nuevo.

### 7.3 Tipos de cambio

`fx_rates` guarda `(base_currency, quote_currency, timestamp, rate, source,
fetched_at)`. La conversión en `PortfolioQueryService`:

1. Si la divisa coincide con la local, no hay conversión.
2. Intenta el par directo `origen → local`.
3. Si no existe, intenta el inverso `local → origen` y divide.
4. Si no hay ninguno, la posición queda `NOT_CALCULABLE`.

En efectivo, si falta el FX de una divisa, esa divisa se omite y el total se
marca como `estimated=true`.

---

## 8. Valoración de la cartera

`PortfolioQueryService.getPortfolioAt(userId, asOf)`:

1. Lee el `trackingStartAt`; si no existe, lanza `IllegalStateException`.
2. Filtra actividades con `occurredAt <= asOf` y holdings con
   `snapshotAt <= asOf`.
3. Ejecuta `PortfolioReplay` para obtener lotes, consumos, posiciones y caja.
4. Para cada posición:
   - `SHARED`: busca el último precio `<= asOf`; exige que la divisa coincida.
   - `USER`: busca el último `user_asset_price <= asOf`; exige que la divisa
     coincida.
   - Multiplica precio × cantidad en la divisa del activo.
   - Convierte a divisa local con FX directo o inverso.
   - Si falta precio, divisa o FX, devuelve `NOT_CALCULABLE` con un motivo.
5. El coste de adquisición se convierte a divisa local **en el mismo `asOf`**,
   no en la fecha de compra. Por eso la ganancia no realizada de la cartera
   mezcla efecto precio y efecto divisa; la pestaña de Rentabilidad es la que
   separa ambos efectos.

El `asOf` lo fija el frontend **al montar la página** y no cambia hasta
recargar. Un refresh manual invalida las queries, pero la nueva valoración se
calcula contra ese mismo `asOf`, no contra "ahora".

### 8.1 Replay del libro

`PortfolioReplay.replay(...)`:

- Ordena actividades por `occurredAt`, `orderingKey` e `id`.
- Procesa compras, ventas, dividendos, depósitos, retiradas, cambios, splits,
  efectivo inicial y posiciones iniciales.
- Las ventas consumen lotes en **FIFO** (`acquiredAt`, `acquisitionOrder`, `id`).
- `cashTracked` es `false` para actividades anteriores a `trackingStartAt`.
- Las posiciones iniciales (`Holding`) aportan lotes con su cantidad y coste
  congelados.
- Puede lanzar `LedgerIntegrityException` con códigos como
  `POSITION_REQUIRED`, `POSITION_NEGATIVE`, `INCONSISTENT_CURRENCY`,
  `CASH_NEGATIVE`, `USER_MISMATCH`, `MULTIPLE_ACTIVE_HOLDINGS` o
  `HOLDING_ACTIVITY_CONFLICT`.

El resultado se persiste en tablas derivadas (`lots`, `lot_consumptions`,
`positions`) mediante `PortfolioRebuildService.rebuild(userId)`, que se ejecuta
tras cada mutación del libro (actividades, holdings, historial, restauración).
Sin embargo, las consultas de cartera, caja y rentabilidad **vuelven a ejecutar
el replay en memoria** para el instante solicitado; en el código actual las
proyecciones persistidas no son la fuente de lectura de esas consultas.

### 8.2 Liquidación de compras y ventas

El precio de una compra o venta se introduce **siempre en la divisa del activo**
(`SharedAsset.currency` / `UserAsset.currency`; p. ej. USD para cripto). El
importe se calcula como `cantidad × precio` en esa divisa y, si difiere de la
divisa local del usuario, se convierte con el FX histórico
(`assetCurrency → localCurrency`) para liquidar contra la caja local:

- Una **compra** descuenta el importe convertido de la caja en divisa local.
- Una **venta** suma el importe convertido a la caja en divisa local.
- Si el importe a pagar supera el saldo local disponible, el replay rechaza la
  operación con `CASH_NEGATIVE` y el mensaje
  "El valor a pagar es mayor que el saldo disponible".

El campo `settlementCurrency` ya no se usa al crear ni editar compras/ventas;
se conserva en las actividades históricas y en el flujo de reconstrucción de
posiciones iniciales (`holdings/{id}/history`).

---

## 9. Salud de los datos

### 9.1 Cuándo se ejecuta

- **Scheduler:** cada 6 h (por defecto), tras 2 min de espera inicial.
  Recorre los usuarios que aparecen en `investment_activities`, `holdings` o
  `user_assets` (unión). No incluye usuarios que solo tengan tracking.
- **Bajo demanda:** `GET /api/v1/investments/health` **ejecuta la comprobación
  en el momento** (no devuelve un informe cacheado) y reconcilia notificaciones
  del sistema.

`InvestmentHealthService` deduplica por `code + resourceId`, ordena por código y
`resourceId`, y publica las incidencias como notificaciones con origen
`investments-health` y título "Aviso de salud de inversiones".

### 9.2 Códigos

| Código | Severidad | Cuándo |
|---|---|---|
| `TRACKING_NOT_CONFIGURED` | `error` | No hay `trackingStartAt` |
| `LOCAL_CURRENCY_MISSING` | `warning` | El usuario no tiene divisa local |
| `ASSET_REFERENCE_MISSING` | `error` | Una actividad/holding apunta a un activo inexistente o ajeno |
| `SECTOR_MISSING` | `warning` | El activo no tiene sector |
| `PRICE_MISSING` | `warning` | No hay precio `<= now` con `close` o `adjustedClose` |
| `PRICE_CURRENCY_MISMATCH` | `error` | La divisa del precio no coincide con la del activo |
| `PRICE_STALE` | `warning` | `timestamp + max-age < now` |
| `FX_MISSING` | `warning` | No hay par directo ni inverso |
| `FX_STALE` | `warning` | El más reciente de directo/inverso supera `max-age` |
| `CASH_NEGATIVE` | `error` | Saldo de caja negativo en una divisa |
| `LEDGER_INVALID` | `error` | El replay falla por una excepción no clasificada |
| `TRANSFER_OUT_OF_SYNC` | `error` | Un `DEPOSIT`/`WITHDRAW` no tiene una transferencia vinculada válida |
| Códigos de `LedgerIntegrityException` | `error` | Integridad del libro (ver §8.1) |

Notas:

- La antigüedad se mide sobre el **`timestamp` de la vela**, no sobre
  `fetchedAt`. Un precio descargado hoy pero referido a un viernes antiguo
  seguirá apareciendo como `PRICE_STALE`. Fines de semana y festivos pueden
  disparar el aviso con `PT72H`.
- `TRANSFER_OUT_OF_SYNC` es especialmente estricto: **todo** depósito o retirada
  registrado en inversiones debe estar vinculado a una transacción del módulo de
  finanzas. Un movimiento manual sin vínculo aparecerá como error.
- `GET /health` es síncrono y escribe notificaciones; no es un endpoint de solo
  lectura barato.

### 9.3 Interfaz

- `HealthBanner` solo se muestra si el tracking está configurado. Enseña hasta 3
  incidencias ordenadas por severidad y enlaza a `Estado`.
- `StatusPanel` muestra el estado del tracking, las incidencias, el refresh
  manual y las acciones de copia/restauración del libro.
- `staleTime` de las queries: tracking y salud `60 s`; cartera `30 s`; precios
  `2 min`; catálogo `10 min`; sectores `Infinity`.

---

## 10. Endpoints relevantes

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/v1/investments/tracking` | Consultar tracking |
| `POST` | `/api/v1/investments/tracking` | Configurar tracking (una vez) |
| `GET` | `/api/v1/investments/shared-assets` | Buscar catálogo compartido |
| `GET` | `/api/v1/investments/shared-assets/{id}` | Detalle de activo compartido |
| `GET` | `/api/v1/investments/shared-assets/{id}/prices` | Histórico de precios |
| `GET` | `/api/v1/investments/fx-rates` | Histórico de tipos de cambio |
| `POST` | `/api/v1/investments/market-data/refresh` | Refresh manual global |
| `GET` | `/api/v1/investments/health` | Comprobar salud (ejecuta) |
| `GET` | `/api/v1/investments/portfolio?asOf=` | Cartera valorada |
| `GET` | `/api/v1/investments/cash?asOf=` | Saldos de efectivo |
| `GET` | `/api/v1/investments/performance?from=&to=` | Rentabilidad |
| `GET` | `/api/v1/investments/ledger/snapshot` | Copia del libro |
| `POST` | `/api/v1/investments/ledger/restore` | Restaurar copia |

Todos derivan el usuario de `JwtUser`; ninguno acepta `userId` por parámetro.

---

## 11. Limitaciones y puntos débiles actuales

1. **Catálogo compartido vacío y sin escritura.** No hay seed ni endpoint de
   creación; sin intervención manual en BD no hay activos `SHARED`, ni precios
   automáticos, ni forma de probar Yahoo/CoinGecko desde la app.
2. **Restaurar un snapshot no recrea el catálogo.** Un libro con referencias
   `SHARED` falla en una base sin esos `stableCatalogId`.
3. **CoinGecko no funciona sin configuración explícita.** Sin
   `COINGECKO_API_KEY` y `COINGECKO_ID_MAP`, cualquier activo `CRYPTO` aborta el
   refresh completo.
4. **Finnhub no aporta nada.** `supports(asset)` y `supportsFxRates()` devuelven
   `false`: ni precios de acciones ni FX. Los tipos de cambio los sirve Yahoo.
5. **El refresh es global y todo-o-nada.** Un activo problemático o un proveedor
   caído impide guardar los precios del resto y evita el refresco de FX.
6. **Huecos históricos.** El scheduler solo cubre 5 días; tras una parada larga
   no hay backfill automático.
7. **Proceso secuencial y lento.** Rate limit por petición y hasta 3 intentos;
   un catálogo grande puede tardar minutos.
8. **`asOf` congelado al montar la página.** Los valores no se actualizan solos
   hasta recargar.
9. **Tracking sin validación de futuro en backend.** La fecha es inmutable y
   puede quedar mal configurada de forma permanente.
10. **La divisa del `SharedAsset` se confía.** Yahoo no convierte; una divisa mal
    declarada produce valoraciones incorrectas sin aviso de salud.
11. **Salud agresiva con transferencias.** Todo `DEPOSIT`/`WITHDRAW` sin vínculo
    a una transacción se marca como error.
12. **Rentabilidad no rechaza `from < trackingStartAt`.** El backend solo valida
    `from < to`; la interfaz recorta el rango, pero una llamada directa puede
    obtener métricas menos comparables.
13. **`GET /health` ejecuta y escribe.** No es un simple lector; llamarlo en
    bucle reconcilia notificaciones cada vez.
14. **Documentación antigua.** `InvestmentsDocu.md` describe el modelo anterior y
    no debe usarse como referencia.

---

## 12. Checklist de diagnóstico

| Síntoma | Qué revisar |
|---|---|
| "El catálogo compartido está vacío" | `SELECT count(*) FROM shared_assets;`. No hay UI de alta: insertar filas o seed. |
| Refresh `502` con "COINGECKO_API_KEY is required" | Definir `COINGECKO_API_KEY` y `COINGECKO_ID_MAP`. |
| Refresh `502` con "No CoinGecko ID is configured" | El ticker del `SharedAsset` no está en `COINGECKO_ID_MAP`. |
| Refresh `502` con "FINNHUB_API_KEY is required" | Ya no aplica a FX: Yahoo no necesita clave. Si aparece, hay código antiguo desplegado. |
| No se actualizan precios | Catálogo vacío, proveedor caído, o tipos no soportados (`FUND`, etc.). |
| `PRICE_MISSING` | No hay precio `<= now`; lanzar refresh con un rango que cubra la fecha. |
| `PRICE_STALE` | Última vela demasiado antigua; revisar fin de semana/festivo o refresh. |
| `FX_MISSING` / `FX_STALE` | Añadir el par `BASE/QUOTE` en el refresh manual (Yahoo) o importar el seed de `fx_rates`. |
| `TRANSFER_OUT_OF_SYNC` | Depósito/retirada sin transacción vinculada. |
| Restore falla por `SharedAsset` | Crear primero los `SharedAsset` con el mismo `stable_catalog_id`. |
| Cartera "No calculable" | Falta precio, divisa del precio distinta o falta FX. |
| Valores no cambian tras refrescar | `asOf` está fijado al montar; recargar la página. |

---

## 13. Mapa de archivos

### Backend

| Archivo | Responsabilidad |
|---|---|
| `investments/application/services/InvestmentTrackingService.java` | Configuración inmutable del tracking |
| `investments/infrastructure/in/controller/InvestmentTrackingApiController.java` | `GET/POST /tracking` |
| `investments/application/services/PortfolioRebuildService.java` | Replay y proyecciones tras mutaciones |
| `investments/domain/model/PortfolioReplay.java` | Replay FIFO, caja y validaciones de integridad |
| `investments/application/services/PortfolioQueryService.java` | Valoración de posiciones, caja y FX |
| `investments/application/services/MarketDataRefreshService.java` | Orquestación del refresh |
| `investments/application/services/InvestmentMarketDataScheduler.java` | Refresh automático cada 6 h |
| `investments/application/services/MarketDataQueryService.java` | Consulta de precios y FX |
| `investments/application/services/InvestmentHealthService.java` | Incidencias y umbrales |
| `investments/application/services/InvestmentHealthScheduler.java` | Salud automática cada 6 h |
| `investments/infrastructure/out/marketdata/*` | Yahoo, CoinGecko, Finnhub, composite y reintentos |
| `investments/infrastructure/out/persistence/postgresql/repository/JpaAssetPriceRepository.java` | Último precio y upsert |
| `investments/infrastructure/in/controller/MarketDataApiController.java` | Precios, FX y refresh manual |
| `investments/infrastructure/in/controller/InvestmentHealthApiController.java` | `GET /health` |
| `dataexport/.../InvestmentImportAdapter.java` | Importación del libro (no crea `SharedAsset`) |

### Frontend

| Archivo | Responsabilidad |
|---|---|
| `features/investments/pages/page.tsx` | Gate de tracking y pestañas |
| `features/investments/components/TrackingSetupDialog.tsx` | Alta del tracking |
| `features/investments/components/MarketDataRefreshDialog.tsx` | Refresh manual |
| `features/investments/components/HealthBanner.tsx` | Avisos de salud |
| `features/investments/components/StatusPanel.tsx` | Estado, refresh y libro |
| `features/investments/components/AssetsPanel.tsx` | Catálogo compartido y activos propios |
| `features/investments/hooks/useInvestmentQueries.ts` | Claves y `staleTime` |
| `features/investments/services/investments.service.ts` | Llamadas a la API |
