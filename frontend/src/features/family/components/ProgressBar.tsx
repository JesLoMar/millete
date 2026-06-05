import { useTranslation } from "react-i18next"
import type { ContributionMember } from "../types"
import { MEMBER_COLORS } from "../constants"

interface ProgressBarProps {
  contributions: ContributionMember[]
  monthlyGoal: number
}

export function ProgressBar({ contributions, monthlyGoal }: ProgressBarProps) {
  const { t } = useTranslation()
  const totalContributed = contributions.reduce((sum, m) => sum + m.contributed, 0)
  const totalPercentage = monthlyGoal > 0 
    ? Math.min((totalContributed / monthlyGoal) * 100, 100) 
    : 0

  return (
    <div className="space-y-1.5">
      <div 
        className="h-2.5 sm:h-3 w-full bg-secondary rounded-full overflow-hidden flex"
        role="progressbar"
        aria-valuenow={Math.round(totalPercentage)}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label={t("family.progressBar.label")}
      >
        {contributions.map((member, index) => {
          const memberPct = monthlyGoal > 0 
            ? Math.min((member.contributed / monthlyGoal) * 100, 100) 
            : 0
          return memberPct > 0 ? (
            <div
              key={member.id}
              className={`h-full transition-all duration-700 ${MEMBER_COLORS[index % MEMBER_COLORS.length]}`}
              style={{ width: `${memberPct}%` }}
              title={`${member.name}: ${member.contributed.toLocaleString()} €`}
              aria-label={`${member.name}: ${member.contributed.toLocaleString()} € (${Math.round(memberPct)}%)`}
            />
          ) : null
        })}
      </div>
      
      <div className="flex items-center justify-between text-xs text-muted-foreground">
        <span>
          {totalContributed.toLocaleString()} € / <span className="hidden xs:inline">{t("family.progressBar.of")}</span> {monthlyGoal.toLocaleString()} €
        </span>
        <span className="font-medium tabular-nums">
          {Math.round(totalPercentage)}%
        </span>
      </div>
    </div>
  )
}