import { useState, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/shared/components/core/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select"

interface UpdateGoalDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentMonthlyTarget: number
  currentDistributionMode: string
  onSave: (monthlyTarget: number, distributionMode: string) => void
}

export function UpdateGoalDialog({
  open,
  onOpenChange,
  currentMonthlyTarget,
  currentDistributionMode,
  onSave,
}: UpdateGoalDialogProps) {
  const { t } = useTranslation()
  const [monthlyTarget, setMonthlyTarget] = useState("")
  const [distributionMode, setDistributionMode] = useState(currentDistributionMode)

  useEffect(() => {
    if (open) {
      setMonthlyTarget(currentMonthlyTarget > 0 ? currentMonthlyTarget.toString() : "")
      setDistributionMode(currentDistributionMode)
    }
  }, [open, currentMonthlyTarget, currentDistributionMode])

  const handleSave = () => {
    const parsedGoal = parseFloat(monthlyTarget)
    if (parsedGoal > 0) {
      onSave(parsedGoal, distributionMode)
      onOpenChange(false)
    }
  }

  const isValid = monthlyTarget.trim() !== "" && parseFloat(monthlyTarget) > 0

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-106.25"
        aria-describedby="update-goal-description"
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-lg sm:text-xl font-semibold text-foreground">
              {t("groupGoals.changeGoalTitle")}
            </DialogTitle>
            <DialogDescription id="update-goal-description" className="text-sm text-muted-foreground">
              {t("groupGoals.changeGoalDesc")}
            </DialogDescription>
          </DialogHeader>

          <div className="py-2 sm:py-4 space-y-4">
            <div className="space-y-2">
              <Label htmlFor="goal" className="text-sm font-semibold text-foreground/80">
                {t("groupGoals.monthlyGoal")} (€)
              </Label>
              <div className="relative">
                <Input
                  id="goal"
                  type="number"
                  value={monthlyTarget}
                  onChange={(e) => setMonthlyTarget(e.target.value)}
                  className="bg-background border-border text-base pr-12"
                  min="0.01"
                  step="0.01"
                  aria-label={t("groupGoals.monthlyGoal")}
                />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-muted-foreground select-none">
                  EUR
                </span>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold text-foreground/80">
                {t("groupGoals.distributionMode")}
              </Label>
              <Select value={distributionMode} onValueChange={setDistributionMode}>
                <SelectTrigger className="bg-background border-border">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-card border-border">
                  <SelectItem value="EQUITATIVE">{t("groupGoals.modes.equitative")}</SelectItem>
                  <SelectItem value="PROPORTIONAL">{t("groupGoals.modes.proportional")}</SelectItem>
                  <SelectItem value="CUSTOM">{t("groupGoals.modes.custom")}</SelectItem>
                </SelectContent>
              </Select>
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
              {t("groupGoals.save")}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}