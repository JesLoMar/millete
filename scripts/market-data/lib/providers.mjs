/**
 * Adaptadores de proveedores de precios diarios para el generador de seeds.
 *
 * Cada adaptador devuelve una lista ordenada de filas con la forma:
 *   { date: 'YYYY-MM-DD', open: number, close: number, volume: number | null }
 *
 * `date` es siempre la fecha UTC del inicio de la vela diaria. Para las velas
 * OHLC se usa el precio de apertura, que es el precio vigente a las 00:00 UTC
 * del día (o al inicio de la sesión en proveedores bursátiles).
 */

const DAY_MS = 86_400_000;
const REQUEST_TIMEOUT_MS = 30_000;
const USER_AGENT = 'millete-market-data-seed/1.0 (+https://github.com/puntomartinez/millete)';

const COINGECKO_PUBLIC_WINDOW_DAYS = 365;
const COINGECKO_MIN_INTERVAL_MS = 11_000;
let lastCoinGeckoRequestAt = 0;

export class ProviderError extends Error {
    constructor(message, { retryable = false } = {}) {
        super(message);
        this.name = 'ProviderError';
        this.retryable = retryable;
    }
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function toEpochMs(date) {
    return Date.parse(`${date}T00:00:00Z`);
}

function toDateKey(epochMs) {
    return new Date(epochMs).toISOString().slice(0, 10);
}

function addDays(date, days) {
    return toDateKey(toEpochMs(date) + days * DAY_MS);
}

function normaliseRows(rows, from, to) {
    const byDate = new Map();

    for (const row of rows) {
        if (!Number.isFinite(row.open) || row.open <= 0) {
            continue;
        }

        const date = toDateKey(row.timestamp);

        if (date < from || date > to || byDate.has(date)) {
            continue;
        }

        byDate.set(date, {
            date,
            open: row.open,
            close: Number.isFinite(row.close) && row.close > 0 ? row.close : row.open,
            volume: Number.isFinite(row.volume) && row.volume >= 0 ? row.volume : null,
        });
    }

    return [...byDate.values()].sort((a, b) => a.date.localeCompare(b.date));
}

async function requestJsonOnce(url, headers) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    try {
        const response = await fetch(url, {
            headers: {
                'User-Agent': USER_AGENT,
                Accept: 'application/json',
                ...headers,
            },
            signal: controller.signal,
        });
        const body = await response.text();

        if (!response.ok) {
            throw new ProviderError(
                `HTTP ${response.status}: ${body.slice(0, 240)}`,
                { retryable: response.status === 429 || response.status >= 500 },
            );
        }

        try {
            return JSON.parse(body);
        } catch {
            throw new ProviderError(`Respuesta no JSON: ${body.slice(0, 240)}`);
        }
    } catch (error) {
        if (error instanceof ProviderError) {
            throw error;
        }

        throw new ProviderError(
            `Error de red: ${error?.message ?? String(error)}`,
            { retryable: true },
        );
    } finally {
        clearTimeout(timeout);
    }
}

async function requestJson(url, { headers = {}, retries = 2 } = {}) {
    for (let attempt = 0; ; attempt += 1) {
        try {
            return await requestJsonOnce(url, headers);
        } catch (error) {
            const canRetry = error instanceof ProviderError && error.retryable;

            if (!canRetry || attempt >= retries) {
                throw error;
            }

            await sleep(1_500 * (attempt + 1));
        }
    }
}

async function fetchBinance({ pair, from, to }) {
    const url = new URL('https://api.binance.com/api/v3/klines');
    url.search = new URLSearchParams({
        symbol: pair,
        interval: '1d',
        startTime: String(toEpochMs(from)),
        endTime: String(toEpochMs(to)),
        limit: '1000',
    });

    const data = await requestJson(url);

    if (!Array.isArray(data)) {
        throw new ProviderError(`Binance devolvió una respuesta inesperada para ${pair}`);
    }

    return normaliseRows(
        data.map((row) => ({
            timestamp: Number(row[0]),
            open: Number(row[1]),
            close: Number(row[4]),
            volume: Number(row[5]),
        })),
        from,
        to,
    );
}

async function fetchYahoo({ ticker, from, to }) {
    const period1 = Math.floor(toEpochMs(from) / 1000);
    const period2 = Math.floor((toEpochMs(to) + DAY_MS) / 1000);
    const url = `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(ticker)}`
        + `?period1=${period1}&period2=${period2}&interval=1d`;

    const json = await requestJson(url);
    const result = json?.chart?.result?.[0];

    if (!result) {
        const detail = JSON.stringify(json?.chart?.error ?? {});
        throw new ProviderError(`Yahoo no devolvió datos para ${ticker}: ${detail}`);
    }

    const timestamps = result.timestamp ?? [];
    const quote = result.indicators?.quote?.[0] ?? {};

    return normaliseRows(
        timestamps.map((timestamp, index) => ({
            timestamp: timestamp * 1000,
            open: quote.open?.[index] ?? quote.close?.[index],
            close: quote.close?.[index],
            volume: quote.volume?.[index],
        })),
        from,
        to,
    );
}

async function fetchBybit({ pair, from, to }) {
    const url = new URL('https://api.bybit.com/v5/market/kline');
    url.search = new URLSearchParams({
        category: 'spot',
        symbol: pair,
        interval: 'D',
        start: String(toEpochMs(from)),
        end: String(toEpochMs(to) + DAY_MS - 1000),
        limit: '1000',
    });

    const json = await requestJson(url);
    const list = json?.result?.list;

    if (!Array.isArray(list)) {
        throw new ProviderError(`Bybit devolvió una respuesta inesperada para ${pair}`);
    }

    return normaliseRows(
        list.map((row) => ({
            timestamp: Number(row[0]),
            open: Number(row[1]),
            close: Number(row[4]),
            volume: Number(row[5]),
        })).reverse(),
        from,
        to,
    );
}

async function fetchGate({ pair, from, to }) {
    const url = new URL('https://api.gateio.ws/api/v4/spot/candlesticks');
    url.search = new URLSearchParams({
        currency_pair: pair,
        from: String(Math.floor(toEpochMs(from) / 1000)),
        to: String(Math.floor((toEpochMs(to) + DAY_MS) / 1000)),
        interval: '1d',
    });

    const data = await requestJson(url);

    if (!Array.isArray(data)) {
        throw new ProviderError(`Gate devolvió una respuesta inesperada para ${pair}`);
    }

    return normaliseRows(
        data.map((row) => ({
            timestamp: Number(row[0]) * 1000,
            open: Number(row[5]),
            close: Number(row[2]),
            volume: Number(row[6]),
        })),
        from,
        to,
    );
}

async function fetchMexc({ pair, from, to }) {
    const url = new URL('https://api.mexc.com/api/v3/klines');
    url.search = new URLSearchParams({
        symbol: pair,
        interval: '1d',
        startTime: String(toEpochMs(from)),
        endTime: String(toEpochMs(to)),
    });

    const data = await requestJson(url);

    if (!Array.isArray(data)) {
        throw new ProviderError(`MEXC devolvió una respuesta inesperada para ${pair}`);
    }

    return normaliseRows(
        data.map((row) => ({
            timestamp: Number(row[0]),
            open: Number(row[1]),
            close: Number(row[4]),
            volume: Number(row[5]),
        })),
        from,
        to,
    );
}

async function fetchCoinGecko({ id, quoteCurrency, from, to }) {
    const vsCurrency = (quoteCurrency ?? 'usd').toLowerCase();

    if (!/^[a-z]{3,5}$/.test(vsCurrency)) {
        throw new ProviderError(`CoinGecko: divisa no válida ${quoteCurrency}`);
    }

    const apiKey = process.env.COINGECKO_API_KEY;
    const today = new Date().toISOString().slice(0, 10);
    const publicLimit = addDays(today, -(COINGECKO_PUBLIC_WINDOW_DAYS - 1));

    if (!apiKey && to < publicLimit) {
        throw new ProviderError(
            `CoinGecko público no permite fechas anteriores a ${publicLimit}; `
            + 'configura COINGECKO_API_KEY para consultar rangos más antiguos',
        );
    }

    const effectiveFrom = apiKey
        ? from
        : (from > publicLimit ? from : publicLimit);

    if (effectiveFrom > to) {
        return [];
    }

    const minInterval = apiKey ? 2_500 : COINGECKO_MIN_INTERVAL_MS;
    const wait = minInterval - (Date.now() - lastCoinGeckoRequestAt);

    if (wait > 0) {
        await sleep(wait);
    }

    try {
        const url = new URL(`https://api.coingecko.com/api/v3/coins/${encodeURIComponent(id)}/market_chart/range`);
        url.search = new URLSearchParams({
            vs_currency: vsCurrency,
            from: String(Math.floor(toEpochMs(effectiveFrom) / 1000)),
            to: String(Math.floor(toEpochMs(to) / 1000)),
        });

        const headers = apiKey ? { 'x-cg-demo-api-key': apiKey } : {};
        const json = await requestJson(url, { headers });

        if (!Array.isArray(json?.prices)) {
            throw new ProviderError(`CoinGecko devolvió una respuesta inesperada para ${id}`);
        }

        return normaliseRows(
            json.prices.map(([timestamp, price]) => ({
                timestamp,
                open: Number(price),
                close: Number(price),
            })),
            from,
            to,
        );
    } finally {
        lastCoinGeckoRequestAt = Date.now();
    }
}

export async function fetchSource(source, { from, to }) {
    switch (source.provider) {
        case 'binance':
            return fetchBinance({ ...source, from, to });
        case 'yahoo':
            return fetchYahoo({ ...source, from, to });
        case 'bybit':
            return fetchBybit({ ...source, from, to });
        case 'gate':
            return fetchGate({ ...source, from, to });
        case 'mexc':
            return fetchMexc({ ...source, from, to });
        case 'coingecko':
            return fetchCoinGecko({ ...source, from, to });
        default:
            throw new ProviderError(`Proveedor desconocido: ${source.provider}`);
    }
}

export function sourceLabel(source) {
    return `SEED_${source.provider.toUpperCase()}_${source.quoteCurrency.toUpperCase()}`;
}
