import { memo } from "react"
import { useTranslation } from "react-i18next"
import { Card, CardContent } from "@/shared/components/core/card"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { ProgressBar } from "@/shared/components/core/progress-bar"
import { Crown, MoreHorizontal, Edit2, Trash2 } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/shared/components/core/dropdown-menu"
import { MEMBER_COLORS } from "../constants"
import type { ContributionMember } from "../types"

interface MemberCardProps {
  member: ContributionMember
  index: number
  isAdmin: boolean
  isCustomMode: boolean
  customPercentage: number
  onCustomPercentageChange: (member: ContributionMember, percentage: number) => void
  onEdit: (member: ContributionMember) => void
  onDelete: (memberId: string) => void
}

export const MemberCard = memo(function MemberCard({
  member,
  index,
  isAdmin,
  isCustomMode,
  customPercentage,
  onCustomPercentageChange,
  onEdit,
  onDelete,
}: MemberCardProps) {
  const { t } = useTranslation()
  const percentageInputId = `member-percentage-${member.id}`

  const handlePercentageBlur = () => {
    onCustomPercentageChange(member, customPercentage)
  }

  const handlePercentageChange = (value: number) => {
    const clamped = Math.max(0, Math.min(100, value))
    onCustomPercentageChange(member, clamped)
  }

  return (
    <Card className="border group">
      <CardContent className="p-4 sm:p-6 relative">
        {isAdmin && (
          <div className="absolute top-3 sm:top-4 right-3 sm:right-4">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="size-8 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity"
                  aria-label={t("groupGoals:memberOptions", { name: member.name })}
                >
                  <MoreHorizontal className="size-4" aria-hidden="true" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-40 bg-card border-border">
                <DropdownMenuItem className="gap-2 cursor-pointer" onClick={() => onEdit(member)}>
                  <Edit2 className="size-4" aria-hidden="true" /> {t('groupGoals:edit')}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="text-destructive gap-2 cursor-pointer" onClick={() => onDelete(member.id)}>
                  <Trash2 className="size-4" aria-hidden="true" /> {t('groupGoals:delete')}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )}

        <div className="flex items-center gap-2 mb-2 pr-8">
          <h3 className="font-semibold text-sm sm:text-base truncate">{member.name}</h3>
          {member.role === "ADMIN" && (
            <Crown className="size-3 text-warning shrink-0" aria-label={t('groupGoals:admin')} />
          )}
        </div>
        <p className="text-xs sm:text-xs text-muted-foreground uppercase tracking-widest mb-3 sm:mb-4">
          {member.role === "ADMIN" ? t('groupGoals:admin') : t('groupGoals:member')}
        </p>

        {isCustomMode && isAdmin && (
          <div className="mb-3 sm:mb-4">
            <label htmlFor={percentageInputId} className="text-xs text-muted-foreground">
              {t('groupGoals:customPercentage')}
            </label>
            <div className="flex items-center gap-2 mt-1">
              <Input
                id={percentageInputId}
                type="number"
                min="0"
                max="100"
                step="0.1"
                value={customPercentage}
                onChange={(e) => handlePercentageChange(Number(e.target.value))}
                onBlur={handlePercentageBlur}
                className="h-8 w-20 bg-background border-border text-sm"
              />
              <span className="text-sm text-muted-foreground">%</span>
            </div>
          </div>
        )}
        {isCustomMode && !isAdmin && (
          <div className="mb-3 sm:mb-4">
            <span className="text-xs text-muted-foreground">{t('groupGoals:customPercentage')}</span>
            <p className="text-sm font-medium mt-1">{customPercentage}%</p>
          </div>
        )}

        <div className="space-y-1.5 sm:space-y-2">
          <div className="flex justify-between text-xs sm:text-sm gap-2">
            <span className="text-muted-foreground truncate">{t('groupGoals:salary')}</span>
            <span className="whitespace-nowrap shrink-0 tabular-nums">{member.salary.toLocaleString()} €</span>
          </div>
          <div className="flex justify-between text-xs sm:text-sm gap-2">
            <span className="text-muted-foreground truncate">{t('groupGoals:toContribute')}</span>
            <span className="font-medium whitespace-nowrap shrink-0 tabular-nums">{member.expectedContribution.toLocaleString()} €</span>
          </div>
          <div className="flex justify-between text-xs sm:text-sm gap-2">
            <span className="text-muted-foreground truncate">{t('groupGoals:contributed')}</span>
            <span className="font-medium whitespace-nowrap shrink-0 tabular-nums">{member.contributed.toLocaleString()} €</span>
          </div>
          <ProgressBar
            value={Math.min(member.percentage, 100)}
            max={100}
            size="sm"
            className="mt-2"
            barClassName={MEMBER_COLORS[index % MEMBER_COLORS.length]}
            ariaLabel={t("groupGoals:memberProgress", { name: member.name })}
          />
        </div>
      </CardContent>
    </Card>
  )
});