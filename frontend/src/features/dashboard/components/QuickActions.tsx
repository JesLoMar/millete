import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/ui/button"
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
  const { t } = useTranslation()
  const isAnyLoading = isExporting || isImporting

  const allActions: Action[] = [
    {
      icon: PlusCircle,
      labelKey: "dashboard.quickActions.addExpense",
      color: "bg-primary/10 text-primary group-hover:bg-primary group-hover:text-primary-foreground",
      onClick: onAddClick,
    },
    {
      icon: FolderPlus,
      labelKey: "dashboard.quickActions.createCategory",
      color: "bg-emerald-500/10 text-emerald-500 group-hover:bg-emerald-500 group-hover:text-white",
      onClick: onAddCategoryClick,
    },
    {
      icon: isImporting ? Loader2 : FileUp,
      labelKey: isImporting ? "dashboard.quickActions.importing" : "dashboard.quickActions.importData",
      color: "bg-amber-500/10 text-amber-500 group-hover:bg-amber-500 group-hover:text-white",
      onClick: onImportClick,
      disabled: isAnyLoading,
      isLoading: isImporting,
    },
    {
      icon: isExporting ? Loader2 : FileDown,
      labelKey: isExporting ? "dashboard.quickActions.exporting" : "dashboard.quickActions.exportData",
      color: "bg-purple-500/10 text-purple-500 group-hover:bg-purple-500 group-hover:text-white",
      onClick: onExportClick,
      disabled: isAnyLoading,
      isLoading: isExporting,
    },
  ]

  const actions = allActions.filter((action) => action.onClick !== undefined)

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {actions.map((action) => (
        <Button
          key={action.labelKey}
          onClick={action.onClick}
          disabled={action.disabled}
          className="h-28 flex flex-col items-center justify-center gap-3 rounded-xl border border-border/50 bg-card hover:border-primary/30 transition-all duration-200 group disabled:opacity-50 disabled:pointer-events-none"
        >
          <div className={`p-3 rounded-xl transition-all duration-200 ${action.color}`}>
            <action.icon className={`size-6 ${action.isLoading ? 'animate-spin' : ''}`} />
          </div>
          <span className="font-medium text-sm text-foreground">
            {t(action.labelKey)}
          </span>
        </Button>
      ))}
    </div>
  )
}