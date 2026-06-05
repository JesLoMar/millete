import { useMemo } from "react"
import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import type { FamilyListItem, FamilyUnitData, FamilyMember, FamilyContribution } from "../types"

interface RawFamilyResponse {
  id: string
  name: string
  monthlyGoal: number
  distributionMode: "EQUITATIVE" | "PROPORTIONAL" | "CUSTOM"
  isAdmin: boolean
  members: RawFamilyMember[]
  contributions: RawFamilyContribution[]
}

interface RawFamilyMember {
  id: string
  name?: string
  role: string
  salary?: number
  customPercentage?: number
  userId?: string
}

interface RawFamilyContribution {
  id: string
  userId: string
  name: string
  amount: number
  date: string
}

export function useFamilyQueries(selectedFamilyId: string | null) {
  const { data: families = [], isLoading } = useQuery<FamilyListItem[]>({
    queryKey: ['families'],
    queryFn: async () => {
      const response = await apiClient.get('families')
      return response.data
    },
  })

  const sortedFamilies = useMemo(() => {
    return [...families].sort((a, b) => {
      if (a.isAdmin && !b.isAdmin) return -1
      if (!a.isAdmin && b.isAdmin) return 1
      return a.name.localeCompare(b.name)
    })
  }, [families])

  const { data: rawFamily } = useQuery<RawFamilyResponse>({
    queryKey: ['family', selectedFamilyId],
    queryFn: async () => {
      const response = await apiClient.get(`families/${selectedFamilyId}`)
      return response.data
    },
    enabled: !!selectedFamilyId,
  })

  const selectedFamily: FamilyUnitData | undefined = useMemo(() => {
    if (!rawFamily) return undefined
    return {
      id: rawFamily.id,
      name: rawFamily.name,
      monthlyGoal: rawFamily.monthlyGoal ?? 0,
      distributionMode: rawFamily.distributionMode,
      isAdmin: rawFamily.isAdmin,
      members: rawFamily.members.map((m: RawFamilyMember): FamilyMember => ({
        id: m.id,
        name: m.name || "Member",
        role: m.role === "ADMIN" ? "ADMIN" : "MEMBER",
        salary: m.salary || 0,
        customPercentage: m.customPercentage,
        userId: m.userId,
      })),
      contributions: (rawFamily.contributions || []).map((c: RawFamilyContribution): FamilyContribution => ({
        id: c.id,
        memberId: c.userId,
        memberName: c.name || "Member",
        amount: c.amount,
        date: c.date || "",
      })),
    }
  }, [rawFamily])

  return { families: sortedFamilies, isLoading, selectedFamily }
}