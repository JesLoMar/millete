import { useCallback, useReducer, useState } from 'react'
import * as m from 'framer-motion/m';
import { useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { Header, type PeriodFilter } from '@/shared/components/Header'
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
import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat'
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

const metricCards = [
  { key: 'balance', trend: 'balanceTrend' },
  { key: 'income', trend: 'incomeTrend' },
  { key: 'expenses', trend: 'expensesTrend' },
  { key: 'savings', trend: 'savingsTrend' },
] as const

export const DashboardPage = () => {
  const { t } = useTranslation('dashboard')
  const [period, setPeriod] = useState<PeriodFilter>('month')
  const [view, setView] = useState<'history' | 'categories'>('history')
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
          className="flex min-w-0 flex-1 flex-col gap-6 px-4 pt-20 pb-4 sm:gap-8 sm:px-6 sm:pt-22 sm:pb-6 lg:gap-0"
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

          <section className="flex min-h-[calc(100svh-1.25rem)] min-w-0 flex-col sm:min-h-[calc(100svh-.75rem)] lg:grid lg:min-h-0 lg:grid-cols-[minmax(0,1fr)_clamp(13rem,15vw,18rem)] lg:grid-rows-[auto_auto] lg:gap-x-6">
            <div className="contents lg:col-start-2 lg:row-start-1 lg:flex lg:min-w-0 lg:flex-col lg:gap-4 lg:border-s lg:border-border lg:ps-6 lg:pt-4">
              <div className="mb-6 sm:mb-8 lg:order-2 lg:mb-0">
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
              <dl className="grid min-w-0 grid-cols-2 gap-x-4 gap-y-2 border-b border-border pb-2 sm:grid-cols-4 sm:gap-x-6 lg:order-1 lg:grid-cols-1 lg:gap-y-4 lg:pb-5" aria-busy={metrics.isLoading}>
                {metricCards.map(({ key, trend }) => {
                  const change = metrics.data?.[trend] ?? 0
                  const favorable = key === 'expenses' ? change < 0 : change > 0
                  const changeColor = change === 0
                    ? 'text-muted-foreground'
                    : favorable ? 'text-financial-positive' : 'text-financial-negative'
                  return (
                    <div key={key} className="min-w-0">
                      <dt className="text-xs font-medium text-muted-foreground lg:text-sm">
                        {t(`metrics.${key}`, { period: periodName })}
                      </dt>
                      <dd className="min-w-0 text-sm leading-tight font-semibold tabular-nums lg:text-2xl">
                        {metrics.isLoading ? (
                          <span aria-hidden="true" className="block h-8 w-24 animate-pulse rounded bg-muted/50 motion-reduce:animate-none" />
                        ) : (
                          <>
                            <span className="block wrap-break-word">{formatCurrency(metrics.data?.[key] ?? 0)}</span>
                            <span className="flex flex-wrap gap-x-1 text-[11px] font-normal lg:text-sm">
                              <span className={changeColor}>
                                {formatNumber(change / 100, { style: 'percent', signDisplay: 'exceptZero', maximumFractionDigits: 1 })}
                              </span>
                              <span className="text-muted-foreground">{periodLabel}</span>
                            </span>
                          </>
                        )}
                      </dd>
                    </div>
                  )
                })}
              </dl>
            </div>

            <div className="flex min-w-0 flex-1 flex-col lg:col-start-1 lg:row-start-1 lg:h-[clamp(25rem,60svh,42rem)]">
              {view === 'history' ? (
                <HistoryChart period={period} data={history.data} loading={history.isLoading} />
              ) : (
                <div className="grid min-w-0 flex-1 grid-cols-1 divide-y divide-border md:grid-cols-2 md:divide-x md:divide-y-0">
                  <div className="min-w-0">
                    <CategoryDonut data={categories.data?.income} loading={categories.isLoading}
                      title={t('donut.incomeTitle')} emptyText={t('donut.incomeEmpty')} />
                  </div>
                  <div className="min-w-0">
                    <CategoryDonut data={categories.data?.expenses} loading={categories.isLoading} />
                  </div>
                </div>
              )}
            </div>

            <div className="flex justify-center lg:col-span-2 lg:row-start-2">
              {(['history', 'categories'] as const).map((option) => {
                const label = t(option === 'history' ? 'chart.title' : 'donut.view')
                return (
                  <button key={option} type="button" aria-label={label} title={label} aria-pressed={view === option}
                    className="group grid size-11 place-items-center rounded-full focus-visible:outline-2 focus-visible:outline-ring"
                    onClick={() => setView(option)}>
                    <span className="size-2.5 rounded-full bg-muted-foreground/40 group-hover:bg-muted-foreground group-aria-pressed:bg-primary" />
                  </button>
                )
              })}
            </div>
          </section>

          <m.div
            className="grid min-w-0 grid-cols-1 gap-4 sm:gap-6 lg:ms-auto lg:w-[clamp(42rem,36%,52rem)] lg:grid-cols-2 lg:gap-4"
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
            <div className="min-h-96 min-w-0 lg:min-h-0 lg:border-s lg:border-border lg:ps-4">
              <BudgetBars
                data={budgets.data}
                loading={budgets.isLoading}
                period={period}
              />
            </div>

            <div className="min-h-96 min-w-0 lg:min-h-0 lg:border-s lg:border-border lg:ps-4">
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
