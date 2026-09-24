interface TransactionSkeletonProps {
  rows?: number;
}

export function TransactionSkeleton({
  rows = 5,
}: TransactionSkeletonProps) {
  return (
    <div
      className="overflow-hidden rounded-xl border border-border bg-card"
      aria-hidden="true"
    >
      <div className="space-y-4 p-6">
        {Array.from({ length: rows }).map((_, index) => (
          <div
            key={`skeleton-${index}`}
            className="flex items-center gap-4"
          >
            <div className="size-10 shrink-0 animate-pulse rounded-full bg-muted" />

            <div className="flex-1 space-y-2">
              <div className="h-4 w-32 animate-pulse rounded bg-muted" />
              <div className="h-3 w-20 animate-pulse rounded bg-muted" />
            </div>

            <div className="h-4 w-16 animate-pulse rounded bg-muted" />
          </div>
        ))}
      </div>
    </div>
  );
}