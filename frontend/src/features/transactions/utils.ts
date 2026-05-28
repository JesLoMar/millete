import type { PlannedTransaction } from "@/shared/hooks/usePlannedTransactions"
import { FREQUENCY_LABELS, FREQUENCY_SINGULAR } from "./constants"
import i18n from '@/lib/i18n';
export { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat.ts';

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString(i18n.language, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export function formatFrequency(
  tx: PlannedTransaction,
  t: (key: string, options?: Record<string, unknown>) => string
): string {
  if (tx.frequencyInterval === 1) {
    return t(FREQUENCY_SINGULAR[tx.frequencyType] || "")
  }
  return t("transactions.recurring.every", {
    interval: tx.frequencyInterval,
    unit: t(FREQUENCY_LABELS[tx.frequencyType] || ""),
  })
}

export function calculateNextExecution(tx: PlannedTransaction): string {
  if (!tx.startDate) return "—"
  const start = new Date(tx.startDate)
  const today = new Date()
  if (start > today) return formatDate(tx.startDate)

  const interval = tx.frequencyInterval || 1
  const next = new Date(start)

  while (next <= today) {
    switch (tx.frequencyType) {
      case "DAYS": next.setDate(next.getDate() + interval); break
      case "WEEKS": next.setDate(next.getDate() + interval * 7); break
      case "MONTHS": next.setMonth(next.getMonth() + interval); break
      case "YEARS": next.setFullYear(next.getFullYear() + interval); break
      default: return "—"
    }
  }

  if (tx.endDate && next > new Date(tx.endDate)) return "—"
  return formatDate(next.toISOString())
}