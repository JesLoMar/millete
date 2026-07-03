import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Plus, PiggyBank } from "lucide-react"
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select"
import { useCreateSavingsGoal } from "../hooks/useSavingsGoals"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"

const PRIORITIES = [
  { value: "LOW", labelKey: "savingsGoals:priorities.LOW" },
  { value: "MEDIUM", labelKey: "savingsGoals:priorities.MEDIUM" },
  { value: "HIGH", labelKey: "savingsGoals:priorities.HIGH" },
] as const

export function SavingsGoalDialog() {
  const { t } = useTranslation()
  const { mutateAsync: createGoal, isPending: isCreating } = useCreateSavingsGoal()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({
    name: "",
    targetAmount: "",
    priority: "MEDIUM" as "LOW" | "MEDIUM" | "HIGH",
    deadline: "",
    link: "",
  })
  const inputRef = useRef<HTMLInputElement>(null)

  const resetForm = () => {
    setForm({
      name: "",
      targetAmount: "",
      priority: "MEDIUM",
      deadline: "",
      link: "",
    })
  }

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen)
    if (!isOpen) resetForm()
  }

  const handleSave = async () => {
    if (!form.name.trim() || !form.targetAmount) return

    try {
      await createGoal({
        name: form.name.trim(),
        targetAmount: Number(form.targetAmount),
        priority: form.priority,
        deadline: form.deadline || undefined,
        link: form.link.trim() || undefined,
      })
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t('savingsGoals:alerts.createError')
      notify.error(message)
    }
  }

  const isValid = form.name.trim() && form.targetAmount && Number(form.targetAmount) > 0

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-4">
          <Plus size={16} />
          {t('savingsGoals:newGoal')}
        </Button>
      </DialogTrigger>

      <DialogContent
        className="bg-card border-border sm:max-w-md"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold flex items-center gap-2">
              <PiggyBank className="text-primary size-5" />
              {t('savingsGoals:newGoalTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('savingsGoals:name')}</Label>
              <Input
                ref={inputRef}
                placeholder={t('savingsGoals:namePlaceholder')}
                value={form.name}
                onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('savingsGoals:targetAmount')}</Label>
                <Input
                  type="number"
                  placeholder="0.00"
                  value={form.targetAmount}
                  onChange={(e) => setForm((prev) => ({ ...prev, targetAmount: e.target.value }))}
                  disabled={isCreating}
                  className="bg-background border-border"
                  min="0.01"
                  step="0.01"
                />
              </div>
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t('savingsGoals:priority')}</Label>
                <Select value={form.priority} onValueChange={(v) => setForm((prev) => ({ ...prev, priority: v as typeof prev.priority }))}>
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
                value={form.deadline}
                onChange={(e) => setForm((prev) => ({ ...prev, deadline: e.target.value }))}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t('savingsGoals:link')}</Label>
              <Input
                placeholder={t('savingsGoals:linkPlaceholder')}
                value={form.link}
                onChange={(e) => setForm((prev) => ({ ...prev, link: e.target.value }))}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>
          </div>

          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={isCreating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>
            <Button
              onClick={handleSave}
              disabled={isCreating || !isValid}
              className="bg-primary hover:bg-primary/90 px-6"
            >
              {isCreating ? <Spinner size={20} /> : t('savingsGoals:create')}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
