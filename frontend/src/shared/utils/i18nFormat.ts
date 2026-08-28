import i18n from '@/lib/i18n';
import { sessionCache } from '@/shared/utils/sessionCache';

const LOCALE_MAP: Record<string, string> = {
  es: 'es-ES',
  en: 'en-GB',
  fr: 'fr-FR',
  de: 'de-DE',
  it: 'it-IT',
  pt: 'pt-PT',
  ja: 'ja-JP',
};

const DEFAULT_LOCALE = 'es-ES';
const DEFAULT_CURRENCY = 'EUR';

function getLocale(): string {
  const lang = i18n.language?.split('-')[0];
  return (lang && LOCALE_MAP[lang]) || DEFAULT_LOCALE;
}

// TODO(moneda preferida): nadie escribe 'userPreferences' en sessionCache,
// así que esto siempre devuelve EUR. Pendiente decidir si se conecta a
// updatePreferences del perfil o se elimina (ver revisión, punto 9).
function getCurrency(): string {
  try {
    const raw = sessionCache.getItem('userPreferences');
    if (raw) {
      const prefs = JSON.parse(raw) as { currencyFormat?: { currency?: string } };
      if (prefs.currencyFormat?.currency) {
        return prefs.currencyFormat.currency;
      }
    }
  } catch {
  }
  return DEFAULT_CURRENCY;
}

const CURRENCY_FORMATTERS = new Map<string, Intl.NumberFormat>();
const NUMBER_FORMATTERS = new Map<string, Intl.NumberFormat>();

const COMMON_LOCALES = Object.values(LOCALE_MAP);
const COMMON_CURRENCIES = ['EUR', 'USD', 'GBP', 'CHF', 'CAD', 'AUD', 'JPY'];

for (const locale of COMMON_LOCALES) {
  for (const currency of COMMON_CURRENCIES) {
    CURRENCY_FORMATTERS.set(
      `${locale}:${currency}`,
      new Intl.NumberFormat(locale, {
        style: 'currency',
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      })
    );
  }
  NUMBER_FORMATTERS.set(
    locale,
    new Intl.NumberFormat(locale, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  );
}

export function formatCurrency(value: number, currency?: string): string {
  const locale = getLocale();
  const currencyCode = currency || getCurrency();
  const key = `${locale}:${currencyCode}`;
  const formatter = CURRENCY_FORMATTERS.get(key);
  if (formatter) {
    return formatter.format(value);
  }
  return CURRENCY_FORMATTERS.get(`${DEFAULT_LOCALE}:${DEFAULT_CURRENCY}`)!.format(value);
}

export function formatNumber(value: number, options?: Intl.NumberFormatOptions): string {
  const locale = getLocale();
  const { maximumFractionDigits, minimumFractionDigits, ...restOptions } = options || {};
  const finalOptions: Intl.NumberFormatOptions = {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
    ...restOptions,
  };
  if (
    typeof maximumFractionDigits === 'number' &&
    !isNaN(maximumFractionDigits) &&
    maximumFractionDigits >= 0 &&
    maximumFractionDigits <= 20
  ) {
    finalOptions.maximumFractionDigits = maximumFractionDigits;
  }
  if (
    typeof minimumFractionDigits === 'number' &&
    !isNaN(minimumFractionDigits) &&
    minimumFractionDigits >= 0 &&
    minimumFractionDigits <= 20
  ) {
    finalOptions.minimumFractionDigits = minimumFractionDigits;
  }
  if ((finalOptions.minimumFractionDigits ?? 0) > (finalOptions.maximumFractionDigits ?? 20)) {
    finalOptions.minimumFractionDigits = finalOptions.maximumFractionDigits;
  }
  const hasExtraOptions = Object.keys(restOptions).length > 0;
  const isDefaultMinMax =
    finalOptions.minimumFractionDigits === 2 && finalOptions.maximumFractionDigits === 2;
  if (!hasExtraOptions && isDefaultMinMax) {
    const formatter = NUMBER_FORMATTERS.get(locale);
    if (formatter) {
      return formatter.format(value);
    }
  }
  const fallbackFormatter = NUMBER_FORMATTERS.get(DEFAULT_LOCALE);
  return fallbackFormatter ? fallbackFormatter.format(value) : String(value);
}
