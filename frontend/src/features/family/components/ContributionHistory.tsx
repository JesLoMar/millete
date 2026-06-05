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
      <div className="flex items-center justify-between gap-3 mb-4">
        <h2 className="text-lg sm:text-xl font-headline">
          {t("family.contributionsHistory")}
        </h2>
        <Button onClick={onAddClick} className="gap-1.5 sm:gap-2 shrink-0 text-sm" size="sm">
          <Plus className="size-3.5 sm:size-4" />
          <span className="hidden xs:inline">{t("family.addContribution")}</span>
          <span className="xs:hidden">{t("family.addContributionShort")}</span>
        </Button>
      </div>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {(!contributions || contributions.length === 0) ? (
          <p className="text-center text-muted-foreground py-12 text-sm">
            {t("family.noContributions")}
          </p>
        ) : (
          <div className="w-full overflow-x-auto">
            <div className="flex flex-col min-w-100 sm:min-w-0">
              <div className="hidden sm:grid grid-cols-3 gap-4 px-4 py-2 border-b border-border/50 bg-muted/30">
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                  {t("family.member")}
                </span>
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider text-right">
                  {t("family.amount")}
                </span>
                <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider text-right">
                  {t("family.date")}
                </span>
              </div>

              {contributions.map((c) => (
                <div
                  key={c.id}
                  className="grid grid-cols-3 gap-2 sm:gap-4 items-center p-3.5 sm:p-4 hover:bg-accent/30 transition-colors border-b last:border-0"
                >
                  <span className="font-semibold text-sm sm:text-base truncate min-w-0">
                    {c.memberName || c.name || t("family.member")}
                  </span>

                  <span className="font-bold text-sm sm:text-base text-emerald-500 text-right whitespace-nowrap tabular-nums">
                    +{c.amount.toLocaleString()} €
                  </span>

                  <span className="text-xs sm:text-sm text-muted-foreground text-right whitespace-nowrap">
                    {formatDate(c.date || c.contributionDate || "")}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}