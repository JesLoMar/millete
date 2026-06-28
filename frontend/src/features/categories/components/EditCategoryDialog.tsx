import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Loader2, CheckCircle } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/shared/components/core/dialog"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import { ColorPicker } from "./ColorPicker"
import { useCategoryMutations } from "../hooks/useCategoryMutation"
import type { Category } from "@/shared/hooks/useCategories"

interface EditCategoryDialogProps {
  category: Category | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

function getInitialState(category: Category | null) {
  return {
    name: category?.name ?? "",
    color: category?.color ?? "",
    budgetLimit: category?.budgetLimit !== null && category?.budgetLimit !== undefined ? String(category.budgetLimit) : "",
    error: null as string | null,
  }
}

export function EditCategoryDialog({ category, open, onOpenChange }: EditCategoryDialogProps) {
  const { t } = useTranslation(['categories', 'common'])
  const { updateCategory, isUpdating } = useCategoryMutations()

  const [{ name, color, budgetLimit, error }, setState] = useState(() => getInitialState(category))

  const setName = (name: string) => setState((prev) => ({ ...prev, name }))
  const setColor = (color: string) => setState((prev) => ({ ...prev, color }))
  const setBudgetLimit = (budgetLimit: string) => setState((prev) => ({ ...prev, budgetLimit }))
  const setError = (error: string | null) => setState((prev) => ({ ...prev, error }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!category) return
    setError(null)

    const trimmedName = name.trim()
    if (!trimmedName) {
      setError(t('categories:nameRequired'))
      return
    }

    const parsedBudget = budgetLimit.trim() === "" ? null : parseFloat(budgetLimit)
    if (parsedBudget !== null && (isNaN(parsedBudget) || parsedBudget < 0)) {
      setError(t('categories:invalidBudget'))
      return
    }

    try {
      updateCategory.mutate({
        id: category.id,
        data: {
          name: trimmedName,
          color,
          budgetLimit: parsedBudget,
        },
      })
      onOpenChange(false)
    } catch (err) {
      const message = err instanceof Error ? err.message : t('categories:updateError')
      setError(message)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-120 bg-card border-border rounded-2xl">
        <div className="max-h-[85dvh] overflow-y-auto p-4 sm:p-6">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold tracking-tight text-foreground">
              {t('categories:editTitle', { name: category?.name ?? "" })}
            </DialogTitle>
            <DialogDescription className="text-sm text-muted-foreground">
              {t('categories:editDescription')}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-5 pt-2">
            <div className="space-y-2">
              <Label htmlFor="edit-name" className="text-sm font-medium text-foreground/80">
                {t('categories:nameLabel')}
              </Label>
              <Input
                id="edit-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isUpdating}
                placeholder={t('categories:namePlaceholder')}
                className="bg-background border-border h-11 rounded-xl text-base"
                maxLength={50}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-budget" className="text-sm font-medium text-foreground/80">
                {t('categories:budgetLabel')}
                <span className="text-xs text-muted-foreground ml-1">({t('auth:form.optional')})</span>
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
                  placeholder={t('categories:budgetPlaceholder')}
                  className="bg-background border-border h-11 rounded-xl pr-12 text-base [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-muted-foreground select-none">
                  EUR
                </span>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-medium text-foreground/80">
                {t('categories:colorLabel')}
              </Label>
              <ColorPicker 
                value={color} 
                onChange={setColor} 
                disabled={isUpdating} 
              />
            </div>

            {error && (
              <p className="text-destructive text-xs font-medium bg-destructive/10 p-3 rounded-xl border border-destructive/20">
                {error}
              </p>
            )}

            <div className="flex justify-end gap-3 pt-3 border-t border-border/40">
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={isUpdating}
                className="border-border hover:bg-secondary text-foreground h-10 rounded-xl px-4"
              >
                {t('common:actions.cancel')}
              </Button>
              <Button
                type="submit"
                disabled={isUpdating}
                className="bg-primary hover:bg-primary/90 text-primary-foreground font-semibold h-10 rounded-xl px-5 transition-all min-h-11"
              >
                {isUpdating ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" aria-hidden="true" />
                    {t('common:actions.saving')}
                  </>
                ) : (
                  <>
                    <CheckCircle className="mr-2 size-4" aria-hidden="true" />
                    {t('common:actions.save')}
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
