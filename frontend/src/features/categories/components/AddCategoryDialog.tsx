import {
  useId,
  useRef,
  useState,
  type FormEvent,
} from 'react';
import axios from 'axios';
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

import {
  CATEGORY_COLORS,
} from '../constants';
import {
  CATEGORY_NAME_MAX_LENGTH,
  categoryFormSchema,
  parseCategoryBudget,
} from '../schemas/category.schema';
import { useCategoryMutations } from '../hooks/useCategoryMutation';
import { ColorPicker } from './ColorPicker';

interface AddCategoryDialogProps {
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
}

interface CategoryFormState {
  name: string;
  color: string;
  budgetLimit: string;
  error: string | null;
}

const createInitialFormState =
  (): CategoryFormState => ({
    name: '',
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
  ]);

  const { createCategory, isCreating } =
    useCategoryMutations();

  const [internalOpen, setInternalOpen] =
    useState(false);

  const [form, setForm] =
    useState<CategoryFormState>(
      createInitialFormState,
    );

  const inputRef =
    useRef<HTMLInputElement>(null);

  const nameInputId = useId();
  const colorInputId = useId();
  const budgetInputId = useId();

  const isControlled =
    controlledOpen !== undefined;

  const open = isControlled
    ? controlledOpen
    : internalOpen;

  const setOpen = (value: boolean) => {
    if (isControlled) {
      controlledOnOpenChange?.(value);
      return;
    }

    setInternalOpen(value);
  };

  const resetForm = () => {
    setForm(createInitialFormState());
  };

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen);

    if (!isOpen) {
      resetForm();
    }
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
    const validation = categoryFormSchema.safeParse(
      form,
    );

    if (!validation.success) {
      setForm((previous) => ({
        ...previous,
        error:
          validation.error.issues[0]?.message ??
          t('categories:createError'),
      }));
      return;
    }

    const {
      name,
      color,
      budgetLimit,
    } = validation.data;

    try {
      await createCategory.mutateAsync({
        name,
        color,
        budgetLimit:
          parseCategoryBudget(budgetLimit),
      });

      handleOpenChange(false);
    } catch (error) {
      const backendMessage =
        axios.isAxiosError(error)
          ? error.response?.data?.message
          : undefined;

      const message =
        typeof backendMessage === 'string' &&
        backendMessage.trim()
          ? backendMessage
          : t('categories:createError');

      setForm((previous) => ({
        ...previous,
        error: message,
      }));
    }
  };

  const handleSubmit = (
    event: FormEvent<HTMLFormElement>,
  ) => {
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
                placeholder={t(
                  'categories:namePlaceholder',
                )}
                value={form.name}
                onChange={(event) =>
                  updateField(
                    'name',
                    event.target.value,
                  )
                }
                disabled={isCreating}
                maxLength={CATEGORY_NAME_MAX_LENGTH}
                className="border-border bg-background"
              />
            </div>

            <div className="space-y-2">
              <Label
                htmlFor={colorInputId}
                className="text-sm font-semibold"
              >
                {t('categories:color')}
              </Label>

              <div id={colorInputId}>
                <ColorPicker
                  value={form.color}
                  onChange={(color) =>
                    updateField(
                      'color',
                      color,
                    )
                  }
                  disabled={isCreating}
                />
              </div>
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
                  updateField(
                    'budgetLimit',
                    event.target.value,
                  )
                }
                disabled={isCreating}
                min="0"
                step="0.01"
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
              onClick={() =>
                handleOpenChange(false)
              }
              disabled={isCreating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="submit"
              disabled={
                isCreating ||
                !form.name.trim()
              }
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