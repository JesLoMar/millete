import { useState, useCallback } from "react"
import { useTranslation } from "react-i18next"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { Header, type PeriodFilter } from "@/shared/components/Header"
import { TransactionSummary } from "../components/TransactionSummary"
import { RecurringTransactionsList } from "../components/RecurringTransactionsList"
import { TransactionList } from "../components/TransactionList"
import { TransactionActions } from "../components/TransactionActions"

export const TransactionsPage = () => {
  const { t } = useTranslation("transactions")
  const [period, setPeriod] = useState<PeriodFilter>("month")

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

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
      <div className="flex-1 flex flex-col overflow-hidden pt-16">
        <TopNav desktopCenter={periodControls} />
        <main className="flex-1 space-y-4 overflow-y-auto p-4 sm:px-6 sm:py-3">
          <div className="lg:hidden">{periodControls}</div>
          <div className="grid min-w-0 gap-x-6 gap-y-6 md:grid-cols-2 xl:grid-cols-[minmax(0,5fr)_minmax(0,5fr)_minmax(12rem,2fr)]">
            <aside className="min-w-0 self-start md:col-span-2 xl:col-span-1 xl:col-start-3 xl:row-start-1 xl:border-s xl:border-border xl:ps-5">
              <div className="border-b border-border pb-4">
                <TransactionSummary period={period} />
              </div>
              <TransactionActions className="mt-3 flex-wrap" />
            </aside>
            <section aria-labelledby="transactions-list-title" className="min-w-0 space-y-3 xl:col-start-1 xl:row-start-1">
              <h1 id="transactions-list-title" className="font-serif text-lg font-semibold">{t("title")}</h1>
              <TransactionList period={period} />
            </section>
            <section aria-labelledby="recurring-list-title" className="min-w-0 space-y-3 md:border-s md:border-border md:ps-5 xl:col-start-2 xl:row-start-1">
              <h2 id="recurring-list-title" className="font-serif text-lg font-semibold">{t("recurring.listTitle")}</h2>
              <RecurringTransactionsList />
            </section>
          </div>
        </main>
      </div>
    </div>
  )
}
