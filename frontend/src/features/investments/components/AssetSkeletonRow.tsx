export function AssetSkeletonRow() {
  return (
    <div className="flex items-center justify-between gap-2 sm:gap-4 p-3 sm:p-4 border-b border-border/50 last:border-0">
      <div className="flex items-center gap-3 sm:gap-4">
        <div className="size-8 rounded-xl bg-muted animate-pulse shrink-0" />
        <div className="space-y-2 min-w-0">
          <div className="h-4 w-28 sm:w-32 bg-muted rounded animate-pulse" />
          <div className="h-3 w-20 sm:w-24 bg-muted rounded animate-pulse" />
        </div>
      </div>
      <div className="flex items-center gap-2 sm:gap-4 shrink-0">
        <div className="h-8 w-16 sm:w-20 bg-muted rounded animate-pulse" />
        <div className="w-25 sm:w-30 flex flex-col items-end space-y-2">
          <div className="h-4 w-16 sm:w-20 bg-muted rounded animate-pulse" />
          <div className="h-4 w-10 sm:w-12 bg-muted rounded animate-pulse" />
        </div>
        <div className="size-8 bg-muted rounded animate-pulse" />
      </div>
    </div>
  )
}