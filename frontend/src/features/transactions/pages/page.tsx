import { useState, useCallback } from "react"
import { useTranslation } from "react-i18next"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { Header, type PeriodFilter } from "@/shared/components/Header"
import { PeriodSelector } from "@/shared/components/PeriodSelector"
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

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden pt-16">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 sm:space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
            <Header
              onPeriodChange={handlePeriodChange}
              defaultPeriod={period}
              hidePeriodSelector
            />
            <div className="flex flex-col xs:flex-row items-stretch xs:items-center gap-2 sm:gap-3 w-full sm:w-auto">
              <PeriodSelector
                period={period}
                onPeriodChange={handlePeriodChange}
                className="w-full xs:flex-1 sm:flex-none"
              />
              <TransactionActions className="w-full xs:w-auto" />
            </div>
          </div>
          <div className="grid min-w-0 gap-x-6 gap-y-6 md:grid-cols-2 xl:grid-cols-[minmax(0,5fr)_minmax(0,5fr)_minmax(12rem,2fr)]">
            <aside className="min-w-0 self-start border-b border-border pb-4 md:col-span-2 xl:col-span-1 xl:col-start-3 xl:row-start-1 xl:border-s xl:ps-5">
              <TransactionSummary period={period} />
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
