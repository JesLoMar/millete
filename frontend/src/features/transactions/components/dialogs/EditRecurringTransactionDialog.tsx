import { useState, useRef, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Loader2 } from "lucide-react"
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
import { notify } from "@/shared/utils/notifications/notify"

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

export function EditRecurringTransactionDialog({
  transaction,
  open,
  onOpenChange,
}: EditRecurringTransactionDialogProps) {
  const { t } = useTranslation(['transactions', 'common', 'categories', 'auth', 'dashboard'])
  const queryClient = useQueryClient()
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [form, setForm] = useState({
    description: "",
    category: "",
    amount: "",
    type: "EXPENSE" as "INCOME" | "EXPENSE",
    frequencyType: "",
    frequencyInterval: "1",
    startDate: "",
    endDate: "",
    error: null as string | null,
  })
  const inputRef = useRef<HTMLInputElement>(null)

  // Pre-popular formulario cuando se abre el modal con una transacción existente
  useEffect(() => {
    if (open && transaction) {
      setForm({
        description: transaction.description || "",
        category: transaction.categoryId || "",
        amount: transaction.amount ? String(Math.abs(transaction.amount)) : "",
        type: transaction.type === "INCOME" ? "INCOME" : "EXPENSE",
        frequencyType: transaction.frequencyType || "",
        frequencyInterval: transaction.frequencyInterval ? String(transaction.frequencyInterval) : "1",
        startDate: transaction.startDate || "",
        endDate: transaction.endDate || "",
        error: null,
      })
    }
  }, [open, transaction])

  const handleSave = async () => {
    if (!transaction || !form.description || !form.category || !form.amount || !form.frequencyType || !form.startDate) return
    setForm((prev) => ({ ...prev, error: null }))
    setIsSubmitting(true)

    try {
      await apiClient.put(`/planned-transactions/${transaction.id}`, {
        description: form.description.trim(),
        categoryId: form.category || null,
        amount: form.type === "EXPENSE" ? -Math.abs(Number(form.amount)) : Math.abs(Number(form.amount)),
        type: form.type,
        frequencyType: form.frequencyType,
        frequencyInterval: Number(form.frequencyInterval),
        startDate: form.startDate,
        endDate: form.endDate || null,
      })

      queryClient.invalidateQueries({ queryKey: ['plannedTransactions'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] })
      onOpenChange(false)
    } catch (err) {
      const axiosError = err as { response?: { data?: { message?: string } } }
      setForm((prev) => ({ ...prev, error: axiosError?.response?.data?.message || t('transactions:createError') }))
    } finally {
      setIsSubmitting(false)
    }
  }

  const isValid = form.description.trim() && form.amount && Number(form.amount) > 0 && form.frequencyType && form.startDate

  return (
    <Dialog open={open} onOpenChange={onOpenChange} key={transaction?.id}>
      <DialogContent
        className="bg-card border-border sm:max-w-106.25"
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
                onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
                placeholder={t('transactions:descriptionPlaceholder')}
                disabled={isSubmitting}
                className="bg-background border-border text-base"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('transactions:type')}</Label>
                <Select value={form.type} onValueChange={(v) => setForm((prev) => ({ ...prev, type: v as "INCOME" | "EXPENSE" }))}>
                  <SelectTrigger className="bg-background border-border">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border">
                    <SelectItem value="INCOME">{t('transactions:types.income')}</SelectItem>
                    <SelectItem value="EXPENSE">{t('transactions:types.expense')}</SelectItem>
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
                  onChange={(e) => setForm((prev) => ({ ...prev, amount: e.target.value }))}
                  disabled={isSubmitting}
                  className="bg-background border-border text-base"
                  min="0.01"
                  step="0.01"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('transactions:category')}</Label>
              <Select value={form.category} onValueChange={(v) => setForm((prev) => ({ ...prev, category: v }))}>
                <SelectTrigger className="bg-background border-border">
                  <SelectValue placeholder={t('transactions:selectCategory')} />
                </SelectTrigger>
                <SelectContent className="bg-card border-border">
                  {/* Categories would be loaded here */}
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('transactions:recurring.frequencyType')}</Label>
                <Select value={form.frequencyType} onValueChange={(v) => setForm((prev) => ({ ...prev, frequencyType: v }))}>
                  <SelectTrigger className="bg-background border-border">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border">
                    <SelectItem value="DAILY">{t('transactions:recurring.frequencies.daily')}</SelectItem>
                    <SelectItem value="WEEKLY">{t('transactions:recurring.frequencies.weekly')}</SelectItem>
                    <SelectItem value="MONTHLY">{t('transactions:recurring.frequencies.monthly')}</SelectItem>
                    <SelectItem value="YEARLY">{t('transactions:recurring.frequencies.yearly')}</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-interval" className="text-sm font-semibold">
                  {t('transactions:recurring.frequencyInterval')}
                </Label>
                <Input
                  id="edit-recurring-interval"
                  type="number"
                  value={form.frequencyInterval}
                  onChange={(e) => setForm((prev) => ({ ...prev, frequencyInterval: e.target.value }))}
                  disabled={isSubmitting}
                  className="bg-background border-border text-base"
                  min="1"
                  step="1"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-start" className="text-sm font-semibold">
                  {t('transactions:recurring.startDate')}
                </Label>
                <Input
                  id="edit-recurring-start"
                  type="date"
                  value={form.startDate}
                  onChange={(e) => setForm((prev) => ({ ...prev, startDate: e.target.value }))}
                  disabled={isSubmitting}
                  className="bg-background border-border"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-end" className="text-sm font-semibold">
                  {t('transactions:recurring.endDate')}
                </Label>
                <Input
                  id="edit-recurring-end"
                  type="date"
                  value={form.endDate}
                  onChange={(e) => setForm((prev) => ({ ...prev, endDate: e.target.value }))}
                  disabled={isSubmitting}
                  className="bg-background border-border"
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
              disabled={isSubmitting}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>
            <Button
              onClick={handleSave}
              disabled={isSubmitting || !isValid}
              className="bg-primary hover:bg-primary/90 px-6 min-h-11"
            >
              {isSubmitting ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" aria-hidden="true" />
                  {t('common:actions.saving')}
                </>
              ) : (
                t('transactions:save')
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
