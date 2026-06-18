export type GoalRole = 'ADMIN' | 'MEMBER'
export type DistributionMode = 'EQUITATIVE' | 'PROPORTIONAL' | 'CUSTOM'
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface GoalResponse {
  id: string
  name: string
  monthlyTarget: number
  distributionMode: DistributionMode
  createdAt: string
}

export interface CreateGoalRequest {
  name: string
  monthlyTarget: number
  distributionMode: DistributionMode
}

export interface InviteMemberRequest {
  identifier: string
}

export interface InvitationResponse {
  id: string
  goalId: string
  inviterId: string
  inviterName?: string
  invitedUserId: string
  status: InvitationStatus
  createdAt: string
  goalName?: string
}

export interface GoalMember {
  id: string
  userId: string
  name: string
  role: GoalRole
  salary: number
  customPercentage?: number
}

export interface GoalContribution {
  id: string
  userId: string
  name: string
  amount: number
  date: string
}

export interface GroupGoalDetail {
  id: string
  name: string
  monthlyTarget: number
  distributionMode: DistributionMode
  isAdmin: boolean
  members: GoalMember[]
  contributions: GoalContribution[]
}

export interface GoalListItem {
  id: string
  name: string
  monthlyTarget: number
  memberCount: number
  isAdmin: boolean
}

export interface ContributionMember extends GoalMember {
  expectedContribution: number
  contributed: number
  percentage: number
}