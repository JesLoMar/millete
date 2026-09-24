export type GoalRole = 'ADMIN' | 'MEMBER';

export type DistributionMode =
  | 'EQUITATIVE'
  | 'PROPORTIONAL'
  | 'CUSTOM';

export type InvitationStatus =
  | 'PENDING'
  | 'ACCEPTED'
  | 'REJECTED';

export interface GoalMember {
  readonly id: string;
  readonly userId: string;
  readonly name: string;
  readonly role: GoalRole;
  readonly salary: number;
  readonly customPercentage?: number;
}

export interface GoalContribution {
  readonly id: string;
  readonly userId: string;
  readonly name: string;
  readonly amount: number;
  readonly date: string;
}

export interface GroupGoalDetail {
  readonly id: string;
  readonly name: string;
  readonly monthlyTarget: number;
  readonly distributionMode: DistributionMode;
  readonly isAdmin: boolean;
  readonly members: GoalMember[];
  readonly contributions: GoalContribution[];
  readonly contributionTotals: Record<string, number>;
}

export interface GoalListItem {
  readonly id: string;
  readonly name: string;
  readonly monthlyTarget: number;
  readonly memberCount: number;
  readonly isAdmin: boolean;
}

export interface ContributionMember extends GoalMember {
  readonly expectedContribution: number;
  readonly contributed: number;
  readonly percentage: number;
}