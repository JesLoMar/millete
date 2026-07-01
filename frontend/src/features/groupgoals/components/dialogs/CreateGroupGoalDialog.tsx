import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { Spinner } from "@/shared/components/Spinner"
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

interface CreateGroupGoalDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onCreate: (name: string, monthlyTarget: number, distributionMode: string) => Promise<void>
  isCreating?: boolean
}

export function CreateGroupGoalDialog({ open, onOpenChange, onCreate, isCreating = false }: CreateGroupGoalDialogProps) {
  const { t } = useTranslation()
  const [name, setName] = useState("")
  const [monthlyTarget, setMonthlyTarget] = useState(0)
  const [distributionMode, setDistributionMode] = useState("EQUITATIVE")
  const inputRef = useRef<HTMLInputElement>(null)

  const handleCreate = async () => {
    if (name.trim() && monthlyTarget > 0) {
      await onCreate(name.trim(), monthlyTarget, distributionMode)
      setName("")
      setMonthlyTarget(0)
      setDistributionMode("EQUITATIVE")
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-md"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle>{t('groupGoals:createTitle')}</DialogTitle>
          <DialogDescription>{t('groupGoals:createDesc')}</DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label>{t('groupGoals:familyName')}</Label>
            <Input
              ref={inputRef}
              placeholder={t('groupGoals:familyNamePlaceholder')}
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="bg-background border-border"
            />
          </div>
          <div className="space-y-2">
            <Label>{t('groupGoals:monthlyGoal')} (€)</Label>
            <Input
              type="number"
              placeholder="0"
              value={monthlyTarget || ""}
              onChange={(e) => setMonthlyTarget(Number(e.target.value))}
              className="bg-background border-border"
            />
          </div>
          <div className="space-y-2">
            <Label>{t('groupGoals:distributionMode')}</Label>
            <Select value={distributionMode} onValueChange={setDistributionMode}>
              <SelectTrigger className="bg-background border-border">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-card border-border">
                <SelectItem value="EQUITATIVE">{t('groupGoals:modes.equitative')}</SelectItem>
                <SelectItem value="PROPORTIONAL">{t('groupGoals:modes.proportional')}</SelectItem>
                <SelectItem value="CUSTOM">{t('groupGoals:modes.custom')}</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-border">
            {t('common:actions.cancel')}
          </Button>
          <Button onClick={handleCreate} disabled={!name.trim() || monthlyTarget <= 0 || isCreating}>
            {isCreating ? <Spinner size={20} /> : t('groupGoals:create')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
