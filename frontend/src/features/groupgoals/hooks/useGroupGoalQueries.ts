import { useMemo } from "react"
import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import type { GoalListItem, GroupGoalDetail, GoalMember, GoalContribution } from "../types"

interface RawGoalDetailResponse {
  id: string
  name: string
  monthlyTarget: number
  distributionMode: "EQUITATIVE" | "PROPORTIONAL" | "CUSTOM"
  isAdmin: boolean
  members: RawGoalMember[]
  contributions: RawGoalContribution[]
}

interface RawGoalMember {
  id: string
  userId: string
  name?: string
  role: string
  salary?: number
  customPercentage?: number
}

interface RawGoalContribution {
  id: string
  userId: string
  name: string
  amount: number
  date: string
}

export function useGroupGoalQueries(selectedGoalId: string | null) {
  // Lista de goals del usuario
  const { data: goals = [], isLoading } = useQuery<GoalListItem[]>({
    queryKey: ["group-goals"],
    queryFn: async () => {
      const response = await apiClient.get("/group-goals")
      return response.data
    },
  })

  // Ordenar: primero los que eres admin, luego alfabéticamente
  const sortedGoals = useMemo(() => {
    return [...goals].sort((a, b) => {
      if (a.isAdmin && !b.isAdmin) return -1
      if (!a.isAdmin && b.isAdmin) return 1
      return a.name.localeCompare(b.name)
    })
  }, [goals])

  // Detalle del goal seleccionado
  const { data: rawGoal } = useQuery<RawGoalDetailResponse>({
    queryKey: ["group-goals", selectedGoalId],
    queryFn: async () => {
      const response = await apiClient.get(`/group-goals/${selectedGoalId}`)
      return response.data
    },
    enabled: !!selectedGoalId,
  })

  // Mapear la respuesta raw a GroupGoalDetail
  const selectedGoal: GroupGoalDetail | undefined = useMemo(() => {
    if (!rawGoal) return undefined
    return {
      id: rawGoal.id,
      name: rawGoal.name,
      monthlyTarget: rawGoal.monthlyTarget ?? 0,
      distributionMode: rawGoal.distributionMode,
      isAdmin: rawGoal.isAdmin,
      members: rawGoal.members.map((m: RawGoalMember): GoalMember => ({
        id: m.id,
        userId: m.userId,
        name: m.name || "Member",
        role: m.role === "ADMIN" ? "ADMIN" : "MEMBER",
        salary: m.salary || 0,
        customPercentage: m.customPercentage,
      })),
      contributions: (rawGoal.contributions || []).map((c: RawGoalContribution): GoalContribution => ({
        id: c.id,
        userId: c.userId,
        name: c.name || "Member",
        amount: c.amount,
        date: c.date || "",
      })),
    }
  }, [rawGoal])

  return { goals: sortedGoals, isLoading, selectedGoal }
}