import { useState, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Plus, Loader2, TrendingUp } from "lucide-react"
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/ui/select"
import { INVESTMENT_TYPES } from "../constants"
import { useInvestmentMutations } from "../hooks/useInvestmentMutations"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"

export function NewInvestmentDialog() {
  const { t } = useTranslation()
  const { createInvestment, isCreating } = useInvestmentMutations()
  const [open, setOpen] = useState(false)
  const [assetName, setAssetName] = useState("")
  const [ticker, setTicker] = useState("")
  const [quantity, setQuantity] = useState("")
  const [purchasePrice, setPurchasePrice] = useState("")
  const [type, setType] = useState("STOCK")
  const [purchaseDate, setPurchaseDate] = useState(new Date().toISOString().split('T')[0])
  const inputRef = useRef<HTMLInputElement>(null)

  const resetForm = () => {
    setAssetName("")
    setTicker("")
    setQuantity("")
    setPurchasePrice("")
    setType("STOCK")
    setPurchaseDate(new Date().toISOString().split('T')[0])
  }

  const handleSave = async () => {
    if (!assetName || !quantity || !purchasePrice) return

    try {
      await createInvestment.mutateAsync({
        assetName: assetName.trim(),
        ticker: ticker.toUpperCase().trim() || null,
        quantity: Number(quantity),
        purchasePrice: Number(purchasePrice),
        type,
        purchaseDate: purchaseDate,
      })
      setOpen(false)
      resetForm()
    } catch (err) {
      const apiError = err as ApiError
      const message = apiError?.response?.data?.message || t("investments.createError") || "Error al registrar la inversión"
      notify.error(message)
    }
  }

  const isValid = assetName.trim() && quantity && Number(quantity) > 0 && purchasePrice && Number(purchasePrice) > 0

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { setOpen(isOpen); if (!isOpen) resetForm() }}>
      <DialogTrigger asChild>
        <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-4 shrink-0">
          <Plus size={16} />
          {t("investments.new")}
        </Button>
      </DialogTrigger>

      <DialogContent
        className="bg-card border-border sm:max-w-120"
        onOpenAutoFocus={(e) => {
          e.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold flex items-center gap-2">
              <TrendingUp className="text-primary size-5" />
              {t("investments.newTitle")}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="grid grid-cols-3 gap-3 sm:gap-4">
              <div className="col-span-2 space-y-2">
                <Label className="text-sm font-semibold">{t("investments.assetName")}</Label>
                <Input
                  ref={inputRef}
                  placeholder={t("investments.assetNamePlaceholder")}
                  value={assetName}
                  onChange={(e) => setAssetName(e.target.value)}
                  disabled={isCreating}
                  className="bg-background border-border"
                />
              </div>
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t("investments.ticker")}</Label>
                <Input
                  placeholder="AAPL"
                  value={ticker}
                  onChange={(e) => setTicker(e.target.value.toUpperCase())}
                  disabled={isCreating}
                  className="bg-background border-border"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("investments.type")}</Label>
              <Select value={type} onValueChange={setType}>
                <SelectTrigger className="bg-background border-border">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-card border-border">
                  {INVESTMENT_TYPES.map((invType) => (
                    <SelectItem key={invType.value} value={invType.value}>
                      <div className="flex items-center gap-2">
                        <invType.icon size={14} className={invType.color} />
                        {t(invType.labelKey)}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t("investments.quantity")}</Label>
                <Input
                  type="number"
                  placeholder="0.00"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  disabled={isCreating}
                  className="bg-background border-border"
                  min="0.0001"
                  step="any"
                />
              </div>
              <div className="space-y-2">
                <Label className="text-sm font-semibold">{t("investments.purchasePrice")}</Label>
                <Input
                  type="number"
                  placeholder="0.00"
                  value={purchasePrice}
                  onChange={(e) => setPurchasePrice(e.target.value)}
                  disabled={isCreating}
                  className="bg-background border-border"
                  min="0.01"
                  step="0.01"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">{t("investments.purchaseDate")}</Label>
              <Input
                type="date"
                value={purchaseDate}
                onChange={(e) => setPurchaseDate(e.target.value)}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>
          </div>

          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button variant="outline" onClick={() => setOpen(false)} disabled={isCreating} className="border-border">
              {t("common.cancel")}
            </Button>
            <Button onClick={handleSave} disabled={isCreating || !isValid} className="bg-primary hover:bg-primary/90 px-6">
              {isCreating ? <Loader2 size={16} className="animate-spin" /> : t("investments.save")}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}