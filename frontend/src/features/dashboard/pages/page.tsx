import { useState, useCallback, useReducer } from "react"
import { m } from "framer-motion"
import { useTranslation } from "react-i18next"
import { useQueryClient } from "@tanstack/react-query"
import { TopNav } from '@/shared/components/TopNav'
import { Sidebar } from '@/shared/components/Sidebar'
import { Header, type PeriodFilter } from '@/shared/components/Header'
import { FormattedMetricCard } from '@/shared/components/FormattedMetricCard'
import { NewTransactionDialog } from '@/features/transactions/components/dialogs/NewTransactionDialog'
import { AddCategoryDialog } from '@/features/categories/components/AddCategoryDialog'
import { BudgetBars } from '../components/BudgetBars'
import { CategoryDonut } from '../components/CategoryDonut'
import { HistoryChart } from '../components/HistoryChart'
import { QuickActions } from '../components/QuickActions'
import { RecentTransactions } from '../components/RecentTransactions'
import { ImportModal } from '../components/ImportModal'
import { ExportModal } from '../components/ExportModal'
import { useDashboardQueries } from '../hooks/useDashboardQueries'
import { Wallet, TrendingUp, TrendingDown, PiggyBank } from "lucide-react"
import { apiClient } from '@/shared/api/axiosClient'
import { notify } from '@/shared/utils/notifications/notify'

interface UIState {
  isImportOpen: boolean
  isExportOpen: boolean
  isImporting: boolean
  isAddOpen: boolean
  isAddCategoryOpen: boolean
}

type UIAction =
  | { type: 'OPEN_MODAL'; modal: 'import' | 'export' | 'add' | 'addCategory' }
  | { type: 'CLOSE_MODAL'; modal: 'import' | 'export' | 'add' | 'addCategory' }
  | { type: 'SET_IMPORTING'; value: boolean }

function uiReducer(state: UIState, action: UIAction): UIState {
  switch (action.type) {
    case 'OPEN_MODAL':
      return { ...state, [`is${capitalize(action.modal)}Open`]: true }
    case 'CLOSE_MODAL':
      return { ...state, [`is${capitalize(action.modal)}Open`]: false }
    case 'SET_IMPORTING':
      return { ...state, isImporting: action.value }
    default:
      return state
  }
}

function capitalize(str: string) {
  return str.charAt(0).toUpperCase() + str.slice(1)
}

const initialUIState: UIState = {
  isImportOpen: false,
  isExportOpen: false,
  isImporting: false,
  isAddOpen: false,
  isAddCategoryOpen: false,
}

export const DashboardPage = () => {
  const { t } = useTranslation(['dashboard', 'common'])
  const [period, setPeriod] = useState<PeriodFilter>("month")
  const [ui, dispatch] = useReducer(uiReducer, initialUIState)
  const queryClient = useQueryClient()

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  const { metrics, history, categories, budgets, recentTransactions } = useDashboardQueries(period)

  const handleImport = useCallback(async (file: File) => {
    dispatch({ type: 'SET_IMPORTING', value: true })
    try {
      const formData = new FormData()
      formData.append('file', file)

      await apiClient.post('/data/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        skipGlobalErrorNotify: true
      })

      await queryClient.invalidateQueries()
      dispatch({ type: 'CLOSE_MODAL', modal: 'import' })
      notify.success(t('dashboard:import.success') || 'Datos importados correctamente')
    } catch (err) {
      const error = err as { response?: { status?: number; data?: { message?: string } } }
      let errorMessage = t('dashboard:import.errorGeneric') || 'Error al importar el archivo'

      if (error?.response?.status === 403) {
        errorMessage = error.response.data?.message || t('dashboard:import.errorProperty') || 'No tienes permiso para importar'
      } else if (error?.response?.status === 400) {
        errorMessage = error.response.data?.message || t('dashboard:import.errorFormat') || 'Formato de archivo inválido'
      }

      notify.error(errorMessage)
    } finally {
      dispatch({ type: 'SET_IMPORTING', value: false })
    }
  }, [queryClient, t])

  const handleExportClick = useCallback(() => {
    dispatch({ type: 'OPEN_MODAL', modal: 'export' })
  }, [])

  const periodLabel = t(`dashboard:metrics.vsLast${period === "week" ? "Week" : period === "month" ? "Month" : "Year"}`)
  const periodName = t(`dashboard:header.period.${period}`)

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden pt-16">
        <TopNav />
        <m.main
          className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-6 sm:space-y-8"
          initial="hidden"
          animate="visible"
          variants={{
            hidden: { opacity: 0 },
            visible: {
              opacity: 1,
              transition: { staggerChildren: 0.05 }
            }
          }}
        >
          <Header onPeriodChange={handlePeriodChange} defaultPeriod={period} />
          <m.div className="mb-6" variants={{ hidden: { opacity: 0, y: -20 }, visible: { opacity: 1, y: 0 } }}>
            <QuickActions
              onImportClick={() => dispatch({ type: 'OPEN_MODAL', modal: 'import' })}
              onExportClick={handleExportClick}
              onAddClick={() => dispatch({ type: 'OPEN_MODAL', modal: 'add' })}
              onAddCategoryClick={() => dispatch({ type: 'OPEN_MODAL', modal: 'addCategory' })}
              isExporting={false}
              isImporting={ui.isImporting}
            />
          </m.div>

          <m.div className="min-h-32" variants={{ hidden: { opacity: 0, y: -20 }, visible: { opacity: 1, y: 0 } }}>
            <div className="grid grid-cols-1 min-[390px]:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 w-full min-w-0">
              <FormattedMetricCard
                title={t('dashboard:metrics.balance')}
                value={metrics.data?.balance ?? 0}
                trend={metrics.data?.balanceTrend ?? 0}
                icon={Wallet}
                color="bg-primary/10 text-primary"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
              <FormattedMetricCard
                title={t('dashboard:metrics.income', { period: periodName })}
                value={metrics.data?.income ?? 0}
                trend={metrics.data?.incomeTrend ?? 0}
                icon={TrendingUp}
                color="bg-primary/10 text-primary"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
              <FormattedMetricCard
                title={t('dashboard:metrics.expenses', { period: periodName })}
                value={metrics.data?.expenses ?? 0}
                trend={metrics.data?.expensesTrend ?? 0}
                icon={TrendingDown}
                color="bg-destructive/10 text-destructive"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                invertedTrend
              />
              <FormattedMetricCard
                title={t('dashboard:metrics.savings')}
                value={metrics.data?.savings ?? 0}
                trend={metrics.data?.savingsTrend ?? 0}
                icon={PiggyBank}
                color="bg-warning/10 text-warning"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
            </div>
          </m.div>

          <m.div className="grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6" variants={{ hidden: { opacity: 0, y: -20 }, visible: { opacity: 1, y: 0 } }}>
            <div className="lg:col-span-8 min-h-96">
              <HistoryChart period={period} data={history.data} loading={history.isLoading} />
            </div>
            <div className="lg:col-span-4 min-h-96">
              <CategoryDonut data={categories.data} loading={categories.isLoading} />
            </div>
          </m.div>
          <m.div className="grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6" variants={{ hidden: { opacity: 0, y: -20 }, visible: { opacity: 1, y: 0 } }}>
            <div className="lg:col-span-5 min-h-96">
              <BudgetBars data={budgets.data} loading={budgets.isLoading} period={period} />
            </div>
            <div className="lg:col-span-7 min-h-96">
              <RecentTransactions data={recentTransactions.data} loading={recentTransactions.isLoading} />
            </div>
          </m.div>
        </m.main>
      </div>
      <ImportModal
        isOpen={ui.isImportOpen}
        onClose={() => dispatch({ type: 'CLOSE_MODAL', modal: 'import' })}
        onImport={handleImport}
      />
      <ExportModal
        open={ui.isExportOpen}
        onOpenChange={(open) => dispatch({ type: open ? 'OPEN_MODAL' : 'CLOSE_MODAL', modal: 'export' })}
      />
      <NewTransactionDialog open={ui.isAddOpen} onOpenChange={(open) => dispatch({ type: open ? 'OPEN_MODAL' : 'CLOSE_MODAL', modal: 'add' })} />
      <AddCategoryDialog open={ui.isAddCategoryOpen} onOpenChange={(open) => dispatch({ type: open ? 'OPEN_MODAL' : 'CLOSE_MODAL', modal: 'addCategory' })} />
    </div>
  )
}
