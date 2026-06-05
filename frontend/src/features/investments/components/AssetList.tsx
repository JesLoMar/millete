import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Search } from "lucide-react"
import { Input } from "@/shared/components/ui/input"
import { Button } from "@/shared/components/ui/button"
import { Badge } from "@/shared/components/ui/badge"
import { AssetRow } from "./AssetRow"
import { AssetListSkeleton } from "./AssetListSkeleton"
import { INVESTMENT_TYPES } from "../constants"
import type { InvestmentResponse } from "../types"

interface AssetListProps {
  investments: InvestmentResponse[]
  isLoading: boolean
  onDelete: (investment: InvestmentResponse) => void
}

export function AssetList({ investments, isLoading, onDelete }: AssetListProps) {
  const { t } = useTranslation()
  const [searchTerm, setSearchTerm] = useState("")
  const [typeFilter, setTypeFilter] = useState<string>("all")

  const filteredData = investments.filter((inv) => {
    const matchesSearch =
      inv.assetName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      inv.ticker?.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesType = typeFilter === "all" || inv.type === typeFilter
    return matchesSearch && matchesType
  })

  if (isLoading) return <AssetListSkeleton />

  return (
    <div className="bg-card border border-border rounded-xl p-4 sm:p-6 space-y-3 sm:space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
        <h2 className="text-lg sm:text-xl font-semibold text-foreground font-headline">
          {t("investments.myAssets")}
        </h2>
        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder={t("investments.searchAsset")}
            className="pl-10 bg-background border-border h-9 sm:h-10 text-sm"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="flex items-center gap-1.5 sm:gap-2 flex-wrap">
        <Button
          variant={typeFilter === "all" ? "secondary" : "ghost"}
          size="sm"
          onClick={() => setTypeFilter("all")}
          className="h-7 text-xs rounded-md"
        >
          {t("investments.filterAll")}
        </Button>
        {INVESTMENT_TYPES.map((invType) => (
          <Button
            key={invType.value}
            variant={typeFilter === invType.value ? "secondary" : "ghost"}
            size="sm"
            onClick={() => setTypeFilter(invType.value)}
            className="h-7 text-xs rounded-md gap-1.5"
          >
            <invType.icon size={12} className={invType.color} aria-hidden="true" />
            {t(invType.labelKey)}
          </Button>
        ))}
        <Badge variant="outline" className="text-xs ml-auto shrink-0">
          {filteredData.length}
        </Badge>
      </div>

      <div className="overflow-x-auto">
        <div className="flex flex-col gap-1 sm:gap-2 min-w-100">
          {filteredData.length === 0 ? (
            <p className="text-center text-muted-foreground py-12 text-sm">
              {t("investments.noAssets")}
            </p>
          ) : (
            filteredData.map((inv) => (
              <AssetRow key={inv.id} investment={inv} onDelete={onDelete} />
            ))
          )}
        </div>
      </div>
    </div>
  )
}