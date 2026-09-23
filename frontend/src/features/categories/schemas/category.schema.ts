import { z } from 'zod';

export const CATEGORY_NAME_MAX_LENGTH = 20;

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
          parsedValue >= 0
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