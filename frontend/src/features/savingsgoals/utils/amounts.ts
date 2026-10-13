import { MAX_AMOUNT } from '../constants';

const AMOUNT_PATTERN = /^\d+(\.\d{1,2})?$/;

/**
 * Convierte el texto de un input numérico en un importe válido:
 * mayor que 0, como máximo 2 decimales y sin notación científica.
 * Devuelve null si no es válido.
 */
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

/** Compara importes en céntimos para evitar errores de coma flotante. */
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