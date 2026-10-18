import i18n from '@/lib/i18n';

const DATE_ONLY_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;

export function formatDate(dateStr: string): string {
  const locale = i18n.language || 'es-ES';
  const dateOnlyMatch = DATE_ONLY_PATTERN.exec(dateStr);

  let date: Date;

  if (dateOnlyMatch) {
    const year = Number(dateOnlyMatch[1]);
    const month = Number(dateOnlyMatch[2]);
    const day = Number(dateOnlyMatch[3]);

    date = new Date(year, month - 1, day);

    if (
      date.getFullYear() !== year ||
      date.getMonth() !== month - 1 ||
      date.getDate() !== day
    ) {
      return dateStr;
    }
  } else {
    date = new Date(dateStr);

    if (Number.isNaN(date.getTime())) {
      return dateStr;
    }
  }

  return date.toLocaleDateString(locale, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}
