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
  DialogFooter,
} from "@/shared/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/ui/select"
import type { FamilyMember } from "../../types"

interface EditMemberDialogProps {
  member: FamilyMember | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSave: (member: FamilyMember) => void
}

export function EditMemberDialog({ member, open, onOpenChange, onSave }: EditMemberDialogProps) {
  const { t } = useTranslation()
  const [role, setRole] = useState<string>(member?.role || "MEMBER")
  const [salary, setSalary] = useState(member?.salary?.toString() || "")
  const [customPercentage, setCustomPercentage] = useState(member?.customPercentage?.toString() || "")

  const handleSave = () => {
    if (member) {
      const clampedPercentage = Math.max(0, Math.min(100, Number(customPercentage) || 0))
      onSave({
        ...member,
        role: role as FamilyMember["role"],
        salary: Number(salary) || 0,
        customPercentage: clampedPercentage,
      })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange} key={member?.id ?? "new"}>
      <DialogContent className="bg-card border-border sm:max-w-106.25">
        <DialogHeader>
          <DialogTitle>{t("family.editMember")}</DialogTitle>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label>{t("family.name")}</Label>
            <Input
              value={member?.name || ""}
              disabled
              className="bg-background border-border opacity-60"
            />
          </div>

          <div className="space-y-2">
            <Label>{t("family.role")}</Label>
            <Select value={role} onValueChange={setRole}>
              <SelectTrigger className="bg-background border-border">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-card border-border">
                <SelectItem value="ADMIN">{t("family.admin")}</SelectItem>
                <SelectItem value="MEMBER">{t("family.member")}</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>{t("family.monthlySalary")}</Label>
            <Input
              type="number"
              value={salary}
              onChange={(e) => setSalary(e.target.value)}
              className="bg-background border-border"
              min="0"
            />
          </div>

          <div className="space-y-2">
            <Label>{t("family.customPercentage")}</Label>
            <Input
              type="number"
              value={customPercentage}
              onChange={(e) => setCustomPercentage(e.target.value)}
              className="bg-background border-border"
              min="0"
              max="100"
              step="0.1"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button onClick={handleSave}>{t("family.save")}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}