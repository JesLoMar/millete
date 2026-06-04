import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import { Button } from "@/shared/components/ui/button"
import { ArrowLeft, UserPlus, Target } from "lucide-react"
import { ProgressBar } from "./ProgressBar"
import { MemberCard } from "./MemberCard"
import { DistributionCard } from "./DistributionCard"
import { ContributionHistory } from "./ContributionHistory"
import type { FamilyUnitData, ContributionMember } from "../types"
import { MEMBER_COLORS } from "../constants"

interface FamilyDetailProps {
  family: FamilyUnitData
  contributions: ContributionMember[]
  totalContributed: number
  percentageCompleted: number
  customPercentages: Record<string, number>
  onCustomPercentageChange: (member: ContributionMember, percentage: number) => void
  totalCustomPercentage: number
  onBack: () => void
  onInviteClick: () => void
  onGoalClick: () => void
  onEditMember: (member: ContributionMember) => void
  onDeleteMember: (memberId: string) => void
  onModeChange: (mode: string) => void
  onAddContribution: () => void
}

export function FamilyDetail({
  family,
  contributions,
  totalContributed,
  percentageCompleted,
  customPercentages,
  onCustomPercentageChange,
  totalCustomPercentage,
  onBack,
  onInviteClick,
  onGoalClick,
  onEditMember,
  onDeleteMember,
  onModeChange,
  onAddContribution,
}: FamilyDetailProps) {
  const { t } = useTranslation()
  const isAdmin = family.isAdmin
  const isCustomMode = family.distributionMode === "CUSTOM"
  const isPercentageInvalid = isCustomMode && Math.abs(totalCustomPercentage - 100) > 0.01

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={onBack} className="size-8">
            <ArrowLeft className="size-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-semibold font-headline">{family.name}</h1>
            <p className="text-muted-foreground text-sm">
              {family.members.length} {t("family.members")}
            </p>
          </div>
        </div>
        {isAdmin && (
          <div className="flex gap-3">
            <Button onClick={onInviteClick} className="gap-2" size="sm">
              <UserPlus className="size-4" />
              {t("family.inviteMember")}
            </Button>
            <Button variant="outline" onClick={onGoalClick} className="gap-2" size="sm">
              <Target className="size-4" />
              {t("family.changeGoal")}
            </Button>
          </div>
        )}
      </div>

      {/* Goal Progress + Distribution */}
      <div className="grid grid-cols-12 gap-6">
        <Card className="col-span-12 lg:col-span-9 border-subtle">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg">{t("family.goalProgress")}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-end justify-between mb-4">
              <div>
                <p className="text-sm text-muted-foreground">{t("family.collected")}</p>
                <p className="text-3xl font-headline">
                  {totalContributed.toLocaleString()} €
                  <span className="text-sm text-muted-foreground"> / {(family.monthlyGoal ?? 0).toLocaleString()} €</span>
                </p>
              </div>
              <p className="text-3xl font-semibold text-primary">{Math.round(percentageCompleted)}%</p>
            </div>

            <ProgressBar contributions={contributions} monthlyGoal={family.monthlyGoal} />

            <div className="space-y-2 mt-4">
              {contributions.map((member, index) => (
                <div key={member.id} className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-2">
                    <div className={`size-3 rounded-full ${MEMBER_COLORS[index % MEMBER_COLORS.length]}`} />
                    <span className="text-muted-foreground">{member.name}</span>
                  </div>
                  <span className="font-medium">
                    {member.contributed.toLocaleString()} € / {member.expectedContribution.toLocaleString()} €
                    <span className="text-muted-foreground ml-1">({member.percentage.toFixed(0)}%)</span>
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <DistributionCard
          distributionMode={family.distributionMode}
          isAdmin={isAdmin}
          isCustomMode={isCustomMode}
          isPercentageInvalid={isPercentageInvalid}
          onModeChange={onModeChange}
        />
      </div>

      {/* Member Cards */}
      <div>
        <h2 className="text-xl font-headline mb-4">{t("family.memberDetails")}</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {contributions.map((member, index) => (
            <MemberCard
              key={member.id}
              member={member}
              index={index}
              isAdmin={isAdmin}
              isCustomMode={isCustomMode}
              customPercentage={customPercentages[member.id] ?? member.customPercentage ?? 0}
              onCustomPercentageChange={onCustomPercentageChange}
              onEdit={onEditMember}
              onDelete={onDeleteMember}
            />
          ))}
        </div>
      </div>

      {/* Contribution History */}
      <ContributionHistory contributions={family.contributions} onAddClick={onAddContribution} />
    </div>
  )
}