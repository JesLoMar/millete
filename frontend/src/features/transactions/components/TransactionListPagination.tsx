import { useTranslation } from "react-i18next"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/shared/components/core/button"

interface TransactionListPaginationProps {
  currentPage: number
  totalPages: number
  from: number
  to: number
  total: number
  onPrev: () => void
  onNext: () => void
}

export function TransactionListPagination({ currentPage, totalPages, from, to, total, onPrev, onNext }: TransactionListPaginationProps) {
  const { t } = useTranslation()

  if (totalPages <= 1) return null

  return (
    <div className="px-4 sm:px-6 py-3 sm:py-4 flex flex-col xs:flex-row items-center justify-between gap-3 border-t border-border bg-background/20">
      <p className="text-xs text-muted-foreground font-medium text-center xs:text-left">
        {t("transactions:showingInterval", { from, to, total })}
      </p>
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={onPrev}
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
          onClick={onNext}
          disabled={currentPage >= totalPages - 1}
          className="h-8 border-border"
        >
          <ChevronRight size={16} aria-hidden="true" />
        </Button>
      </div>
    </div>
  )
}
