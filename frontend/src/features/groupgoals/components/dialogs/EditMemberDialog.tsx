import { useState } from "react"
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
  DialogFooter,
} from "@/shared/components/core/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select"
import type { GoalMember } from "../../types"

interface EditMemberDialogProps {
  member: GoalMember | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSave: (memberId: string, role: string, salary: number, customPercentage?: number) => Promise<void>
  isSaving?: boolean
  totalCustomPercentage?: number
}

export function EditMemberDialog({ member, open, onOpenChange, onSave, isSaving = false, totalCustomPercentage }: EditMemberDialogProps) {
  const { t } = useTranslation()
  const [role, setRole] = useState<string>(member?.role || "MEMBER")
  const [salary, setSalary] = useState(member?.salary?.toString() || "")
  const [customPercentage, setCustomPercentage] = useState(
    member?.customPercentage?.toString() || ""
  )

  const handleSave = async () => {
    if (member) {
      await onSave(
        member.id,
        role,
        Number(salary) || 0,
        customPercentage ? Number(customPercentage) : undefined
      )
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange} key={member?.id ?? "new"}>
      <DialogContent className="bg-card border-border sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{t('groupGoals:editMember')}</DialogTitle>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label>{t('groupGoals:name')}</Label>
            <Input
              value={member?.name || ""}
              disabled
              className="bg-background border-border opacity-60"
            />
          </div>

          <div className="space-y-2">
            <Label>{t('groupGoals:role')}</Label>
            <Select value={role} onValueChange={setRole}>
              <SelectTrigger className="bg-background border-border">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-card border-border">
                <SelectItem value="ADMIN">{t('groupGoals:admin')}</SelectItem>
                <SelectItem value="MEMBER">{t('groupGoals:member')}</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>{t('groupGoals:monthlySalary')}</Label>
            <Input
              type="number"
              value={salary}
              onChange={(e) => setSalary(e.target.value)}
              className="bg-background border-border"
              min="0"
              step="0.01"
            />
          </div>

          <div className="space-y-2">
            <Label>{t('groupGoals:customPercentage')}</Label>
            <Input
              type="number"
              value={customPercentage}
              onChange={(e) => setCustomPercentage(e.target.value)}
              className="bg-background border-border"
              min="0"
              max="100"
              step="0.01"
              placeholder="0"
            />
            <p className="text-xs text-muted-foreground">
              {t('groupGoals:customPercentageHint', { total: totalCustomPercentage?.toFixed(2) ?? '0.00' })}
            </p>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-border">
            {t('common:actions.cancel')}
          </Button>
          <Button onClick={handleSave} disabled={isSaving}>
            {isSaving ? <Spinner size={20} /> : t('groupGoals:save')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
