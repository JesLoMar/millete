import { useTranslation } from "react-i18next"

export const EmptyState = () => {
  const { t } = useTranslation()
  return (
    <div className="flex w-full items-center justify-center min-h-30 rounded-lg border border-border bg-surface">
      <p className="text-sm text-muted-foreground">{t("savingsGoals.emptyState")}</p>
    </div>
  )
}