import { FileDown, FileUp, FolderPlus, Loader2, PlusCircle, Sparkles } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { Button } from '@/shared/components/core/button'
import { AssistantDialog } from '@/features/assistant/components/AssistantDialog'
import { assistantService } from '@/features/assistant/services/assistant.service'

const actionButtonClass = 'group h-auto min-w-0 basis-[calc((100%_-_1.5rem)/3)] cursor-pointer flex-col justify-start gap-1.5 whitespace-normal px-0 py-1 text-center text-xs leading-tight text-foreground transition-colors hover:bg-transparent hover:shadow-none'

function ActionContent({ icon: Icon, label, loading }: {
  icon: Action['icon']
  label: string
  loading?: boolean
}) {
  return (
    <>
      <span className="grid size-11 shrink-0 place-items-center rounded-full bg-primary text-primary-foreground transition-colors group-hover:bg-primary/90">
        <Icon className={loading ? 'size-5 animate-spin motion-reduce:animate-none' : 'size-5'} aria-hidden="true" />
      </span>
      <span className="w-full wrap-anywhere">{label}</span>
    </>
  )
}

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
  const { data: assistantAvailable, isSuccess } = useQuery({
    queryKey: ['assistant', 'availability'],
    queryFn: ({ signal }) => assistantService.availability(signal),
    staleTime: 30_000,
    retry: false,
  })
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
      className="mx-auto my-0 flex w-full max-w-80 min-w-0 flex-wrap justify-center gap-3 border-0 p-0"
      aria-label={String(
        t('dashboard:quickActions.groupLabel')
      )}
    >
      {actions.map((action) => (
        <Button
          key={action.labelKey}
          variant="ghost"
          onClick={action.onClick}
          disabled={action.disabled}
          title={t(action.ariaLabelKey)}
          className={actionButtonClass}
        >
          <ActionContent icon={action.icon} label={t(action.labelKey)} loading={action.isLoading} />
        </Button>
      ))}
      {isSuccess && assistantAvailable && (
        <AssistantDialog
          trigger={
            <Button
              variant="ghost"
              className={actionButtonClass}
            >
              <ActionContent icon={Sparkles} label={t('assistant.title')} />
            </Button>
          }
        />
      )}
    </fieldset>
  )
}
