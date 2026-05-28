import { useTranslation } from "react-i18next"
import { Plus } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import { formatDate } from "../utils"
import type { FamilyContribution } from "../types"

interface ContributionHistoryProps {
  contributions: FamilyContribution[]
  onAddClick: () => void
}

export function ContributionHistory({ contributions, onAddClick }: ContributionHistoryProps) {
  const { t } = useTranslation()

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-headline">{t("family.contributionsHistory")}</h2>
        <Button onClick={onAddClick} className="gap-2" size="sm">
          <Plus className="size-4" />
          {t("family.addContribution")}
        </Button>
      </div>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {(!contributions || contributions.length === 0) ? (
          <p className="text-center text-muted-foreground py-12 text-sm">
            {t("family.noContributions")}
          </p>
        ) : (
          <div className="flex flex-col">
            {contributions.map((c) => (
              <div
                key={c.id}
                className="flex items-center justify-between p-4 hover:bg-accent/30 transition-colors border-b last:border-0"
              >
                <div className="font-medium">{c.memberName || c.name || "Miembro"}</div>
                <div className="flex items-center gap-6">
                  <span className="text-sm text-muted-foreground">{formatDate(c.date || c.contributionDate || "")}</span>
                  <span className="font-semibold text-emerald-500">+{c.amount.toLocaleString()} €</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}