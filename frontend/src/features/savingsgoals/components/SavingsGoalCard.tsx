import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Calendar,
  ExternalLink,
  Pencil,
  Plus,
  Trash2,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { ProgressBar } from '@/shared/components/core/progress-bar';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type { SavingsGoal } from '../types';

interface PriorityBadgeProps {
  priority: SavingsGoal['priority'];
}

function PriorityBadge({ priority }: PriorityBadgeProps) {
  const { t } = useTranslation('savingsGoals');

  switch (priority) {
    case 'HIGH':
      return (
        <Badge variant="destructive">
          {t('priorities.HIGH')}
        </Badge>
      );

    case 'MEDIUM':
      return (
        <Badge
          variant="secondary"
          className="bg-warning/10 text-warning"
        >
          {t('priorities.MEDIUM')}
        </Badge>
      );

    case 'LOW':
    default:
      return (
        <Badge variant="outline">
          {t('priorities.LOW')}
        </Badge>
      );
  }
}

interface SavingsGoalCardProps {
  goal: SavingsGoal;
  onAddContribution: (goal: SavingsGoal) => void;
  onEdit: (goal: SavingsGoal) => void;
  onDelete: (goal: SavingsGoal) => void;
}

export const SavingsGoalCard = memo(
  ({
    goal,
    onAddContribution,
    onEdit,
    onDelete,
  }: SavingsGoalCardProps) => {
    const { t } = useTranslation('savingsGoals');

    const progressPercentage =
      goal.targetAmount > 0
        ? Math.min(
            (goal.currentAmount / goal.targetAmount) * 100,
            100,
          )
        : 0;

    const formattedProgress = progressPercentage.toFixed(0);

    return (
      <Card className="flex flex-col transition-colors hover:border-ring">
        <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
          <div className="min-w-0 pr-2">
            <CardTitle className="truncate text-base font-medium">
              {goal.name}
            </CardTitle>

            {goal.deadline && (
              <div className="mt-1 flex items-center text-xs text-muted-foreground">
                <Calendar
                  className="mr-1 size-3"
                  aria-hidden="true"
                />
                {goal.deadline}
              </div>
            )}
          </div>

          <div className="shrink-0">
            <PriorityBadge priority={goal.priority} />
          </div>
        </CardHeader>

        <CardContent className="mt-auto flex flex-col gap-4 pt-4">
          <div>
            <div className="mb-2 flex justify-between text-sm">
              <span className="text-muted-foreground">
                {t('progress')}
              </span>

              <span className="font-medium text-foreground">
                {formattedProgress}%
              </span>
            </div>

            <ProgressBar
              value={goal.currentAmount}
              max={goal.targetAmount}
              size="sm"
              ariaLabel={`${goal.name}: ${formattedProgress}%`}
            />
          </div>

          <div className="flex items-end justify-between">
            <div>
              <div className="text-xl font-semibold text-foreground">
                {formatCurrency(goal.currentAmount)}
              </div>

              <div className="text-xs text-muted-foreground">
                {t('of')} {formatCurrency(goal.targetAmount)}
              </div>
            </div>

            <div className="flex gap-1">
              {goal.link && (
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  asChild
                >
                  <a
                    href={goal.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    title={t('viewLink')}
                    aria-label={t('viewLink')}
                  >
                    <ExternalLink
                      className="size-4"
                      aria-hidden="true"
                    />
                  </a>
                </Button>
              )}

              <Button
                type="button"
                variant="ghost"
                size="icon"
                onClick={() => onEdit(goal)}
                title={t('editGoal')}
                aria-label={t('editGoal')}
              >
                <Pencil
                  className="size-4"
                  aria-hidden="true"
                />
              </Button>

              <Button
                type="button"
                variant="ghost"
                size="icon"
                onClick={() => onDelete(goal)}
                title={t('deleteGoal')}
                aria-label={t('deleteGoal')}
              >
                <Trash2
                  className="size-4"
                  aria-hidden="true"
                />
              </Button>

              <Button
                type="button"
                variant="secondary"
                size="icon"
                onClick={() => onAddContribution(goal)}
                title={t('addContribution')}
                aria-label={t('addContribution')}
              >
                <Plus
                  className="size-4"
                  aria-hidden="true"
                />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  },
);