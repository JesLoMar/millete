import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select"
import { apiClient } from "@/shared/api/axiosClient"
import { useQueryClient } from "@tanstack/react-query"
import { FREQUENCY_TYPES } from "../../constants"

interface PlannedTransaction {
  id: string
  description: string
  categoryId: string | null
  amount: number
  type: "INCOME" | "EXPENSE"
  frequencyType: string
  frequencyInterval: number
  startDate: string
  endDate: string | null
  lastExecutedDate: string | null
}

interface EditRecurringTransactionDialogProps {
  transaction: PlannedTransaction | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

interface FormState {
  description: string
  amount: string
  type: "INCOME" | "EXPENSE"
  frequencyType: string
  frequencyInterval: string
  categoryId: string | null
  error: string | null
  isSubmitting: boolean
}

function getInitialForm(transaction: PlannedTransaction | null): FormState {
  return {
    description: transaction?.description || "",
    amount: transaction?.amount ? String(Math.abs(transaction.amount)) : "",
    type: transaction?.type || "EXPENSE",
    frequencyType: transaction?.frequencyType || "MONTHS",
    frequencyInterval: transaction?.frequencyInterval ? String(transaction.frequencyInterval) : "1",
    categoryId: transaction?.categoryId || null,
    error: null,
    isSubmitting: false,
  }
}

export function EditRecurringTransactionDialog({
  transaction,
  open,
  onOpenChange,
}: EditRecurringTransactionDialogProps) {
  const { t } = useTranslation(['transactions', 'common', 'categories'])
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
      await apiClient.put(`/planned-transactions/${transaction.id}`, {
        description: form.description.trim(),
        categoryId: form.categoryId,
        amount: Math.abs(Number(form.amount)),
        type: form.type,
        frequencyType: form.frequencyType,
        frequencyInterval: Number(form.frequencyInterval),
        startDate: transaction.startDate,
        endDate: transaction.endDate,
      })

      queryClient.invalidateQueries({ queryKey: ['plannedTransactions'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] })

      onOpenChange(false)
    } catch (err) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      const message = axiosError?.response?.data?.message || t('transactions:alerts.updateRecurringError')
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
              {t('transactions:recurring.editTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label htmlFor="edit-recurring-description" className="text-sm font-semibold">
                {t('transactions:description')}
              </Label>
              <Input
                id="edit-recurring-description"
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
                <Label htmlFor="edit-recurring-type" className="text-sm font-semibold">{t('transactions:type')}</Label>
                <Select
                  value={form.type}
                  onValueChange={(value: "INCOME" | "EXPENSE") => updateForm({ type: value })}
                  disabled={form.isSubmitting}
                >
                  <SelectTrigger id="edit-recurring-type" className="bg-background border-border">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="INCOME">{t('transactions:income')}</SelectItem>
                    <SelectItem value="EXPENSE">{t('transactions:expense')}</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-amount" className="text-sm font-semibold">
                  {t('transactions:amount')}
                </Label>
                <Input
                  id="edit-recurring-amount"
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

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-frequency" className="text-sm font-semibold">
                  {t('transactions:recurring.frequency')}
                </Label>
                <Select
                  value={form.frequencyType}
                  onValueChange={(value) => updateForm({ frequencyType: value })}
                  disabled={form.isSubmitting}
                >
                  <SelectTrigger id="edit-recurring-frequency" className="bg-background border-border">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {FREQUENCY_TYPES.map((freq) => (
                      <SelectItem key={freq.value} value={freq.value}>
                        {t(freq.labelKey)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-interval" className="text-sm font-semibold">
                  {t('transactions:recurring.interval')}
                </Label>
                <Input
                  id="edit-recurring-interval"
                  type="number"
                  value={form.frequencyInterval}
                  onChange={(e) => updateForm({ frequencyInterval: e.target.value })}
                  disabled={form.isSubmitting}
                  className="bg-background border-border text-base"
                  min="1"
                  step="1"
                />
              </div>
            </div>

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