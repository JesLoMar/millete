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
        <Button onClick={onAddClick} className="gap-1.5 sm:gap-2 shrink-0" size="sm">
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
            <div className="flex flex-col min-w-[320px]">
              {contributions.map((c) => (
                <div
                  key={c.id}
                  className="flex items-center justify-between gap-3 sm:gap-6 p-3 sm:p-4 hover:bg-accent/30 transition-colors border-b last:border-0"
                >
                  <span className="font-medium text-sm sm:text-base truncate min-w-0">
                    {c.memberName || c.name || "Miembro"}
                  </span>
                  <div className="flex items-center gap-3 sm:gap-6 shrink-0">
                    <span className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap">
                      {formatDate(c.date || c.contributionDate || "")}
                    </span>
                    <span className="font-semibold text-sm sm:text-base text-emerald-500 whitespace-nowrap tabular-nums">
                      +{c.amount.toLocaleString()} €
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}