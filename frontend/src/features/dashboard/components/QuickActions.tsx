import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { PlusCircle, FolderPlus, FileUp, FileDown, Loader2 } from "lucide-react"

interface QuickActionsProps {
  onImportClick: () => void
  onExportClick: () => void
  onAddClick?: () => void
  onAddCategoryClick?: () => void
  isExporting?: boolean
  isImporting?: boolean
}

interface Action {
  icon: React.ComponentType<{ className?: string }>
  labelKey: string
  ariaLabelKey: string
  color: string
  onClick?: () => void
  disabled?: boolean
  isLoading?: boolean
}

export function QuickActions({
  onImportClick,
  onExportClick,
  onAddClick,
  onAddCategoryClick,
  isExporting = false,
  isImporting = false,
}: QuickActionsProps) {
  // Usamos el namespace dashboard y common
  const { t } = useTranslation(['dashboard', 'common'])
  const isAnyLoading = isExporting || isImporting

  const allActions: Action[] = [
    {
      icon: PlusCircle,
      labelKey: "dashboard:quickActions.addExpense",
      ariaLabelKey: "dashboard:quickActions.addExpenseAria",
      color: "bg-primary/10 text-primary group-hover:bg-primary group-hover:text-primary-foreground",
      onClick: onAddClick,
    },
    {
      icon: FolderPlus,
      labelKey: "dashboard:quickActions.createCategory",
      ariaLabelKey: "dashboard:quickActions.createCategoryAria",
      color: "bg-primary/10 text-primary group-hover:bg-primary group-hover:text-primary-foreground",
      onClick: onAddCategoryClick,
    },
    {
      icon: isImporting ? Loader2 : FileUp,
      labelKey: isImporting ? "dashboard:quickActions.importing" : "dashboard:quickActions.importData",
      ariaLabelKey: isImporting ? "dashboard:quickActions.importingAria" : "dashboard:quickActions.importDataAria",
      color: "bg-warning/10 text-warning group-hover:bg-warning group-hover:text-warning-foreground",
      onClick: onImportClick,
      disabled: isAnyLoading,
      isLoading: isImporting,
    },
    {
      icon: isExporting ? Loader2 : FileDown,
      labelKey: isExporting ? "dashboard:quickActions.exporting" : "dashboard:quickActions.exportData",
      ariaLabelKey: isExporting ? "dashboard:quickActions.exportingAria" : "dashboard:quickActions.exportDataAria",
      color: "bg-chart-5/10 text-chart-5 group-hover:bg-chart-5 group-hover:text-primary-foreground",
      onClick: onExportClick,
      disabled: isAnyLoading,
      isLoading: isExporting,
    },
  ]

  const actions = allActions.filter((action) => action.onClick !== undefined)

  return (
    <fieldset 
      className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 w-full border-0 p-0 m-0"
      aria-label={String(t('dashboard:quickActions.groupLabel'))}
    >
      {actions.map((action) => (
        <Button
          key={action.labelKey}
          onClick={action.onClick}
          disabled={action.disabled}
          aria-label={String(t(action.ariaLabelKey))}
          className={`
            h-auto min-h-25 sm:min-h-28 w-full
            flex flex-col items-center justify-center gap-2 sm:gap-2.5
            rounded-xl border border-border/50 bg-card px-2 py-4 sm:py-5
            hover:border-primary/30 hover:shadow-sm text-wrap whitespace-normal
            transition-all duration-200 group cursor-pointer
            disabled:opacity-50 disabled:pointer-events-none disabled:cursor-not-allowed
            focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2
          `}
        >
          <div className={`p-2 sm:p-2.5 rounded-xl transition-all duration-200 shrink-0 ${action.color}`}>
            <action.icon className={`size-5 sm:size-5.5 ${action.isLoading ? 'animate-spin' : ''}`} aria-hidden="true" />
          </div>
          
          <span className="font-medium text-xs sm:text-sm md:text-sm text-foreground text-center leading-tight w-full wrap-break-word px-1">
            {String(t(action.labelKey))}
          </span>
        </Button>
      ))}
    </fieldset>
  )
}