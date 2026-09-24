import type {
  ContributionMember,
  GroupGoalDetail,
} from './types';

export function calculateContributions(
  selectedGoal: GroupGoalDetail,
  totalCustomPercentage: number,
): ContributionMember[] {
  const {
    members,
    monthlyTarget,
    distributionMode,
    contributionTotals,
  } = selectedGoal;

  const contributedMap = contributionTotals ?? {};
  const expectedMap: Record<string, number> = {};

  if (distributionMode === 'CUSTOM') {
    members.forEach((member) => {
      expectedMap[member.userId] =
        totalCustomPercentage > 0
          ? ((member.customPercentage ?? 0) / 100) *
            monthlyTarget
          : 0;
    });
  } else if (
    distributionMode === 'EQUITATIVE' &&
    members.length > 0
  ) {
    const amount = monthlyTarget / members.length;

    members.forEach((member) => {
      expectedMap[member.userId] = amount;
    });
  } else if (distributionMode === 'PROPORTIONAL') {
    const totalSalary = members.reduce(
      (sum, member) => sum + member.salary,
      0,
    );

    members.forEach((member) => {
      expectedMap[member.userId] =
        totalSalary > 0
          ? (member.salary / totalSalary) *
            monthlyTarget
          : 0;
    });
  } else {
    members.forEach((member) => {
      expectedMap[member.userId] = 0;
    });
  }

  return members.map((member) => {
    const contributed =
      contributedMap[member.userId] ?? 0;

    const expected =
      expectedMap[member.userId] ?? 0;

    return {
      ...member,
      expectedContribution: expected,
      contributed,
      percentage:
        expected > 0
          ? (contributed / expected) * 100
          : 0,
    };
  });
}