import { z } from 'zod';

import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

export const CATEGORY_NAME_MAX_LENGTH = 20;
export const CATEGORY_DESCRIPTION_MAX_LENGTH = 500;
const CATEGORY_BUDGET_PATTERN = /^\d+(?:\.(\d+))?$/;

function getLocalCurrencyFractionDigits(): number {
  try {
    return new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency: getLocalCurrencyCode(),
    }).resolvedOptions().maximumFractionDigits;
  } catch {
    // Keep a sensible fallback if the configured currency is invalid.
    return 2;
  }
}

function hasValidBudgetPrecision(value: string): boolean {
  const match = CATEGORY_BUDGET_PATTERN.exec(value);

  if (!match) {
    return false;
  }

  const decimalPlaces = match[1]?.length ?? 0;
  return decimalPlaces <= getLocalCurrencyFractionDigits();
}

export function getCategoryBudgetStep(): string {
  const decimalPlaces = getLocalCurrencyFractionDigits();

  if (decimalPlaces === 0) {
    return '1';
  }

  return `0.${'0'.repeat(decimalPlaces - 1)}1`;
}

export const categoryFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, {
      message: 'categories:nameRequired',
    })
    .max(CATEGORY_NAME_MAX_LENGTH, {
      message: 'categories:nameTooLong',
    }),

  description: z
    .string()
    .trim()
    .max(CATEGORY_DESCRIPTION_MAX_LENGTH, {
      message: 'categories:descriptionTooLong',
    }),

  color: z
    .string()
    .regex(/^#[0-9A-Fa-f]{6}$/, {
      message: 'categories:invalidColor',
    }),

  budgetLimit: z
    .string()
    .trim()
    .refine(
      (value) => {
        if (value === '') {
          return true;
        }

        const parsedValue = Number(value);

        return (
          Number.isFinite(parsedValue) &&
          parsedValue >= 0 &&
          hasValidBudgetPrecision(value)
        );
      },
      {
        message: 'categories:invalidBudget',
      },
    ),
});

export type CategoryFormData = z.infer<
  typeof categoryFormSchema
>;

export const parseCategoryBudget = (
  value: string,
): number | null => {
  const trimmedValue = value.trim();

  if (trimmedValue === '') {
    return null;
  }

  return Number(trimmedValue);
};

/**
 * Empty descriptions are sent as null so the backend does not
 * store blank strings.
 */
export const parseCategoryDescription = (
  value: string,
): string | null => {
  const trimmedValue = value.trim();

  return trimmedValue === '' ? null : trimmedValue;
};
