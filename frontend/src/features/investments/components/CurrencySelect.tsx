import { useMemo } from 'react';
import type { ChangeEventHandler } from 'react';
import { useTranslation } from 'react-i18next';

import { CURRENCIES } from '../constants';
import { isValidCurrencyCode } from '../utils';

const availableCurrencies = typeof Intl.supportedValuesOf === 'function'
  ? Intl.supportedValuesOf('currency')
  : CURRENCIES;

interface CurrencySelectProps {
  id: string;
  value: string;
  pairedCurrency: string;
  onChange: ChangeEventHandler<HTMLSelectElement>;
  disabled?: boolean;
}

export function CurrencySelect({
  id,
  value,
  pairedCurrency,
  onChange,
  disabled,
}: CurrencySelectProps) {
  const { i18n } = useTranslation('investments');
  const locale = i18n.resolvedLanguage ?? i18n.language;
  const options = useMemo(() => {
    const names = new Intl.DisplayNames(locale, { type: 'currency' });
    return [...new Set([...availableCurrencies, ...CURRENCIES, value, pairedCurrency])]
      .filter(isValidCurrencyCode)
      .sort()
      .map((code) => (
        <option key={code} value={code}>
          {code} — {names.of(code) ?? code}
        </option>
      ));
  }, [locale, value, pairedCurrency]);

  return (
    <select
      id={id}
      value={value}
      onChange={onChange}
      disabled={disabled}
      className="h-9 w-full min-w-0 truncate rounded-md border border-input bg-background px-3 text-sm shadow-xs outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50"
    >
      {options}
    </select>
  );
}
