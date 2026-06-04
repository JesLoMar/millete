import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/ui/button"
import { Card, CardContent } from "@/shared/components/ui/card"
import { Badge } from "@/shared/components/ui/badge"
import { Users, Plus, ArrowRight, Crown } from "lucide-react"
import type { FamilyListItem } from "../types"

interface FamilySelectorProps {
  families: FamilyListItem[]
  isLoading: boolean
  onSelect: (familyId: string) => void
  onCreateClick: () => void
}

export function FamilySelector({ families, isLoading, onSelect, onCreateClick }: FamilySelectorProps) {
  const { t } = useTranslation()

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto space-y-4 sm:space-y-6 pt-6 sm:pt-12 px-4 sm:px-0">
        <div className="text-center space-y-3">
          <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
            <Users className="size-10 text-primary" aria-hidden="true" />
          </div>
          <div className="h-8 sm:h-9 w-40 sm:w-48 bg-muted rounded animate-pulse mx-auto" />
          <div className="h-4 sm:h-5 w-56 sm:w-64 bg-muted rounded animate-pulse mx-auto" />
        </div>
        <div className="space-y-3 sm:space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={`skeleton-${i}`} className="h-20 sm:h-24 bg-muted animate-pulse rounded-xl" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-4 sm:space-y-6 pt-6 sm:pt-12 px-4 sm:px-0">
      <div className="text-center space-y-2 sm:space-y-3">
        <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
          <Users className="size-10 text-primary" aria-hidden="true" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-semibold font-headline">{t("family.title")}</h1>
        <p className="text-sm sm:text-base text-muted-foreground">{t("family.selectOrCreate")}</p>
      </div>

      {families.length === 0 ? (
        <div className="text-center py-8 sm:py-12 space-y-4 sm:space-y-6">
          <p className="text-muted-foreground">{t("family.noFamilies")}</p>
          <Button onClick={onCreateClick} className="gap-2 min-h-11">
            <Plus className="size-4" aria-hidden="true" />
            {t("family.createFirst")}
          </Button>
        </div>
      ) : (
        <div className="space-y-3 sm:space-y-4">
          {families.map((family) => (
            <Card
              key={family.id}
              className="border-subtle hover:border-primary/50 transition-all cursor-pointer group"
              role="button"
              tabIndex={0}
              onClick={() => onSelect(family.id)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault()
                  onSelect(family.id)
                }
              }}
              aria-label={t("family.selectFamily", { name: family.name })}
            >
              <CardContent className="p-4 sm:p-6 flex items-center justify-between gap-3 sm:gap-4">
                <div className="space-y-1.5 min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="text-base sm:text-lg font-semibold truncate">{family.name}</h3>
                    {family.isAdmin && (
                      <Badge variant="outline" className="border-amber-500/30 text-amber-400 gap-1 text-xs shrink-0">
                        <Crown className="size-3" aria-hidden="true" />
                        {t("family.admin")}
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs sm:text-sm text-muted-foreground truncate">
                    <Users className="size-3 sm:size-3.5 inline mr-1" aria-hidden="true" />
                    {family.memberCount} {t("family.members")} • {t("family.goal")}: {family.monthlyGoal.toLocaleString()} €
                  </p>
                </div>
                <ArrowRight className="size-5 text-muted-foreground group-hover:text-primary group-hover:translate-x-1 transition-all shrink-0" aria-hidden="true" />
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {families.length > 0 && (
        <div className="flex justify-center pt-2 sm:pt-4">
          <Button onClick={onCreateClick} className="gap-2 min-h-11" variant="outline" size="lg">
            <Plus className="size-4" aria-hidden="true" />
            {t("family.createNew")}
          </Button>
        </div>
      )}
    </div>
  )
}