import { useTranslation } from "react-i18next"
import { cn } from "@/lib/utils"

interface TypeToggleProps {
  value: "INCOME" | "EXPENSE"
  onChange: (type: "INCOME" | "EXPENSE") => void
}

export function TypeToggle({ value, onChange }: TypeToggleProps) {
  const { t } = useTranslation(['transactions', 'common', 'categories', 'auth', 'dashboard'])

  return (
    <div className="flex rounded-lg border border-border overflow-hidden">
      <button
        type="button"
        onClick={() => onChange("EXPENSE")}
        className={cn(
          "flex-1 py-2 text-sm font-medium transition-colors",
          value === "EXPENSE"
            ? "bg-destructive/20 text-destructive"
            : "bg-background text-muted-foreground hover:text-foreground"
        )}
      >
        {t('transactions:expense')}
      </button>
      <button
        type="button"
        onClick={() => onChange("INCOME")}
        className={cn(
          "flex-1 py-2 text-sm font-medium transition-colors",
          value === "INCOME"
            ? "bg-primary/20 text-primary"
            : "bg-background text-muted-foreground hover:text-foreground"
        )}
      >
        {t('transactions:income')}
      </button>
    </div>
  )
}
