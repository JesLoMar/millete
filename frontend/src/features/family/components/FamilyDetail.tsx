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
    <div className="max-w-6xl mx-auto space-y-4 sm:space-y-6 px-4 sm:px-0">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
        <div className="flex items-center gap-3 sm:gap-4 min-w-0">
          <Button 
            variant="ghost" 
            size="icon" 
            onClick={onBack} 
            className="size-8 shrink-0"
            aria-label={t("family.back")}
          >
            <ArrowLeft className="size-5" aria-hidden="true" />
          </Button>
          <div className="min-w-0">
            <h1 className="text-xl sm:text-2xl md:text-3xl font-semibold font-headline truncate">
              {family.name}
            </h1>
            <p className="text-muted-foreground text-xs sm:text-sm">
              {family.members.length} {t("family.members")}
            </p>
          </div>
        </div>
        
        {isAdmin && (
          <div className="flex gap-2 sm:gap-3 sm:shrink-0">
            <Button onClick={onInviteClick} className="gap-1.5 sm:gap-2 flex-1 sm:flex-none" size="sm">
              <UserPlus className="size-3.5 sm:size-4" />
              <span className="hidden xs:inline">{t("family.inviteMember")}</span>
              <span className="xs:hidden">{t("family.inviteShort")}</span>
            </Button>
            <Button variant="outline" onClick={onGoalClick} className="gap-1.5 sm:gap-2 flex-1 sm:flex-none" size="sm">
              <Target className="size-3.5 sm:size-4" />
              <span className="hidden xs:inline">{t("family.changeGoal")}</span>
              <span className="xs:hidden">{t("family.goalShort")}</span>
            </Button>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6">
        <Card className="lg:col-span-9 border-subtle">
          <CardHeader className="pb-2">
            <CardTitle className="text-base sm:text-lg">{t("family.goalProgress")}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col xs:flex-row xs:items-end justify-between gap-2 mb-4">
              <div className="min-w-0">
                <p className="text-xs sm:text-sm text-muted-foreground">{t("family.collected")}</p>
                <p className="text-2xl sm:text-3xl font-headline truncate">
                  {totalContributed.toLocaleString()} €
                  <span className="text-sm text-muted-foreground"> / {(family.monthlyGoal ?? 0).toLocaleString()} €</span>
                </p>
              </div>
              <p className="text-2xl sm:text-3xl font-semibold text-primary tabular-nums">
                {Math.round(percentageCompleted)}%
              </p>
            </div>

            <ProgressBar contributions={contributions} monthlyGoal={family.monthlyGoal} />
            <div className="space-y-2 mt-4 overflow-x-auto">
              {contributions.map((member, index) => (
                <div key={member.id} className="flex items-center justify-between text-xs sm:text-sm gap-2 min-w-70">
                  <div className="flex items-center gap-2 min-w-0">
                    <div className={`size-2.5 sm:size-3 rounded-full shrink-0 ${MEMBER_COLORS[index % MEMBER_COLORS.length]}`} />
                    <span className="text-muted-foreground truncate">{member.name}</span>
                  </div>
                  <span className="font-medium whitespace-nowrap shrink-0 tabular-nums">
                    {member.contributed.toLocaleString()} € / {member.expectedContribution.toLocaleString()} €
                    <span className="text-muted-foreground ml-1">({member.percentage.toFixed(0)}%)</span>
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
        <div className="lg:col-span-3">
          <DistributionCard
            distributionMode={family.distributionMode}
            isAdmin={isAdmin}
            isCustomMode={isCustomMode}
            isPercentageInvalid={isPercentageInvalid}
            onModeChange={onModeChange}
          />
        </div>
      </div>
      <div>
        <h2 className="text-lg sm:text-xl font-headline mb-3 sm:mb-4">{t("family.memberDetails")}</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
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
      <ContributionHistory contributions={family.contributions} onAddClick={onAddContribution} />
    </div>
  )
}