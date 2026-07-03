import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { useQueryClient } from "@tanstack/react-query"
import { Spinner } from "@/shared/components/Spinner"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/shared/components/core/dialog"
import { apiClient } from "@/shared/api/axiosClient"
import { CategorySelect } from "../CategorySelect"
import { TypeToggle } from "../TypeToggle"

interface Transaction {
  id: string
  description: string
  category: string
  categoryId: string
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
}

interface EditTransactionDialogProps {
  transaction: Transaction | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

interface FormState {
  description: string
  category: string
  amount: string
  type: "INCOME" | "EXPENSE"
  isSubmitting: boolean
  error: string | null
}

function getInitialForm(transaction: Transaction | null): FormState {
  return {
    description: transaction?.description || "",
    category: transaction?.categoryId || "",
    amount: transaction?.amount ? String(Math.abs(transaction.amount)) : "",
    type: transaction?.type === "INCOME" ? "INCOME" : "EXPENSE",
    isSubmitting: false,
    error: null,
  }
}

export function EditTransactionDialog({ transaction, open, onOpenChange }: EditTransactionDialogProps) {
  const { t } = useTranslation(['transactions', 'common', 'categories', 'auth', 'dashboard'])
  const queryClient = useQueryClient()
  const inputRef = useRef<HTMLInputElement>(null)

  const [form, setForm] = useState<FormState>(() => getInitialForm(transaction))

  const updateForm = (updates: Partial<FormState>) => {
    setForm(prev => ({ ...prev, ...updates }))
  }

  const handleSave = async () => {
    if (!transaction || !form.description || !form.amount) return
    updateForm({ error: null, isSubmitting: true })

    try {
      await apiClient.put(`/transactions/${transaction.id}`, {
        description: form.description.trim(),
        categoryId: form.category || null,
        amount: Math.abs(Number(form.amount)),
        type: form.type,
        date: transaction.date,
      })

      queryClient.invalidateQueries({ queryKey: ['transactions'] })
      queryClient.invalidateQueries({ queryKey: ['transactionMetrics'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] })
      queryClient.invalidateQueries({ queryKey: ['historyChart'] })
      queryClient.invalidateQueries({ queryKey: ['categoryStats'] })
      queryClient.invalidateQueries({ queryKey: ['budgets'] })
      queryClient.invalidateQueries({ queryKey: ['recentTransactions'] })
      queryClient.invalidateQueries({ queryKey: ['categoryExpenses'] })

      onOpenChange(false)
    } catch (err) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      const message = axiosError?.response?.data?.message || t('transactions:createError')
      updateForm({ error: message, isSubmitting: false })
    }
  }

  const isValid = form.description.trim() && form.amount && Number(form.amount) > 0

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-md"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold text-foreground">
              {t('transactions:editTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label htmlFor="edit-description" className="text-sm font-semibold">
                {t('transactions:description')}
              </Label>
              <Input
                id="edit-description"
                ref={inputRef}
                value={form.description}
                onChange={(e) => updateForm({ description: e.target.value })}
                placeholder={t('transactions:descriptionPlaceholder')}
                disabled={form.isSubmitting}
                className="bg-background border-border text-base"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('transactions:type')}</Label>
                <TypeToggle value={form.type} onChange={(type) => updateForm({ type })} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-amount" className="text-sm font-semibold">
                  {t('transactions:amount')}
                </Label>
                <Input
                  id="edit-amount"
                  type="number"
                  value={form.amount}
                  onChange={(e) => updateForm({ amount: e.target.value })}
                  disabled={form.isSubmitting}
                  className="bg-background border-border text-base"
                  min="0.01"
                  step="0.01"
                />
              </div>
            </div>

            <CategorySelect value={form.category} onValueChange={(category) => updateForm({ category })} />

            {form.error && (
              <p className="text-destructive text-sm text-center font-medium">{form.error}</p>
            )}
          </div>

          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={form.isSubmitting}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>
            <Button
              onClick={handleSave}
              disabled={form.isSubmitting || !isValid}
              className="bg-primary hover:bg-primary/90 px-6 min-h-11"
            >
              {form.isSubmitting ? <Spinner size={20} /> : t('transactions:save')}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
