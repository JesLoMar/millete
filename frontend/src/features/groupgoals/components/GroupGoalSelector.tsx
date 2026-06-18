import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { Card, CardContent } from "@/shared/components/core/card"
import { Badge } from "@/shared/components/core/badge"
import { Users, Plus, ArrowRight, Crown, Pencil, Trash2, MoreHorizontal } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/shared/components/core/dropdown-menu"
import type { GoalListItem } from "../types"

interface GroupGoalSelectorProps {
  goals: GoalListItem[]
  isLoading: boolean
  onSelect: (goalId: string) => void
  onCreateClick: () => void
  onEditClick: (goal: GoalListItem) => void
  onDeleteClick: (goal: GoalListItem) => void
}

export function GroupGoalSelector({
  goals,
  isLoading,
  onSelect,
  onCreateClick,
  onEditClick,
  onDeleteClick,
}: GroupGoalSelectorProps) {
  const { t } = useTranslation()

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto space-y-4 sm:space-y-6 pt-6 sm:pt-12 px-4 sm:px-0">
        <div className="text-center space-y-3">
          <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
            <Users className="size-10 text-primary" aria-hidden="true" />
          </div>
          <div className="h-8 sm:h-9 w-40 sm:w-48 bg-muted rounded animate-pulse mx-auto" />
          <div className="h-4 sm:h-5 w-56 sm:w-64 bg-muted rounded animate-pulse mx-auto" />
        </div>
        <div className="space-y-3 sm:space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={`skeleton-${i}`} className="h-20 sm:h-24 bg-muted animate-pulse rounded-xl" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-4 sm:space-y-6 pt-6 sm:pt-12 px-4 sm:px-0">
      <div className="text-center space-y-2 sm:space-y-3">
        <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
          <Users className="size-10 text-primary" aria-hidden="true" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-semibold font-headline">{t("groupGoals.title")}</h1>
        <p className="text-sm sm:text-base text-muted-foreground">{t("groupGoals.selectOrCreate")}</p>
      </div>

      {goals.length === 0 ? (
        <div className="text-center py-8 sm:py-12 space-y-4 sm:space-y-6">
          <p className="text-muted-foreground">{t("groupGoals.noFamilies")}</p>
          <Button onClick={onCreateClick} className="gap-2 min-h-11">
            <Plus className="size-4" aria-hidden="true" />
            {t("groupGoals.createFirst")}
          </Button>
        </div>
      ) : (
        <div className="space-y-3 sm:space-y-4">
          {goals.map((goal) => (
            <Card
              key={goal.id}
              className="border-subtle hover:border-primary/50 transition-all cursor-pointer group"
              role="button"
              tabIndex={0}
              onClick={() => onSelect(goal.id)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault()
                  onSelect(goal.id)
                }
              }}
              aria-label={t("groupGoals.selectFamily", { name: goal.name })}
            >
              <CardContent className="p-4 sm:p-6 flex items-center justify-between gap-3 sm:gap-4">
                <div className="space-y-1.5 min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <h3 className="text-base sm:text-lg font-semibold truncate">{goal.name}</h3>
                    {goal.isAdmin && (
                      <Badge variant="outline" className="border-amber-500/30 text-amber-400 gap-1 text-xs shrink-0">
                        <Crown className="size-3" aria-hidden="true" />
                        {t("groupGoals.admin")}
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs sm:text-sm text-muted-foreground truncate">
                    <Users className="size-3 sm:size-3.5 inline mr-1" aria-hidden="true" />
                    {goal.memberCount} {t("groupGoals.members")} • {t("groupGoals.goal")}: {goal.monthlyTarget.toLocaleString()} €
                  </p>
                </div>
                <div className="flex items-center gap-1 shrink-0" onClick={(e) => e.stopPropagation()}>
                  {goal.isAdmin && (
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="w-4 h-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => onEditClick(goal)}>
                          <Pencil className="w-4 h-4 mr-2" />
                          {t("groupGoals.edit")}
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          onClick={() => onDeleteClick(goal)}
                          className="text-destructive"
                        >
                          <Trash2 className="w-4 h-4 mr-2" />
                          {t("groupGoals.delete")}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  )}
                  <ArrowRight className="size-5 text-muted-foreground group-hover:text-primary group-hover:translate-x-1 transition-all" aria-hidden="true" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {goals.length > 0 && (
        <div className="flex justify-center pt-2 sm:pt-4">
          <Button onClick={onCreateClick} className="gap-2 min-h-11" variant="outline" size="lg">
            <Plus className="size-4" aria-hidden="true" />
            {t("groupGoals.createNew")}
          </Button>
        </div>
      )}
    </div>
  )
}