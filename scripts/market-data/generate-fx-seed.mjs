#!/usr/bin/env node
/**
 * Genera un seed SQL con tipos de cambio diarios (`fx_rates`).
 *
 * Uso:
 *   node scripts/market-data/generate-fx-seed.mjs
 *   node scripts/market-data/generate-fx-seed.mjs --from 2026-01-01 --to 2026-11-07
 *
 * El manifiesto `fx-pairs.json` define los pares y sus fuentes en orden de
 * preferencia. El SQL resultante es idempotente.
 */

import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { fetchSource } from './lib/providers.mjs';
import {
    daysBetween,
    validateDate,
} from './lib/seed-builder.mjs';
import {
    fxSourceLabel,
    pairLabel,
    providerDescription,
    renderFxCoverageReport,
    renderFxSeedSql,
    validateFxManifest,
} from './lib/fx-seed-builder.mjs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const DEFAULT_MANIFEST = resolve(__dirname, 'fx-pairs.json');
const DEFAULT_FROM = '2026-01-01';
const DEFAULT_TO = '2026-11-07';
const GENERATED_BY = 'scripts/market-data/generate-fx-seed.mjs';

const PROVIDER_DELAY_MS = {
    binance: 120,
    yahoo: 200,
    bybit: 200,
    gate: 250,
    mexc: 200,
    coingecko: 0,
};

const sleep = (ms) => new Promise((resolvePromise) => setTimeout(resolvePromise, ms));

function printHelp() {
    console.log(`Genera el seed SQL de tipos de cambio históricos.

Uso:
  node scripts/market-data/generate-fx-seed.mjs [opciones]

Opciones:
  --from YYYY-MM-DD   Fecha inicial (por defecto ${DEFAULT_FROM})
  --to YYYY-MM-DD     Fecha final (por defecto ${DEFAULT_TO})
  --manifest <ruta>   Manifiesto de pares (por defecto scripts/market-data/fx-pairs.json)
  --out <ruta>        Archivo SQL de salida
  -h, --help          Muestra esta ayuda
`);
}

function nextValue(argv, index, flag) {
    const value = argv[index];

    if (value === undefined || value.startsWith('--')) {
        throw new Error(`Falta el valor de ${flag}`);
    }

    return value;
}

function parseArgs(argv) {
    const args = {
        from: DEFAULT_FROM,
        to: DEFAULT_TO,
        manifest: DEFAULT_MANIFEST,
        out: null,
    };

    for (let index = 0; index < argv.length; index += 1) {
        const arg = argv[index];

        if (arg === '--from') {
            args.from = nextValue(argv, index + 1, arg);
            index += 1;
        } else if (arg === '--to') {
            args.to = nextValue(argv, index + 1, arg);
            index += 1;
        } else if (arg === '--manifest') {
            args.manifest = resolve(process.cwd(), nextValue(argv, index + 1, arg));
            index += 1;
        } else if (arg === '--out') {
            args.out = resolve(process.cwd(), nextValue(argv, index + 1, arg));
            index += 1;
        } else if (arg === '--help' || arg === '-h') {
            printHelp();
            process.exit(0);
        } else {
            throw new Error(`Argumento desconocido: ${arg}`);
        }
    }

    validateDate(args.from, '--from');
    validateDate(args.to, '--to');

    if (args.from > args.to) {
        throw new Error('--from no puede ser posterior a --to');
    }

    return args;
}

function mergePairRates(pair, fetchedSources) {
    const byDate = new Map();

    for (const entry of fetchedSources) {
        const source = fxSourceLabel(entry.source.provider);

        for (const row of entry.rows) {
            if (byDate.has(row.date) || !Number.isFinite(row.open) || row.open <= 0) {
                continue;
            }

            byDate.set(row.date, {
                date: row.date,
                rate: row.open,
                source,
            });
        }
    }

    const rows = [...byDate.values()].sort((a, b) => a.date.localeCompare(b.date));
    const sourceCounts = {};

    for (const row of rows) {
        sourceCounts[row.source] = (sourceCounts[row.source] ?? 0) + 1;
    }

    return { rows, sourceCounts };
}

function formatInteger(value) {
    return String(value).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const manifest = JSON.parse(await readFile(args.manifest, 'utf8'));
    validateFxManifest(manifest);

    const range = { from: args.from, to: args.to };
    const totalDays = daysBetween(range.from, range.to);
    const outPath = args.out
        ?? resolve(__dirname, 'seeds', `fx-rates-${range.from}_${range.to}.sql`);
    const reportPath = outPath.replace(/\.sql$/i, '') + '-coverage.md';

    console.log(`Manifiesto: ${args.manifest}`);
    console.log(`Rango: ${range.from} -> ${range.to} (${totalDays} días)`);
    console.log(`Pares: ${manifest.pairs.length}`);
    console.log('');

    const results = [];

    for (const pair of manifest.pairs) {
        const label = pairLabel(pair);
        const fetchedSources = [];

        for (const source of pair.sources) {
            const description = providerDescription(source);

            try {
                const rows = await fetchSource(source, range);
                fetchedSources.push({ source, rows });
                console.log(
                    `  ${label.padEnd(7)} ${description.padEnd(16)} `
                    + `${String(rows.length).padStart(3)} días`,
                );
            } catch (error) {
                fetchedSources.push({ source, rows: [], error });
                console.warn(
                    `  ${label.padEnd(7)} ${description.padEnd(16)} `
                    + `ERROR: ${error.message}`,
                );
            }

            const delay = PROVIDER_DELAY_MS[source.provider] ?? 150;

            if (delay > 0) {
                await sleep(delay);
            }
        }

        const merged = mergePairRates(pair, fetchedSources);
        results.push({ pair, ...merged });

        const sources = Object.entries(merged.sourceCounts)
            .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
            .map(([source, count]) => `${source}:${count}`)
            .join(' ');

        console.log(
            `  ${label.padEnd(7)} TOTAL ${String(merged.rows.length).padStart(3)}/${totalDays} `
            + `${sources}`,
        );
        console.log('');
    }

    const fetchedAt = `${range.to}T00:00:00Z`;
    const sql = renderFxSeedSql({
        pairs: manifest.pairs,
        range,
        results,
        fetchedAt,
        generatedBy: GENERATED_BY,
    });
    const report = renderFxCoverageReport({
        pairs: manifest.pairs,
        range,
        results,
    });

    await mkdir(dirname(outPath), { recursive: true });
    await writeFile(outPath, sql, 'utf8');
    await writeFile(reportPath, report, 'utf8');

    const totalRows = results.reduce((sum, result) => sum + result.rows.length, 0);
    const complete = results.filter((result) => result.rows.length === totalDays).length;
    const partial = results.length - complete;
    const empty = results.filter((result) => result.rows.length === 0);

    console.log('Resumen');
    console.log('-------');
    console.log(`Pares: ${results.length}`);
    console.log(`Tipos de cambio: ${formatInteger(totalRows)}`);
    console.log(`Cobertura completa: ${complete}`);
    console.log(`Cobertura parcial: ${partial}`);
    console.log(`SQL: ${outPath}`);
    console.log(`Informe: ${reportPath}`);

    if (empty.length > 0) {
        console.error('');
        console.error(`ERROR: sin tipos de cambio para ${empty.map((result) => pairLabel(result.pair)).join(', ')}`);
        process.exitCode = 1;
    }

    if (partial > 0) {
        console.log('');
        console.log(
            'Aviso: la cobertura parcial es normal en divisas porque el mercado cierra '
            + 'los fines de semana.',
        );
    }
}

main().catch((error) => {
    console.error('');
    console.error(`ERROR: ${error.message}`);

    if (error.cause) {
        console.error(error.cause);
    }

    process.exit(1);
});
