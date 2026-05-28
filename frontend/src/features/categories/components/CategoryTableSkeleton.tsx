export function CategoryTableSkeleton() {
  return (
    <div className="space-y-4">
      <div className="flex gap-4">
        <div className="h-10 w-[320px] bg-muted animate-pulse rounded" />
        <div className="h-10 w-24 bg-muted animate-pulse rounded ml-auto" />
      </div>
      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="flex items-center gap-4 p-4 border-b last:border-0">
            <div className="size-5 rounded-full bg-muted animate-pulse shrink-0" />
            <div className="w-32 h-5 bg-muted animate-pulse rounded" />
            <div className="flex-1">
              <div className="h-1.5 w-full bg-muted animate-pulse rounded" />
            </div>
            <div className="w-40 h-5 bg-muted animate-pulse rounded" />
            <div className="size-8 bg-muted animate-pulse rounded" />
          </div>
        ))}
      </div>
    </div>
  )
}