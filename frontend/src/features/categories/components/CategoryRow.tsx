import { useTranslation } from "react-i18next"
import { MoreHorizontal, Edit2, Trash2 } from "lucide-react"
import { Button } from "@/shared/components/core/button"
import { ProgressBar } from "@/shared/components/core/progress-bar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/core/dropdown-menu"
import { cn } from "@/lib/utils"
import { formatCurrency } from '@/shared/utils/i18nFormat'
import type { Category } from "../types"

interface CategoryRowProps {
  category: Category
  spent: number
  budgetLimit: number | null
  percentage: number
  onEdit: (category: Category) => void
  onDelete: (category: Category) => void
}

export function CategoryRow({ category, spent, budgetLimit, percentage, onEdit, onDelete }: CategoryRowProps) {
  const { t } = useTranslation(['categories', 'common'])
  const isOverBudget = percentage >= 100
  const hasBudget = budgetLimit !== null && budgetLimit > 0

  return (
    <>
      {/* ============ DESKTOP (≥640px): Fila de tabla ============ */}
      <div className="hidden sm:flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group">
        <div
          className="size-5 rounded-full shrink-0"
          style={{ backgroundColor: category.color || "#3B82F6" }}
        />

        <div className="w-32 min-w-0">
          <p className="text-sm font-semibold truncate">{category.name}</p>
        </div>

        <div className="flex-1 min-w-0 px-2">
          {hasBudget ? (
            <div className="space-y-1">
              <div className="flex justify-between text-[11px] font-medium">
                <span className={cn(isOverBudget ? "text-destructive" : "text-muted-foreground")}>
                  {percentage.toFixed(0)}%
                </span>
              </div>
              <ProgressBar
                value={percentage}
                max={100}
                color={isOverBudget ? undefined : category.color}
                variant={isOverBudget ? "overbudget" : "default"}
                size="sm"
                ariaLabel={`${category.name}: ${percentage.toFixed(0)}%`}
              />
            </div>
          ) : (
            <p className="text-xs text-muted-foreground italic">
              {t('categories:noBudgetTooltip')}
            </p>
          )}
        </div>

        <div className="w-40 text-right text-sm text-muted-foreground tabular-nums">
          {hasBudget
            ? `${formatCurrency(spent)} / ${formatCurrency(budgetLimit!)}`
            : `${formatCurrency(spent)} / —`}
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
              className="size-8"
              aria-label={t('categories:edit')}
            >
              <MoreHorizontal size={16} aria-hidden="true" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="bg-card border-border">
            <DropdownMenuItem className="cursor-pointer" onClick={() => onEdit(category)}>
              <Edit2 className="mr-2 size-4" aria-hidden="true" />
              {t('categories:edit')}
            </DropdownMenuItem>
            <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(category)}>
              <Trash2 className="mr-2 size-4" aria-hidden="true" />
              {t('categories:delete')}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* ============ MÓVIL (<640px): Tarjeta compacta ============ */}
      <div className="sm:hidden p-3 border-b last:border-0 hover:bg-accent/30 transition-colors">
        <div className="flex items-center gap-2.5 mb-2">
          <div
            className="size-4 rounded-full shrink-0"
            style={{ backgroundColor: category.color || "#3B82F6" }}
          />
          <p className="text-sm font-semibold truncate flex-1 min-w-0">{category.name}</p>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="size-7 shrink-0 -mr-1"
                aria-label={t('categories:edit')}
              >
                <MoreHorizontal size={15} aria-hidden="true" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="bg-card border-border">
              <DropdownMenuItem className="cursor-pointer" onClick={() => onEdit(category)}>
                <Edit2 className="mr-2 size-4" aria-hidden="true" />
                {t('categories:edit')}
              </DropdownMenuItem>
              <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(category)}>
                <Trash2 className="mr-2 size-4" aria-hidden="true" />
                {t('categories:delete')}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex-1 min-w-0">
            {hasBudget ? (
              <div className="space-y-1">
                <ProgressBar
                  value={percentage}
                  max={100}
                  color={isOverBudget ? undefined : category.color}
                  variant={isOverBudget ? "overbudget" : "default"}
                  size="sm"
                  ariaLabel={`${category.name}: ${percentage.toFixed(0)}%`}
                />
                <p className={cn(
                  "text-[11px] font-medium",
                  isOverBudget ? "text-destructive" : "text-muted-foreground"
                )}>
                  {percentage.toFixed(0)}%
                </p>
              </div>
            ) : (
              <p className="text-xs text-muted-foreground italic">
                {t('categories:noBudgetTooltip')}
              </p>
            )}
          </div>
          <span className="text-xs text-muted-foreground tabular-nums shrink-0 text-right">
            {hasBudget
              ? `${formatCurrency(spent)} / ${formatCurrency(budgetLimit!)}`
              : `${formatCurrency(spent)} / —`}
          </span>
        </div>
      </div>
    </>
  )
}