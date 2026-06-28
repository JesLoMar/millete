export interface Transaction {
  id: string
  description: string
  category: string
  categoryColor?: string | null
  categoryId: string
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
  active?: boolean
}
