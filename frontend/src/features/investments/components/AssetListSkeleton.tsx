import { AssetSkeletonRow } from "./AssetSkeletonRow"

export function AssetListSkeleton() {
  return (
    <div className="bg-card border border-border rounded-xl p-6 space-y-4">
      <div className="flex gap-4">
        <div className="h-10 w-48 bg-muted animate-pulse rounded" />
        <div className="h-10 w-64 bg-muted animate-pulse rounded ml-auto" />
      </div>
      {Array.from({ length: 5 }).map((_, i) => (
        <AssetSkeletonRow key={`skeleton-${i}`} />
      ))}
    </div>
  )
}