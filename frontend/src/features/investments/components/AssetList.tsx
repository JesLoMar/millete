import { useTranslation } from "react-i18next"
import { m } from "framer-motion"
import { Search, ChevronLeft, ChevronRight } from "lucide-react"
import { Input } from "@/shared/components/core/input"
import { Button } from "@/shared/components/core/button"
import { Badge } from "@/shared/components/core/badge"
import { AssetRow } from "./AssetRow"
import { AssetListSkeleton } from "./AssetListSkeleton"
import { INVESTMENT_TYPES } from "../constants"
import type { InvestmentResponse } from "../types"

interface AssetListProps {
  investments: InvestmentResponse[]
  isLoading: boolean
  displayPage: number
  totalDisplayPages: number
  totalElements: number
  displaySize: number
  searchTerm: string
  typeFilter: string
  onSearchChange: (value: string) => void
  onTypeFilterChange: (value: string) => void
  onNextPage: () => void
  onPrevPage: () => void
  onDelete: (investment: InvestmentResponse) => void
}

export function AssetList({
  investments,
  isLoading,
  displayPage,
  totalDisplayPages,
  totalElements,
  displaySize,
  searchTerm,
  typeFilter,
  onSearchChange,
  onTypeFilterChange,
  onNextPage,
  onPrevPage,
  onDelete,
}: AssetListProps) {
  const { t } = useTranslation()

  const from = totalElements > 0 ? displayPage * displaySize + 1 : 0
  const to = Math.min((displayPage + 1) * displaySize, totalElements)

  if (isLoading && investments.length === 0) return <AssetListSkeleton />

  return (
    <div className="bg-card border border-border rounded-xl p-4 sm:p-6 space-y-3 sm:space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
        <h2 className="text-lg sm:text-xl font-semibold text-foreground font-serif">
          {t('investments:myAssets')}
        </h2>
        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder={t('investments:searchAsset')}
            className="pl-10 bg-background border-border h-9 sm:h-10 text-sm"
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
          />
        </div>
      </div>

      <div className="flex items-center gap-1.5 sm:gap-2 flex-wrap">
        <Button
          variant={typeFilter === "all" ? "secondary" : "ghost"}
          size="sm"
          onClick={() => onTypeFilterChange("all")}
          className="h-7 text-xs rounded-md"
        >
          {t('investments:filterAll')}
        </Button>
        {INVESTMENT_TYPES.map((invType) => (
          <Button
            key={invType.value}
            variant={typeFilter === invType.value ? "secondary" : "ghost"}
            size="sm"
            onClick={() => onTypeFilterChange(invType.value)}
            className="h-7 text-xs rounded-md gap-1.5"
          >
            <invType.icon size={12} className={invType.color} aria-hidden="true" />
            {t(invType.labelKey)}
          </Button>
        ))}
        <Badge variant="outline" className="text-xs ml-auto shrink-0">
          {totalElements}
        </Badge>
      </div>

      <div className="overflow-x-auto">
        <m.div
          className="flex flex-col gap-1 sm:gap-2 min-w-0"
          initial="hidden"
          animate="visible"
          variants={{
            hidden: { opacity: 0 },
            visible: {
              opacity: 1,
              transition: { staggerChildren: 0.04 }
            }
          }}
        >
          {investments.length === 0 ? (
            <p className="text-center text-muted-foreground py-12 text-sm">
              {t('investments:noAssets')}
            </p>
          ) : (
            investments.map((inv) => (
              <m.div
                key={inv.id}
                variants={{
                  hidden: { opacity: 0, x: -20 },
                  visible: { opacity: 1, x: 0 }
                }}
                transition={{ duration: 0.3, ease: "easeOut" }}
              >
                <AssetRow investment={inv} onDelete={onDelete} />
              </m.div>
            ))
          )}
        </m.div>
      </div>

      {totalDisplayPages > 1 && (
        <div className="px-4 sm:px-6 py-3 sm:py-4 flex flex-col xs:flex-row items-center justify-between gap-3 border-t border-border bg-background/20">
          <p className="text-xs text-muted-foreground font-medium text-center xs:text-left">
            {t("transactions:showingInterval", { from, to, total: totalElements })}
          </p>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={onPrevPage}
              disabled={displayPage === 0}
              className="h-8 border-border"
            >
              <ChevronLeft size={16} aria-hidden="true" />
            </Button>
            <span className="text-sm text-muted-foreground min-w-12 text-center tabular-nums">
              {displayPage + 1} / {totalDisplayPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={onNextPage}
              disabled={displayPage >= totalDisplayPages - 1}
              className="h-8 border-border"
            >
              <ChevronRight size={16} aria-hidden="true" />
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
