import { useCallback, useMemo } from "react"
import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import { useServerPagination, type PaginatedResponse } from "@/shared/hooks/useServerPagination"
import type { GoalListItem, GroupGoalDetail, GoalContribution } from "../types"

const GOAL_SERVER_SIZE = 12
const GOAL_DISPLAY_SIZE = 4
const CONTRIBUTION_SERVER_SIZE = 60
const CONTRIBUTION_DISPLAY_SIZE = 20

interface RawGoalDetailResponse {
  id: string
  name: string
  monthlyTarget: number
  distributionMode: "EQUITATIVE" | "PROPORTIONAL" | "CUSTOM"
  isAdmin: boolean
  members: RawGoalMember[]
  contributions: RawGoalContribution[]
  contributionTotals: Record<string, number>
}

interface RawGoalMember {
  id: string
  userId: string
  memberName?: string
  role: string
  salary?: number
  customPercentage?: number
}

interface RawGoalContribution {
  id: string
  userId: string
  memberName: string
  amount: number
  date: string
}

export function useGroupGoals() {
  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<GoalListItem>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(GOAL_SERVER_SIZE),
      })
      const response = await apiClient.get(`/goals?${params.toString()}`)
      return response.data
    },
    []
  )

  return {
    ...useServerPagination<GoalListItem>({
      queryKey: ["group-goals"],
      fetchPage,
      serverSize: GOAL_SERVER_SIZE,
      displaySize: GOAL_DISPLAY_SIZE,
    }),
    serverSize: GOAL_SERVER_SIZE,
    displaySize: GOAL_DISPLAY_SIZE,
  }
}

export function useGroupGoalDetail(selectedGoalId: string | null) {
  const { data: rawGoal } = useQuery<RawGoalDetailResponse>({
    queryKey: ["group-goals", "detail", selectedGoalId],
    queryFn: async () => {
      const response = await apiClient.get(`/goals/${selectedGoalId}`)
      return response.data
    },
    enabled: !!selectedGoalId,
  })

  const selectedGoal: GroupGoalDetail | undefined = useMemo(() => {
    if (!rawGoal) return undefined
    return {
      id: rawGoal.id,
      name: rawGoal.name,
      monthlyTarget: rawGoal.monthlyTarget ?? 0,
      distributionMode: rawGoal.distributionMode,
      isAdmin: rawGoal.isAdmin,
      members: rawGoal.members.map((m) => ({
        id: m.id,
        userId: m.userId,
        name: m.memberName || "Member",
        role: m.role === "ADMIN" ? "ADMIN" : "MEMBER",
        salary: m.salary || 0,
        customPercentage: m.customPercentage,
      })),
      contributions: (rawGoal.contributions || []).map((c) => ({
        id: c.id,
        userId: c.userId,
        name: c.memberName || "Member",
        amount: c.amount,
        date: c.date || "",
      })),
      contributionTotals: rawGoal.contributionTotals || {},
    }
  }, [rawGoal])

  return { selectedGoal }
}

export function useGroupGoalContributions(goalId: string | null) {
  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<GoalContribution>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(CONTRIBUTION_SERVER_SIZE),
      })
      const response = await apiClient.get(`/goals/${goalId}/contributions?${params.toString()}`)
      return response.data
    },
    [goalId]
  )

  return {
    ...useServerPagination<GoalContribution>({
      queryKey: ["group-goals", goalId ?? "", "contributions"],
      fetchPage,
      serverSize: CONTRIBUTION_SERVER_SIZE,
      displaySize: CONTRIBUTION_DISPLAY_SIZE,
      enabled: !!goalId,
    }),
    serverSize: CONTRIBUTION_SERVER_SIZE,
    displaySize: CONTRIBUTION_DISPLAY_SIZE,
  }
}
