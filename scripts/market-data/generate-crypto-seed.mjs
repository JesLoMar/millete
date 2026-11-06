#!/usr/bin/env node
/**
 * Genera un seed SQL con precios diarios de las criptomonedas del catálogo.
 *
 * Uso:
 *   node scripts/market-data/generate-crypto-seed.mjs
 *   node scripts/market-data/generate-crypto-seed.mjs --from 2025-01-01 --to 2025-12-31
 *
 * El manifiesto `crypto-assets.json` define los activos y sus fuentes en orden
 * de preferencia. El SQL resultante es idempotente y se puede importar con
 * `sh manage.sh seed-prices`.
 */

import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { fetchSource, sourceLabel } from './lib/providers.mjs';
import {
    daysBetween,
    mergeAssetPrices,
    renderCoverageReport,
    renderSeedSql,
    validateDate,
} from './lib/seed-builder.mjs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const DEFAULT_MANIFEST = resolve(__dirname, 'crypto-assets.json');
const DEFAULT_FROM = '2024-11-04';
const DEFAULT_TO = '2026-11-04';
const GENERATED_BY = 'scripts/market-data/generate-crypto-seed.mjs';

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
    console.log(`Genera el seed SQL de precios históricos de criptomonedas.

Uso:
  node scripts/market-data/generate-crypto-seed.mjs [opciones]

Opciones:
  --from YYYY-MM-DD   Fecha inicial (por defecto ${DEFAULT_FROM})
  --to YYYY-MM-DD     Fecha final (por defecto ${DEFAULT_TO})
  --manifest <ruta>   Manifiesto de activos (por defecto scripts/market-data/crypto-assets.json)
  --out <ruta>        Archivo SQL de salida
  -h, --help          Muestra esta ayuda

Variables de entorno:
  COINGECKO_API_KEY   Clave demo de CoinGecko (opcional). Sin clave, CoinGecko
                      solo permite los últimos 365 días.
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

function validateManifest(manifest) {
    if (!manifest || typeof manifest !== 'object') {
        throw new Error('El manifiesto no es un objeto JSON válido');
    }

    if (manifest.baseCurrency !== 'USD') {
        throw new Error('Este generador solo soporta manifiestos con baseCurrency USD');
    }

    if (!Array.isArray(manifest.assets) || manifest.assets.length === 0) {
        throw new Error('El manifiesto no contiene activos');
    }

    const seen = new Set();

    for (const asset of manifest.assets) {
        for (const field of [
            'stableCatalogId',
            'name',
            'symbol',
            'type',
            'currency',
            'sectorCode',
            'sectorDisplayName',
        ]) {
            if (typeof asset[field] !== 'string' || asset[field].trim() === '') {
                throw new Error(`El activo ${asset.symbol ?? '?'} no tiene ${field}`);
            }
        }

        if (asset.type !== 'CRYPTO') {
            throw new Error(`${asset.symbol}: este generador solo admite activos CRYPTO`);
        }

        if (!asset.stableCatalogId.startsWith('CRYPTO:')) {
            throw new Error(`${asset.symbol}: stableCatalogId debe empezar por CRYPTO:`);
        }

        if (asset.currency !== 'USD') {
            throw new Error(`${asset.symbol}: este generador solo admite activos en USD`);
        }

        if (typeof asset.sectorCustom !== 'boolean') {
            throw new Error(`${asset.symbol}: sectorCustom debe ser booleano`);
        }

        if (seen.has(asset.stableCatalogId)) {
            throw new Error(`stableCatalogId duplicado: ${asset.stableCatalogId}`);
        }

        seen.add(asset.stableCatalogId);

        if (!Array.isArray(asset.sources) || asset.sources.length === 0) {
            throw new Error(`${asset.symbol}: no tiene fuentes`);
        }

        for (const source of asset.sources) {
            if (!source || typeof source !== 'object') {
                throw new Error(`${asset.symbol}: fuente no válida`);
            }

            if (!(source.provider in PROVIDER_DELAY_MS)) {
                throw new Error(`${asset.symbol}: proveedor desconocido ${source.provider}`);
            }

            if (typeof source.quoteCurrency !== 'string' || !/^[A-Z]{3,5}$/.test(source.quoteCurrency)) {
                throw new Error(`${asset.symbol}: quoteCurrency no válida`);
            }

            const identifier = source.pair ?? source.ticker ?? source.id;

            if (typeof identifier !== 'string' || identifier.trim() === '') {
                throw new Error(`${asset.symbol}: la fuente ${source.provider} no tiene identificador`);
            }
        }
    }
}

function providerDescription(source) {
    return `${source.provider}:${source.pair ?? source.ticker ?? source.id}`;
}

function formatInteger(value) {
    return String(value).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const manifest = JSON.parse(await readFile(args.manifest, 'utf8'));
    validateManifest(manifest);

    const range = { from: args.from, to: args.to };
    const totalDays = daysBetween(range.from, range.to);
    const outPath = args.out
        ?? resolve(__dirname, 'seeds', `crypto-prices-${range.from}_${range.to}.sql`);
    const reportPath = outPath.replace(/\.sql$/i, '') + '-coverage.md';

    console.log(`Manifiesto: ${args.manifest}`);
    console.log(`Rango: ${range.from} -> ${range.to} (${totalDays} días)`);
    console.log(`Activos: ${manifest.assets.length}`);
    console.log('');

    const results = [];

    for (const asset of manifest.assets) {
        const fetchedSources = [];

        for (const source of asset.sources) {
            const description = providerDescription(source);

            try {
                const rows = await fetchSource(source, range);
                fetchedSources.push({ source, rows });
                console.log(
                    `  ${asset.symbol.padEnd(6)} ${description.padEnd(28)} `
                    + `${String(rows.length).padStart(3)} días`,
                );
            } catch (error) {
                fetchedSources.push({ source, rows: [], error });
                console.warn(
                    `  ${asset.symbol.padEnd(6)} ${description.padEnd(28)} `
                    + `ERROR: ${error.message}`,
                );
            }

            const delay = PROVIDER_DELAY_MS[source.provider] ?? 150;

            if (delay > 0) {
                await sleep(delay);
            }
        }

        const merged = mergeAssetPrices(asset, fetchedSources);
        results.push({ asset, ...merged });

        const sources = Object.entries(merged.sourceCounts)
            .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
            .map(([label, count]) => `${label}:${count}`)
            .join(' ');

        console.log(
            `  ${asset.symbol.padEnd(6)} TOTAL ${String(merged.rows.length).padStart(3)}/${totalDays} `
            + `${sources}`,
        );
        console.log('');
    }

    const fetchedAt = `${range.to}T00:00:00Z`;
    const sql = renderSeedSql({
        manifest,
        range,
        results,
        fetchedAt,
        generatedBy: GENERATED_BY,
    });
    const report = renderCoverageReport({ manifest, range, results });

    await mkdir(dirname(outPath), { recursive: true });
    await writeFile(outPath, sql, 'utf8');
    await writeFile(reportPath, report, 'utf8');

    const totalRows = results.reduce((sum, result) => sum + result.rows.length, 0);
    const complete = results.filter((result) => result.rows.length === totalDays).length;
    const partial = results.length - complete;
    const empty = results.filter((result) => result.rows.length === 0);
    const conversionFailures = results.reduce(
        (sum, result) => sum + result.conversionFailures.length,
        0,
    );

    console.log('Resumen');
    console.log('-------');
    console.log(`Activos: ${results.length}`);
    console.log(`Precios: ${formatInteger(totalRows)}`);
    console.log(`Cobertura completa: ${complete}`);
    console.log(`Cobertura parcial: ${partial}`);
    console.log(`SQL: ${outPath}`);
    console.log(`Informe: ${reportPath}`);

    if (empty.length > 0) {
        console.error('');
        console.error(`ERROR: sin precios para ${empty.map((result) => result.asset.symbol).join(', ')}`);
        process.exitCode = 1;
    }

    if (conversionFailures > 0) {
        console.error('');
        console.error(
            `ERROR: ${conversionFailures} precios no se pudieron convertir a la divisa del activo. `
            + 'Revisa el informe de cobertura.',
        );
        process.exitCode = 1;
    }

    if (partial > 0) {
        console.log('');
        console.log(
            'Aviso: hay activos con cobertura parcial. Es normal en activos listados '
            + 'después del inicio del rango; revisa el informe.',
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
