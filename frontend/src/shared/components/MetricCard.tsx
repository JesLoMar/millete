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
        <CardContent className="p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="h-4 w-28 bg-muted rounded animate-pulse" />
            <div className="size-10 bg-muted rounded-lg animate-pulse" />
          </div>
          <div className="space-y-2 mb-3">
            <div className="h-8 w-36 bg-muted rounded animate-pulse" />
          </div>
          <div className="flex items-center gap-2">
            <div className="h-5 w-16 bg-muted rounded animate-pulse" />
            <div className="h-4 w-24 bg-muted rounded animate-pulse" />
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className={cn("overflow-hidden border-subtle transition-all duration-300 hover:border-primary/50", className)}>
      <CardContent className="p-6">
        <div className="flex items-center justify-between mb-4">
          <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            {title}
          </p>
          <div className={cn("p-2 rounded-lg", color)}>
            <Icon className="size-5" />
          </div>
        </div>
        
        <div className="flex flex-col gap-1">
          <h2 className="text-3xl font-semibold tracking-tight tabular-nums text-foreground">
            {value}
          </h2>
          {children && (
            <div className="mt-2 pt-1 flex items-center gap-2">
              {children}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}