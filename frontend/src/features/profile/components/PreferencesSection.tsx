import { useTranslation } from 'react-i18next';
import { Settings } from 'lucide-react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';
import { Label } from '@/shared/components/core/label';
import { SettingsSection } from './SettingsSection';
import { usePreferences } from '../hooks/usePreferences';
import type { UserPreferences } from '../types';

const themes: { value: UserPreferences['theme']; labelKey: string }[] = [
  { value: 'light', labelKey: 'preferences.themeLight' },
  { value: 'dark', labelKey: 'preferences.themeDark' },
  { value: 'system', labelKey: 'preferences.themeSystem' },
];

const languages: { value: string; labelKey: string }[] = [
  { value: 'es', labelKey: 'preferences.langEs' },
  { value: 'en', labelKey: 'preferences.langEn' },
];

const dateFormats: string[] = ['DD/MM/YYYY', 'MM/DD/YYYY', 'YYYY-MM-DD'];

const currencies: { value: string; label: string }[] = [
  { value: 'EUR', label: 'EUR (€)' },
  { value: 'USD', label: 'USD ($)' },
  { value: 'GBP', label: 'GBP (£)' },
];

export function PreferencesSection() {
  const { t } = useTranslation('userProfile');
  const { preferences, isLoading, updatePreferences } = usePreferences();

  const handleThemeChange = (value: string) => {
    updatePreferences({ theme: value as UserPreferences['theme'] });
  };

  const handleLanguageChange = (value: string) => {
    updatePreferences({ language: value });
  };

  const handleDateFormatChange = (value: string) => {
    updatePreferences({ dateFormat: value });
  };

  const handleCurrencyChange = (value: string) => {
    const localeMap: Record<string, string> = {
      EUR: 'es-ES',
      USD: 'en-US',
      GBP: 'en-GB',
    };
    updatePreferences({
      currencyFormat: {
        locale: localeMap[value] ?? 'es-ES',
        currency: value,
      },
    });
  };

  return (
    <SettingsSection
      icon={Settings}
      title={t('preferences.title')}
      description={t('preferences.description')}
    >
      {isLoading ? (
        <div className="text-sm text-muted-foreground">{t('common:loading')}</div>
      ) : (
        <div className="space-y-4">
          <div className="space-y-2">
            <Label>{t('preferences.theme')}</Label>
            <Select
              value={preferences?.theme ?? 'system'}
              onValueChange={handleThemeChange}
            >
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {themes.map((tOption) => (
                  <SelectItem key={tOption.value} value={tOption.value}>
                    {t(tOption.labelKey)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>{t('preferences.language')}</Label>
            <Select
              value={preferences?.language ?? 'es'}
              onValueChange={handleLanguageChange}
            >
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {languages.map((lang) => (
                  <SelectItem key={lang.value} value={lang.value}>
                    {t(lang.labelKey)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>{t('preferences.dateFormat')}</Label>
            <Select
              value={preferences?.dateFormat ?? 'DD/MM/YYYY'}
              onValueChange={handleDateFormatChange}
            >
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {dateFormats.map((fmt) => (
                  <SelectItem key={fmt} value={fmt}>
                    {fmt}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>{t('preferences.currency')}</Label>
            <Select
              value={preferences?.currencyFormat?.currency ?? 'EUR'}
              onValueChange={handleCurrencyChange}
            >
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {currencies.map((curr) => (
                  <SelectItem key={curr.value} value={curr.value}>
                    {curr.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      )}
    </SettingsSection>
  );
}
