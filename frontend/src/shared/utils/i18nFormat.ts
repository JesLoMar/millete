import i18n from '@/lib/i18n'
import { sessionCache } from '@/shared/utils/sessionCache'

const LOCALE_MAP: Record<string, string> = {
  es: 'es-ES',
  en: 'en-GB',
  fr: 'fr-FR',
  de: 'de-DE',
  it: 'it-IT',
  pt: 'pt-PT',
  ja: 'ja-JP',
}

const DEFAULT_LOCALE = 'es-ES'
const DEFAULT_CURRENCY = 'EUR'
const MAX_FORMATTER_CACHE_SIZE = 100

function getLocale(): string {
  const lang = i18n.language?.split('-')[0]
  return (lang && LOCALE_MAP[lang]) || DEFAULT_LOCALE
}

let lastPreferencesRaw: string | null | undefined
let lastCurrencyCode = DEFAULT_CURRENCY

export function getLocalCurrencyCode(): string {
  const raw = sessionCache.getItem('userPreferences')

  if (raw === lastPreferencesRaw) {
    return lastCurrencyCode
  }

  lastPreferencesRaw = raw
  lastCurrencyCode = DEFAULT_CURRENCY

  if (!raw) {
    return lastCurrencyCode
  }

  try {
    const prefs = JSON.parse(raw) as {
      localCurrency?: unknown
      currencyFormat?: {
        currency?: unknown
      }
    }

    const configuredCurrency =
      prefs.localCurrency || prefs.currencyFormat?.currency

    if (
      typeof configuredCurrency !== 'string' ||
      configuredCurrency.trim().length === 0
    ) {
      return lastCurrencyCode
    }

    const currency = configuredCurrency.trim().toUpperCase()

    new Intl.NumberFormat('en', {
      style: 'currency',
      currency,
    })

    lastCurrencyCode = currency
  } catch {
  }

  return lastCurrencyCode
}

const FORMATTERS = new Map<string, Intl.NumberFormat>()

function getFormatter(
  locale: string,
  options: Intl.NumberFormatOptions,
): Intl.NumberFormat {
  const normalizedOptions = Object.keys(options)
    .sort()
    .map((key) => [key, options[key as keyof Intl.NumberFormatOptions]])

  const key = JSON.stringify([locale, normalizedOptions])
  const cachedFormatter = FORMATTERS.get(key)

  if (cachedFormatter) {
    FORMATTERS.delete(key)
    FORMATTERS.set(key, cachedFormatter)
    return cachedFormatter
  }

  const formatter = new Intl.NumberFormat(locale, options)
  FORMATTERS.set(key, formatter)

  if (FORMATTERS.size > MAX_FORMATTER_CACHE_SIZE) {
    const oldestKey = FORMATTERS.keys().next().value

    if (oldestKey !== undefined) {
      FORMATTERS.delete(oldestKey)
    }
  }

  return formatter
}

interface FormatCurrencyOptions {
  compact?: boolean
  minimumFractionDigits?: number
  maximumFractionDigits?: number
}

function normalizeFractionDigits(
  options: {
    minimumFractionDigits?: number
    maximumFractionDigits?: number
  },
  defaults: {
    minimum: number
    maximum: number
  },
): {
  minimumFractionDigits: number
  maximumFractionDigits: number
} {
  const normalize = (value: number | undefined, fallback: number) => {
    if (
      typeof value !== 'number' ||
      !Number.isFinite(value) ||
      !Number.isInteger(value)
    ) {
      return fallback
    }

    return Math.max(0, Math.min(20, value))
  }

  let minimumFractionDigits = normalize(
    options.minimumFractionDigits,
    defaults.minimum,
  )

  const maximumFractionDigits = normalize(
    options.maximumFractionDigits,
    Math.max(defaults.maximum, minimumFractionDigits),
  )

  if (minimumFractionDigits > maximumFractionDigits) {
    minimumFractionDigits = maximumFractionDigits
  }

  return {
    minimumFractionDigits,
    maximumFractionDigits,
  }
}

export function formatCurrency(
  value: number,
  currency?: string,
  options: FormatCurrencyOptions = {},
): string {
  const locale = getLocale()
  const currencyCode = currency || getLocalCurrencyCode()

  const useCompactNotation =
    options.compact === true && Math.abs(value) >= 1_000

  const {
    minimumFractionDigits,
    maximumFractionDigits,
  } = normalizeFractionDigits(
    options,
    useCompactNotation
      ? { minimum: 1, maximum: 1 }
      : { minimum: 2, maximum: 2 },
  )

  return getFormatter(locale, {
    style: 'currency',
    currency: currencyCode,
    minimumFractionDigits,
    maximumFractionDigits,
    ...(useCompactNotation
      ? {
          notation: 'compact',
          compactDisplay: 'short',
        }
      : {}),
  }).format(value)
}

export function formatNumber(
  value: number,
  options?: Intl.NumberFormatOptions,
): string {
  const locale = getLocale()

  const {
    minimumFractionDigits: requestedMinimumFractionDigits,
    maximumFractionDigits: requestedMaximumFractionDigits,
    ...restOptions
  } = options || {}

  const {
    minimumFractionDigits,
    maximumFractionDigits,
  } = normalizeFractionDigits(
    {
      minimumFractionDigits: requestedMinimumFractionDigits,
      maximumFractionDigits: requestedMaximumFractionDigits,
    },
    {
      minimum: 2,
      maximum: 2,
    },
  )

  return getFormatter(locale, {
    ...restOptions,
    minimumFractionDigits,
    maximumFractionDigits,
  }).format(value)
}