import { Card, CardContent } from "@/shared/components/ui/card"
import { type LucideIcon } from "lucide-react"
import { cn } from "@/lib/utils"

export interface MetricCardProps {
  title: string
  value: string | number
  icon: LucideIcon
  color: string
  loading?: boolean
  children?: React.ReactNode
  className?: string
}

export function MetricCard({ 
  title, 
  value, 
  icon: Icon, 
  color,
  loading = false,
  children,
  className,
}: MetricCardProps) {
  if (loading) {
    return (
      <Card className={cn("overflow-hidden border-subtle", className)}>
        <CardContent className="p-4 sm:p-6">
          <div className="flex items-center justify-between mb-3 sm:mb-4">
            <div className="h-4 w-24 sm:w-28 bg-muted rounded animate-pulse" />
            <div className="size-8 sm:size-10 bg-muted rounded-lg animate-pulse" />
          </div>
          <div className="space-y-2 mb-2 sm:mb-3">
            <div className="h-6 sm:h-8 w-28 sm:w-36 bg-muted rounded animate-pulse" />
          </div>
          <div className="flex items-center gap-2">
            <div className="h-4 sm:h-5 w-14 sm:w-16 bg-muted rounded animate-pulse" />
            <div className="h-3 sm:h-4 w-20 sm:w-24 bg-muted rounded animate-pulse" />
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className={cn("overflow-hidden border-subtle transition-all duration-300 hover:border-primary/50", className)}>
      <CardContent className="p-4 sm:p-6">
        <div className="flex items-center justify-between mb-3 sm:mb-4">
          <p className="text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-muted-foreground truncate mr-2">
            {title}
          </p>
          <div className={cn("p-1.5 sm:p-2 rounded-lg shrink-0", color)}>
            <Icon className="size-4 sm:size-5" />
          </div>
        </div>
        
        <div className="flex flex-col gap-1">
          <h2 className="text-xl sm:text-2xl md:text-3xl font-semibold tracking-tight tabular-nums text-foreground truncate">
            {value}
          </h2>
          {children && (
            <div className="mt-1 sm:mt-2 pt-0.5 sm:pt-1 flex items-center gap-1.5 sm:gap-2 flex-wrap">
              {children}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}