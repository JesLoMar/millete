import { FileDown, FileUp, FolderPlus, Loader2, PlusCircle, Sparkles } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { Button } from '@/shared/components/core/button'
import { AssistantDialog } from '@/features/assistant/components/AssistantDialog'

const actionButtonClass = 'size-11 shrink-0 rounded-full p-0 text-muted-foreground hover:bg-accent/50 hover:text-foreground transition-colors duration-200 group cursor-pointer disabled:opacity-50 disabled:pointer-events-none disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'

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
  const { t } = useTranslation('dashboard');
  const isAnyLoading = isExporting || isImporting

  const allActions: Action[] = [
    {
      icon: PlusCircle,
      labelKey: 'dashboard:quickActions.addExpense',
      ariaLabelKey: 'dashboard:quickActions.addExpenseAria',
      onClick: onAddClick,
    },
    {
      icon: FolderPlus,
      labelKey: 'dashboard:quickActions.createCategory',
      ariaLabelKey:
        'dashboard:quickActions.createCategoryAria',
      onClick: onAddCategoryClick,
    },
    {
      icon: isImporting ? Loader2 : FileUp,
      labelKey: isImporting
        ? 'dashboard:quickActions.importing'
        : 'dashboard:quickActions.importData',
      ariaLabelKey: isImporting
        ? 'dashboard:quickActions.importingAria'
        : 'dashboard:quickActions.importDataAria',
      onClick: onImportClick,
      disabled: isAnyLoading,
      isLoading: isImporting,
    },
    {
      icon: isExporting ? Loader2 : FileDown,
      labelKey: isExporting
        ? 'dashboard:quickActions.exporting'
        : 'dashboard:quickActions.exportData',
      ariaLabelKey: isExporting
        ? 'dashboard:quickActions.exportingAria'
        : 'dashboard:quickActions.exportDataAria',
      onClick: onExportClick,
      disabled: isAnyLoading,
      isLoading: isExporting,
    },
  ]

  const actions = allActions.filter(
    (action) => action.onClick !== undefined
  )

  return (
    <fieldset
      className="flex w-full items-center gap-2 border-0 p-0 m-0"
      aria-label={String(
        t('dashboard:quickActions.groupLabel')
      )}
    >
      {actions.map((action) => (
        <Button
          key={action.labelKey}
          onClick={action.onClick}
          disabled={action.disabled}
          aria-label={String(t(action.ariaLabelKey))}
          title={String(t(action.labelKey))}
          className={actionButtonClass}
        >
          <action.icon
            className={`size-5 ${
              action.isLoading ? 'animate-spin' : ''
            } text-primary-foreground`}
            aria-hidden="true"
          />
        </Button>
      ))}
      <AssistantDialog
        trigger={
          <Button
            aria-label={t('assistant.title')}
            title={t('assistant.title')}
            className={actionButtonClass}
          >
            <Sparkles className="size-5 text-primary-foreground" aria-hidden="true" />
          </Button>
        }
      />
    </fieldset>
  )
}
