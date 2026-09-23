import {
  useEffect,
  useState,
  type FormEvent,
} from 'react';
import { useTranslation } from 'react-i18next';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import { useCategoryMutations } from '../hooks/useCategoryMutation';
import type { Category } from '../types';
import {
  CATEGORY_NAME_MAX_LENGTH,
  categoryFormSchema,
  parseCategoryBudget,
} from '../schemas/category.schema';
import { ColorPicker } from './ColorPicker';

interface EditCategoryDialogProps {
  category: Category | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface EditCategoryFormState {
  name: string;
  color: string;
  budgetLimit: string;
  error: string | null;
}

const getInitialState = (
  category: Category | null,
): EditCategoryFormState => ({
  name: category?.name ?? '',
  color: category?.color ?? '',
  budgetLimit:
    category?.budgetLimit !== null &&
    category?.budgetLimit !== undefined
      ? String(category.budgetLimit)
      : '',
  error: null,
});

export function EditCategoryDialog({
  category,
  open,
  onOpenChange,
}: EditCategoryDialogProps) {
  const { t } = useTranslation([
    'categories',
    'common',
    'auth',
  ]);

  const { updateCategory, isUpdating } =
    useCategoryMutations();

  const [form, setForm] =
    useState<EditCategoryFormState>(
      () => getInitialState(category),
    );

  useEffect(() => {
    if (open) {
      setForm(getInitialState(category));
    }
  }, [open, category]);

  const updateField = <
    K extends keyof EditCategoryFormState,
  >(
    field: K,
    value: EditCategoryFormState[K],
  ) => {
    setForm((previous) => ({
      ...previous,
      [field]: value,
      error: null,
    }));
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault();

    if (!category) {
      return;
    }

    const validation =
      categoryFormSchema.safeParse(form);

    if (!validation.success) {
      setForm((previous) => ({
        ...previous,
        error:
          validation.error.issues[0]?.message ??
          t('categories:updateError'),
      }));
      return;
    }

    const {
      name,
      color,
      budgetLimit,
    } = validation.data;

    try {
      await updateCategory.mutateAsync({
        id: category.id,
        data: {
          name,
          color,
          budgetLimit:
            parseCategoryBudget(budgetLimit),
        },
      });

      onOpenChange(false);
    } catch (error) {
      const message =
        error instanceof Error &&
        error.message
          ? error.message
          : t('categories:updateError');

      setForm((previous) => ({
        ...previous,
        error: message,
      }));
    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
    >
      <DialogContent className="rounded-lg border-border bg-card sm:max-w-lg">
        <div className="max-h-[85dvh] overflow-y-auto p-4 sm:p-6">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold tracking-tight text-foreground">
              {t('categories:editTitle', {
                name: category?.name ?? '',
              })}
            </DialogTitle>

            <DialogDescription className="text-sm text-muted-foreground">
              {t('categories:editDescription')}
            </DialogDescription>
          </DialogHeader>

          <form
            onSubmit={handleSubmit}
            className="space-y-5 pt-2"
            noValidate
          >
            <div className="space-y-2">
              <Label
                htmlFor="edit-name"
                className="text-sm font-medium text-foreground/80"
              >
                {t('categories:nameLabel')}
              </Label>

              <Input
                id="edit-name"
                value={form.name}
                onChange={(event) =>
                  updateField(
                    'name',
                    event.target.value,
                  )
                }
                disabled={isUpdating}
                placeholder={t(
                  'categories:namePlaceholder',
                )}
                className="h-11 rounded-xl border-border bg-background text-base"
                maxLength={CATEGORY_NAME_MAX_LENGTH}
              />
            </div>

            <div className="space-y-2">
              <Label
                htmlFor="edit-budget"
                className="text-sm font-medium text-foreground/80"
              >
                {t('categories:budgetLabel')}

                <span className="ml-1 text-xs text-muted-foreground">
                  ({t('auth:form.optional')})
                </span>
              </Label>

              <div className="relative">
                <Input
                  id="edit-budget"
                  type="number"
                  inputMode="decimal"
                  step="0.01"
                  min="0"
                  value={form.budgetLimit}
                  onChange={(event) =>
                    updateField(
                      'budgetLimit',
                      event.target.value,
                    )
                  }
                  disabled={isUpdating}
                  placeholder={t(
                    'categories:budgetPlaceholder',
                  )}
                  className="h-11 rounded-xl border-border bg-background pr-12 text-base [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
                />

                <span className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 select-none text-sm font-semibold text-muted-foreground">
                  EUR
                </span>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-medium text-foreground/80">
                {t('categories:colorLabel')}
              </Label>

              <ColorPicker
                value={form.color}
                onChange={(color) =>
                  updateField(
                    'color',
                    color,
                  )
                }
                disabled={isUpdating}
              />
            </div>

            {form.error && (
              <p
                className="rounded-xl border border-destructive/20 bg-destructive/10 p-3 text-xs font-medium text-destructive"
                role="alert"
              >
                {form.error}
              </p>
            )}

            <div className="flex justify-end gap-3 border-t border-border/40 pt-3">
              <Button
                type="button"
                variant="outline"
                onClick={() =>
                  onOpenChange(false)
                }
                disabled={isUpdating}
                className="h-10 rounded-xl border-border px-4 text-foreground hover:bg-secondary"
              >
                {t('common:actions.cancel')}
              </Button>

              <Button
                type="submit"
                disabled={isUpdating}
                className="min-h-11 h-10 rounded-xl bg-primary px-5 font-semibold text-primary-foreground transition-all hover:bg-primary/90"
              >
                {isUpdating ? (
                  <Spinner size={20} />
                ) : (
                  t('common:actions.save')
                )}
              </Button>
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
}