import { useState } from "react"
import { useTranslation } from "react-i18next"
import type { SavingsGoal } from "../types"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/shared/components/core/dialog"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import { Button } from "@/shared/components/core/button"

interface Props {
  isOpen: boolean
  onClose: () => void
  onSubmit: (amount: number) => void
  goal: SavingsGoal | null
}

export const ContributionModal = ({ isOpen, onClose, onSubmit, goal }: Props) => {
  const { t } = useTranslation()
  const [amount, setAmount] = useState("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const numAmount = parseFloat(amount)
    if (!isNaN(numAmount) && numAmount > 0) {
      onSubmit(numAmount)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()} key={goal?.id}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("savingsGoals:addFunds", { name: goal?.name })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="amount">{t('savingsGoals:amount')}</Label>
            <Input id="amount" type="number" step="0.01" min="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={onClose}>{t('common:actions.cancel')}</Button>
            <Button type="submit">{t('savingsGoals:add')}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
