import {
  useId,
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
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import { CATEGORY_COLORS } from '../constants';
import { useCategoryMutations } from '../hooks/useCategoryMutations';
import type { Category } from '../types';
import {
  CATEGORY_DESCRIPTION_MAX_LENGTH,
  CATEGORY_NAME_MAX_LENGTH,
  categoryFormSchema,
  parseCategoryBudget,
  parseCategoryDescription,
} from '../schemas/category.schema';
import { ColorPicker } from './ColorPicker';

interface EditCategoryDialogProps {
  category: Category | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface EditCategoryFormState {
  name: string;
  description: string;
  color: string;
  budgetLimit: string;
  error: string | null;
}

const getInitialState = (
  category: Category | null,
): EditCategoryFormState => ({
  name: category?.name ?? '',
  description: category?.description ?? '',
  color: category?.color || CATEGORY_COLORS[0],
  budgetLimit:
    category?.budgetLimit !== null &&
      category?.budgetLimit !== undefined
      ? String(category.budgetLimit)
      : '',
  error: null,
});

/**
 * El estado del formulario se inicializa una sola vez
 * a partir de category.
 *
 * El componente padre debe remontarlo mediante key
 * cada vez que se abra una categoría diferente.
 */
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

  const colorLabelId = useId();

  const handleOpenChange = (isOpen: boolean) => {

    if (!isOpen && isUpdating) {
      return;
    }

    onOpenChange(isOpen);
  };

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

    if (isUpdating || !category) {
      return;
    }

    const validation = categoryFormSchema.safeParse({
      name: form.name,
      description: form.description,
      color: form.color,
      budgetLimit: form.budgetLimit,
    });

    if (!validation.success) {

      const issueKey =
        validation.error.issues[0]?.message;

      setForm((previous) => ({
        ...previous,
        error: issueKey
          ? t(issueKey)
          : t('categories:updateError'),
      }));

      return;
    }

    const {
      name,
      description,
      color,
      budgetLimit,
    } = validation.data;

    try {
      await updateCategory.mutateAsync({
        id: category.id,
        data: {
          name,
          description:
            parseCategoryDescription(description),
          color,
          budgetLimit:
            parseCategoryBudget(budgetLimit),
        },
      });

      onOpenChange(false);
    } catch {


    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={handleOpenChange}
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
              <div className="flex items-baseline justify-between gap-2">
                <Label
                  htmlFor="edit-description"
                  className="text-sm font-medium text-foreground/80"
                >
                  {t('categories:descriptionLabel')}

                  <span className="ml-1 text-xs text-muted-foreground">
                    ({t('auth:form.optional')})
                  </span>
                </Label>

                <span
                  className="text-xs tabular-nums text-muted-foreground"
                  aria-hidden="true"
                >
                  {form.description.length}/
                  {CATEGORY_DESCRIPTION_MAX_LENGTH}
                </span>
              </div>

              <textarea
                id="edit-description"
                rows={3}
                value={form.description}
                onChange={(event) =>
                  updateField('description', event.target.value)
                }
                disabled={isUpdating}
                placeholder={t('categories:descriptionPlaceholder')}
                maxLength={CATEGORY_DESCRIPTION_MAX_LENGTH}
                className="w-full min-w-0 resize-none rounded-xl border border-border bg-background px-3 py-2 text-base shadow-xs transition-[color,box-shadow] outline-none selection:bg-primary selection:text-primary-foreground placeholder:text-muted-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-inset focus-visible:ring-ring/50 aria-invalid:border-crust aria-invalid:ring-crust/30"
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
                  {getLocalCurrencyCode()}
                </span>
              </div>
            </div>

            <div className="space-y-2">
              <Label
                id={colorLabelId}
                className="text-sm font-medium text-foreground/80"
              >
                {t('categories:colorLabel')}
              </Label>

              <ColorPicker
                value={form.color}
                onChange={(color) =>
                  updateField('color', color)
                }
                disabled={isUpdating}
                labelledBy={colorLabelId}
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
                onClick={() => handleOpenChange(false)}
                disabled={isUpdating}
                className="h-10 rounded-xl border-border px-4 text-foreground hover:bg-secondary"
              >
                {t('common:actions.cancel')}
              </Button>

              <Button
                type="submit"
                disabled={
                  isUpdating || !form.name.trim()
                }
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
