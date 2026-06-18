import { useState, useRef, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Loader2, PiggyBank } from "lucide-react"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/shared/components/core/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select"
import { useUpdateSavingsGoal } from "../hooks/useSavingsGoals"
import { notify } from "@/shared/utils/notifications/notify"
import type { SavingsGoal } from "../types"
import type { ApiError } from "@/shared/types/api"

const PRIORITIES = [
  { value: "LOW", labelKey: "savingsGoals:priorities.LOW" },
  { value: "MEDIUM", labelKey: "savingsGoals:priorities.MEDIUM" },
  { value: "HIGH", labelKey: "savingsGoals:priorities.HIGH" },
] as const

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
  goal: SavingsGoal | null
}

export function SavingsGoalEditDialog({ open, onOpenChange, goal }: Props) {
  const { t } = useTranslation()
  const { mutateAsync: updateGoal, isPending: isUpdating } = useUpdateSavingsGoal()
  const [name, setName] = useState("")
  const [targetAmount, setTargetAmount] = useState("")
  const [priority, setPriority] = useState<"LOW" | "MEDIUM" | "HIGH">("MEDIUM")
  const [deadline, setDeadline] = useState("")
  const [link, setLink] = useState("")
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (open && goal) {
      setName(goal.name)
      setTargetAmount(goal.targetAmount.toString())
      setPriority(goal.priority)
      setDeadline(goal.deadline || "")
      setLink(goal.link || "")
    }
  }, [open, goal])

  const handleSave = async () => {
    if (!goal || !name.trim() || !targetAmount) return

    try {
      await updateGoal({
        id: goal.id,
        dto: {
          name: name.trim(),
          targetAmount: Number(targetAmount),
          priority,
          status: goal.status,
          deadline: deadline || undefined,
          link: link.trim() || undefined,
        },
      })
      onOpenChange(false)
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t('savingsGoals:alerts.updateError')
      notify.error(message)
    }
  }

  const isValid = name.trim() && targetAmount && Number(targetAmount) > 0

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-112.5"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold flex items-center gap-2">
              <PiggyBank className="text-primary size-5" />
              {t('savingsGoals:editGoalTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('savingsGoals:name')}</Label>
              <Input
                ref={inputRef}
                placeholder={t('savingsGoals:namePlaceholder')}
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isUpdating}
                className="bg-background border-border"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('savingsGoals:targetAmount')}</Label>
                <Input
                  type="number"
                  placeholder="0.00"
                  value={targetAmount}
                  onChange={(e) => setTargetAmount(e.target.value)}
                  disabled={isUpdating}
                  className="bg-background border-border"
                  min="0.01"
                  step="0.01"
                />
              </div>
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('savingsGoals:priority')}</Label>
                <Select value={priority} onValueChange={(v) => setPriority(v as typeof priority)}>
                  <SelectTrigger className="bg-background border-border">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border">
                    {PRIORITIES.map((p) => (
                      <SelectItem key={p.value} value={p.value}>
                        {t(p.labelKey)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('savingsGoals:deadline')}</Label>
              <Input
                type="date"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
                disabled={isUpdating}
                className="bg-background border-border"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('savingsGoals:link')}</Label>
              <Input
                placeholder={t('savingsGoals:linkPlaceholder')}
                value={link}
                onChange={(e) => setLink(e.target.value)}
                disabled={isUpdating}
                className="bg-background border-border"
              />
            </div>
          </div>

          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isUpdating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>
            <Button
              onClick={handleSave}
              disabled={isUpdating || !isValid}
              className="bg-primary hover:bg-primary/90 px-6"
            >
              {isUpdating ? <Loader2 size={16} className="animate-spin" /> : t('savingsGoals:saveChanges')}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}