import { useTranslation } from "react-i18next"
import { TrendingUp, TrendingDown, MoreHorizontal, Trash2 } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { UpdatePriceDialog } from "./UpdatePriceDialog"
import { cn } from "@/lib/utils"
import { TYPE_COLORS } from "../constants"
import type { InvestmentResponse } from "../types"

interface AssetRowProps {
  investment: InvestmentResponse
  onDelete: (investment: InvestmentResponse) => void
}

export function AssetRow({ investment: inv, onDelete }: AssetRowProps) {
  const { t } = useTranslation()
  const trend = (inv.profitOrLoss ?? 0) >= 0 ? "up" : "down"
  const percentage = inv.roiPercentage ?? 0

  return (
    <div className="flex items-center justify-between p-4 border-b border-border/50 last:border-0 hover:bg-accent/30 rounded-xl transition-all group">
      <div className="flex items-center gap-4">
        <div className={`size-8 rounded-xl flex items-center justify-center font-bold text-xs text-white ${TYPE_COLORS[inv.type] || "bg-primary"}`}>
          {inv.ticker || inv.assetName.substring(0, 3).toUpperCase()}
        </div>
        <div>
          <p className="text-sm font-bold text-foreground">{inv.assetName}</p>
          <p className="text-xs text-muted-foreground">
            {inv.quantity} {t("investments.shares")} • {t(`investments.types.${inv.type.toLowerCase()}`)}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-6">
        <UpdatePriceDialog
          investmentId={inv.id}
          assetName={inv.assetName}
          currentPrice={inv.currentPrice}
        />

        <div className="w-35 flex flex-col items-end">
          <p className={cn(
            "text-sm font-bold flex items-center gap-1",
            trend === "up" ? "text-emerald-500" : "text-rose-500"
          )}>
            {trend === "up" ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
            {inv.currentValue?.toLocaleString("es-ES", { minimumFractionDigits: 2 })} €
          </p>
          <span className={cn(
            "text-[10px] font-bold px-2 py-0.5 rounded-full mt-1 border",
            trend === "up"
              ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
              : "bg-rose-500/10 text-rose-500 border-rose-500/20"
          )}>
            {percentage != null ? `${percentage > 0 ? "+" : ""}${percentage.toFixed(1)}%` : "—"}
          </span>
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="size-8 opacity-0 group-hover:opacity-100 transition-opacity">
              <MoreHorizontal size={16} />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="bg-card border-border">
            <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(inv)}>
              <Trash2 className="mr-2 size-4" />
              {t("investments.delete")}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  )
}