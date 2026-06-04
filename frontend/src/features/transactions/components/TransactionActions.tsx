import { NewTransactionDialog } from "./dialogs/NewTransactionDialog"
import { NewRecurringTransactionDialog } from './dialogs/NewRecurringTransactionDialog'
import { cn } from "@/lib/utils"

interface TransactionActionsProps {
  className?: string
}

export function TransactionActions({ className }: TransactionActionsProps) {
  return (
    <div className={cn("flex items-center gap-2 sm:gap-3", className)}>
      <NewRecurringTransactionDialog />
      <NewTransactionDialog />
    </div>
  )
}