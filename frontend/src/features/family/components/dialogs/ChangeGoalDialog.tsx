import { useState, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/shared/components/ui/dialog"

interface ChangeGoalDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentGoal: number
  onSave: (newGoal: number) => void
}

export function ChangeGoalDialog({ open, onOpenChange, currentGoal, onSave }: ChangeGoalDialogProps) {
  const { t } = useTranslation()
  const [goal, setGoal] = useState("")

  useEffect(() => {
    if (open) {
      setGoal(currentGoal > 0 ? currentGoal.toString() : "")
    }
  }, [open, currentGoal])

  const handleSave = () => {
    const parsedGoal = parseFloat(goal)
    if (parsedGoal > 0) {
      onSave(parsedGoal)
      onOpenChange(false)
    }
  }

  const isValid = goal.trim() !== "" && parseFloat(goal) > 0

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent 
        className="bg-card border-border sm:max-w-106.25"
        aria-describedby="change-goal-description"
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-lg sm:text-xl font-semibold text-foreground">
              {t("family.changeGoalTitle")}
            </DialogTitle>
            <DialogDescription id="change-goal-description" className="text-sm text-muted-foreground">
              {t("family.changeGoalDesc")}
            </DialogDescription>
          </DialogHeader>

          <div className="py-2 sm:py-4">
            <div className="space-y-2">
              <Label htmlFor="goal" className="text-sm font-semibold text-foreground/80">
                {t("family.monthlyGoal")} (€)
              </Label>
              <div className="relative">
                <Input
                  id="goal"
                  type="number"
                  value={goal}
                  onChange={(e) => setGoal(e.target.value)}
                  className="bg-background border-border text-base pr-12"
                  min="0.01"
                  step="0.01"
                  aria-label={t("family.monthlyGoal")}
                />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-muted-foreground select-none">
                  EUR
                </span>
              </div>
            </div>
          </div>

          <DialogFooter className="gap-2 sm:gap-3 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button 
              variant="outline" 
              onClick={() => onOpenChange(false)} 
              className="border-border text-foreground min-h-11"
            >
              {t("common.cancel")}
            </Button>
            <Button 
              onClick={handleSave} 
              disabled={!isValid} 
              className="bg-primary hover:bg-primary/90 text-primary-foreground px-6 min-h-11"
            >
              {t("family.save")}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}