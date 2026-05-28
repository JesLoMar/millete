import { useState, useRef } from "react"
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

interface CreateFamilyDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onCreate: (name: string, monthlyGoal: number) => void
}

export function CreateFamilyDialog({ open, onOpenChange, onCreate }: CreateFamilyDialogProps) {
  const { t } = useTranslation()
  const [name, setName] = useState("")
  const [monthlyGoal, setMonthlyGoal] = useState(0)
  const inputRef = useRef<HTMLInputElement>(null)

  const handleCreate = () => {
    if (name.trim() && monthlyGoal > 0) {
      onCreate(name.trim(), monthlyGoal)
      setName("")
      setMonthlyGoal(0)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-106.25"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle>{t("family.createTitle")}</DialogTitle>
          <DialogDescription>{t("family.createDesc")}</DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label>{t("family.familyName")}</Label>
            <Input
              ref={inputRef}
              placeholder={t("family.familyNamePlaceholder")}
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="bg-background border-border"
            />
          </div>
          <div className="space-y-2">
            <Label>{t("family.monthlyGoal")} (€)</Label>
            <Input
              type="number"
              placeholder="0"
              value={monthlyGoal || ""}
              onChange={(e) => setMonthlyGoal(Number(e.target.value))}
              className="bg-background border-border"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button onClick={handleCreate} disabled={!name.trim() || monthlyGoal <= 0}>
            {t("family.create")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}