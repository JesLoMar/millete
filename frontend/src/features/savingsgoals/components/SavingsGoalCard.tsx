import { Calendar, Plus, Pencil, Trash2 } from "lucide-react";
import type { SavingsGoal } from "../types";
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card";
import { Badge } from "@/shared/components/core/badge";
import { Button } from "@/shared/components/core/button";

const PriorityBadge = ({ priority }: { priority: string }) => {
  switch (priority) {
    case 'HIGH': return <Badge variant="destructive">Alta</Badge>;
    case 'MEDIUM': return <Badge variant="secondary" className="bg-amber-500/10 text-amber-500">Media</Badge>;
    default: return <Badge variant="outline">Baja</Badge>;
  }
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount);
};

interface Props {
  goal: SavingsGoal;
  onAddContribution: (goal: SavingsGoal) => void;
  onEdit: (goal: SavingsGoal) => void;
  onDelete: (goal: SavingsGoal) => void;
}

export const SavingsGoalCard = ({ goal, onAddContribution, onEdit, onDelete }: Props) => {
  const progressPercentage = Math.min((goal.currentAmount / goal.targetAmount) * 100, 100);

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
            <span className="text-muted-foreground">Progreso</span>
            <span className="text-foreground font-medium">{progressPercentage.toFixed(0)}%</span>
          </div>
          <div className="w-full bg-secondary rounded-full h-2 overflow-hidden">
            <div className="bg-primary h-2 rounded-full transition-all duration-500" style={{ width: `${progressPercentage}%` }} />
          </div>
        </div>
        <div className="flex justify-between items-end">
          <div>
            <div className="text-xl font-semibold text-foreground">{formatCurrency(goal.currentAmount)}</div>
            <div className="text-xs text-muted-foreground">de {formatCurrency(goal.targetAmount)}</div>
          </div>
          <div className="flex gap-1">
            <Button variant="ghost" size="icon" onClick={() => onEdit(goal)} title="Editar meta">
              <Pencil className="w-4 h-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => onDelete(goal)} title="Eliminar meta">
              <Trash2 className="w-4 h-4" />
            </Button>
            <Button variant="secondary" size="icon" onClick={() => onAddContribution(goal)} title="Añadir aportación">
              <Plus className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};