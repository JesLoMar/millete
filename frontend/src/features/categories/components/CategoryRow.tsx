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
    <div className="flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group">
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
                  width: `${percentage}%`,
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

      <div className="w-40 text-right text-sm text-muted-foreground font-mono">
        {hasBudget
          ? `${spent.toLocaleString("es-ES")} € / ${budgetLimit!.toLocaleString("es-ES")} €`
          : `${spent.toLocaleString("es-ES")} € / —`}
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="icon" className="size-8 opacity-0 group-hover:opacity-100 transition-opacity">
            <MoreHorizontal size={16} />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="bg-card border-border">
          <DropdownMenuItem className="cursor-pointer" onClick={() => onEdit(category)}>
            <Edit2 className="mr-2 size-4" />
            {t("categories.edit")}
          </DropdownMenuItem>
          <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(category)}>
            <Trash2 className="mr-2 size-4" />
            {t("categories.delete")}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  )
}