const DATE_PREFIX_PATTERN = /^(\d{4})-(\d{2})-(\d{2})/;

/**
 * Devuelve yyyy-MM-dd (el único formato que acepta <input type="date">) a
 * partir de "2026-12-31" o de un ISO con hora ("2026-12-31T00:00:00Z").
 * Devuelve '' si no se reconoce.
 */
export const toDateInputValue = (value?: string | null): string => {
  const match = value ? DATE_PREFIX_PATTERN.exec(value) : null;

  return match ? `${match[1]}-${match[2]}-${match[3]}` : '';
};

const parseDateOnly = (value: string): Date | null => {
  const iso = toDateInputValue(value);

  if (!iso) {
    return null;
  }

  const [year, month, day] = iso.split('-').map(Number);
  const date = new Date(year, month - 1, day);

  return Number.isNaN(date.getTime()) ? null : date;
};

export const formatDeadline = (
  value: string,
  locale?: string,
): string => {
  const date = parseDateOnly(value);

  return date
    ? new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(
        date,
      )
    : value;
};

export const isPastDeadline = (
  value: string,
  now: Date = new Date(),
): boolean => {
  const date = parseDateOnly(value);

  if (!date) {
    return false;
  }

  const today = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
  );

  return date < today;
};