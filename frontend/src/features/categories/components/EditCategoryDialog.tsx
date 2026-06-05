import { useState, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Loader2, CheckCircle } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/shared/components/ui/dialog"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import { ColorPicker } from "./ColorPicker"
import { useCategoryMutations } from "../hooks/useCategoryMutation"
import type { Category } from "@/shared/hooks/useCategories"

interface EditCategoryDialogProps {
  category: Category | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function EditCategoryDialog({ category, open, onOpenChange }: EditCategoryDialogProps) {
  const { t } = useTranslation()
  const { updateCategory, isUpdating } = useCategoryMutations()

  const [name, setName] = useState("")
  const [color, setColor] = useState("")
  const [budgetLimit, setBudgetLimit] = useState("")
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (category) {
      setName(category.name)
      setColor(category.color || "#EF4444")
      setBudgetLimit(category.budgetLimit !== null ? category.budgetLimit.toString() : "")
      setError(null)
    }
  }, [category])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!category) return
    setError(null)

    const trimmedName = name.trim()
    if (!trimmedName) {
      setError(t("categories.nameRequired"))
      return
    }

    const parsedBudget = budgetLimit.trim() === "" ? null : parseFloat(budgetLimit)
    if (parsedBudget !== null && (isNaN(parsedBudget) || parsedBudget < 0)) {
      setError(t("categories.invalidBudget"))
      return
    }

    try {
      await updateCategory.mutateAsync({
        id: category.id,
        data: {
          name: trimmedName,
          color,
          budgetLimit: parsedBudget,
        },
      })
      onOpenChange(false)
    } catch (err: any) {
      setError(err.message || t("categories.updateError"))
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-120 bg-card border-border rounded-2xl">
        {/* ✅ Scroll para teclado en móvil */}
        <div className="max-h-[85dvh] overflow-y-auto p-4 sm:p-6">
          <DialogHeader>
            {/* ✅ Corregido: interpolación de {{name}} */}
            <DialogTitle className="text-xl font-bold tracking-tight text-foreground">
              {t("categories.editTitle", { name: category?.name ?? "" })}
            </DialogTitle>
            <DialogDescription className="text-sm text-muted-foreground">
              {t("categories.editDescription")}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-5 pt-2">
            {/* Campo: Nombre */}
            <div className="space-y-2">
              <Label htmlFor="edit-name" className="text-sm font-medium text-foreground/80">
                {t("categories.nameLabel")}
              </Label>
              <Input
                id="edit-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isUpdating}
                placeholder={t("categories.namePlaceholder")}
                className="bg-background border-border h-11 rounded-xl text-base"
                maxLength={50}
              />
            </div>

            {/* Campo: Límite de Presupuesto Mensual */}
            <div className="space-y-2">
              <Label htmlFor="edit-budget" className="text-sm font-medium text-foreground/80">
                {t("categories.budgetLabel")}
              </Label>
              <div className="relative">
                <Input
                  id="edit-budget"
                  type="number"
                  step="0.01"
                  min="0"
                  value={budgetLimit}
                  onChange={(e) => setBudgetLimit(e.target.value)}
                  disabled={isUpdating}
                  placeholder={t("categories.budgetPlaceholder")}
                  className="bg-background border-border h-11 rounded-xl pr-12 text-base [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-muted-foreground select-none">
                  EUR
                </span>
              </div>
            </div>

            {/* Selector de Color */}
            <div className="space-y-2">
              <Label className="text-sm font-medium text-foreground/80">
                {t("categories.colorLabel")}
              </Label>
              <ColorPicker 
                value={color} 
                onChange={setColor} 
                disabled={isUpdating} 
              />
            </div>

            {/* Error */}
            {error && (
              <p className="text-destructive text-xs font-medium bg-destructive/10 p-3 rounded-xl border border-destructive/20">
                {error}
              </p>
            )}

            {/* Botones */}
            <div className="flex justify-end gap-3 pt-3 border-t border-border/40">
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={isUpdating}
                className="border-border hover:bg-secondary text-foreground h-10 rounded-xl px-4"
              >
                {t("common.cancel")}
              </Button>
              <Button
                type="submit"
                disabled={isUpdating}
                className="bg-primary hover:bg-primary/90 text-primary-foreground font-semibold h-10 rounded-xl px-5 transition-all min-h-11"
              >
                {isUpdating ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" aria-hidden="true" />
                    {t("common.saving")}
                  </>
                ) : (
                  <>
                    <CheckCircle className="mr-2 size-4" aria-hidden="true" />
                    {t("common.save")}
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  )
}