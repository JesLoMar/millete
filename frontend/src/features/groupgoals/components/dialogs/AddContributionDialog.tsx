import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
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
} from "@/shared/components/core/dialog"

interface AddContributionDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSave: (amount: number) => Promise<void>
  isSaving?: boolean
}

export function AddContributionDialog({
  open,
  onOpenChange,
  onSave,
  isSaving = false
}: AddContributionDialogProps) {
  const { t } = useTranslation()
  const [amount, setAmount] = useState("")
  const inputRef = useRef<HTMLInputElement>(null)

  const handleSave = async () => {
    if (!amount || Number(amount) <= 0 || isSaving) return
    try {
      await onSave(Number(amount))
      setAmount("")
      onOpenChange(false)
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isSaving) onOpenChange(isOpen) }}>
      <DialogContent
        className="bg-card border-border sm:max-w-100"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle>{t('groupGoals:addContribution')}</DialogTitle>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t('groupGoals:amount')} (€)</Label>
            <Input
              ref={inputRef}
              type="number"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              disabled={isSaving}
              className="bg-background border-border"
              min="0.01"
              step="0.01"
            />
          </div>
        </div>
        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isSaving} className="border-border">
            {t('common:actions.cancel')}
          </Button>
          <Button onClick={handleSave} disabled={!amount || Number(amount) <= 0 || isSaving} className="min-w-24">
            {isSaving ? <Spinner size={20} /> : t('groupGoals:save')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
