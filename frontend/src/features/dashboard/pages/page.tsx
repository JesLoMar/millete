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
import { useDashboardQueries } from '../hooks/useDashboardQueries'
import { Wallet, TrendingUp, TrendingDown, PiggyBank } from "lucide-react"
import { apiClient } from '@/shared/api/axiosClient'
import { notify } from '@/shared/utils/notifications/notify'

export const DashboardPage = () => {
  const { t } = useTranslation()
  const [period, setPeriod] = useState<PeriodFilter>("month")
  const [isImportOpen, setIsImportOpen] = useState(false)
  const [isExporting, setIsExporting] = useState(false)
  const [isImporting, setIsImporting] = useState(false)
  const [isAddOpen, setIsAddOpen] = useState(false)
  const [isAddCategoryOpen, setIsAddCategoryOpen] = useState(false)
  const queryClient = useQueryClient()

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  const { metrics, history, categories, budgets, recentTransactions } = useDashboardQueries(period)

  const handleImport = useCallback(async (file: File) => {
    setIsImporting(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      await apiClient.post('/data/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        skipGlobalErrorNotify: true
      })

      await queryClient.invalidateQueries()
      setIsImportOpen(false)
      notify.success(t('dashboard.import.success') || 'Datos importados correctamente')
    } catch (err) {
      const error = err as { response?: { status?: number; data?: { message?: string } } }
      let errorMessage = t('dashboard.import.errorGeneric') || 'Error al importar el archivo'
      
      if (error?.response?.status === 403) {
        errorMessage = error.response.data?.message || t('dashboard.import.errorProperty') || 'No tienes permiso para importar'
      } else if (error?.response?.status === 400) {
        errorMessage = error.response.data?.message || t('dashboard.import.errorFormat') || 'Formato de archivo inválido'
      }

      notify.error(errorMessage)
    } finally {
      setIsImporting(false)
    }
  }, [queryClient, t])

  const handleExport = useCallback(async () => {
    setIsExporting(true)
    try {
      const response = await apiClient.get('/data/export', { responseType: 'blob' })
      const blob = new Blob([response.data], { type: 'application/json' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'familybudget_export.json'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      notify.success(t('dashboard.export.success') || 'Copia de seguridad exportada correctamente')
    } catch {
      notify.error(t('dashboard.export.error') || 'Error al exportar los datos')
    } finally {
      setIsExporting(false)
    }
  }, [t])

  const periodLabel = t(`dashboard.metrics.vsLast${period === "week" ? "Week" : period === "month" ? "Month" : "Year"}`)

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 sm:space-y-6">
          <Header onPeriodChange={handlePeriodChange} defaultPeriod={period} />
          
          <QuickActions
            onImportClick={() => setIsImportOpen(true)}
            onExportClick={handleExport}
            onAddClick={() => setIsAddOpen(true)}
            onAddCategoryClick={() => setIsAddCategoryOpen(true)}
            isExporting={isExporting}
            isImporting={isImporting}
          />

          <div className="min-h-30">
            <div className="grid grid-cols-1 min-[390px]:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 w-full min-w-0">
              <FormattedMetricCard
                title={t("dashboard.metrics.balance")}
                value={metrics.data?.balance ?? 0}
                trend={metrics.data?.balanceTrend ?? 0}
                icon={Wallet}
                color="bg-primary/10 text-primary"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
              <FormattedMetricCard
                title={t("dashboard.metrics.income", { period: t(`dashboard.header.period.${period}`) })}
                value={metrics.data?.income ?? 0}
                trend={metrics.data?.incomeTrend ?? 0}
                icon={TrendingUp}
                color="bg-emerald-500/10 text-emerald-500"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
              />
              <FormattedMetricCard
                title={t("dashboard.metrics.expenses", { period: t(`dashboard.header.period.${period}`) })}
                value={metrics.data?.expenses ?? 0}
                trend={metrics.data?.expensesTrend ?? 0}
                icon={TrendingDown}
                color="bg-rose-500/10 text-rose-500"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                invertedTrend
              />
              <FormattedMetricCard
                title={t("dashboard.metrics.savings")}
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
        isOpen={isImportOpen} 
        onClose={() => setIsImportOpen(false)} 
        onImport={handleImport}
      />
      <NewTransactionDialog open={isAddOpen} onOpenChange={setIsAddOpen} />
      <AddCategoryDialog open={isAddCategoryOpen} onOpenChange={setIsAddCategoryOpen} />
    </div>
  )
}