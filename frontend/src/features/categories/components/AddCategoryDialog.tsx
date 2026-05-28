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
import { ColorPicker } from "./ColorPicker"
import { CATEGORY_COLORS } from "../constants"
import { useCategoryMutations } from "../hooks/useCategoryMutation"
import type { ApiError } from "@/shared/types/api"

interface AddCategoryDialogProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
}

export function AddCategoryDialog({ open: controlledOpen, onOpenChange: controlledOnOpenChange }: AddCategoryDialogProps = {}) {
  const { t } = useTranslation()
  const { createCategory, isCreating } = useCategoryMutations()
  const [internalOpen, setInternalOpen] = useState(false)
  const [name, setName] = useState("")
  const [color, setColor] = useState(CATEGORY_COLORS[0])
  const [budgetLimit, setBudgetLimit] = useState("")
  const [error, setError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const isControlled = controlledOpen !== undefined
  const open = isControlled ? controlledOpen : internalOpen
  const setOpen = isControlled ? controlledOnOpenChange! : setInternalOpen

  const resetForm = () => {
    setName("")
    setColor(CATEGORY_COLORS[0])
    setBudgetLimit("")
    setError(null)
  }

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen)
    if (!isOpen) resetForm()
  }

  const handleSave = async () => {
    if (!name.trim()) return
    setError(null)

    try {
      await createCategory.mutateAsync({
        name: name.trim(),
        color: color,
        budgetLimit: budgetLimit ? Number(budgetLimit) : null,
      })
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t("categories.createError")
      setError(message)
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {!isControlled && (
        <DialogTrigger asChild>
          <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-4">
            <Plus size={16} />
            {t("categories.add")}
          </Button>
        </DialogTrigger>
      )}

      <DialogContent
        className="bg-card border-border sm:max-w-112.5"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">
            {t("categories.newTitle")}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("categories.name")}</Label>
            <Input
              ref={inputRef}
              placeholder={t("categories.namePlaceholder")}
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={isCreating}
              className="bg-background border-border"
            />
          </div>

          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("categories.color")}</Label>
            <ColorPicker value={color} onChange={setColor} />
          </div>

          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("categories.budget")}</Label>
            <Input
              type="number"
              placeholder="0.00"
              value={budgetLimit}
              onChange={(e) => setBudgetLimit(e.target.value)}
              disabled={isCreating}
              className="bg-background border-border"
              min="0"
              step="0.01"
            />
            <p className="text-xs text-muted-foreground">{t("categories.budgetHint")}</p>
          </div>

          {error && (
            <p className="text-red-400 text-sm text-center">{error}</p>
          )}
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => setOpen(false)} disabled={isCreating} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button
            onClick={handleSave}
            disabled={isCreating || !name.trim()}
            className="bg-primary hover:bg-primary/90 px-6"
          >
            {isCreating ? <Loader2 size={16} className="animate-spin" /> : t("categories.save")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}