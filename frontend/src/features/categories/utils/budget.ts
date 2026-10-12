import type { PeriodFilter } from '@/shared/components/PeriodSelector';

const MONTHS_PER_YEAR = 12;
const WEEKS_PER_YEAR = 52;

/**
 * Categories store their budget as a monthly amount. This converts it to
 * the selected period.
 *
 * A month is not exactly 4 weeks (it is ~4.33), so the weekly amount is
 * derived from the yearly one: monthly * 12 / 52.
 *
 * A limit of 0 or less is treated as "no budget".
 */
export function getPeriodBudgetLimit(
  monthlyLimit: number | null | undefined,
  period: PeriodFilter,
): number | null {
  if (
    monthlyLimit === null ||
    monthlyLimit === undefined ||
    monthlyLimit <= 0
  ) {
    return null;
  }

  switch (period) {
    case 'week':
      return (monthlyLimit * MONTHS_PER_YEAR) / WEEKS_PER_YEAR;

    case 'year':
      return monthlyLimit * MONTHS_PER_YEAR;

    default:
      return monthlyLimit;
  }
}
