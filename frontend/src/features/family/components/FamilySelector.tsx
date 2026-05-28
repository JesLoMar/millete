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
      <div className="max-w-2xl mx-auto space-y-6 pt-12">
        <div className="text-center space-y-3">
          <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
            <Users className="size-10 text-primary" />
          </div>
          <div className="h-9 w-48 bg-muted rounded animate-pulse mx-auto" />
          <div className="h-5 w-64 bg-muted rounded animate-pulse mx-auto" />
        </div>
        <div className="space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={`skeleton-${i}`} className="h-24 bg-muted animate-pulse rounded-xl" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 pt-12">
      <div className="text-center space-y-3">
        <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
          <Users className="size-10 text-primary" />
        </div>
        <h1 className="text-3xl font-semibold font-headline">{t("family.title")}</h1>
        <p className="text-muted-foreground">{t("family.selectOrCreate")}</p>
      </div>

      {families.length === 0 ? (
        <div className="text-center py-12 space-y-6">
          <p className="text-muted-foreground">{t("family.noFamilies")}</p>
          <Button onClick={onCreateClick} className="gap-2">
            <Plus className="size-4" />
            {t("family.createFirst")}
          </Button>
        </div>
      ) : (
        <div className="space-y-4">
          {families.map((family) => (
            <Card
              key={family.id}
              className="border-subtle hover:border-primary/50 transition-all cursor-pointer group"
              onClick={() => onSelect(family.id)}
            >
              <CardContent className="p-6 flex items-center justify-between">
                <div className="space-y-1.5">
                  <div className="flex items-center gap-2">
                    <h3 className="text-lg font-semibold">{family.name}</h3>
                    {family.isAdmin && (
                      <Badge variant="outline" className="border-amber-500/30 text-amber-400 gap-1 text-xs">
                        <Crown className="size-3" />
                        {t("family.admin")}
                      </Badge>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground">
                    <Users className="size-3.5 inline mr-1" />
                    {family.memberCount} {t("family.members")} • {t("family.goal")}: {family.monthlyGoal.toLocaleString()} €
                  </p>
                </div>
                <ArrowRight className="size-5 text-muted-foreground group-hover:text-primary group-hover:translate-x-1 transition-all shrink-0" />
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {families.length > 0 && (
        <div className="flex justify-center pt-4">
          <Button onClick={onCreateClick} className="gap-2" variant="outline" size="lg">
            <Plus className="size-4" />
            {t("family.createNew")}
          </Button>
        </div>
      )}
    </div>
  )
}