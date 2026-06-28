const ITEMS_PER_PAGE = 10

export function TransactionListSkeleton() {
  return (
    <div className="bg-card border border-border rounded-xl overflow-hidden">
      <div className="p-4 sm:p-6 space-y-4">
        {Array.from({ length: ITEMS_PER_PAGE }).map((_, i) => (
          <div key={`skeleton-${i}`} className="flex items-center gap-3 sm:gap-4">
            <div className="size-8 sm:size-10 rounded-full bg-muted animate-pulse shrink-0" />
            <div className="flex-1 min-w-0 space-y-2">
              <div className="h-4 w-28 sm:w-32 bg-muted rounded animate-pulse" />
              <div className="h-3 w-16 sm:w-20 bg-muted rounded animate-pulse" />
            </div>
            <div className="h-4 w-12 sm:w-16 bg-muted rounded animate-pulse shrink-0" />
          </div>
        ))}
      </div>
    </div>
  )
}
