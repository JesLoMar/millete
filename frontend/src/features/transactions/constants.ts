export const FREQUENCY_TYPES = [
  { value: "DAYS", labelKey: "transactions.recurring.days" },
  { value: "WEEKS", labelKey: "transactions.recurring.weeks" },
  { value: "MONTHS", labelKey: "transactions.recurring.months" },
  { value: "YEARS", labelKey: "transactions.recurring.years" },
] as const

export const FREQUENCY_LABELS: Record<string, string> = {
  DAYS: "transactions.recurring.days",
  WEEKS: "transactions.recurring.weeks",
  MONTHS: "transactions.recurring.months",
  YEARS: "transactions.recurring.years",
}

export const FREQUENCY_SINGULAR: Record<string, string> = {
  DAYS: "transactions.recurring.everyDay",
  WEEKS: "transactions.recurring.everyWeek",
  MONTHS: "transactions.recurring.everyMonth",
  YEARS: "transactions.recurring.everyYear",
}

export const CATEGORY_COLORS: Record<string, string> = {
  Alimentación: "text-emerald-500 bg-emerald-500/10",
  Hogar: "text-amber-500 bg-amber-500/10",
  Transporte: "text-blue-500 bg-blue-500/10",
  Suministros: "text-purple-500 bg-purple-500/10",
  Ocio: "text-pink-500 bg-pink-500/10",
}

export const FILTERS = ["all", "income", "expense"] as const
export type Filter = typeof FILTERS[number]

export const FILTER_LABELS: Record<Filter, string> = {
  all: "transactions.filterAll",
  income: "transactions.filterIncome",
  expense: "transactions.filterExpense",
}