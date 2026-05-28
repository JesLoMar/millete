import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { useQueryClient } from "@tanstack/react-query"
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

export function EditTransactionDialog({ transaction, open, onOpenChange }: EditTransactionDialogProps) {
  const { t } = useTranslation()
  const queryClient = useQueryClient()
  const [description, setDescription] = useState(transaction?.description || "")
  const [category, setCategory] = useState(transaction?.categoryId || "")
  const [amount, setAmount] = useState(transaction ? Math.abs(transaction.amount).toString() : "")
  const [type, setType] = useState<"INCOME" | "EXPENSE">(transaction?.type || "EXPENSE")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const handleSave = async () => {
    if (!transaction || !description || !amount) return
    setError(null)
    setIsSubmitting(true)

    try {
      await apiClient.put(`/transactions/${transaction.id}`, {
        description: description.trim(),
        categoryId: category || null,
        amount: type === "EXPENSE" ? -Math.abs(Number(amount)) : Math.abs(Number(amount)),
        type,
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
      const message = axiosError?.response?.data?.message || t("transactions.createError")
      setError(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const isValid = description.trim() && amount && Number(amount) > 0

  return (
    <Dialog open={open} onOpenChange={onOpenChange} key={transaction?.id ?? "new"}>
      <DialogContent
        className="bg-card border-border sm:max-w-106.25"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">
            {t("transactions.editTitle")}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("transactions.description")}</Label>
            <Input
              ref={inputRef}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="bg-background border-border"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.type")}</Label>
              <TypeToggle value={type} onChange={setType} />
            </div>
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("transactions.amount")}</Label>
              <Input
                type="number"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="bg-background border-border"
                min="0.01"
                step="0.01"
              />
            </div>
          </div>

          <CategorySelect value={category} onValueChange={setCategory} />

          {error && <p className="text-red-400 text-sm text-center">{error}</p>}
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button onClick={handleSave} disabled={isSubmitting || !isValid} className="bg-primary hover:bg-primary/90 px-6">
            {isSubmitting ? <Loader2 size={16} className="animate-spin" /> : t("transactions.save")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}