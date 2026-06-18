import type { GroupGoalDetail, ContributionMember } from "./types"

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString("es-ES", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  })
}

export function calculateContributions(
  selectedGoal: GroupGoalDetail,
  customPercentages: Record<string, number>,
  totalCustomPercentage: number
): ContributionMember[] {
  const { members, monthlyTarget, distributionMode } = selectedGoal

  const contributedMap: Record<string, number> = {}
  if (selectedGoal.contributions) {
    selectedGoal.contributions.forEach((c) => {
      const key = c.userId
      if (key) {
        contributedMap[key] = (contributedMap[key] || 0) + c.amount
      }
    })
  }

  const expectedMap: Record<string, number> = {}

  if (distributionMode === "CUSTOM") {
    members.forEach((m) => {
      expectedMap[m.userId] =
        totalCustomPercentage > 0
          ? ((customPercentages[m.userId] || 0) / 100) * monthlyTarget
          : 0
    })
  } else if (distributionMode === "EQUITATIVE" && members.length > 0) {
    const amount = monthlyTarget / members.length
    members.forEach((m) => {
      expectedMap[m.userId] = amount
    })
  } else if (distributionMode === "PROPORTIONAL") {
    const totalSalary = members.reduce((sum, m) => sum + m.salary, 0)
    members.forEach((m) => {
      expectedMap[m.userId] =
        totalSalary > 0 ? (m.salary / totalSalary) * monthlyTarget : 0
    })
  } else {
    members.forEach((m) => {
      expectedMap[m.userId] = 0
    })
  }

  return members.map((m) => {
    const contributed = contributedMap[m.userId] || 0
    const expected = expectedMap[m.userId] || 0
    return {
      ...m,
      expectedContribution: expected,
      contributed,
      percentage: expected > 0 ? (contributed / expected) * 100 : 0,
    }
  })
}