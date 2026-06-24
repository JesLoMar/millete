import { useState, useCallback } from "react"
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
import { notify } from "@/shared/utils/notifications/notify"

export const DashboardPage = () => {
  const { t } = useTranslation(['dashboard', 'common'])
  const [period, setPeriod] = useState<PeriodFilter>("month")
  const [ui, setUi] = useState({
    isImportOpen: false,
    isExportOpen: false,
    isImporting: false,
    isAddOpen: false,
    isAddCategoryOpen: false,
  })
  const queryClient = useQueryClient()

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  const { metrics, history, categories, budgets, recentTransactions } = useDashboardQueries(period)

  const handleImport = useCallback(async (file: File) => {
    setUi((prev) => ({ ...prev, isImporting: true }))
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      await apiClient.post('/data/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        skipGlobalErrorNotify: true
      })

      await queryClient.invalidateQueries()
      setUi((prev) => ({ ...prev, isImportOpen: false }))
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
      setUi((prev) => ({ ...prev, isImporting: false }))
    }
  }, [queryClient, t])

  const handleExportClick = useCallback(() => {
    setUi((prev) => ({ ...prev, isExportOpen: true }))
  }, [])

  const periodLabel = t(`dashboard:metrics.vsLast${period === "week" ? "Week" : period === "month" ? "Month" : "Year"}`)
  const periodName = t(`dashboard:header.period.${period}`)

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 sm:space-y-6">
          <Header onPeriodChange={handlePeriodChange} defaultPeriod={period} />
          
          <QuickActions
            onImportClick={() => setUi((prev) => ({ ...prev, isImportOpen: true }))}
            onExportClick={handleExportClick}
            onAddClick={() => setUi((prev) => ({ ...prev, isAddOpen: true }))}
            onAddCategoryClick={() => setUi((prev) => ({ ...prev, isAddCategoryOpen: true }))}
            isExporting={false}
            isImporting={ui.isImporting}
          />

          <div className="min-h-30">
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
                color="bg-emerald-500/10 text-emerald-500"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
              <FormattedMetricCard
                title={t('dashboard:metrics.expenses', { period: periodName })}
                value={metrics.data?.expenses ?? 0}
                trend={metrics.data?.expensesTrend ?? 0}
                icon={TrendingDown}
                color="bg-rose-500/10 text-rose-500"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                invertedTrend
              />
              <FormattedMetricCard
                title={t('dashboard:metrics.savings')}
                value={metrics.data?.savings ?? 0}
                trend={metrics.data?.savingsTrend ?? 0}
                icon={PiggyBank}
                color="bg-amber-500/10 text-amber-500"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6">
            <div className="lg:col-span-8 min-h-87.5">
              <HistoryChart period={period} data={history.data} loading={history.isLoading} />
            </div>
            <div className="lg:col-span-4 min-h-87.5">
              <CategoryDonut data={categories.data} loading={categories.isLoading} />
            </div>
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6">
            <div className="lg:col-span-5 min-h-100">
              <BudgetBars data={budgets.data} loading={budgets.isLoading} />
            </div>
            <div className="lg:col-span-7 min-h-100">
              <RecentTransactions data={recentTransactions.data} loading={recentTransactions.isLoading} />
            </div>
          </div>
        </main>
      </div>
      <ImportModal 
        isOpen={ui.isImportOpen} 
        onClose={() => setUi((prev) => ({ ...prev, isImportOpen: false }))} 
        onImport={handleImport}
      />
      <ExportModal
        open={ui.isExportOpen}
        onOpenChange={(open) => setUi((prev) => ({ ...prev, isExportOpen: open }))}
      />
      <NewTransactionDialog open={ui.isAddOpen} onOpenChange={(open) => setUi((prev) => ({ ...prev, isAddOpen: open }))} />
      <AddCategoryDialog open={ui.isAddCategoryOpen} onOpenChange={(open) => setUi((prev) => ({ ...prev, isAddCategoryOpen: open }))} />
    </div>
  )
}