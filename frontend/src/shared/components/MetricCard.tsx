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
    <Card className={cn("overflow-hidden border-subtle transition-all duration-300 hover:border-primary/50 w-full min-w-0", className)}>
      <CardContent className="p-4 sm:p-6 flex flex-col justify-between h-full w-full min-w-0">
        
        {/* PARTE SUPERIOR: Título e Icono */}
        <div className="flex items-center justify-between gap-2 mb-3 sm:mb-4 w-full min-w-0">
          <p className="text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-muted-foreground truncate flex-1 min-w-0">
            {title}
          </p>
          <div className={cn("p-1.5 sm:p-2 rounded-lg shrink-0 flex items-center justify-center", color)}>
            <Icon className="size-4 sm:size-5 shrink-0" />
          </div>
        </div>
        
        {/* PARTE INFERIOR: Valor numérico y tendencia */}
        <div className="flex flex-col gap-1 w-full min-w-0">
          <h2 className="text-base min-[360px]:text-lg sm:text-xl md:text-2xl lg:text-3xl font-semibold tracking-tight tabular-nums text-foreground whitespace-normal text-wrap wrap-break-word leading-tight w-full">
            {value}
          </h2>
          
          {children && (
            <div className="mt-1 sm:mt-2 pt-0.5 sm:pt-1 flex items-center gap-1.5 sm:gap-2 flex-wrap w-full min-w-0">
              {children}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}