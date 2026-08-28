import i18n from '@/lib/i18n';

export function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString(i18n.language || 'es-ES', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}
