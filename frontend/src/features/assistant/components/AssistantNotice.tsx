import { AlertTriangle, Sparkles } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

import type { AssistantInterpretation } from '../types';

interface AssistantNoticeProps {
  interpretation: AssistantInterpretation;
}

export function AssistantNotice({
  interpretation,
}: AssistantNoticeProps) {
  const { t } = useTranslation('dashboard');

  const hasMissingFields =
    interpretation.missingFields.length > 0;

  const hasUnresolvedEntities =
    interpretation.unresolvedEntities.length > 0;

  const hasDefaults =
    interpretation.defaultsApplied.length > 0;

  const hasWarnings =
    interpretation.status === 'NEEDS_INFORMATION' ||
    hasMissingFields ||
    hasUnresolvedEntities;

  const fieldLabel = (field: string) =>
    String(
      t(`assistant.fields.${field}`, {
        defaultValue: field,
      }),
    );

  const missingFields = interpretation.missingFields
    .map(({ field }) => fieldLabel(field))
    .join(', ');

  const unresolvedEntities =
    interpretation.unresolvedEntities
      .map((entity) => {
        const type = String(
          t(`assistant.entityTypes.${entity.type}`, {
            defaultValue: entity.type,
          }),
        );

        const status = String(
          t(`assistant.entityStatus.${entity.status}`, {
            defaultValue: entity.status,
          }),
        );

        return `${type} "${entity.reference}" (${status})`;
      })
      .join(', ');

  const defaultFields = interpretation.defaultsApplied
    .map(({ field }) => fieldLabel(field))
    .join(', ');

  const Icon = hasWarnings ? AlertTriangle : Sparkles;

  return (
    <div
      className={cn(
        'mt-3 rounded-lg border p-3 text-sm',
        hasWarnings
          ? 'border-warning/40 bg-warning/10'
          : 'border-primary/20 bg-primary/5',
      )}
      role="status"
    >
      <p className="flex items-center gap-2 font-medium text-foreground">
        <Icon
          className={cn(
            'size-4 shrink-0',
            hasWarnings ? 'text-warning' : 'text-primary',
          )}
          aria-hidden="true"
        />
        {t('assistant.notice.reviewTitle')}
      </p>

      <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
        {t('assistant.notice.reviewDescription')}
      </p>

      {hasMissingFields && (
        <p className="mt-2 text-xs leading-relaxed text-foreground">
          <span className="font-medium">
            {t('assistant.notice.missingTitle')}:
          </span>{' '}
          {t('assistant.notice.missingDescription', {
            fields: missingFields,
          })}
        </p>
      )}

      {hasUnresolvedEntities && (
        <p className="mt-1 text-xs leading-relaxed text-foreground">
          <span className="font-medium">
            {t('assistant.notice.unresolvedTitle')}:
          </span>{' '}
          {t('assistant.notice.unresolvedDescription', {
            entities: unresolvedEntities,
          })}
        </p>
      )}

      {hasDefaults && (
        <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
          {t('assistant.notice.defaultsDescription', {
            fields: defaultFields,
          })}
        </p>
      )}
    </div>
  );
}
