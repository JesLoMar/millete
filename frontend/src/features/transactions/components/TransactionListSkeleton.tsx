const ITEMS_PER_PAGE = 10;

export function TransactionListSkeleton() {
  return (
    <div
      className="overflow-hidden rounded-xl border border-border bg-card"
      aria-hidden="true"
    >
      <div className="space-y-4 p-4 sm:p-6">
        {Array.from({ length: ITEMS_PER_PAGE }).map((_, index) => (
          <div
            key={`skeleton-${index}`}
            className="flex items-center gap-3 sm:gap-4"
          >
            <div className="size-8 shrink-0 animate-pulse rounded-full bg-muted sm:size-10" />

            <div className="min-w-0 flex-1 space-y-2">
              <div className="h-4 w-28 animate-pulse rounded bg-muted sm:w-32" />
              <div className="h-3 w-16 animate-pulse rounded bg-muted sm:w-20" />
            </div>

            <div className="h-4 w-12 shrink-0 animate-pulse rounded bg-muted sm:w-16" />
          </div>
        ))}
      </div>
    </div>
  );
}