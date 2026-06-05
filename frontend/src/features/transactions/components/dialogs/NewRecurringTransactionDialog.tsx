import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { RefreshCcw, Loader2 } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
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
import type { ApiError } from "@/shared/types/api"

export function NewRecurringTransactionDialog() {
  const { t } = useTranslation()
  const { createRecurring, isCreating } = useTransactionMutations()
  const [open, setOpen] = useState(false)
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

  const resetForm = () => {
    setDescription("")
    setCategory("")
    setAmount("")
    setType("EXPENSE")
    setFrequencyType("")
    setFrequencyInterval("1")
    setStartDate("")
    setEndDate("")
    setError(null)
  }

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen)
    if (!isOpen) resetForm()
  }

  const handleSave = async () => {
    if (!description || !category || !amount || !frequencyType || !startDate) return
    setError(null)

    try {
      const payload: Record<string, unknown> = {
        categoryId: category,
        amount: type === "EXPENSE" ? -Math.abs(Number(amount)) : Math.abs(Number(amount)),
        type,
        description: description.trim(),
        frequencyType,
        frequencyInterval: Number(frequencyInterval),
        startDate,
      }
      if (endDate) payload.endDate = endDate

      await createRecurring.mutateAsync(payload)
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      setError(apiError?.response?.data?.message || t("transactions.createError"))
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
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button 
          variant="outline" 
          className="gap-2 border-border h-9 sm:h-10 px-3 sm:px-4 font-semibold bg-card hover:bg-background transition-colors text-xs sm:text-sm"
          aria-label={t("transactions.recurring.newTitle")}
        >
          <RefreshCcw size={15} aria-hidden="true" />
          <span className="hidden xs:inline">{t("transactions.recurring.label")}</span>
          <span className="xs:hidden">{t("transactions.recurring.shortLabel")}</span>
        </Button>
      </DialogTrigger>

      <DialogContent
        className="bg-card border-border sm:max-w-125"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-lg sm:text-xl font-semibold">
              {t("transactions.recurring.newTitle")}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label htmlFor="recurring-description" className="text-sm font-semibold">
                {t("transactions.description")}
              </Label>
              <Input 
                id="recurring-description"
                ref={inputRef} 
                placeholder={t("transactions.descriptionPlaceholder")} 
                value={description} 
                onChange={(e) => setDescription(e.target.value)} 
                disabled={isCreating} 
                className="bg-background border-border text-base" 
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t("transactions.type")}</Label>
                <TypeToggle value={type} onChange={setType} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="recurring-amount" className="text-sm font-semibold">
                  {t("transactions.amount")}
                </Label>
                <Input 
                  id="recurring-amount"
                  type="number" 
                  placeholder="0.00" 
                  value={amount} 
                  onChange={(e) => setAmount(e.target.value)} 
                  disabled={isCreating} 
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
                <Label htmlFor="recurring-interval" className="text-sm font-semibold">
                  {t("transactions.recurring.interval")}
                </Label>
                <Input 
                  id="recurring-interval"
                  type="number" 
                  placeholder="1" 
                  value={frequencyInterval} 
                  onChange={(e) => setFrequencyInterval(e.target.value)} 
                  disabled={isCreating} 
                  className="bg-background border-border text-base" 
                  min="1" 
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label htmlFor="recurring-start-date" className="text-sm font-semibold">
                  {t("transactions.recurring.startDate")}
                </Label>
                <Input 
                  id="recurring-start-date"
                  type="date" 
                  value={startDate} 
                  onChange={(e) => setStartDate(e.target.value)} 
                  disabled={isCreating} 
                  className="bg-background border-border text-base" 
                  min={today} 
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="recurring-end-date" className="text-sm font-semibold">
                  {t("transactions.recurring.endDate")}
                  <span className="text-xs text-muted-foreground ml-1">({t("auth.form.optional")})</span>
                </Label>
                <Input 
                  id="recurring-end-date"
                  type="date" 
                  value={endDate} 
                  onChange={(e) => setEndDate(e.target.value)} 
                  disabled={isCreating} 
                  className="bg-background border-border text-base" 
                  min={startDate || today} 
                />
              </div>
            </div>

            {frequencyType && startDate && (
              <div className="bg-accent/20 rounded-lg p-3 sm:p-4 text-xs sm:text-sm text-muted-foreground space-y-1">
                <p className="font-medium text-foreground mb-1.5 flex items-center gap-1.5">
                  <RefreshCcw size={14} className="text-primary shrink-0" aria-hidden="true" />
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
              onClick={() => setOpen(false)} 
              disabled={isCreating} 
              className="border-border min-h-11"
            >
              {t("common.cancel")}
            </Button>
            <Button 
              onClick={handleSave} 
              disabled={isCreating || !isValid} 
              className="bg-primary hover:bg-primary/90 px-6 min-h-11"
            >
              {isCreating ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" aria-hidden="true" />
                  {t("common.saving")}
                </>
              ) : (
                t("transactions.add")
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}