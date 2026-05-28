import { NewTransactionDialog } from "./dialogs/NewTransactionDialog"
import { NewRecurringTransactionDialog } from './dialogs/NewRecurringTransactionDialog'

interface TransactionActionsProps {
  className?: string
}

export function TransactionActions({ className }: TransactionActionsProps) {
  return (
    <div className={`flex items-center gap-3 ${className || ""}`}>
      <NewRecurringTransactionDialog />
      <NewTransactionDialog />
    </div>
  )
}