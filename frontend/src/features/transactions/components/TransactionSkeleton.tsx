interface TransactionSkeletonProps {
  rows?: number
}

export function TransactionSkeleton({ rows = 5 }: TransactionSkeletonProps) {
  return (
    <div className="bg-card border border-border rounded-xl overflow-hidden">
      <div className="p-6 space-y-4">
        {Array.from({ length: rows }).map((_, i) => (
          <div key={`skeleton-${i}`} className="flex items-center gap-4">
            <div className="size-10 rounded-full bg-muted animate-pulse shrink-0" />
            <div className="flex-1 space-y-2">
              <div className="h-4 w-32 bg-muted rounded animate-pulse" />
              <div className="h-3 w-20 bg-muted rounded animate-pulse" />
            </div>
            <div className="h-4 w-16 bg-muted rounded animate-pulse" />
          </div>
        ))}
      </div>
    </div>
  )
}