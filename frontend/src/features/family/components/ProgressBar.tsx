import type { ContributionMember } from "../types"
import { MEMBER_COLORS } from "../constants"

interface ProgressBarProps {
  contributions: ContributionMember[]
  monthlyGoal: number
}

export function ProgressBar({ contributions, monthlyGoal }: ProgressBarProps) {
  return (
    <div className="h-3 w-full bg-secondary rounded-full overflow-hidden flex">
      {contributions.map((member, index) => {
        const memberPct = monthlyGoal > 0 ? (member.contributed / monthlyGoal) * 100 : 0
        return memberPct > 0 ? (
          <div
            key={member.id}
            className={`h-full transition-all duration-700 ${MEMBER_COLORS[index % MEMBER_COLORS.length]}`}
            style={{ width: `${memberPct}%` }}
            title={`${member.name}: ${member.contributed.toLocaleString()} €`}
          />
        ) : null
      })}
    </div>
  )
}