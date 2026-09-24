import { useCallback, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'

import { apiClient } from '@/shared/api/axiosClient'
import {
  useServerPagination,
  type PaginatedResponse,
} from '@/shared/hooks/useServerPagination'

import type {
  GoalContribution,
  GoalListItem,
  GroupGoalDetail,
} from '../types'

const GOAL_SERVER_SIZE = 12
const GOAL_DISPLAY_SIZE = 4
const CONTRIBUTION_SERVER_SIZE = 60
const CONTRIBUTION_DISPLAY_SIZE = 20

interface RawGoalDetailResponse {
  id: string
  name: string
  monthlyTarget: number
  distributionMode: 'EQUITATIVE' | 'PROPORTIONAL' | 'CUSTOM'
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
  memberName?: string
  userName?: string
  amount: number
  date?: string
}

interface RawPaginatedContribution {
  id: string
  userId: string
  userName?: string
  memberName?: string
  amount: number
  date?: string
}

interface RawGoalListItem {
  id: string
  name: string
  monthlyTarget: number
  memberCount?: number
  activeMembers?: number
  isAdmin: boolean
}

export function useGroupGoals() {
  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<GoalListItem>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(GOAL_SERVER_SIZE),
      })

      const response = await apiClient.get<RawGoalListItem[]>(
        `/goals?${params.toString()}`
      )

      const rawContent = response.data

      const content: GoalListItem[] = rawContent.map(
        (goal) => ({
          id: goal.id,
          name: goal.name,
          monthlyTarget: goal.monthlyTarget,
          memberCount:
            goal.memberCount ??
            goal.activeMembers ??
            0,
          isAdmin: goal.isAdmin,
        })
      )

      const totalElements = content.length
      const totalPages = Math.max(
        1,
        Math.ceil(
          totalElements / GOAL_SERVER_SIZE
        )
      )

      return {
        content,
        currentPage: page,
        totalPages,
        totalElements,
        size: GOAL_SERVER_SIZE,
        first: page === 0,
        last: true,
      }
    },
    []
  )

  return {
    ...useServerPagination<GoalListItem>({
      queryKey: ['group-goals'],
      fetchPage,
      serverSize: GOAL_SERVER_SIZE,
      displaySize: GOAL_DISPLAY_SIZE,
    }),
    serverSize: GOAL_SERVER_SIZE,
    displaySize: GOAL_DISPLAY_SIZE,
  }
}

export function useGroupGoalDetail(
  selectedGoalId: string | null
) {
  const { data: rawGoal } =
    useQuery<RawGoalDetailResponse>({
      queryKey: [
        'group-goals',
        'detail',
        selectedGoalId,
      ],
      queryFn: async () => {
        const response =
          await apiClient.get<RawGoalDetailResponse>(
            `/goals/${selectedGoalId}`
          )

        return response.data
      },
      enabled: !!selectedGoalId,
    })

  const selectedGoal:
    | GroupGoalDetail
    | undefined = useMemo(() => {
    if (!rawGoal) return undefined

    return {
      id: rawGoal.id,
      name: rawGoal.name,
      monthlyTarget:
        rawGoal.monthlyTarget ?? 0,
      distributionMode:
        rawGoal.distributionMode,
      isAdmin: rawGoal.isAdmin,
      members: (
        rawGoal.members ?? []
      ).map((member) => ({
        id: member.id,
        userId: member.userId,
        name:
          member.memberName ||
          'Member',
        role:
          member.role === 'ADMIN'
            ? 'ADMIN'
            : 'MEMBER',
        salary: member.salary ?? 0,
        customPercentage:
          member.customPercentage,
      })),
      contributions: (
        rawGoal.contributions ?? []
      ).map((contribution) => ({
        id: contribution.id,
        userId:
          contribution.userId,
        name:
          contribution.memberName ||
          contribution.userName ||
          'Member',
        amount:
          contribution.amount,
        date:
          contribution.date ?? '',
      })),
      contributionTotals:
        rawGoal.contributionTotals ?? {},
    }
  }, [rawGoal])

  return { selectedGoal }
}

export function useGroupGoalContributions(
  goalId: string | null
) {
  const fetchPage = useCallback(
    async (
      page: number
    ): Promise<
      PaginatedResponse<GoalContribution>
    > => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(
          CONTRIBUTION_SERVER_SIZE
        ),
      })

      const response =
        await apiClient.get(
          `/goals/${goalId}/contributions?${params.toString()}`
        )

      const data = response.data

      return {
        content: (
          data.content ??
          data.contributions ??
          []
        ).map(
          (
            contribution: RawPaginatedContribution
          ) => ({
            id: contribution.id,
            userId:
              contribution.userId,
            name:
              contribution.userName ||
              contribution.memberName ||
              '',
            amount:
              contribution.amount,
            date:
              contribution.date ?? '',
          })
        ),
        currentPage:
          data.currentPage ?? page,
        totalPages:
          data.totalPages ?? 0,
        totalElements:
          data.totalElements ?? 0,
        size:
          data.size ??
          CONTRIBUTION_SERVER_SIZE,
        first:
          data.first ??
          page === 0,
        last:
          data.last ?? false,
      }
    },
    [goalId]
  )

  return {
    ...useServerPagination<GoalContribution>({
      queryKey: [
        'group-goals',
        goalId ?? '',
        'contributions',
      ],
      fetchPage,
      serverSize:
        CONTRIBUTION_SERVER_SIZE,
      displaySize:
        CONTRIBUTION_DISPLAY_SIZE,
      enabled: !!goalId,
    }),
    serverSize:
      CONTRIBUTION_SERVER_SIZE,
    displaySize:
      CONTRIBUTION_DISPLAY_SIZE,
  }
}