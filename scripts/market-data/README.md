# Seed de precios históricos

Genera e importa un archivo SQL con precios diarios de las criptomonedas del
catálogo. El mismo mecanismo está preparado para reutilizarse con acciones y
ETFs más adelante.

## Contenido

| Archivo | Descripción |
|---|---|
| `crypto-assets.json` | Manifiesto de los 50 activos `CRYPTO` del catálogo, con `stable_catalog_id`, metadatos y fuentes ordenadas por preferencia. |
| `lib/providers.mjs` | Adaptadores de Binance, Yahoo Finance, Bybit, Gate, MEXC y CoinGecko. |
| `lib/seed-builder.mjs` | Mezcla de fuentes, normalización a USD (USDT ≈ USD), generación de SQL e informe de cobertura. |
| `generate-crypto-seed.mjs` | CLI del seed de criptomonedas. |
| `seeds/crypto-prices-2024-11-04_2026-11-04.sql` | Seed generado para el rango solicitado. |
| `seeds/crypto-prices-2024-11-04_2026-11-04-coverage.md` | Informe de cobertura por activo y fuente. |

## Requisitos

- Node.js 20 o superior (el repo usa Node 24).
- Acceso a las APIs públicas de los proveedores.
- PostgreSQL 13 o superior para `gen_random_uuid()`; Millete usa PostgreSQL 16.
- Para importar con Docker: el stack de Millete y el volumen de PostgreSQL.

## Generar

```bash
node scripts/market-data/generate-crypto-seed.mjs
```

Opciones:

```text
--from YYYY-MM-DD   Fecha inicial (por defecto 2024-11-04)
--to YYYY-MM-DD     Fecha final (por defecto 2026-11-04)
--manifest <ruta>   Manifiesto de activos
--out <ruta>        Archivo SQL de salida
-h, --help          Ayuda
```

Ejemplo con otro rango:

```bash
node scripts/market-data/generate-crypto-seed.mjs \
  --from 2025-01-01 --to 2025-12-31 \
  --out scripts/market-data/seeds/crypto-prices-2025.sql
```

Los precios se guardan en **USD**. Las fuentes cotizadas en USDT se tratan como
USD 1:1 (convención habitual en precios históricos de cripto); no se aplica un
FX adicional. Si `COINGECKO_API_KEY` está definida, la usa; sin clave, CoinGecko
solo permite consultar los últimos 365 días y el generador recorta el rango
automáticamente.

Salida por defecto:

```text
scripts/market-data/seeds/crypto-prices-<desde>_<hasta>.sql
scripts/market-data/seeds/crypto-prices-<desde>_<hasta>-coverage.md
```

## Importar

Recomendado:

```bash
sh manage.sh seed-prices
```

El comando arranca PostgreSQL, espera a que esté listo y ejecuta el SQL con
`ON_ERROR_STOP=1`. Para importar otro archivo:

```bash
sh manage.sh seed-prices ruta/al/seed.sql
```

Manual, sin `manage.sh`:

```bash
docker compose exec -T postgres sh -c \
  'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
  < scripts/market-data/seeds/crypto-prices-2024-11-04_2026-11-04.sql
```

En Windows/Git Bash, si `manage.sh` tiene saltos de línea CRLF:

```bash
sh manage.sh fix-scripts
```

La primera vez, el esquema debe existir. Arranca Millete al menos una vez
(`sh manage.sh start`) para que Flyway cree `shared_assets` y `asset_prices`
antes de importar el seed.

Si la base ya tiene criptos en EUR de una versión anterior del seed, el
`ON CONFLICT` no las convierte: el seed USD omite sus precios. Al tratarse de un
módulo nuevo, lo esperado es borrar esos activos de prueba (y sus precios) y
volver a importar.

## Qué hace el SQL

- Abre una transacción (`BEGIN`/`COMMIT`).
- Comprueba que existan `shared_assets` y `asset_prices`; si no, falla con un
  mensaje claro.
- Inserta los 50 `shared_assets` que falten, con
  `ON CONFLICT (stable_catalog_id) DO NOTHING`.
- Inserta los precios con
  `ON CONFLICT (shared_asset_id, price_timestamp, source) DO NOTHING`, por lo
  que se puede ejecutar varias veces sin duplicar filas.
- Usa `gen_random_uuid()` para los identificadores.
- Guarda el precio en `close` y deja `open`, `high`, `low`, `volume` y
  `adjusted_close` a `NULL`; el backend usa `close` para valorar.
- Etiqueta las fuentes como `SEED_<PROVEEDOR>_<DIVISA>` para no pisar los
  precios vivos de la aplicación.
- Fija `fetched_at` al final del rango (`2026-11-04T00:00:00Z`) para que una
  actualización viva posterior tenga prioridad.
- Solo inserta precios si el activo existente está en la misma divisa
  (`sa.currency = ...`). Si el activo ya existe en otra divisa, sus precios se
  omiten.

## Divisa y compras

El catálogo se declara en **USD** y los precios del seed también. Es la divisa
natural de la mayoría de pares cripto (`BTCUSDT`, `BTC-USD`) y evita
conversiones innecesarias.

Cuando un usuario compra una cripto con una divisa local distinta de USD, el
backend aplica el flujo `LOCAL -> USD -> cripto`:

- La compra se registra con `settlementCurrency = LOCAL` y el precio unitario en
  la divisa local.
- `TradeActivityService` busca el FX histórico `LOCAL/USD` (o su inverso) a la
  fecha de la operación y calcula el precio unitario en USD.
- La cantidad de cripto se valora con el precio del activo en USD.

El seed **no** incluye `fx_rates`. Para que la conversión funcione, el usuario
debe ejecutar el refresco de market data de la aplicación con el par
`LOCAL/USD` (por ejemplo `EUR/USD`). Sin ese FX, el backend rechaza la compra
con un error claro en lugar de inventar un tipo de cambio.

## Verificar

Resumen por activo:

```sql
SELECT
    sa.symbol,
    count(*) AS dias,
    min(p.price_timestamp) AS primero,
    max(p.price_timestamp) AS ultimo
FROM asset_prices p
JOIN shared_assets sa ON sa.id = p.shared_asset_id
WHERE p.source LIKE 'SEED%'
GROUP BY sa.symbol
ORDER BY sa.symbol;
```

Duplicados:

```sql
SELECT shared_asset_id, price_timestamp, source, count(*)
FROM asset_prices
WHERE source LIKE 'SEED%'
GROUP BY 1, 2, 3
HAVING count(*) > 1;
```

## Rollback

Borra solo los precios del seed:

```sql
DELETE FROM asset_prices WHERE source LIKE 'SEED%';
```

No borres los `shared_assets`: pueden estar referenciados por actividades,
posiciones o precios de usuario. Si quieres eliminarlos, hazlo solo después de
comprobar que no tienen referencias.

## Cobertura y limitaciones

Generación del 2026-11-04:

- 50 activos y 33.327 precios.
- 38 activos con los 731 días completos.
- 12 activos con cobertura parcial, normalmente porque se listaron después del
  inicio del rango: ASTER, BTW, CC, GRAM, HYPE, M, MORPHO, ONDO, PUMP, RAIN,
  SKY y WLFI. No se inventan precios anteriores al listado.
- `GRAM`: Yahoo `GRAM-USD` es la serie ajustada tras la redenominación de
  Toncoin; el primer precio es ~0,0028 USD. Si necesitas historia nominal de
  TON, usa otra fuente o manifiesto.
- CoinGecko público: 365 días. Para rangos anteriores hace falta una clave de
  pago; el generador recorta automáticamente sin clave.
- Las fuentes en USDT se guardan como USD 1:1. Es una aproximación suficiente
  para precios históricos, no un tipo de cambio contable.
- El seed no incluye `fx_rates`: la conversión `LOCAL -> USD` se resuelve con
  los FX que obtiene el refresco de market data de la aplicación.
- La primera vela de una fuente con apertura de subasta anómala (≥2x o ≤0,5x
  del cierre) usa el cierre del día. Evita valores como el primer precio de
  MEXC para RAIN (0,0001 frente a 0,0032 de cierre).
- El precio guardado es la apertura/snapshot de la vela diaria a las 00:00 UTC
  (o al inicio de la sesión en proveedores bursátiles). No se guarda el OHLC
  completo.

## Añadir o cambiar activos

Edita `crypto-assets.json`. Cada activo tiene esta forma:

```json
{
  "stableCatalogId": "CRYPTO:bitcoin",
  "name": "Bitcoin",
  "symbol": "BTC",
  "type": "CRYPTO",
  "currency": "USD",
  "sectorCode": "CRYPTO",
  "sectorDisplayName": "Cripto",
  "sectorCustom": false,
  "sources": [
    { "provider": "binance", "pair": "BTCUSDT", "quoteCurrency": "USDT" },
    { "provider": "yahoo", "ticker": "BTC-USD", "quoteCurrency": "USD" }
  ]
}
```

El generador exige `baseCurrency: "USD"` y `currency: "USD"` en cada activo. El
orden de `sources` expresa preferencia editorial. Las fuentes que estén a
7 días o menos de la cobertura máxima se consideran primarias; el resto
rellena únicamente los huecos. Así una fuente casi completa gana a una
secundaria, pero una fuente deslistada no bloquea a la que sigue viva.

Proveedores soportados:

| Proveedor | Identificador | Notas |
|---|---|---|
| `binance` | `pair` (`BTCUSDT`) | Velas diarias; límite de 1000 filas. |
| `yahoo` | `ticker` (`SPY`, `CRO-USD`, `HYPE32196-USD`) | Acciones, ETFs y algunas criptos. |
| `bybit` | `pair` (`MNTUSDT`) | Spot. |
| `gate` | `pair` (`M_USDT`) | Spot. |
| `mexc` | `pair` (`MUSDT`) | Spot. |
| `coingecko` | `id` (`bitcoin`) | Sin clave, últimos 365 días. |

Comprueba siempre que el ticker corresponde al activo correcto, no solo al
símbolo. Ejemplo: `M-USD` de Yahoo es MetaVerse-M, no MemeCore; MemeCore se
resuelve con Gate `M_USDT` y MEXC `MUSDT`.

## Acciones y ETFs (siguiente paso)

El mecanismo está separado en `lib/providers.mjs` y `lib/seed-builder.mjs` para
reutilizarlo:

1. Crear `equity-assets.json` con `type: STOCK`/`ETF`, `stableCatalogId`
   (`STOCK:AAPL`, `ETF:SPY`, ...), la divisa nativa del activo y fuentes Yahoo.
2. Crear `generate-equity-seed.mjs` reutilizando `fetchSource`,
   `mergeAssetPrices`, `renderSeedSql` y `renderCoverageReport`.
3. Guardar los precios en la divisa nativa del activo. El backend exige que
   `asset_prices.currency` coincida con `shared_assets.currency`; no conviertas
   a otra divisa en el seed si el activo está declarado en USD, JPY o BRL.
4. Si la divisa del activo no coincide con la divisa local del usuario, el
   backend necesita `fx_rates` para valorar. El seed de acciones deberá
   insertar tipos de cambio diarios o documentar que hay que ejecutar una
   actualización de mercado después de importar.
5. Para acciones, valorar usar `adjusted_close` si se quieren series ajustadas
   por splits y dividendos. El backend usa `close` primero y `adjusted_close`
   como fallback (`AssetPrice.valuationPrice()`).

## Mantenimiento

- No edites a mano el SQL generado; cambia el manifiesto o el generador.
- No commitees claves API. `COINGECKO_API_KEY` se lee del entorno.
- Las APIs públicas cambian y limitan peticiones. El generador reintenta
  `429`/`5xx` y espacia las llamadas a CoinGecko.
- El manifiesto se generó desde el catálogo real (`shared_assets`,
  `type = 'CRYPTO'`) y se mantiene a mano cuando cambian las fuentes.
