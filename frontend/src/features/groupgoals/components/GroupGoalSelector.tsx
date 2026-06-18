import { Button } from "@/shared/components/core/button"
import { Card, CardContent } from "@/shared/components/core/card"
import { Users, Plus, ArrowRight, Crown, Pencil, Trash2 } from "lucide-react"
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

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Group Goals</h2>
        <Button onClick={onCreateClick} size="sm" className="gap-1">
          <Plus className="w-4 h-4" />
          Nuevo
        </Button>
      </div>
      {isLoading ? (
        <p className="text-muted-foreground text-sm">Cargando...</p>
      ) : goals.length === 0 ? (
        <p className="text-muted-foreground text-sm">No perteneces a ningún Group Goal</p>
      ) : (
        goals.map((goal) => (
          <Card
            key={goal.id}
            className="hover:border-ring transition-colors cursor-pointer"
            onClick={() => onSelect(goal.id)}
          >
            <CardContent className="flex items-center justify-between p-4">
              <div className="flex items-center gap-3">
                <Users className="w-5 h-5 text-muted-foreground" />
                <div>
                  <div className="font-medium flex items-center gap-2">
                    {goal.name}
                    {goal.isAdmin && <Crown className="w-4 h-4 text-amber-500" />}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Meta mensual: {goal.monthlyTarget} € · {goal.memberCount} miembros
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
                {goal.isAdmin && (
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon" className="h-8 w-8">
                        <Pencil className="w-4 h-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => onEditClick(goal)}>
                        <Pencil className="w-4 h-4 mr-2" />
                        Editar nombre
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem
                        onClick={() => onDeleteClick(goal)}
                        className="text-destructive"
                      >
                        <Trash2 className="w-4 h-4 mr-2" />
                        Eliminar goal
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                )}
                <ArrowRight className="w-5 h-5 text-muted-foreground" />
              </div>
            </CardContent>
          </Card>
        ))
      )}
    </div>
  )
}