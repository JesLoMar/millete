import { useState, useRef, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Loader2 } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/shared/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/ui/select"
import { CategorySelect } from "../CategorySelect"
import { TypeToggle } from "../TypeToggle"
import { FREQUENCY_TYPES } from "../../constants"
import { useTransactionMutations } from "../../hooks/useTransactionMutation"
import type { PlannedTransaction } from "@/shared/hooks/usePlannedTransactions"
import type { ApiError } from "@/shared/types/api"

interface EditRecurringTransactionDialogProps {
  transaction: PlannedTransaction | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function EditRecurringTransactionDialog({ transaction, open, onOpenChange }: EditRecurringTransactionDialogProps) {
  const { t } = useTranslation()
  const { updateRecurring, isUpdating } = useTransactionMutations()
  const [description, setDescription] = useState("")
  const [category, setCategory] = useState("")
  const [amount, setAmount] = useState("")
  const [type, setType] = useState<"INCOME" | "EXPENSE">("EXPENSE")
  const [frequencyType, setFrequencyType] = useState("")
  const [frequencyInterval, setFrequencyInterval] = useState("1")
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  // ✅ Sincronizar estados cuando cambia la transacción o se abre el diálogo
  useEffect(() => {
    if (open && transaction) {
      setDescription(transaction.description || "")
      setCategory(transaction.categoryId || "")
      setAmount(Math.abs(transaction.amount).toString())
      setType(transaction.type || "EXPENSE")
      setFrequencyType(transaction.frequencyType || "")
      setFrequencyInterval(transaction.frequencyInterval?.toString() || "1")
      setStartDate(transaction.startDate || "")
      setEndDate(transaction.endDate || "")
      setError(null)
    }
  }, [open, transaction])

  const handleSave = async () => {
    if (!transaction || !description || !category || !amount || !frequencyType || !startDate) return
    setError(null)

    try {
      await updateRecurring.mutateAsync({
        id: transaction.id,
        data: {
          description: description.trim(),
          categoryId: category,
          amount: type === "EXPENSE" ? -Math.abs(Number(amount)) : Math.abs(Number(amount)),
          type,
          frequencyType,
          frequencyInterval: Number(frequencyInterval),
          startDate,
          endDate: endDate || null,
        },
      })
      onOpenChange(false)
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t("transactions.createError")
      setError(message)
    }
  }

  const isValid = description.trim() && category && amount && Number(amount) > 0 && frequencyType && startDate
  const today = new Date().toISOString().split('T')[0]

  const frequencyUnit = 
    frequencyType === "DAYS" ? t("transactions.recurring.days") :
    frequencyType === "WEEKS" ? t("transactions.recurring.weeks") :
    frequencyType === "MONTHS" ? t("transactions.recurring.months") :
    t("transactions.recurring.years")

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-125"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-lg sm:text-xl font-semibold text-foreground">
              {t("transactions.recurring.editTitle")}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label htmlFor="edit-recurring-description" className="text-sm font-semibold">
                {t("transactions.description")}
              </Label>
              <Input
                id="edit-recurring-description"
                ref={inputRef}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                disabled={isUpdating}
                placeholder={t("transactions.descriptionPlaceholder")}
                className="bg-background border-border text-base"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t("transactions.type")}</Label>
                <TypeToggle value={type} onChange={setType} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-amount" className="text-sm font-semibold">
                  {t("transactions.amount")}
                </Label>
                <Input
                  id="edit-recurring-amount"
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  disabled={isUpdating}
                  className="bg-background border-border text-base"
                  min="0.01"
                  step="0.01"
                />
              </div>
            </div>

            <CategorySelect value={category} onValueChange={setCategory} />

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t("transactions.recurring.frequency")}</Label>
                <Select value={frequencyType} onValueChange={setFrequencyType}>
                  <SelectTrigger 
                    className="bg-background border-border text-base"
                    aria-label={t("transactions.recurring.selectFrequency")}
                  >
                    <SelectValue placeholder={t("transactions.recurring.selectFrequency")} />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border">
                    {FREQUENCY_TYPES.map((freq) => (
                      <SelectItem key={freq.value} value={freq.value}>{t(freq.labelKey)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-interval" className="text-sm font-semibold">
                  {t("transactions.recurring.interval")}
                </Label>
                <Input
                  id="edit-recurring-interval"
                  type="number"
                  value={frequencyInterval}
                  onChange={(e) => setFrequencyInterval(e.target.value)}
                  disabled={isUpdating}
                  className="bg-background border-border text-base"
                  min="1"
                />
              </div>
            </div>

            {/* Fechas */}
            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-start" className="text-sm font-semibold">
                  {t("transactions.recurring.startDate")}
                </Label>
                <Input
                  id="edit-recurring-start"
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  disabled={isUpdating}
                  className="bg-background border-border text-base"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-recurring-end" className="text-sm font-semibold">
                  {t("transactions.recurring.endDate")}
                  <span className="text-xs text-muted-foreground ml-1">({t("auth.form.optional")})</span>
                </Label>
                <Input
                  id="edit-recurring-end"
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  disabled={isUpdating}
                  className="bg-background border-border text-base"
                  min={startDate || today}
                />
              </div>
            </div>

            {frequencyType && startDate && (
              <div className="bg-accent/20 rounded-lg p-3 sm:p-4 text-xs sm:text-sm text-muted-foreground space-y-1">
                <p className="font-medium text-foreground mb-1.5">
                  {t("transactions.recurring.summary")}
                </p>
                <p className="leading-relaxed">
                  {t("transactions.recurring.summaryText", {
                    description: description || "...",
                    amount: amount || "0",
                    frequency: frequencyInterval,
                    type: frequencyUnit,
                    start: startDate || "...",
                    end: endDate || t("transactions.recurring.indefinite"),
                  })}
                </p>
              </div>
            )}

            {error && (
              <p className="text-destructive text-sm text-center font-medium">{error}</p>
            )}
          </div>

          <DialogFooter className="gap-2 sm:gap-3 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isUpdating}
              className="border-border min-h-11"
            >
              {t("common.cancel")}
            </Button>
            <Button
              onClick={handleSave}
              disabled={isUpdating || !isValid}
              className="bg-primary hover:bg-primary/90 text-primary-foreground px-6 min-h-11"
            >
              {isUpdating ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" aria-hidden="true" />
                  {t("common.saving")}
                </>
              ) : (
                t("transactions.save")
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}