import { useTranslation } from "react-i18next"
import { Card, CardContent } from "@/shared/components/ui/card"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Crown, MoreHorizontal, Edit2, Trash2 } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
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

export function MemberCard({
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

  const handlePercentageBlur = () => {
    onCustomPercentageChange(member, customPercentage)
  }

  const handlePercentageChange = (value: number) => {
    const clamped = Math.max(0, Math.min(100, value))
    // Llamamos con el objeto member completo para que el padre pueda persistir
    onCustomPercentageChange({ ...member, customPercentage: clamped }, clamped)
  }

  return (
    <Card className="border-subtle group">
      <CardContent className="p-6 relative">
        {isAdmin && (
          <div className="absolute top-4 right-4">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="size-8 opacity-0 group-hover:opacity-100 transition-opacity">
                  <MoreHorizontal className="size-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-40 bg-card border-border">
                <DropdownMenuItem className="gap-2 cursor-pointer" onClick={() => onEdit(member)}>
                  <Edit2 className="size-4" /> {t("family.edit")}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="text-destructive gap-2 cursor-pointer" onClick={() => onDelete(member.id)}>
                  <Trash2 className="size-4" /> {t("family.delete")}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )}

        <div className="flex items-center gap-2 mb-2">
          <h3 className="font-semibold">{member.name}</h3>
          {member.role === "ADMIN" && <Crown className="size-3 text-amber-400" />}
        </div>
        <p className="text-xs text-muted-foreground uppercase tracking-widest mb-4">
          {member.role === "ADMIN" ? t("family.admin") : t("family.member")}
        </p>

        {isCustomMode && isAdmin && (
          <div className="mb-4">
            <label className="text-xs text-muted-foreground">{t("family.customPercentage")}</label>
            <div className="flex items-center gap-2 mt-1">
              <Input
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
          <div className="mb-4">
            <label className="text-xs text-muted-foreground">{t("family.customPercentage")}</label>
            <p className="text-sm font-medium mt-1">{customPercentage}%</p>
          </div>
        )}

        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">{t("family.salary")}</span>
            <span>{member.salary.toLocaleString()} €</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">{t("family.toContribute")}</span>
            <span className="font-medium">{member.expectedContribution.toLocaleString()} €</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">{t("family.contributed")}</span>
            <span className="font-medium">{member.contributed.toLocaleString()} €</span>
          </div>
          <div className="h-1.5 w-full bg-secondary rounded-full overflow-hidden mt-2">
            <div
              className={`h-full transition-all duration-700 ${MEMBER_COLORS[index % MEMBER_COLORS.length]}`}
              style={{ width: `${Math.min(member.percentage, 100)}%` }}
            />
          </div>
        </div>
      </CardContent>
    </Card>
  )
}