import { useTranslation } from "react-i18next"
import { MoreHorizontal, Edit2, Trash2 } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { cn } from "@/lib/utils"
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
  const { t } = useTranslation()
  const isOverBudget = percentage >= 100
  const hasBudget = budgetLimit !== null && budgetLimit > 0

  return (
    <>
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
              <div className="h-1.5 w-full bg-accent/20 rounded-full overflow-hidden">
                <div
                  className={cn("h-full transition-all duration-500", isOverBudget ? "bg-destructive" : "")}
                  style={{
                    width: `${Math.min(percentage, 100)}%`,
                    backgroundColor: isOverBudget ? undefined : (category.color || "#3B82F6"),
                  }}
                />
              </div>
            </div>
          ) : (
            <p className="text-xs text-muted-foreground italic">
              {t("categories.noBudgetTooltip")}
            </p>
          )}
        </div>

        <div className="w-40 text-right text-sm text-muted-foreground tabular-nums">
          {hasBudget
            ? `${spent.toLocaleString("es-ES")} € / ${budgetLimit!.toLocaleString("es-ES")} €`
            : `${spent.toLocaleString("es-ES")} € / —`}
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button 
              variant="ghost" 
              size="icon" 
              className="size-8 opacity-0 group-hover:opacity-100 transition-opacity"
              aria-label={t("categories.edit")}
            >
              <MoreHorizontal size={16} aria-hidden="true" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="bg-card border-border">
            <DropdownMenuItem className="cursor-pointer" onClick={() => onEdit(category)}>
              <Edit2 className="mr-2 size-4" aria-hidden="true" />
              {t("categories.edit")}
            </DropdownMenuItem>
            <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(category)}>
              <Trash2 className="mr-2 size-4" aria-hidden="true" />
              {t("categories.delete")}
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
                aria-label={t("categories.edit")}
              >
                <MoreHorizontal size={15} aria-hidden="true" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="bg-card border-border">
              <DropdownMenuItem className="cursor-pointer" onClick={() => onEdit(category)}>
                <Edit2 className="mr-2 size-4" aria-hidden="true" />
                {t("categories.edit")}
              </DropdownMenuItem>
              <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(category)}>
                <Trash2 className="mr-2 size-4" aria-hidden="true" />
                {t("categories.delete")}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex-1 min-w-0">
            {hasBudget ? (
              <div className="space-y-1">
                <div className="h-1.5 w-full bg-accent/20 rounded-full overflow-hidden">
                  <div
                    className={cn("h-full transition-all duration-500", isOverBudget ? "bg-destructive" : "")}
                    style={{
                      width: `${Math.min(percentage, 100)}%`,
                      backgroundColor: isOverBudget ? undefined : (category.color || "#3B82F6"),
                    }}
                  />
                </div>
                <p className={cn(
                  "text-[11px] font-medium",
                  isOverBudget ? "text-destructive" : "text-muted-foreground"
                )}>
                  {percentage.toFixed(0)}%
                </p>
              </div>
            ) : (
              <p className="text-xs text-muted-foreground italic">
                {t("categories.noBudgetTooltip")}
              </p>
            )}
          </div>
          <span className="text-xs text-muted-foreground tabular-nums shrink-0 text-right">
            {hasBudget
              ? `${spent.toLocaleString("es-ES")} € / ${budgetLimit!.toLocaleString("es-ES")} €`
              : `${spent.toLocaleString("es-ES")} € / —`}
          </span>
        </div>
      </div>
    </>
  )
}