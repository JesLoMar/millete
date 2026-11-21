import { useCallback, useReducer, useState } from 'react'
import * as m from 'framer-motion/m';
import { useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import {
  PiggyBank,
  TrendingDown,
  TrendingUp,
  Wallet,
} from 'lucide-react'

import { Header, type PeriodFilter } from '@/shared/components/Header'
import { FormattedMetricCard } from '@/shared/components/FormattedMetricCard'
import { Sidebar } from '@/shared/components/Sidebar'
import { TopNav } from '@/shared/components/TopNav'

import { NewTransactionDialog } from '@/features/transactions/components/dialogs/NewTransactionDialog'
import { AddCategoryDialog } from '@/features/categories/components/AddCategoryDialog'

import { BudgetBars } from '../components/BudgetBars'
import { CategoryDonut } from '../components/CategoryDonut'
import { ExportModal } from '../components/ExportModal'
import { HistoryChart } from '../components/HistoryChart'
import { ImportModal } from '../components/ImportModal'
import { QuickActions } from '../components/QuickActions'
import { RecentTransactions } from '../components/RecentTransactions'
import { useDashboardQueries } from '../hooks/useDashboardQueries'

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
  | {
      type: 'OPEN_MODAL'
      modal: 'import' | 'export' | 'add' | 'addCategory'
    }
  | {
      type: 'CLOSE_MODAL'
      modal: 'import' | 'export' | 'add' | 'addCategory'
    }
  | {
      type: 'SET_IMPORTING'
      value: boolean
    }

function uiReducer(
  state: UIState,
  action: UIAction
): UIState {
  switch (action.type) {
    case 'OPEN_MODAL':
      return {
        ...state,
        [`is${capitalize(action.modal)}Open`]: true,
      }

    case 'CLOSE_MODAL':
      return {
        ...state,
        [`is${capitalize(action.modal)}Open`]: false,
      }

    case 'SET_IMPORTING':
      return {
        ...state,
        isImporting: action.value,
      }

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
  const { t } = useTranslation('dashboard')
  const [period, setPeriod] = useState<PeriodFilter>('month')
  const [ui, dispatch] = useReducer(uiReducer, initialUIState)
  const queryClient = useQueryClient()

  const handlePeriodChange = useCallback(
    (newPeriod: PeriodFilter) => {
      setPeriod(newPeriod)
    },
    []
  )

  const {
    metrics,
    history,
    categories,
    budgets,
    recentTransactions,
  } = useDashboardQueries(period)

  const handleImport = useCallback(
    async (file: File) => {
      dispatch({
        type: 'SET_IMPORTING',
        value: true,
      })

      try {
        const formData = new FormData()
        formData.append('file', file)

        await apiClient.post('/data/import', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          skipGlobalErrorNotify: true,
        })

        await queryClient.invalidateQueries()

        dispatch({
          type: 'CLOSE_MODAL',
          modal: 'import',
        })

        notify.success(
          t('dashboard:import.success') ||
            'Datos importados correctamente'
        )
      } catch (err) {
        const error = err as {
          response?: {
            status?: number
            data?: {
              message?: string
            }
          }
        }

        let errorMessage =
          t('dashboard:import.errorGeneric') ||
          'Error al importar el archivo'

        if (error?.response?.status === 403) {
          errorMessage =
            error.response.data?.message ||
            t('dashboard:import.errorProperty') ||
            'No tienes permiso para importar'
        } else if (error?.response?.status === 400) {
          errorMessage =
            error.response.data?.message ||
            t('dashboard:import.errorFormat') ||
            'Formato de archivo inválido'
        }

        notify.error(errorMessage)
      } finally {
        dispatch({
          type: 'SET_IMPORTING',
          value: false,
        })
      }
    },
    [queryClient, t]
  )

  const handleExportClick = useCallback(() => {
    dispatch({
      type: 'OPEN_MODAL',
      modal: 'export',
    })
  }, [])

  const periodLabel = t(
    `dashboard:metrics.vsLast${
      period === 'week'
        ? 'Week'
        : period === 'month'
          ? 'Month'
          : 'Year'
    }`
  )

  const periodName = t(
    `dashboard:header.period.${period}`
  )
  const periodControls = (
    <Header
      className="lg:max-xl:[&_.no-scrollbar]:flex-wrap lg:max-xl:[&_.no-scrollbar]:gap-x-1 lg:max-xl:[&_.no-scrollbar]:gap-y-0 lg:max-xl:[&_.no-scrollbar]:text-xs lg:max-xl:[&_fieldset_button]:min-w-14 lg:max-xl:[&_fieldset_button]:px-2 lg:max-xl:[&_fieldset_button]:text-xs"
      onPeriodChange={handlePeriodChange}
      defaultPeriod={period}
    />
  )

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />

      <div className="flex-1 flex min-w-0 flex-col overflow-hidden">
        <TopNav desktopCenter={periodControls} />

        <m.main
          className="flex min-w-0 flex-1 flex-col gap-6 px-4 pt-20 pb-4 sm:gap-8 sm:px-6 sm:pt-22 sm:pb-6 lg:grid lg:grid-cols-[minmax(0,1fr)_clamp(15rem,18vw,21rem)] lg:grid-rows-[minmax(55dvh,auto)_auto] lg:pb-24"
          initial="hidden"
          animate="visible"
          variants={{
            hidden: {
              opacity: 0,
            },
            visible: {
              opacity: 1,
              transition: {
                staggerChildren: 0.05,
              },
            },
          }}
        >
          <div className="lg:hidden">{periodControls}</div>

          <div className="lg:fixed lg:right-6 lg:bottom-6 lg:z-40 lg:rounded-lg lg:border lg:border-border lg:bg-card lg:p-1 lg:shadow-md">
            <QuickActions
              onImportClick={() =>
                dispatch({
                  type: 'OPEN_MODAL',
                  modal: 'import',
                })
              }
              onExportClick={handleExportClick}
              onAddClick={() =>
                dispatch({
                  type: 'OPEN_MODAL',
                  modal: 'add',
                })
              }
              onAddCategoryClick={() =>
                dispatch({
                  type: 'OPEN_MODAL',
                  modal: 'addCategory',
                })
              }
              isExporting={false}
              isImporting={ui.isImporting}
            />
          </div>

          <m.div
            className="min-h-32 min-w-0 lg:col-start-2 lg:row-span-2 lg:row-start-1"
            variants={{
              hidden: {
                opacity: 0,
                y: -20,
              },
              visible: {
                opacity: 1,
                y: 0,
              },
            }}
          >
            <div className="grid w-full min-w-0 grid-cols-1 gap-3 min-[390px]:grid-cols-2 sm:gap-4 lg:grid-cols-1">
              <FormattedMetricCard
                title={t('dashboard:metrics.balance')}
                value={metrics.data?.balance ?? 0}
                trend={metrics.data?.balanceTrend ?? 0}
                icon={Wallet}
                color="bg-primary/10 text-primary"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                className="rounded-none border-0 bg-transparent shadow-none transition-none hover:translate-y-0 hover:border-transparent hover:shadow-none"
              />

              <FormattedMetricCard
                title={t('dashboard:metrics.income', {
                  period: periodName,
                })}
                value={metrics.data?.income ?? 0}
                trend={metrics.data?.incomeTrend ?? 0}
                icon={TrendingUp}
                color="bg-primary/10 text-primary"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                className="rounded-none border-0 bg-transparent shadow-none transition-none hover:translate-y-0 hover:border-transparent hover:shadow-none"
              />

              <FormattedMetricCard
                title={t('dashboard:metrics.expenses', {
                  period: periodName,
                })}
                value={metrics.data?.expenses ?? 0}
                trend={metrics.data?.expensesTrend ?? 0}
                icon={TrendingDown}
                color="bg-destructive/10 text-destructive"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                invertedTrend
                className="rounded-none border-0 bg-transparent shadow-none transition-none hover:translate-y-0 hover:border-transparent hover:shadow-none"
              />

              <FormattedMetricCard
                title={t('dashboard:metrics.savings')}
                value={metrics.data?.savings ?? 0}
                trend={metrics.data?.savingsTrend ?? 0}
                icon={PiggyBank}
                color="bg-warning/10 text-warning"
                periodLabel={periodLabel}
                loading={metrics.isLoading}
                className="rounded-none border-0 bg-transparent shadow-none transition-none hover:translate-y-0 hover:border-transparent hover:shadow-none"
              />
            </div>
          </m.div>

          <m.div
            className="grid min-w-0 grid-cols-1 gap-4 sm:gap-6 lg:col-start-1 lg:row-start-1 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]"
            variants={{
              hidden: {
                opacity: 0,
                y: -20,
              },
              visible: {
                opacity: 1,
                y: 0,
              },
            }}
          >
            <div className="min-h-96 min-w-0">
              <HistoryChart
                period={period}
                data={history.data}
                loading={history.isLoading}
              />
            </div>

            <div className="min-h-96 min-w-0">
              <CategoryDonut
                data={categories.data}
                loading={categories.isLoading}
              />
            </div>
          </m.div>

          <m.div
            className="grid min-w-0 grid-cols-1 gap-4 sm:gap-6 lg:col-start-1 lg:row-start-2 lg:grid-cols-2"
            variants={{
              hidden: {
                opacity: 0,
                y: -20,
              },
              visible: {
                opacity: 1,
                y: 0,
              },
            }}
          >
            <div className="min-h-96 min-w-0">
              <BudgetBars
                data={budgets.data}
                loading={budgets.isLoading}
                period={period}
              />
            </div>

            <div className="min-h-96 min-w-0">
              <RecentTransactions
                data={recentTransactions.data}
                loading={recentTransactions.isLoading}
              />
            </div>
          </m.div>
        </m.main>
      </div>

      {ui.isImportOpen && (
        <ImportModal
          isOpen={true}
          onClose={() =>
            dispatch({
              type: 'CLOSE_MODAL',
              modal: 'import',
            })
          }
          onImport={handleImport}
        />
      )}

      {ui.isExportOpen && (
        <ExportModal
          open={true}
          onOpenChange={(open) =>
            dispatch({
              type: open ? 'OPEN_MODAL' : 'CLOSE_MODAL',
              modal: 'export',
            })
          }
        />
      )}

      {ui.isAddOpen && (
        <NewTransactionDialog
          open={true}
          onOpenChange={(open) =>
            dispatch({
              type: open ? 'OPEN_MODAL' : 'CLOSE_MODAL',
              modal: 'add',
            })
          }
        />
      )}

      {ui.isAddCategoryOpen && (
        <AddCategoryDialog
          open={true}
          onOpenChange={(open) =>
            dispatch({
              type: open ? 'OPEN_MODAL' : 'CLOSE_MODAL',
              modal: 'addCategory',
            })
          }
        />
      )}
    </div>
  )
}
