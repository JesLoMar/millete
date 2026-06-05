import type { FamilyUnitData, ContributionMember } from "./types"

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString("es-ES", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  })
}

export function calculateContributions(
  selectedFamily: FamilyUnitData,
  customPercentages: Record<string, number>,
  totalCustomPercentage: number
): ContributionMember[] {
  const { members, monthlyGoal, distributionMode } = selectedFamily

  const contributedMap: Record<string, number> = {}
  if (selectedFamily.contributions) {
    selectedFamily.contributions.forEach((c) => {
      const key = c.memberId
      if (key) {
        contributedMap[key] = (contributedMap[key] || 0) + c.amount
      }
    })
  }

  const expectedMap: Record<string, number> = {}

  if (distributionMode === "CUSTOM") {
    members.forEach((m) => {
      expectedMap[m.id] = totalCustomPercentage > 0 ? ((customPercentages[m.id] || 0) / 100) * monthlyGoal : 0
    })
  } else if (distributionMode === "EQUITATIVE" && members.length > 0) {
    const amount = monthlyGoal / members.length
    members.forEach((m) => { expectedMap[m.id] = amount })
  } else if (distributionMode === "PROPORTIONAL") {
    const totalSalary = members.reduce((sum, m) => sum + m.salary, 0)
    members.forEach((m) => {
      expectedMap[m.id] = totalSalary > 0 ? (m.salary / totalSalary) * monthlyGoal : 0
    })
  } else {
    members.forEach((m) => { expectedMap[m.id] = 0 })
  }

  return members.map((m) => {
    const contributed = contributedMap[m.userId || m.id] || 0
    const expected = expectedMap[m.id] || 0
    return {
      ...m,
      expectedContribution: expected,
      contributed,
      percentage: expected > 0 ? (contributed / expected) * 100 : 0,
    }
  })
}