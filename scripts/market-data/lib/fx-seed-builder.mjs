/**
 * Utilidades para construir el seed SQL de tipos de cambio históricos
 * (`fx_rates`).
 *
 * A diferencia del seed de precios, aquí no hay conversión de divisas: cada
 * fila ya es un par directo BASE/QUOTE descargado de un proveedor.
 */

import { daysBetween, eachDate } from './seed-builder.mjs';

const RATE_SCALE = 16;

export function fxSourceLabel(provider) {
    return `SEED_${provider.toUpperCase()}`;
}

export function validateFxManifest(manifest) {
    if (!manifest || typeof manifest !== 'object') {
        throw new Error('El manifiesto de FX no es un objeto JSON válido');
    }

    if (!Array.isArray(manifest.pairs) || manifest.pairs.length === 0) {
        throw new Error('El manifiesto de FX no contiene pares');
    }

    const seen = new Set();

    for (const pair of manifest.pairs) {
        for (const field of ['base', 'quote']) {
            if (typeof pair[field] !== 'string' || !/^[A-Z]{3}$/.test(pair[field])) {
                throw new Error(`Par FX con ${field} no válido: ${JSON.stringify(pair)}`);
            }
        }

        if (pair.base === pair.quote) {
            throw new Error(`Par FX con divisas iguales: ${pair.base}/${pair.quote}`);
        }

        const key = `${pair.base}/${pair.quote}`;

        if (seen.has(key)) {
            throw new Error(`Par FX duplicado: ${key}`);
        }

        seen.add(key);

        if (!Array.isArray(pair.sources) || pair.sources.length === 0) {
            throw new Error(`${key}: no tiene fuentes`);
        }

        for (const source of pair.sources) {
            if (!source || typeof source !== 'object') {
                throw new Error(`${key}: fuente no válida`);
            }

            if (typeof source.provider !== 'string' || source.provider.trim() === '') {
                throw new Error(`${key}: la fuente no tiene proveedor`);
            }

            const identifier = source.ticker ?? source.pair ?? source.id;

            if (typeof identifier !== 'string' || identifier.trim() === '') {
                throw new Error(`${key}: la fuente ${source.provider} no tiene identificador`);
            }
        }
    }
}

export function pairLabel(pair) {
    return `${pair.base}/${pair.quote}`;
}

export function providerDescription(source) {
    return `${source.provider}:${source.ticker ?? source.pair ?? source.id}`;
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

function formatRate(value) {
    if (!Number.isFinite(value) || value <= 0) {
        throw new Error(`Tipo de cambio no válido: ${value}`);
    }

    const fixed = value.toFixed(RATE_SCALE);

    if (fixed === (0).toFixed(RATE_SCALE)) {
        throw new Error(
            `El tipo de cambio ${value} es demasiado pequeño para DECIMAL(28,${RATE_SCALE}) `
            + 'y se perdería precisión',
        );
    }

    return fixed;
}

function renderFxInsert(result, fetchedAt) {
    const { pair, rows } = result;

    const valueRows = rows.map((row, index) => {
        const timestamp = `${row.date}T00:00:00Z`;
        const timestampSql = index === 0
            ? `'${timestamp}'::timestamptz`
            : `'${timestamp}'`;

        const suffix = index === rows.length - 1 ? '' : ',';

        return `        (${timestampSql}, ${formatRate(row.rate)}, '${row.source}')${suffix}`;
    });

    return [
        `-- ${pairLabel(pair)} - ${rows.length} tipos de cambio`,
        'INSERT INTO fx_rates (',
        '    id,',
        '    base_currency,',
        '    quote_currency,',
        '    rate_timestamp,',
        '    rate,',
        '    source,',
        '    fetched_at',
        ')',
        'SELECT',
        '    gen_random_uuid(),',
        `    '${pair.base}',`,
        `    '${pair.quote}',`,
        '    v.rate_timestamp,',
        '    v.rate,',
        '    v.source,',
        `    '${fetchedAt}'::timestamptz`,
        'FROM (VALUES',
        ...valueRows,
        ') AS v(rate_timestamp, rate, source)',
        'ON CONFLICT (base_currency, quote_currency, rate_timestamp, source) DO NOTHING;',
        '',
    ].join('\n');
}

export function renderFxSeedSql({ pairs, range, results, fetchedAt, generatedBy }) {
    const totalRows = results.reduce((sum, result) => sum + result.rows.length, 0);

    const lines = [
        '-- =====================================================================',
        '-- Millete - Seed de tipos de cambio históricos',
        '-- =====================================================================',
        `-- Rango: ${range.from} 00:00 UTC -> ${range.to} 00:00 UTC (${daysBetween(range.from, range.to)} días)`,
        `-- Pares: ${pairs.length}`,
        `-- Tipos de cambio: ${formatInteger(totalRows)}`,
        `-- Generado por: ${generatedBy}`,
        '--',
        '-- Características:',
        '--   * Idempotente: se puede ejecutar varias veces sin duplicar tipos de cambio.',
        '--   * Fuentes con prefijo SEED_ para no pisar los FX vivos de la aplicación.',
        '--   * fetched_at fijo al final del rango: una actualización viva posterior gana.',
        '--   * Cada fila es un par directo BASE/QUOTE; no se guardan inversos derivados.',
        '--',
        '-- Importación manual:',
        '--   docker compose exec -T postgres sh -c \'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"\' < <archivo.sql>',
        '--',
        '-- Rollback:',
        '--   DELETE FROM fx_rates WHERE source LIKE \'SEED%\';',
        '-- =====================================================================',
        '',
        'BEGIN;',
        '',
        'DO $$',
        'BEGIN',
        '    IF to_regclass(\'public.fx_rates\') IS NULL THEN',
        '        RAISE EXCEPTION \'Falta la tabla fx_rates. Arranca Millete una vez para aplicar las migraciones Flyway antes de importar el seed.\';',
        '    END IF;',
        'END',
        '$$;',
        '',
        '-- ---------------------------------------------------------------------',
        '-- Tipos de cambio diarios',
        '-- ---------------------------------------------------------------------',
        '',
    ];

    for (const result of results) {
        if (result.rows.length === 0) {
            lines.push(`-- ${pairLabel(result.pair)}: sin tipos de cambio; no se genera INSERT.`);
            lines.push('');
            continue;
        }

        lines.push(renderFxInsert(result, fetchedAt));
    }

    lines.push('COMMIT;');
    lines.push('');
    lines.push('-- Actualiza las estadísticas para que el planificador use bien el nuevo rango.');
    lines.push('ANALYZE fx_rates;');
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

export function renderFxCoverageReport({ pairs, range, results }) {
    const totalDays = daysBetween(range.from, range.to);
    const totalRows = results.reduce((sum, result) => sum + result.rows.length, 0);
    const complete = results.filter((result) => result.rows.length === totalDays).length;
    const partial = results.length - complete;

    const lines = [
        '# Cobertura del seed de tipos de cambio',
        '',
        `- **Rango:** ${range.from} -> ${range.to}`,
        `- **Días naturales:** ${totalDays}`,
        `- **Pares:** ${pairs.length}`,
        `- **Tipos de cambio:** ${formatInteger(totalRows)}`,
        `- **Pares con cobertura completa:** ${complete}`,
        `- **Pares con cobertura parcial:** ${partial}`,
        '',
        '## Detalle por par',
        '',
        '| Par | Días | Cobertura | Primero | Último | Huecos internos | Fuentes |',
        '|---|---:|---:|---|---|---:|---|',
    ];

    for (const result of results) {
        const { pair, rows } = result;
        const first = rows[0]?.date ?? '—';
        const last = rows.at(-1)?.date ?? '—';
        const present = new Set(rows.map((row) => row.date));
        const allDates = eachDate(range.from, range.to);
        const internalGaps = first === '—'
            ? []
            : allDates.filter((date) => date > first && date < last && !present.has(date));

        lines.push(
            `| ${pairLabel(pair)} | ${formatInteger(rows.length)} | `
            + `${formatPercent(rows.length, totalDays)} | ${first} | ${last} | `
            + `${formatGaps(internalGaps)} | ${formatSources(result.sourceCounts)} |`,
        );
    }

    lines.push('');
    lines.push('## Notas');
    lines.push('');
    lines.push('- Las fechas son UTC y el tipo de cambio guardado es la apertura de la vela diaria de Yahoo Finance.');
    lines.push('- El mercado de divisas cierra los fines de semana: los sábados y domingos aparecen como huecos normales.');
    lines.push('- `fetched_at` se fija al final del rango para que los FX vivos posteriores tengan prioridad.');
    lines.push('- Las fuentes `SEED_*` se pueden borrar sin afectar a los tipos obtenidos por la aplicación.');
    lines.push('- Yahoo Finance es un endpoint no oficial: si cambia, regenera el seed o usa el refresco de market data.');
    lines.push('');

    return lines.join('\n');
}
