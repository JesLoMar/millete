import { useState, useRef, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Loader2, AlertTriangle } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from "@/shared/components/ui/dialog"
import { useInvestmentMutations } from "../hooks/useInvestmentMutations"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"

interface UpdatePriceDialogProps {
  investmentId: string
  assetName: string
  currentPrice: number
}

export function UpdatePriceDialog({ investmentId, assetName, currentPrice }: UpdatePriceDialogProps) {
  const { t } = useTranslation()
  const { updatePrice, isUpdating } = useInvestmentMutations()
  const [open, setOpen] = useState(false)
  const [newPrice, setNewPrice] = useState(() => currentPrice.toString())
  const [needsConfirmation, setNeedsConfirmation] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const isSamePrice = Number(newPrice) === currentPrice
  const targetPrice = Number(newPrice)

  const deviationRatio = currentPrice > 0 ? Math.abs(targetPrice - currentPrice) / currentPrice : 0
  const isCriticalDeviation = deviationRatio > 0.5

  useEffect(() => {
    setNeedsConfirmation(false)
  }, [newPrice])

  const handleUpdate = async () => {
    if (isSamePrice || targetPrice <= 0) return

    if (isCriticalDeviation && !needsConfirmation) {
      setNeedsConfirmation(true)
      return
    }

    try {
      await updatePrice.mutateAsync({ id: investmentId, price: targetPrice })
      setOpen(false)
      setNeedsConfirmation(false)
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t("investments.updatePriceError")
      notify.error(message)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { setOpen(isOpen); if (!isOpen) setNeedsConfirmation(false) }}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="sm" className="h-8 text-primary font-semibold hover:bg-primary/10 px-3 rounded-lg">
          {t("investments.updatePrice")}
        </Button>
      </DialogTrigger>
      <DialogContent
        className="bg-card border-border sm:max-w-95"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle className="text-lg font-semibold">{t("investments.marketPrice")}</DialogTitle>
          <p className="text-sm text-muted-foreground">{assetName}</p>
        </DialogHeader>

        <div className="py-4 space-y-3">
          <div className="space-y-2">
            <Label className="text-sm font-semibold">{t("investments.newPrice")}</Label>
            <Input
              ref={inputRef}
              type="number"
              value={newPrice}
              onChange={(e) => setNewPrice(e.target.value)}
              disabled={isUpdating}
              className="bg-background border-border text-xl font-semibold"
              min="0.01"
              step="0.01"
            />
          </div>

          {needsConfirmation && (
            <div className="flex items-start gap-2 p-3 rounded-lg bg-destructive/10 border border-destructive/20 text-destructive text-xs font-medium">
              <AlertTriangle className="size-4 shrink-0 mt-0.5" />
              <div>
                <p className="font-bold">{t("investments.alerts.warningTitle")}</p>
                <p className="text-muted-foreground mt-0.5">
                  {t("investments.alerts.warningDesc")}
                </p>
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button 
            onClick={handleUpdate} 
            disabled={isUpdating || isSamePrice || targetPrice <= 0} 
            variant={needsConfirmation ? "destructive" : "default"}
            className="w-full font-semibold transition-colors"
          >
            {isUpdating ? (
              <Loader2 size={16} className="animate-spin" />
            ) : needsConfirmation ? (
              t("investments.confirmUpdate")
            ) : (
              t("investments.updateNow")
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}