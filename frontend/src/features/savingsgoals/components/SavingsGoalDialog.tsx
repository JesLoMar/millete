import { useId, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { PiggyBank, Plus } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';

import { useCreateSavingsGoal } from '../hooks/useSavingsGoals';
import type { SavingsGoal } from '../types';

const PRIORITIES = [
  {
    value: 'LOW',
    labelKey: 'priorities.LOW',
  },
  {
    value: 'MEDIUM',
    labelKey: 'priorities.MEDIUM',
  },
  {
    value: 'HIGH',
    labelKey: 'priorities.HIGH',
  },
] as const;

type Priority = SavingsGoal['priority'];

interface FormState {
  name: string;
  targetAmount: string;
  priority: Priority;
  deadline: string;
  link: string;
}

const INITIAL_FORM: FormState = {
  name: '',
  targetAmount: '',
  priority: 'MEDIUM',
  deadline: '',
  link: '',
};

const isValidLink = (link: string): boolean => {
  const trimmed = link.trim();

  if (!trimmed) {
    return true;
  }

  const withProtocol = /^https?:\/\//i.test(trimmed)
    ? trimmed
    : `https://${trimmed}`;

  try {
    const url = new URL(withProtocol);

    return (
      url.protocol === 'http:' ||
      url.protocol === 'https:'
    );
  } catch {
    return false;
  }
};

export function SavingsGoalDialog() {
  const { t } = useTranslation([
    'savingsGoals',
    'common',
  ]);

  const {
    mutateAsync: createGoal,
    isPending: isCreating,
  } = useCreateSavingsGoal();

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [linkTouched, setLinkTouched] = useState(false);

  const inputRef = useRef<HTMLInputElement>(null);

  const nameId = useId();
  const targetAmountId = useId();
  const priorityId = useId();
  const deadlineId = useId();
  const linkId = useId();
  const linkErrorId = useId();

  const updateForm = (updates: Partial<FormState>) => {
    setForm((previous) => ({
      ...previous,
      ...updates,
    }));
  };

  const resetForm = () => {
    setForm({ ...INITIAL_FORM });
    setLinkTouched(false);
  };

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen);

    if (!isOpen) {
      resetForm();
    }
  };

  const targetAmount = Number(form.targetAmount);
  const linkError =
    linkTouched && !isValidLink(form.link);

  const isValid =
    form.name.trim().length > 0 &&
    Number.isFinite(targetAmount) &&
    targetAmount > 0 &&
    isValidLink(form.link);

  const handleSave = async () => {
    if (!isValid) {
      setLinkTouched(true);
      return;
    }

    try {
      await createGoal({
        name: form.name.trim(),
        targetAmount,
        priority: form.priority,
        deadline: form.deadline || undefined,
        link: form.link.trim() || undefined,
      });

      setOpen(false);
      resetForm();
    } catch {
      // The mutation hook already displays the error notification.
      // Keep the dialog open so the user can retry.
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button
          type="button"
          className="h-9 gap-2 bg-primary px-4 font-semibold hover:bg-primary/90"
        >
          <Plus size={16} aria-hidden="true" />
          {t('newGoal')}
        </Button>
      </DialogTrigger>

      <DialogContent
        className="border-border bg-card sm:max-w-md"
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-xl font-semibold">
              <PiggyBank
                className="size-5 text-primary"
                aria-hidden="true"
              />
              {t('newGoalTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label
                htmlFor={nameId}
                className="text-sm font-semibold"
              >
                {t('name')}
              </Label>

              <Input
                id={nameId}
                ref={inputRef}
                placeholder={t('namePlaceholder')}
                value={form.name}
                onChange={(event) =>
                  updateForm({
                    name: event.target.value,
                  })
                }
                disabled={isCreating}
                className="border-border bg-background"
              />
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
                  min="0.01"
                  step="0.01"
                  placeholder="0.00"
                  value={form.targetAmount}
                  onChange={(event) =>
                    updateForm({
                      targetAmount: event.target.value,
                    })
                  }
                  disabled={isCreating}
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
                    updateForm({
                      priority: value as Priority,
                    })
                  }
                  disabled={isCreating}
                >
                  <SelectTrigger
                    id={priorityId}
                    className="border-border bg-background"
                  >
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent className="border-border bg-card">
                    {PRIORITIES.map((priority) => (
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
                  updateForm({
                    deadline: event.target.value,
                  })
                }
                disabled={isCreating}
                className="border-border bg-background"
              />
            </div>

            <div className="space-y-2">
              <Label
                htmlFor={linkId}
                className="text-sm font-semibold"
              >
                {t('link')}
              </Label>

              <Input
                id={linkId}
                type="url"
                placeholder={t('linkPlaceholder')}
                value={form.link}
                onChange={(event) =>
                  updateForm({
                    link: event.target.value,
                  })
                }
                onBlur={() => setLinkTouched(true)}
                disabled={isCreating}
                aria-invalid={linkError}
                aria-describedby={
                  linkError ? linkErrorId : undefined
                }
                className="border-border bg-background"
              />

              {linkError && (
                <p
                  id={linkErrorId}
                  className="text-xs text-destructive"
                >
                  {t('invalidLink')}
                </p>
              )}
            </div>
          </div>

          <DialogFooter className="sticky bottom-0 gap-2 bg-card pb-1 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={isCreating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="button"
              onClick={handleSave}
              disabled={isCreating || !isValid}
              className="bg-primary px-6 hover:bg-primary/90"
            >
              {isCreating ? (
                <Spinner size={20} />
              ) : (
                t('create')
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}