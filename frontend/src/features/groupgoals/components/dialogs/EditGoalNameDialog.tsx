import { useEffect, useState, useRef } from "react"
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

interface EditGoalNameDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentName: string
  onSave: (newName: string) => Promise<void>
  isSaving?: boolean
}

export function EditGoalNameDialog({
  open,
  onOpenChange,
  currentName,
  onSave,
  isSaving = false,
}: EditGoalNameDialogProps) {
  const { t } = useTranslation(["groupGoals", "common"])
  const [editedName, setEditedName] = useState<string | null>(null)
  const name = editedName ?? currentName
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (open) setEditedName(null)
  }, [open])

  const handleSave = async () => {
    if (!name.trim()) return
    await onSave(name.trim())
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="bg-card border-border sm:max-w-sm"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle>{t("groupGoals:editNameTitle")}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label>{t("groupGoals:name")}</Label>
            <Input
              ref={inputRef}
              value={name}
              onChange={(e) => setEditedName(e.target.value)}
              placeholder={t("groupGoals:familyNamePlaceholder")}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            {t("common:actions.cancel")}
          </Button>
          <Button onClick={handleSave} disabled={!name.trim() || isSaving}>
            {isSaving ? <Spinner size={20} /> : t("common:actions.save")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
