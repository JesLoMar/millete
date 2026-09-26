import { useEffect, useId, useMemo, useState } from 'react';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Clock3 } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import { Label } from '@/shared/components/core/label';
import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { UserPreferences } from '../types';
import { SettingsSection } from './SettingsSection';

function getBrowserTimezone(): string {
  return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
}

function timezoneLabel(timezone: string): string {
  const offset = new Intl.DateTimeFormat(undefined, {
    timeZone: timezone,
    timeZoneName: 'shortOffset',
  })
    .formatToParts(new Date())
    .find((part) => part.type === 'timeZoneName')?.value;
  const city = timezone.split('/').at(-1)?.replaceAll('_', ' ') ?? timezone;
  return `${offset ? `(${offset}) ` : ''}${city} — ${timezone}`;
}

export function TimezoneSection() {
  const { t } = useTranslation(['userProfile', 'common']);
  const queryClient = useQueryClient();
  const timezoneId = useId();
  const { data: preferences, isLoading } = useQuery<UserPreferences>({
    queryKey: ['preferences'],
    queryFn: profileService.getPreferences,
  });
  const [timezone, setTimezone] = useState(getBrowserTimezone);

  useEffect(() => {
    if (preferences) {
      setTimezone(preferences.timezone ?? getBrowserTimezone());
    }
  }, [preferences]);

  const timezones = useMemo(() => {
    const supported = Intl.supportedValuesOf('timeZone');
    const browserTimezone = getBrowserTimezone();
    return Array.from(new Set([...supported, browserTimezone])).sort(
      (left, right) => left.localeCompare(right),
    );
  }, []);

  const saveTimezone = useMutation({
    mutationFn: async (selectedTimezone: string) => {
      // The backend replaces the preference map, so merge with its current value.
      const current = await profileService.getPreferences();
      await profileService.updatePreferences({
        ...current,
        timezone: selectedTimezone,
      });
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['preferences'] });
      notify.success(t('timezone.success'));
    },
    onError: () => notify.error(t('timezone.error')),
  });

  return (
    <SettingsSection
      icon={Clock3}
      title={t('timezone.title')}
      description={t('timezone.description')}
    >
      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor={timezoneId}>{t('timezone.label')}</Label>
          {isLoading ? (
            <div className="text-sm text-muted-foreground">
              {t('common:loading')}
            </div>
          ) : (
            <select
              id={timezoneId}
              className="h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={timezone}
              onChange={(event) => setTimezone(event.target.value)}
              disabled={saveTimezone.isPending}
            >
              {timezones.map((option) => (
                <option key={option} value={option}>
                  {timezoneLabel(option)}
                </option>
              ))}
            </select>
          )}
        </div>
        <Button
          type="button"
          onClick={() => saveTimezone.mutate(timezone)}
          disabled={isLoading || saveTimezone.isPending || timezone === preferences?.timezone}
        >
          {saveTimezone.isPending ? <Spinner size={20} /> : t('timezone.save')}
        </Button>
      </div>
    </SettingsSection>
  );
}
