import { memo } from "react"
import { useTranslation } from "react-i18next"
import { Calendar, Plus, Pencil, Trash2, ExternalLink } from "lucide-react"
import type { SavingsGoal } from "../types"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import { Badge } from "@/shared/components/core/badge"
import { Button } from "@/shared/components/core/button"
import { ProgressBar } from "@/shared/components/core/progress-bar"

const PriorityBadge = ({ priority }: { priority: string }) => {
  const { t } = useTranslation()
  switch (priority) {
    case 'HIGH': return <Badge variant="destructive">{t('savingsGoals:priorities.HIGH')}</Badge>
    case 'MEDIUM': return <Badge variant="secondary" className="bg-warning/10 text-warning">{t('savingsGoals:priorities.MEDIUM')}</Badge>
    default: return <Badge variant="outline">{t('savingsGoals:priorities.LOW')}</Badge>
  }
}

const currencyFormatter = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })
const formatCurrency = (amount: number) => {
  return currencyFormatter.format(amount)
}

interface Props {
  goal: SavingsGoal
  onAddContribution: (goal: SavingsGoal) => void
  onEdit: (goal: SavingsGoal) => void
  onDelete: (goal: SavingsGoal) => void
}

export const SavingsGoalCard = memo(({ goal, onAddContribution, onEdit, onDelete }: Props) => {
  const { t } = useTranslation()
  const progressPercentage = Math.min((goal.currentAmount / goal.targetAmount) * 100, 100)

  return (
    <Card className="flex flex-col hover:border-ring transition-colors">
      <CardHeader className="flex flex-row items-start justify-between pb-2 space-y-0">
        <div className="min-w-0 pr-2">
          <CardTitle className="text-base font-medium truncate">{goal.name}</CardTitle>
          {goal.deadline && (
            <div className="flex items-center text-xs text-muted-foreground mt-1">
              <Calendar className="w-3 h-3 mr-1" />
              {goal.deadline}
            </div>
          )}
        </div>
        <div className="shrink-0">
          <PriorityBadge priority={goal.priority} />
        </div>
      </CardHeader>
      <CardContent className="mt-auto pt-4 flex flex-col gap-4">
        <div>
          <div className="flex justify-between text-sm mb-2">
            <span className="text-muted-foreground">{t('savingsGoals:progress')}</span>
            <span className="text-foreground font-medium">{progressPercentage.toFixed(0)}%</span>
          </div>
          <ProgressBar
            value={goal.currentAmount}
            max={goal.targetAmount}
            size="sm"
            ariaLabel={`${goal.name}: ${progressPercentage.toFixed(0)}%`}
          />
        </div>
        <div className="flex justify-between items-end">
          <div>
            <div className="text-xl font-semibold text-foreground">{formatCurrency(goal.currentAmount)}</div>
            <div className="text-xs text-muted-foreground">{t('savingsGoals:of')} {formatCurrency(goal.targetAmount)}</div>
          </div>
          <div className="flex gap-1">
            {goal.link && (
              <Button variant="ghost" size="icon" asChild>
                <a
                  href={goal.link}
                  target="_blank"
                  rel="noopener noreferrer"
                  title={t('savingsGoals:viewLink')}
                  aria-label={t('savingsGoals:viewLink')}
                >
                  <ExternalLink className="w-4 h-4" />
                </a>
              </Button>
            )}
            <Button variant="ghost" size="icon" onClick={() => onEdit(goal)} title={t('savingsGoals:editGoal')}>
              <Pencil className="w-4 h-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => onDelete(goal)} title={t('savingsGoals:deleteGoal')}>
              <Trash2 className="w-4 h-4" />
            </Button>
            <Button variant="secondary" size="icon" onClick={() => onAddContribution(goal)} title={t('savingsGoals:addContribution')}>
              <Plus className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  )
});