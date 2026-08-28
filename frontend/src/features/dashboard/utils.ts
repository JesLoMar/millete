import { formatDate as sharedFormatDate } from '@/shared/utils/date'

export function formatDate(dateStr: string): string {
  return sharedFormatDate(dateStr)
}
