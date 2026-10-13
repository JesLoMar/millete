import {
  useEffect,
  useId,
  useRef,
  useState,
  type FormEvent,
} from 'react';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';

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

import { CATEGORY_COLORS } from '../constants';
import {
  CATEGORY_DESCRIPTION_MAX_LENGTH,
  CATEGORY_NAME_MAX_LENGTH,
  categoryFormSchema,
  getCategoryBudgetStep,
  parseCategoryBudget,
  parseCategoryDescription,
} from '../schemas/category.schema';
import { useCategoryMutations } from '../hooks/useCategoryMutations';
import { ColorPicker } from './ColorPicker';

interface AddCategoryDialogProps {
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
}

interface CategoryFormState {
  name: string;
  description: string;
  color: string;
  budgetLimit: string;
  error: string | null;
}

const createInitialFormState = (): CategoryFormState => ({
  name: '',
  description: '',
  color: CATEGORY_COLORS[0],
  budgetLimit: '',
  error: null,
});

export function AddCategoryDialog({
  open: controlledOpen,
  onOpenChange: controlledOnOpenChange,
}: AddCategoryDialogProps = {}) {
  const { t } = useTranslation([
    'categories',
    'common',
    'auth',
  ]);

  const { createCategory, isCreating } =
    useCategoryMutations();

  const [internalOpen, setInternalOpen] = useState(false);
  const [form, setForm] = useState<CategoryFormState>(
    createInitialFormState,
  );

  const inputRef = useRef<HTMLInputElement>(null);
  const nameInputId = useId();
  const descriptionInputId = useId();
  const colorLabelId = useId();
  const budgetInputId = useId();

  const isControlled = controlledOpen !== undefined;
  const open = isControlled ? controlledOpen : internalOpen;

  const previousOpenRef = useRef(open);

  // Reset the form on every real open -> closed transition, including when
  // a parent controls `open` and closes the dialog directly.
  useEffect(() => {
    if (previousOpenRef.current && !open) {
      setForm(createInitialFormState());
    }

    previousOpenRef.current = open;
  }, [open]);

  const setOpen = (value: boolean) => {
    if (isControlled) {
      controlledOnOpenChange?.(value);
      return;
    }

    setInternalOpen(value);
  };

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen && isCreating) {
      return;
    }

    setOpen(isOpen);
  };

  const updateField = <
    K extends keyof CategoryFormState,
  >(
    field: K,
    value: CategoryFormState[K],
  ) => {
    setForm((previous) => ({
      ...previous,
      [field]: value,
      error: null,
    }));
  };

  const handleSave = async () => {
    if (isCreating) {
      return;
    }

    const validation = categoryFormSchema.safeParse({
      name: form.name,
      description: form.description,
      color: form.color,
      budgetLimit: form.budgetLimit,
    });

    if (!validation.success) {
      const issueKey = validation.error.issues[0]?.message;

      setForm((previous) => ({
        ...previous,
        error: issueKey
          ? t(issueKey)
          : t('categories:createError'),
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
      await createCategory.mutateAsync({
        name,
        description: parseCategoryDescription(description),
        color,
        budgetLimit: parseCategoryBudget(budgetLimit),
      });

      handleOpenChange(false);
    } catch {
      // useCategoryMutations already notifies the user. Keep the dialog open
      // after a failed mutation so the form can be corrected or retried.
    }
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    void handleSave();
  };

  return (
    <Dialog
      open={open}
      onOpenChange={handleOpenChange}
    >
      {!isControlled && (
        <DialogTrigger asChild>
          <Button className="h-9 gap-2 px-4 font-semibold">
            <Plus
              size={16}
              aria-hidden="true"
            />
            {t('categories:add')}
          </Button>
        </DialogTrigger>
      )}

      <DialogContent
        className="border-border bg-card sm:max-w-md"
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <form
          onSubmit={handleSubmit}
          className="max-h-[85dvh] overflow-y-auto"
          noValidate
        >
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold">
              {t('categories:newTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label
                htmlFor={nameInputId}
                className="text-sm font-semibold"
              >
                {t('categories:name')}
              </Label>

              <Input
                id={nameInputId}
                ref={inputRef}
                placeholder={t('categories:namePlaceholder')}
                value={form.name}
                onChange={(event) =>
                  updateField('name', event.target.value)
                }
                disabled={isCreating}
                maxLength={CATEGORY_NAME_MAX_LENGTH}
                className="border-border bg-background"
              />
            </div>

            <div className="space-y-2">
              <div className="flex items-baseline justify-between gap-2">
                <Label
                  htmlFor={descriptionInputId}
                  className="text-sm font-semibold"
                >
                  {t('categories:description')}

                  <span className="ml-1 text-xs font-normal text-muted-foreground">
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
                id={descriptionInputId}
                rows={3}
                placeholder={t('categories:descriptionPlaceholder')}
                value={form.description}
                onChange={(event) =>
                  updateField('description', event.target.value)
                }
                disabled={isCreating}
                maxLength={CATEGORY_DESCRIPTION_MAX_LENGTH}
                className="w-full min-w-0 resize-none rounded-md border border-border bg-background px-3 py-2 text-base shadow-xs transition-[color,box-shadow] outline-none selection:bg-primary selection:text-primary-foreground placeholder:text-muted-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-inset focus-visible:ring-ring/50 aria-invalid:border-crust aria-invalid:ring-crust/30"
              />
            </div>

            <div className="space-y-2">
              <Label
                id={colorLabelId}
                className="text-sm font-semibold"
              >
                {t('categories:color')}
              </Label>

              <ColorPicker
                value={form.color}
                onChange={(color) => updateField('color', color)}
                disabled={isCreating}
                labelledBy={colorLabelId}
              />
            </div>

            <div className="space-y-2">
              <Label
                htmlFor={budgetInputId}
                className="text-sm font-semibold"
              >
                {t('categories:budget')}
              </Label>

              <Input
                id={budgetInputId}
                type="number"
                inputMode="decimal"
                placeholder="0.00"
                value={form.budgetLimit}
                onChange={(event) =>
                  updateField('budgetLimit', event.target.value)
                }
                disabled={isCreating}
                min="0"
                step={getCategoryBudgetStep()}
                className="border-border bg-background"
              />

              <p className="text-xs text-muted-foreground">
                {t('categories:budgetHint')}
              </p>
            </div>

            {form.error && (
              <p
                className="text-center text-sm text-destructive"
                role="alert"
              >
                {form.error}
              </p>
            )}
          </div>

          <DialogFooter className="sticky bottom-0 gap-2 bg-card pb-1 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => handleOpenChange(false)}
              disabled={isCreating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="submit"
              disabled={isCreating || !form.name.trim()}
              className="bg-primary px-6 hover:bg-primary/90"
            >
              {isCreating ? (
                <Spinner size={20} />
              ) : (
                t('categories:save')
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
