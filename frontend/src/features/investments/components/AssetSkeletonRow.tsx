export function AssetSkeletonRow() {
  return (
    <div className="flex items-center justify-between p-4 border-b border-border/50 last:border-0">
      <div className="flex items-center gap-4">
        <div className="size-8 rounded-xl bg-muted animate-pulse" />
        <div className="space-y-2">
          <div className="h-4 w-32 bg-muted rounded animate-pulse" />
          <div className="h-3 w-24 bg-muted rounded animate-pulse" />
        </div>
      </div>
      <div className="flex items-center gap-6">
        <div className="h-8 w-20 bg-muted rounded animate-pulse" />
        <div className="w-35 flex flex-col items-end space-y-2">
          <div className="h-4 w-20 bg-muted rounded animate-pulse" />
          <div className="h-4 w-12 bg-muted rounded animate-pulse" />
        </div>
        <div className="size-8 bg-muted rounded animate-pulse" />
      </div>
    </div>
  )
}