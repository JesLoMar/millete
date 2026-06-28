import { useTranslation } from "react-i18next"
import { Search } from "lucide-react"
import { Input } from "@/shared/components/core/input"
import { Button } from "@/shared/components/core/button"
import { Badge } from "@/shared/components/core/badge"
import { cn } from "@/lib/utils"
import { FILTERS, FILTER_LABELS, type Filter } from "../constants"

interface TransactionListFiltersProps {
  filter: Filter
  searchTerm: string
  totalCount: number
  onFilterChange: (filter: Filter) => void
  onSearchChange: (searchTerm: string) => void
}

export function TransactionListFilters({ filter, searchTerm, totalCount, onFilterChange, onSearchChange }: TransactionListFiltersProps) {
  const { t } = useTranslation()

  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
      <div className="flex items-center gap-2 sm:gap-3">
        <div className="flex items-center gap-1 sm:gap-2 bg-card p-1 rounded-lg border border-border">
          {FILTERS.map((f) => (
            <Button
              key={f}
              variant={filter === f ? "secondary" : "ghost"}
              size="sm"
              onClick={() => onFilterChange(f)}
              className={cn(
                "rounded-md text-xs sm:text-sm transition-all h-7 sm:h-8 px-2 sm:px-3",
                filter === f ? "bg-primary/20 text-primary" : "text-muted-foreground"
              )}
            >
              {t(FILTER_LABELS[f])}
            </Button>
          ))}
        </div>
        <Badge variant="outline" className="text-xs shrink-0">
          {totalCount}
        </Badge>
      </div>

      <div className="relative w-full sm:w-[320px]">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
        <Input
          placeholder={t('transactions:search')}
          className="pl-10 bg-card border-border h-9 sm:h-10 text-sm"
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
        />
      </div>
    </div>
  )
}
