import { useEffect, useId, useMemo, useState } from 'react';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Coins } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import { Label } from '@/shared/components/core/label';
import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { CurrencyCode, UserPreferences } from '../types';
import { SettingsSection } from './SettingsSection';

const CURRENCIES: CurrencyCode[] = (() => {
  try {
    return Intl.supportedValuesOf('currency');
  } catch {
    return ['EUR', 'USD', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD'];
  }
})();

export function LocalCurrencySection() {
  const { t, i18n } = useTranslation(['userProfile', 'common']);
  const queryClient = useQueryClient();
  const currencyId = useId();
  const { data: preferences, isLoading } = useQuery<UserPreferences>({
    queryKey: ['preferences'],
    queryFn: profileService.getPreferences,
  });
  const [currency, setCurrency] = useState<CurrencyCode>('EUR');
  const currencyNames = useMemo(
    () => new Intl.DisplayNames([i18n.language], { type: 'currency' }),
    [i18n.language],
  );

  useEffect(() => {
    if (preferences) {
      const saved = preferences.localCurrency ?? preferences.currencyFormat?.currency;
      setCurrency(CURRENCIES.includes(saved as CurrencyCode)
        ? saved as CurrencyCode
        : 'EUR');
    }
  }, [preferences]);

  const saveCurrency = useMutation({
    mutationFn: async (selectedCurrency: CurrencyCode) => {
      const current = await profileService.getPreferences();
      await profileService.updatePreferences({
        ...current,
        localCurrency: selectedCurrency,
      });
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['preferences'] });
      notify.success(t('preferences.success'));
    },
    onError: () => notify.error(t('preferences.error')),
  });

  return (
    <SettingsSection
      icon={Coins}
      title={t('preferences.localCurrencyTitle')}
      description={t('preferences.localCurrencyDescription')}
    >
      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor={currencyId}>{t('preferences.currency')}</Label>
          {isLoading ? (
            <div className="text-sm text-muted-foreground">
              {t('common:loading')}
            </div>
          ) : (
            <select
              id={currencyId}
              className="h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={currency}
              onChange={(event) => setCurrency(event.target.value as CurrencyCode)}
              disabled={saveCurrency.isPending}
            >
              {CURRENCIES.map((code) => (
                <option key={code} value={code}>
                  {code} — {currencyNames.of(code) ?? code}
                </option>
              ))}
            </select>
          )}
        </div>
        <Button
          type="button"
          onClick={() => saveCurrency.mutate(currency)}
          disabled={isLoading || saveCurrency.isPending || currency === (preferences?.localCurrency ?? preferences?.currencyFormat?.currency ?? 'EUR')}
        >
          {saveCurrency.isPending ? <Spinner size={20} /> : t('personalInfo.save')}
        </Button>
      </div>
    </SettingsSection>
  );
}
