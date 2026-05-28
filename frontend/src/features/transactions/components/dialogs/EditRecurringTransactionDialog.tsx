import { useState, useRef } from "react"
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
  const [description, setDescription] = useState(transaction?.description || "")
  const [category, setCategory] = useState(transaction?.categoryId || "")
  const [amount, setAmount] = useState(transaction ? Math.abs(transaction.amount).toString() : "")
  const [type, setType] = useState<"INCOME" | "EXPENSE">(transaction?.type || "EXPENSE")
  const [frequencyType, setFrequencyType] = useState(transaction?.frequencyType || "")
  const [frequencyInterval, setFrequencyInterval] = useState(transaction?.frequencyInterval?.toString() || "1")
  const [startDate, setStartDate] = useState(transaction?.startDate || "")
  const [endDate, setEndDate] = useState(transaction?.endDate || "")
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

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

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-125"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">
            {t("transactions.recurring.editTitle")}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("transactions.description")}</Label>
            <Input ref={inputRef} value={description} onChange={(e) => setDescription(e.target.value)} disabled={isUpdating} className="bg-background border-border" />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.type")}</Label>
              <TypeToggle value={type} onChange={setType} />
            </div>
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.amount")}</Label>
              <Input type="number" value={amount} onChange={(e) => setAmount(e.target.value)} disabled={isUpdating} className="bg-background border-border" min="0.01" step="0.01" />
            </div>
          </div>

          <CategorySelect value={category} onValueChange={setCategory} />

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.recurring.frequency")}</Label>
              <Select value={frequencyType} onValueChange={setFrequencyType}>
                <SelectTrigger className="bg-background border-border">
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
              <Label className="text-sm font-semibold">{t("transactions.recurring.interval")}</Label>
              <Input type="number" value={frequencyInterval} onChange={(e) => setFrequencyInterval(e.target.value)} disabled={isUpdating} className="bg-background border-border" min="1" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.recurring.startDate")}</Label>
              <Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} disabled={isUpdating} className="bg-background border-border" min={today} />
            </div>
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.recurring.endDate")}<span className="text-xs text-muted-foreground ml-1">({t("auth.form.optional")})</span></Label>
              <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} disabled={isUpdating} className="bg-background border-border" min={startDate || today} />
            </div>
          </div>

          {error && <p className="text-red-400 text-sm text-center">{error}</p>}
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isUpdating} className="border-border">{t("common.cancel")}</Button>
          <Button onClick={handleSave} disabled={isUpdating || !isValid} className="bg-primary hover:bg-primary/90 px-6">
            {isUpdating ? <Loader2 size={16} className="animate-spin" /> : t("transactions.save")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}