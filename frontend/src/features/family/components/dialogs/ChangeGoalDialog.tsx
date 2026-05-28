import { useState } from "react"
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
  const [goal, setGoal] = useState(currentGoal)

  const handleSave = () => {
    if (goal > 0) {
      onSave(goal)
      onOpenChange(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange} key={currentGoal}>
      <DialogContent className="bg-card border-border sm:max-w-106.25">
        <DialogHeader>
          <DialogTitle>{t("family.changeGoalTitle")}</DialogTitle>
          <DialogDescription>{t("family.changeGoalDesc")}</DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label htmlFor="goal">{t("family.monthlyGoal")} (€)</Label>
            <Input
              id="goal"
              type="number"
              value={goal}
              onChange={(e) => setGoal(Number(e.target.value))}
              className="bg-background border-border"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button onClick={handleSave} disabled={goal <= 0}>{t("family.save")}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}