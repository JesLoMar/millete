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
    <div className="flex items-center justify-between gap-2 sm:gap-4 p-3 sm:p-4 border-b border-border/50 last:border-0 hover:bg-accent/30 transition-all group">
      <div className="flex items-center gap-3 sm:gap-4 min-w-0">
        <div className={`size-8 rounded-xl flex items-center justify-center font-bold text-xs text-primary-foreground shrink-0 ${TYPE_COLORS[inv.type] || "bg-primary"}`}>
          {inv.ticker || inv.assetName.substring(0, 3).toUpperCase()}
        </div>
        <div className="min-w-0">
          <p className="text-sm font-bold text-foreground truncate">{inv.assetName}</p>
          <p className="text-xs text-muted-foreground truncate">
            {inv.quantity} {t("investments.shares")} • {t(`investments.types.${inv.type.toLowerCase()}`)}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2 sm:gap-4 shrink-0">
        <UpdatePriceDialog
          investmentId={inv.id}
          assetName={inv.assetName}
          currentPrice={inv.currentPrice}
        />

        <div className="w-30 sm:w-35 flex flex-col items-end">
          <p className={cn(
            "text-xs sm:text-sm font-bold flex items-center gap-1 whitespace-nowrap",
            trend === "up" ? "text-emerald-500" : "text-rose-500"
          )}>
            {trend === "up" ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
            {inv.currentValue?.toLocaleString("es-ES", { minimumFractionDigits: 2 })} €
          </p>
          <span className={cn(
            "text-[10px] font-bold px-1.5 sm:px-2 py-0.5 rounded-full mt-1 border whitespace-nowrap",
            trend === "up"
              ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
              : "bg-rose-500/10 text-rose-500 border-rose-500/20"
          )}>
            {percentage != null ? `${percentage > 0 ? "+" : ""}${percentage.toFixed(1)}%` : "—"}
          </span>
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button 
              variant="ghost" 
              size="icon" 
              className="size-8 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity"
              aria-label={t("investments.assetOptions", { name: inv.assetName })}
            >
              <MoreHorizontal size={16} aria-hidden="true" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="bg-card border-border">
            <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(inv)}>
              <Trash2 className="mr-2 size-4" aria-hidden="true" />
              {t("investments.delete")}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  )
}