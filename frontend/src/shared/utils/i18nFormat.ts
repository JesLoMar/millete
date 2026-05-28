import i18n from '@/lib/i18n';

const LOCALE_MAP: Record<string, string> = {
  es: 'es-ES',
  en: 'en-GB',
  fr: 'fr-FR',
  de: 'de-DE',
  it: 'it-IT',
  pt: 'pt-PT',
};

const DEFAULT_LOCALE = 'es-ES';

const DEFAULT_CURRENCY = 'EUR';

function getLocale(): string {
  const lang = i18n.language?.split('-')[0];
  return (lang && LOCALE_MAP[lang]) || DEFAULT_LOCALE;
}

function getCurrency(): string {
  return DEFAULT_CURRENCY;
}

export function formatCurrency(value: number, currency?: string): string {
  const locale = getLocale();
  const currencyCode = currency || getCurrency();

  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currencyCode,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
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

  return new Intl.NumberFormat(locale, finalOptions).format(value);
}