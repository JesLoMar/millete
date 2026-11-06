/**
 * Utilidades genéricas para construir seeds SQL de precios históricos.
 *
 * El módulo no conoce proveedores concretos: recibe las filas ya descargadas y
 * se encarga de mezclarlas, convertir divisas cuando procede y renderizar el
 * SQL y el informe de cobertura.
 */

import { sourceLabel } from './providers.mjs';

const DAY_MS = 86_400_000;

export function toEpochMs(date) {
    return Date.parse(`${date}T00:00:00Z`);
}

export function toDateKey(epochMs) {
    return new Date(epochMs).toISOString().slice(0, 10);
}

export function addDays(date, days) {
    return toDateKey(toEpochMs(date) + days * DAY_MS);
}

export function daysBetween(from, to) {
    return Math.round((toEpochMs(to) - toEpochMs(from)) / DAY_MS) + 1;
}

export function eachDate(from, to) {
    const dates = [];
    const end = toEpochMs(to);

    for (let cursor = toEpochMs(from); cursor <= end; cursor += DAY_MS) {
        dates.push(toDateKey(cursor));
    }

    return dates;
}

export function validateDate(value, label) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        throw new Error(`${label} debe tener el formato YYYY-MM-DD: ${value}`);
    }

    const parsed = toEpochMs(value);

    if (!Number.isFinite(parsed) || toDateKey(parsed) !== value) {
        throw new Error(`${label} no es una fecha válida: ${value}`);
    }
}

function convertToAssetCurrency(row, source, asset) {
    if (source.quoteCurrency === asset.currency) {
        return row.open;
    }

    // USDT se considera 1:1 con USD. Es la convención habitual para precios
    // históricos de cripto y evita introducir un FX adicional en el seed.
    if (asset.currency === 'USD' && source.quoteCurrency === 'USDT') {
        return row.open;
    }

    return null;
}

/**
 * Mezcla las fuentes de un activo.
 *
 * Regla de prioridad:
 *   1. Las fuentes que estén a 7 días o menos de la cobertura máxima se
 *      consideran primarias y se ordenan por el orden del manifiesto (que
 *      expresa la preferencia editorial).
 *   2. Las fuentes con menos cobertura rellenan únicamente los huecos.
 *
 * Así se evita que una fuente secundaria parcial desplace a una fuente casi
 * completa, pero se usa la fuente más larga cuando la diferencia es grande
 * (por ejemplo, un par deslistado frente a un par USDT vivo).
 */
export function mergeAssetPrices(asset, fetchedSources) {
    const prepared = fetchedSources
        .filter((entry) => entry.rows.length > 0)
        .map((entry, index) => ({
            ...entry,
            index,
            label: sourceLabel(entry.source),
        }));

    if (prepared.length === 0) {
        return {
            rows: [],
            sourceCounts: {},
            conversionFailures: [],
        };
    }

    const maxCoverage = Math.max(...prepared.map((entry) => entry.rows.length));
    const ordered = [...prepared].sort((a, b) => {
        const aPrimary = a.rows.length >= maxCoverage - 7 ? 0 : 1;
        const bPrimary = b.rows.length >= maxCoverage - 7 ? 0 : 1;

        return aPrimary - bPrimary || a.index - b.index;
    });

    const byDate = new Map();
    const conversionFailures = [];

    for (const entry of ordered) {
        for (const [rowIndex, row] of entry.rows.entries()) {
            if (byDate.has(row.date)) {
                continue;
            }

            // La primera vela de un activo recién listado suele abrir en el
            // precio de subasta (a veces 10-30 veces por debajo del mercado).
            // En ese caso usamos el cierre del día, que ya refleja el precio
            // de mercado consolidado.
            const isExtremeFirstCandle = rowIndex === 0
                && Number.isFinite(row.close)
                && row.close > 0
                && (row.close / row.open >= 2 || row.close / row.open <= 0.5);
            const effectiveOpen = isExtremeFirstCandle ? row.close : row.open;
            const price = convertToAssetCurrency(
                { ...row, open: effectiveOpen },
                entry.source,
                asset,
            );

            if (!Number.isFinite(price) || price <= 0) {
                conversionFailures.push({
                    date: row.date,
                    source: entry.label,
                    quoteCurrency: entry.source.quoteCurrency,
                    assetCurrency: asset.currency,
                });
                continue;
            }

            byDate.set(row.date, {
                date: row.date,
                price,
                source: entry.label,
            });
        }
    }

    const rows = [...byDate.values()].sort((a, b) => a.date.localeCompare(b.date));
    const sourceCounts = {};
    const unresolvedFailures = conversionFailures.filter(
        (failure) => !byDate.has(failure.date),
    );

    for (const row of rows) {
        sourceCounts[row.source] = (sourceCounts[row.source] ?? 0) + 1;
    }

    return {
        rows,
        sourceCounts,
        conversionFailures: unresolvedFailures,
    };
}

function sqlLiteral(value) {
    return `'${String(value).replace(/'/g, "''")}'`;
}

function formatPrice(value) {
    if (!Number.isFinite(value) || value <= 0) {
        throw new Error(`Precio no válido: ${value}`);
    }

    const fixed = value.toFixed(12);

    if (fixed === '0.000000000000') {
        throw new Error(
            `El precio ${value} es demasiado pequeño para DECIMAL(28,12) y se perdería precisión`,
        );
    }

    return fixed;
}

function formatInteger(value) {
    return String(value).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

function formatPercent(value, total) {
    if (total === 0) {
        return '0,0%';
    }

    return `${((value / total) * 100).toFixed(1).replace('.', ',')}%`;
}

function renderAssetInsert(asset, fetchedAt) {
    return `    (gen_random_uuid(), ${sqlLiteral(asset.stableCatalogId)}, ${sqlLiteral(asset.name)}, `
        + `${sqlLiteral(asset.symbol)}, ${sqlLiteral(asset.type)}, ${sqlLiteral(asset.sectorCode)}, `
        + `${sqlLiteral(asset.sectorDisplayName)}, ${asset.sectorCustom ? 'TRUE' : 'FALSE'}, `
        + `${sqlLiteral(asset.currency)}, ${sqlLiteral(fetchedAt)}::timestamptz, `
        + `${sqlLiteral(fetchedAt)}::timestamptz)`;
}

function renderPriceInsert(result, fetchedAt) {
    const { asset, rows } = result;
    const valueRows = rows.map((row, index) => {
        const timestamp = `${row.date}T00:00:00Z`;
        const timestampSql = index === 0
            ? `${sqlLiteral(timestamp)}::timestamptz`
            : sqlLiteral(timestamp);

        return `        (${timestampSql}, ${formatPrice(row.price)}, ${sqlLiteral(row.source)})${index === rows.length - 1 ? '' : ','}`;
    });

    return [
        `-- ${asset.symbol} (${asset.name}) - ${rows.length} precios`,
        'INSERT INTO asset_prices (',
        '    id,',
        '    shared_asset_id,',
        '    price_timestamp,',
        '    close,',
        '    currency,',
        '    source,',
        '    fetched_at',
        ')',
        'SELECT',
        '    gen_random_uuid(),',
        '    sa.id,',
        '    v.price_timestamp,',
        '    v.close,',
        `    ${sqlLiteral(asset.currency)},`,
        '    v.source,',
        `    ${sqlLiteral(fetchedAt)}::timestamptz`,
        'FROM shared_assets sa',
        'CROSS JOIN (VALUES',
        ...valueRows,
        ') AS v(price_timestamp, close, source)',
        `WHERE sa.stable_catalog_id = ${sqlLiteral(asset.stableCatalogId)}`,
        `  AND sa.currency = ${sqlLiteral(asset.currency)}`,
        'ON CONFLICT (shared_asset_id, price_timestamp, source) DO NOTHING;',
        '',
    ].join('\n');
}

export function renderSeedSql({ manifest, range, results, fetchedAt, generatedBy }) {
    const totalRows = results.reduce((sum, result) => sum + result.rows.length, 0);
    const lines = [
        '-- =====================================================================',
        '-- Millete - Seed de precios históricos',
        '-- =====================================================================',
        `-- Rango: ${range.from} 00:00 UTC -> ${range.to} 00:00 UTC (${daysBetween(range.from, range.to)} días)`,
        `-- Divisa base: ${manifest.baseCurrency}`,
        `-- Activos: ${manifest.assets.length}`,
        `-- Precios: ${formatInteger(totalRows)}`,
        `-- Generado por: ${generatedBy}`,
        '--',
        '-- Características:',
        '--   * Idempotente: se puede ejecutar varias veces sin duplicar precios.',
        '--   * No modifica activos existentes (ON CONFLICT stable_catalog_id DO NOTHING).',
        '--   * Solo inserta precios si el activo existente está en la misma divisa.',
        '--   * Fuentes con prefijo SEED_ para no pisar los precios vivos de la app.',
        '--   * fetched_at fijo al final del rango: una actualización viva posterior gana.',
        '--   * Solo se guarda el precio de apertura/snapshot en close; open/high/low/volume quedan NULL.',
        '--',
        '-- Importación recomendada:',
        '--   sh manage.sh seed-prices',
        '--',
        '-- Importación manual:',
        '--   docker compose exec -T postgres sh -c \'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"\' < <archivo.sql>',
        '--',
        '-- Rollback de los precios del seed (no borra los activos):',
        '--   DELETE FROM asset_prices WHERE source LIKE \'SEED%\';',
        '-- =====================================================================',
        '',
        'BEGIN;',
        '',
        'DO $$',
        'BEGIN',
        '    IF to_regclass(\'public.shared_assets\') IS NULL',
        '       OR to_regclass(\'public.asset_prices\') IS NULL THEN',
        '        RAISE EXCEPTION \'Faltan las tablas shared_assets o asset_prices. Arranca Millete una vez para aplicar las migraciones Flyway antes de importar el seed.\';',
        '    END IF;',
        'END',
        '$$;',
        '',
        '-- ---------------------------------------------------------------------',
        '-- 1. Catálogo de activos compartidos',
        '-- ---------------------------------------------------------------------',
        'INSERT INTO shared_assets (',
        '    id,',
        '    stable_catalog_id,',
        '    name,',
        '    symbol,',
        '    type,',
        '    sector_code,',
        '    sector_display_name,',
        '    sector_custom,',
        '    currency,',
        '    created_at,',
        '    modified_at',
        ') VALUES',
        manifest.assets.map((asset) => renderAssetInsert(asset, fetchedAt)).join(',\n'),
        'ON CONFLICT (stable_catalog_id) DO NOTHING;',
        '',
        '-- ---------------------------------------------------------------------',
        '-- 2. Precios diarios',
        '-- ---------------------------------------------------------------------',
        '',
    ];

    for (const result of results) {
        if (result.rows.length === 0) {
            lines.push(`-- ${result.asset.symbol}: sin precios; no se genera INSERT.`);
            lines.push('');
            continue;
        }

        lines.push(renderPriceInsert(result, fetchedAt));
    }

    lines.push('COMMIT;');
    lines.push('');
    lines.push('-- Actualiza las estadísticas para que el planificador use bien el nuevo rango.');
    lines.push('ANALYZE asset_prices;');
    lines.push('');

    return lines.join('\n');
}

function formatSources(sourceCounts) {
    return Object.entries(sourceCounts)
        .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
        .map(([source, count]) => `${source}: ${formatInteger(count)}`)
        .join('<br>');
}

function formatGaps(dates, limit = 20) {
    if (dates.length === 0) {
        return '0';
    }

    const shown = dates.slice(0, limit).join(', ');
    const suffix = dates.length > limit ? `, … (+${dates.length - limit})` : '';

    return `${dates.length}<br><small>${shown}${suffix}</small>`;
}

export function renderCoverageReport({ manifest, range, results }) {
    const totalDays = daysBetween(range.from, range.to);
    const totalRows = results.reduce((sum, result) => sum + result.rows.length, 0);
    const complete = results.filter((result) => result.rows.length === totalDays).length;
    const partial = results.length - complete;
    const lines = [
        '# Cobertura del seed de precios',
        '',
        `- **Rango:** ${range.from} -> ${range.to}`,
        `- **Días naturales:** ${totalDays}`,
        `- **Activos:** ${manifest.assets.length}`,
        `- **Precios:** ${formatInteger(totalRows)}`,
        `- **Activos con cobertura completa:** ${complete}`,
        `- **Activos con cobertura parcial:** ${partial}`,
        `- **Divisa base:** ${manifest.baseCurrency}`,
        '',
        '## Detalle por activo',
        '',
        '| Activo | Nombre | Días | Cobertura | Primero | Último | Huecos internos | Fuentes |',
        '|---|---|---:|---:|---|---|---:|---|',
    ];

    for (const result of results) {
        const { asset, rows } = result;
        const first = rows[0]?.date ?? '—';
        const last = rows.at(-1)?.date ?? '—';
        const present = new Set(rows.map((row) => row.date));
        const allDates = eachDate(range.from, range.to);
        const internalGaps = first === '—'
            ? []
            : allDates.filter((date) => date > first && date < last && !present.has(date));

        lines.push(
            `| ${asset.symbol} | ${asset.name} | ${formatInteger(rows.length)} | `
            + `${formatPercent(rows.length, totalDays)} | ${first} | ${last} | `
            + `${formatGaps(internalGaps)} | ${formatSources(result.sourceCounts)} |`,
        );
    }

    const partialResults = results.filter((result) => result.rows.length < totalDays);

    if (partialResults.length > 0) {
        lines.push('');
        lines.push('## Activos con cobertura parcial');
        lines.push('');

        for (const result of partialResults) {
            const { asset, rows } = result;
            const first = rows[0]?.date;
            const last = rows.at(-1)?.date;
            const reasons = [];

            if (!first) {
                reasons.push('sin ningún precio');
            } else {
                if (first > range.from) {
                    reasons.push(`el primer precio es del ${first} (listado posterior o sin datos anteriores)`);
                }

                if (last < range.to) {
                    reasons.push(`el último precio es del ${last} (sin datos posteriores)`);
                }
            }

            lines.push(
                `- **${asset.symbol}** (${asset.name}): ${formatInteger(rows.length)}/${totalDays} días `
                + `(${formatPercent(rows.length, totalDays)}). ${reasons.join('; ') || 'huecos internos'}.`,
            );
        }
    }

    const conversionFailures = results.flatMap((result) => result.conversionFailures
        .map((failure) => ({ symbol: result.asset.symbol, ...failure })));

    if (conversionFailures.length > 0) {
        lines.push('');
        lines.push('## Conversiones descartadas');
        lines.push('');
        lines.push('Filas que no se pudieron convertir a la divisa del activo:');
        lines.push('');

        for (const failure of conversionFailures.slice(0, 50)) {
            lines.push(
                `- ${failure.symbol} ${failure.date}: ${failure.source} `
                + `(${failure.quoteCurrency} -> ${failure.assetCurrency})`,
            );
        }

        if (conversionFailures.length > 50) {
            lines.push(`- … y ${conversionFailures.length - 50} más.`);
        }
    }

    lines.push('');
    lines.push('## Notas');
    lines.push('');
    lines.push('- Las fechas son UTC y el precio guardado es la apertura/snapshot de la vela diaria.');
    lines.push('- En la primera vela de una fuente, si la apertura es un valor de subasta anómalo (≥2x o ≤0,5x del cierre), se usa el cierre del día.');
    lines.push('- `fetched_at` se fija al final del rango para que los precios vivos posteriores tengan prioridad.');
    lines.push('- Los activos con cobertura parcial suelen ser listados recientes; no se inventan precios anteriores.');
    lines.push('- Las fuentes `SEED_*` se pueden borrar sin afectar a los precios obtenidos por la aplicación.');
    lines.push('');

    return lines.join('\n');
}
