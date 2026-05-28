import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Plus, Loader2 } from "lucide-react"
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
import { CategorySelect } from "../CategorySelect"
import { TypeToggle } from "../TypeToggle"
import { useTransactionMutations } from "../../hooks/useTransactionMutation"
import type { ApiError } from "@/shared/types/api"

interface NewTransactionDialogProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
}

export function NewTransactionDialog({ open: controlledOpen, onOpenChange: controlledOnOpenChange }: NewTransactionDialogProps = {}) {
  const { t } = useTranslation()
  const { createTransaction, isCreating } = useTransactionMutations()
  const [internalOpen, setInternalOpen] = useState(false)
  const [description, setDescription] = useState("")
  const [category, setCategory] = useState("")
  const [amount, setAmount] = useState("")
  const [type, setType] = useState<"INCOME" | "EXPENSE">("EXPENSE")
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const isControlled = controlledOpen !== undefined
  const open = isControlled ? controlledOpen : internalOpen
  const setOpen = isControlled ? controlledOnOpenChange! : setInternalOpen

  const resetForm = () => {
    setDescription("")
    setCategory("")
    setAmount("")
    setType("EXPENSE")
    setError(null)
  }

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen)
    if (!isOpen) resetForm()
  }

  const handleSave = async () => {
    if (!description || !category || !amount) return
    setError(null)

    try {
      await createTransaction.mutateAsync({
        description: description.trim(),
        categoryId: category,
        amount: type === "EXPENSE" ? -Math.abs(Number(amount)) : Math.abs(Number(amount)),
        type,
        date: new Date().toISOString().split('.')[0],
      })
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message
        || apiError?.response?.data?.error
        || t("transactions.createError")
      setError(message)
    }
  }

  const isValid = description.trim() && category && amount && Number(amount) > 0

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {!isControlled && (
        <DialogTrigger asChild>
          <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-4">
            <Plus size={16} />
            {t("transactions.new")}
          </Button>
        </DialogTrigger>
      )}

      <DialogContent
        className="bg-card border-border sm:max-w-106.25"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">
            {t("transactions.newTitle")}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("transactions.description")}</Label>
            <Input
              ref={inputRef}
              placeholder={t("transactions.descriptionPlaceholder")}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              disabled={isCreating}
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
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                disabled={isCreating}
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
          <Button variant="outline" onClick={() => setOpen(false)} disabled={isCreating} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button onClick={handleSave} disabled={isCreating || !isValid} className="bg-primary hover:bg-primary/90 px-6">
            {isCreating ? <Loader2 size={16} className="animate-spin" /> : t("transactions.add")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}