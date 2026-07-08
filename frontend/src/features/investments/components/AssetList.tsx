import { useState, useMemo, useEffect } from "react"
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
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  onDelete: (investment: InvestmentResponse) => void
}

const ITEMS_PER_PAGE = 10

export function AssetList({ investments, isLoading, currentPage, totalPages, onPageChange, onDelete }: AssetListProps) {
  const { t } = useTranslation()
  const [searchTerm, setSearchTerm] = useState("")
  const [typeFilter, setTypeFilter] = useState<string>("all")

  // Reset to page 0 when filters change
  useEffect(() => {
    onPageChange(0)
  }, [searchTerm, typeFilter, onPageChange])

  const filteredData = useMemo(() => {
    return investments.filter((inv) => {
      const matchesSearch =
        inv.assetName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        inv.ticker?.toLowerCase().includes(searchTerm.toLowerCase())
      const matchesType = typeFilter === "all" || inv.type === typeFilter
      return matchesSearch && matchesType
    })
  }, [investments, searchTerm, typeFilter])

  const from = filteredData.length > 0 ? currentPage * ITEMS_PER_PAGE + 1 : 0
  const to = Math.min((currentPage + 1) * ITEMS_PER_PAGE, filteredData.length)

  if (isLoading) return <AssetListSkeleton />

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
          {t('investments:filterAll')}
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
          {filteredData.length === 0 ? (
            <p className="text-center text-muted-foreground py-12 text-sm">
              {t('investments:noAssets')}
            </p>
          ) : (
            filteredData.map((inv) => (
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

      {totalPages > 1 && (
        <div className="px-4 sm:px-6 py-3 sm:py-4 flex flex-col xs:flex-row items-center justify-between gap-3 border-t border-border bg-background/20">
          <p className="text-xs text-muted-foreground font-medium text-center xs:text-left">
            {t("transactions:showingInterval", { from, to, total: filteredData.length })}
          </p>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="h-8 border-border"
            >
              <ChevronLeft size={16} aria-hidden="true" />
            </Button>
            <span className="text-sm text-muted-foreground min-w-12 text-center tabular-nums">
              {currentPage + 1} / {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onPageChange(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
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
