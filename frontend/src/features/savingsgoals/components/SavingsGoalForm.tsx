import { useId, useState, type FormEvent, type Ref } from 'react';

import { useTranslation } from 'react-i18next';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import {
  NAME_MAX_LENGTH,
  SAVINGS_GOAL_PRIORITIES,
} from '../constants';
import type { SavingsGoal } from '../types';
import { parseAmount, toCents } from '../utils/amounts';
import { toDateInputValue } from '../utils/dates';
import { isValidHttpLink } from '../utils/links';

type Priority = SavingsGoal['priority'];

interface FormState {
  name: string;
  targetAmount: string;
  priority: Priority;
  deadline: string;
  link: string;
}

export interface SavingsGoalFormValues {
  name: string;
  targetAmount: number;
  priority: Priority;
  deadline?: string;
  link?: string;
}

function getInitialForm(goal?: SavingsGoal | null): FormState {
  return {
    name: goal?.name ?? '',
    targetAmount:
      goal?.targetAmount !== undefined
        ? String(goal.targetAmount)
        : '',
    priority: goal?.priority ?? 'MEDIUM',
    deadline: toDateInputValue(goal?.deadline),
    link: goal?.link ?? '',
  };
}

interface SavingsGoalFormProps {
  initialGoal?: SavingsGoal | null;
  minTargetAmount?: number;
  isSubmitting: boolean;
  submitLabel: string;
  nameInputRef?: Ref<HTMLInputElement>;
  onSubmit: (values: SavingsGoalFormValues) => void;
  onCancel: () => void;
}

export function SavingsGoalForm({
  initialGoal,
  minTargetAmount = 0,
  isSubmitting,
  submitLabel,
  nameInputRef,
  onSubmit,
  onCancel,
}: SavingsGoalFormProps) {
  const { t } = useTranslation(['savingsGoals', 'common']);

  const [form, setForm] = useState<FormState>(() =>
    getInitialForm(initialGoal),
  );
  const [submitted, setSubmitted] = useState(false);
  const [linkTouched, setLinkTouched] = useState(false);

  const nameId = useId();
  const targetAmountId = useId();
  const priorityId = useId();
  const deadlineId = useId();
  const linkId = useId();
  const nameErrorId = useId();
  const amountErrorId = useId();
  const linkErrorId = useId();

  const updateForm = (updates: Partial<FormState>) => {
    setForm((previous) => ({ ...previous, ...updates }));
  };

  const trimmedName = form.name.trim();
  const targetAmount = parseAmount(form.targetAmount);

  const isNameValid = trimmedName.length > 0;
  const isLinkValid = isValidHttpLink(form.link);

  const amountError =
    targetAmount === null
      ? 'invalid'
      : toCents(targetAmount) < toCents(minTargetAmount)
        ? 'belowCurrent'
        : null;

  const isValid =
    isNameValid && amountError === null && isLinkValid;

  const nameError = submitted && !isNameValid;
  const showAmountError = submitted && amountError !== null;
  const linkError = (linkTouched || submitted) && !isLinkValid;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isSubmitting) {
      return;
    }

    if (!isValid || targetAmount === null) {
      setSubmitted(true);
      setLinkTouched(true);
      return;
    }

    onSubmit({
      name: trimmedName,
      targetAmount,
      priority: form.priority,
      deadline: form.deadline || undefined,
      link: form.link.trim() || undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="space-y-4 py-2 sm:py-4">
        <div className="space-y-2">
          <Label htmlFor={nameId} className="text-sm font-semibold">
            {t('name')}
          </Label>

          <Input
            id={nameId}
            ref={nameInputRef}
            placeholder={t('namePlaceholder')}
            value={form.name}
            maxLength={NAME_MAX_LENGTH}
            onChange={(event) =>
              updateForm({ name: event.target.value })
            }
            disabled={isSubmitting}
            aria-invalid={nameError}
            aria-describedby={nameError ? nameErrorId : undefined}
            className="border-border bg-background"
          />

          {nameError && (
            <p id={nameErrorId} className="text-xs text-destructive">
              {t('errors.nameRequired')}
            </p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-3 sm:gap-4">
          <div className="space-y-2">
            <Label
              htmlFor={targetAmountId}
              className="text-sm font-semibold"
            >
              {t('targetAmount')}
            </Label>

            <Input
              id={targetAmountId}
              type="number"
              inputMode="decimal"
              min="0.01"
              step="0.01"
              placeholder="0.00"
              value={form.targetAmount}
              onChange={(event) =>
                updateForm({ targetAmount: event.target.value })
              }
              disabled={isSubmitting}
              aria-invalid={showAmountError}
              aria-describedby={
                showAmountError ? amountErrorId : undefined
              }
              className="border-border bg-background"
            />
          </div>

          <div className="space-y-2">
            <Label
              htmlFor={priorityId}
              className="text-sm font-semibold"
            >
              {t('priority')}
            </Label>

            <Select
              value={form.priority}
              onValueChange={(value) =>
                updateForm({ priority: value as Priority })
              }
              disabled={isSubmitting}
            >
              <SelectTrigger
                id={priorityId}
                className="border-border bg-background"
              >
                <SelectValue />
              </SelectTrigger>

              <SelectContent className="border-border bg-card">
                {SAVINGS_GOAL_PRIORITIES.map((priority) => (
                  <SelectItem
                    key={priority.value}
                    value={priority.value}
                  >
                    {t(priority.labelKey)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>

        {showAmountError && (
          <p
            id={amountErrorId}
            className="-mt-2 text-xs text-destructive"
          >
            {amountError === 'belowCurrent'
              ? t('errors.targetBelowCurrent', {
                  amount: formatCurrency(minTargetAmount),
                })
              : t('errors.amountInvalid')}
          </p>
        )}

        <div className="space-y-2">
          <Label
            htmlFor={deadlineId}
            className="text-sm font-semibold"
          >
            {t('deadline')}
          </Label>

          <Input
            id={deadlineId}
            type="date"
            value={form.deadline}
            onChange={(event) =>
              updateForm({ deadline: event.target.value })
            }
            disabled={isSubmitting}
            className="border-border bg-background"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor={linkId} className="text-sm font-semibold">
            {t('link')}
          </Label>

          <Input
            id={linkId}
            type="url"
            placeholder={t('linkPlaceholder')}
            value={form.link}
            onChange={(event) =>
              updateForm({ link: event.target.value })
            }
            onBlur={() => setLinkTouched(true)}
            disabled={isSubmitting}
            aria-invalid={linkError}
            aria-describedby={linkError ? linkErrorId : undefined}
            className="border-border bg-background"
          />

          {linkError && (
            <p id={linkErrorId} className="text-xs text-destructive">
              {t('invalidLink')}
            </p>
          )}
        </div>
      </div>

      <div className="sticky bottom-0 flex flex-col-reverse gap-2 bg-card pb-1 pt-2 sm:flex-row sm:justify-end">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting}
          className="border-border"
        >
          {t('common:actions.cancel')}
        </Button>

        <Button
          type="submit"
          disabled={isSubmitting}
          aria-busy={isSubmitting}
          className="min-h-10 bg-primary px-6 hover:bg-primary/90"
        >
          {isSubmitting ? <Spinner size={20} /> : submitLabel}
        </Button>
      </div>
    </form>
  );
}