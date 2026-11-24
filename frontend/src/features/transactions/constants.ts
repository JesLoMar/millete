export const TRANSACTION_DISPLAY_SIZE = 15
// Keep each backend chunk aligned with four complete UI pages.
export const TRANSACTION_SERVER_SIZE = TRANSACTION_DISPLAY_SIZE * 4

export const FREQUENCY_TYPES = [
  { value: "DAYS", labelKey: "transactions:recurring.days" },
  { value: "WEEKS", labelKey: "transactions:recurring.weeks" },
  { value: "MONTHS", labelKey: "transactions:recurring.months" },
  { value: "YEARS", labelKey: "transactions:recurring.years" },
] as const

export const FREQUENCY_LABELS: Record<string, string> = {
  DAYS: "transactions:recurring.days",
  WEEKS: "transactions:recurring.weeks",
  MONTHS: "transactions:recurring.months",
  YEARS: "transactions:recurring.years",
}

export const FREQUENCY_SINGULAR: Record<string, string> = {
  DAYS: "transactions:recurring.everyDay",
  WEEKS: "transactions:recurring.everyWeek",
  MONTHS: "transactions:recurring.everyMonth",
  YEARS: "transactions:recurring.everyYear",
}

export const FILTERS = ["all", "income", "expense"] as const
export type Filter = typeof FILTERS[number]

export const FILTER_LABELS: Record<Filter, string> = {
  all: "transactions:filterAll",
  income: "transactions:filterIncome",
  expense: "transactions:filterExpense",
}
