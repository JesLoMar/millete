export type FamilyRole = 'ADMIN' | 'MEMBER'
export type DistributionMode = 'EQUAL' | 'PROPORTIONAL' | 'CUSTOM'
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface FamilyResponse {
  id: string
  name: string
  distributionMode: DistributionMode
  createdAt: string
}

export interface CreateFamilyRequest {
  name: string
  distributionMode: DistributionMode
}

export interface InviteMemberRequest {
  email: string
  role: FamilyRole
}

export interface InvitationResponse {
  id: string
  familyId: string
  inviterId: string
  inviteeEmail: string
  status: InvitationStatus
  createdAt: string
}

export interface FamilyMember {
  id: string
  name: string
  role: "ADMIN" | "MEMBER"
  salary: number
  customPercentage?: number
  userId?: string
}

export interface FamilyContribution {
  id: string
  memberId: string
  memberName: string
  amount: number
  date: string
  contributionDate?: string
  name?: string
}

export interface FamilyUnitData {
  id: string
  name: string
  monthlyGoal: number
  distributionMode: "EQUITATIVE" | "PROPORTIONAL" | "CUSTOM"
  members: FamilyMember[]
  contributions: FamilyContribution[]
  isAdmin: boolean
}

export interface FamilyListItem {
  id: string
  name: string
  monthlyGoal: number
  memberCount: number
  isAdmin: boolean
}

export interface ContributionMember extends FamilyMember {
  expectedContribution: number
  contributed: number
  percentage: number
}