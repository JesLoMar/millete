import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Coins } from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';
import { SettingsSection } from './SettingsSection';
import type { CurrencyOption, CurrencyCode } from '../types';

const currencies: CurrencyOption[] = [
  { code: 'EUR', symbol: '€', label: 'Euro' },
  { code: 'USD', symbol: '$', label: 'US Dollar' },
  { code: 'GBP', symbol: '£', label: 'British Pound' },
  { code: 'JPY', symbol: '¥', label: 'Japanese Yen' },
  { code: 'CHF', symbol: 'CHF', label: 'Swiss Franc' },
  { code: 'CAD', symbol: 'C$', label: 'Canadian Dollar' },
  { code: 'AUD', symbol: 'A$', label: 'Australian Dollar' },
];

export function CurrencySelector() {
  const { t } = useTranslation();
  const [selectedCurrency, setSelectedCurrency] = useState<CurrencyCode>('EUR');

  return (
    <SettingsSection
      icon={Coins}
      title={t('settings.currency.title')}
      description={t('settings.currency.description')}
    >
      <Select
        value={selectedCurrency}
        onValueChange={(value) => setSelectedCurrency(value as CurrencyCode)}
      >
        <SelectTrigger className="w-full">
          <SelectValue placeholder={t('settings.currency.placeholder')} />
        </SelectTrigger>
        <SelectContent>
          {currencies.map((currency) => (
            <SelectItem key={currency.code} value={currency.code}>
              {currency.symbol} {currency.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </SettingsSection>
  );
}