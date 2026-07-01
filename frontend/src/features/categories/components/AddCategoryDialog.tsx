import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Plus } from "lucide-react"
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
  DialogTrigger,
} from "@/shared/components/core/dialog"
import { ColorPicker } from "./ColorPicker"
import { CATEGORY_COLORS } from "../constants"
import { useCategoryMutations } from "../hooks/useCategoryMutation"
import type { ApiError } from "@/shared/types/api"

interface AddCategoryDialogProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
}

export function AddCategoryDialog({ open: controlledOpen, onOpenChange: controlledOnOpenChange }: AddCategoryDialogProps = {}) {
  const { t } = useTranslation(['categories', 'common'])
  const { createCategory, isCreating } = useCategoryMutations()
  const [internalOpen, setInternalOpen] = useState(false)
  const [form, setForm] = useState({
    name: "",
    color: CATEGORY_COLORS[0],
    budgetLimit: "",
    error: null as string | null,
  })
  const inputRef = useRef<HTMLInputElement>(null)

  const isControlled = controlledOpen !== undefined
  const open = isControlled ? controlledOpen : internalOpen
  const setOpen = isControlled ? controlledOnOpenChange! : setInternalOpen

  const resetForm = () => {
    setForm({
      name: "",
      color: CATEGORY_COLORS[0],
      budgetLimit: "",
      error: null,
    })
  }

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen)
    if (!isOpen) resetForm()
  }

  const handleSave = async () => {
    if (!form.name.trim()) return
    setForm((prev) => ({ ...prev, error: null }))

    try {
      await createCategory.mutateAsync({
        name: form.name.trim(),
        color: form.color,
        budgetLimit: form.budgetLimit ? Number(form.budgetLimit) : null,
      })
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t('categories:createError')
      setForm((prev) => ({ ...prev, error: message }))
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {!isControlled && (
        <DialogTrigger asChild>
          <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-4">
            <Plus size={16} />
            {t('categories:add')}
          </Button>
        </DialogTrigger>
      )}

      <DialogContent
        className="bg-card border-border sm:max-w-md"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold">
              {t('categories:newTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('categories:name')}</Label>
              <Input
                ref={inputRef}
                placeholder={t('categories:namePlaceholder')}
                value={form.name}
                onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('categories:color')}</Label>
              <ColorPicker value={form.color} onChange={(v) => setForm((prev) => ({ ...prev, color: v }))} />
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('categories:budget')}</Label>
              <Input
                type="number"
                placeholder="0.00"
                value={form.budgetLimit}
                onChange={(e) => setForm((prev) => ({ ...prev, budgetLimit: e.target.value }))}
                disabled={isCreating}
                className="bg-background border-border"
                min="0"
                step="0.01"
              />
              <p className="text-xs text-muted-foreground">{t('categories:budgetHint')}</p>
            </div>

            {form.error && (
              <p className="text-destructive text-sm text-center">{form.error}</p>
            )}
          </div>

          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button variant="outline" onClick={() => setOpen(false)} disabled={isCreating} className="border-border">
              {t('common:actions.cancel')}
            </Button>
            <Button
              onClick={handleSave}
              disabled={isCreating || !form.name.trim()}
              className="bg-primary hover:bg-primary/90 px-6"
            >
              {isCreating ? <Spinner size={20} /> : t('categories:save')}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}