const DATE_PREFIX_PATTERN = /^(\d{4})-(\d{2})-(\d{2})/;

interface ParsedDate {
  iso: string;
  date: Date;
}

const parseDatePrefix = (
  value?: string | null,
): ParsedDate | null => {
  const match = value
    ? DATE_PREFIX_PATTERN.exec(value)
    : null;

  if (!match) {
    return null;
  }

  const [, yearText, monthText, dayText] = match;

  const year = Number(yearText);
  const month = Number(monthText);
  const day = Number(dayText);

  const date = new Date(year, month - 1, day);

  if (
    Number.isNaN(date.getTime()) ||
    date.getFullYear() !== year ||
    date.getMonth() !== month - 1 ||
    date.getDate() !== day
  ) {
    return null;
  }

  return {
    iso: `${yearText}-${monthText}-${dayText}`,
    date,
  };
};

export const toDateInputValue = (
  value?: string | null,
): string => {
  return parseDatePrefix(value)?.iso ?? '';
};

const parseDateOnly = (value: string): Date | null => {
  return parseDatePrefix(value)?.date ?? null;
};

export const formatDeadline = (
  value: string,
  locale?: string,
): string => {
  const date = parseDateOnly(value);

  return date
    ? new Intl.DateTimeFormat(locale, {
        dateStyle: 'medium',
      }).format(date)
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