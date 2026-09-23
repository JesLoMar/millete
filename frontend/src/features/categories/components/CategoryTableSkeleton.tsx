const SKELETON_ROWS = 5;

export function CategoryTableSkeleton() {
  return (
    <div
      className="space-y-4"
      aria-hidden="true"
    >
      <div className="flex flex-col gap-4 sm:flex-row">
        <div className="h-10 w-full animate-pulse rounded bg-muted sm:w-[320px]" />
        <div className="h-10 w-24 animate-pulse rounded bg-muted sm:ml-auto" />
      </div>

      <div className="overflow-hidden rounded-xl border border-border bg-card">
        {Array.from({ length: SKELETON_ROWS }).map(
          (_, index) => (
            <div
              key={index}
              className="border-b p-3 last:border-0 sm:p-4"
            >
              <div className="flex items-center gap-2.5 sm:gap-4">
                <div className="size-4 shrink-0 animate-pulse rounded-full bg-muted sm:size-5" />

                <div className="h-4 min-w-0 flex-1 animate-pulse rounded bg-muted sm:h-5 sm:w-32 sm:flex-none" />

                <div className="size-7 shrink-0 animate-pulse rounded bg-muted sm:size-8" />
              </div>

              <div className="mt-2.5 flex items-center gap-3 sm:mt-0 sm:flex-1">
                <div className="min-w-0 flex-1">
                  <div className="space-y-1.5">
                    <div className="h-1.5 w-full animate-pulse rounded bg-muted" />
                    <div className="h-3 w-10 animate-pulse rounded bg-muted sm:w-12" />
                  </div>
                </div>

                <div className="h-4 w-24 shrink-0 animate-pulse rounded bg-muted sm:h-5 sm:w-40" />
              </div>
            </div>
          ),
        )}
      </div>
    </div>
  );
}