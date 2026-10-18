import { MAX_AMOUNT } from '../constants';

const AMOUNT_PATTERN = /^\d+(\.\d{1,2})?$/;

export const parseAmount = (value: string): number | null => {
  const trimmed = value.trim();

  if (!AMOUNT_PATTERN.test(trimmed)) {
    return null;
  }

  const amount = Number(trimmed);

  if (
    !Number.isFinite(amount) ||
    amount <= 0 ||
    amount > MAX_AMOUNT
  ) {
    return null;
  }

  return amount;
};

export const toCents = (amount: number): number =>
  Math.round(amount * 100);

export const getRemainingAmount = (goal: {
  targetAmount: number;
  currentAmount: number;
}): number =>
  Math.max(
    0,
    (toCents(goal.targetAmount) - toCents(goal.currentAmount)) /
      100,
  );