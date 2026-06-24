import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Plus, Loader2 } from "lucide-react"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from "@/shared/components/core/dialog"
import { CategorySelect } from "../CategorySelect"
import { TypeToggle } from "../TypeToggle"
import { useTransactionMutations } from "../../hooks/useTransactionMutation"
import type { ApiError } from "@/shared/types/api"

interface NewTransactionDialogProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
}

interface FormState {
  description: string
  category: string
  amount: string
  type: "INCOME" | "EXPENSE"
  error: string | null
}

export function NewTransactionDialog({ open: controlledOpen, onOpenChange: controlledOnOpenChange }: NewTransactionDialogProps = {}) {
  const { t } = useTranslation(['transactions', 'common', 'categories', 'auth', 'dashboard'])
  const { createTransaction, isCreating } = useTransactionMutations()
  const [internalOpen, setInternalOpen] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const [form, setForm] = useState<FormState>({
    description: "",
    category: "",
    amount: "",
    type: "EXPENSE",
    error: null,
  })

  const isControlled = controlledOpen !== undefined
  const open = isControlled ? controlledOpen : internalOpen
  const setOpen = isControlled ? controlledOnOpenChange! : setInternalOpen

  const updateForm = (updates: Partial<FormState>) => {
    setForm(prev => ({ ...prev, ...updates }))
  }

  const resetForm = () => {
    setForm({
      description: "",
      category: "",
      amount: "",
      type: "EXPENSE",
      error: null,
    })
  }

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen)
    if (!isOpen) resetForm()
  }

  const handleSave = async () => {
    if (!form.description || !form.category || !form.amount) return
    updateForm({ error: null })

    try {
      await createTransaction.mutateAsync({
        description: form.description.trim(),
        categoryId: form.category,
        amount: form.type === "EXPENSE" ? -Math.abs(Number(form.amount)) : Math.abs(Number(form.amount)),
        type: form.type,
        date: new Date().toISOString().split('.')[0],
      })
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message
        || apiError?.response?.data?.error
        || t('transactions:createError')
      updateForm({ error: message })
    }
  }

  const isValid = form.description.trim() && form.category && form.amount && Number(form.amount) > 0

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {!isControlled && (
        <DialogTrigger asChild>
          <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-3 sm:px-4 text-xs sm:text-sm">
            <Plus size={15} />
            <span className="hidden xs:inline">{t('transactions:new')}</span>
            <span className="xs:hidden">{t('transactions:newShort')}</span>
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
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold">
              {t('transactions:newTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('transactions:description')}</Label>
              <Input
                ref={inputRef}
                placeholder={t('transactions:descriptionPlaceholder')}
                value={form.description}
                onChange={(e) => updateForm({ description: e.target.value })}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('transactions:type')}</Label>
                <TypeToggle value={form.type} onChange={(type) => updateForm({ type })} />
              </div>
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('transactions:amount')}</Label>
                <Input
                  type="number"
                  placeholder="0.00"
                  value={form.amount}
                  onChange={(e) => updateForm({ amount: e.target.value })}
                  disabled={isCreating}
                  className="bg-background border-border"
                  min="0.01"
                  step="0.01"
                />
              </div>
            </div>

            <CategorySelect value={form.category} onValueChange={(category) => updateForm({ category })} />

            {form.error && <p className="text-red-400 text-sm text-center">{form.error}</p>}
          </div>
          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button variant="outline" onClick={() => setOpen(false)} disabled={isCreating} className="border-border">
              {t('common:actions.cancel')}
            </Button>
            <Button onClick={handleSave} disabled={isCreating || !isValid} className="bg-primary hover:bg-primary/90 px-6">
              {isCreating ? <Loader2 size={16} className="animate-spin" /> : t('transactions:add')}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
