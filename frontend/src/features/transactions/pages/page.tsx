import { useState, useCallback } from "react"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { Header, type PeriodFilter } from "@/shared/components/Header"
import { PeriodSelector } from "@/shared/components/PeriodSelector"
import { TransactionSummary } from "../components/TransactionSummary"
import { RecurringTransactionsList } from "../components/RecurringTransactionsList"
import { TransactionList } from "../components/TransactionList"
import { TransactionActions } from "../components/TransactionActions"

export const TransactionsPage = () => {
  const [period, setPeriod] = useState<PeriodFilter>("month")

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-6 space-y-6">
          {/* Header + Selector centrado + Acciones */}
          <div className="flex items-center justify-between gap-4">
            <Header 
              onPeriodChange={handlePeriodChange} 
              defaultPeriod={period} 
              hidePeriodSelector 
            />
            <PeriodSelector period={period} onPeriodChange={handlePeriodChange} />
            <TransactionActions />
          </div>

          <TransactionSummary period={period} />
          <TransactionList period={period} />
          <RecurringTransactionsList />
        </main>
      </div>
    </div>
  )
}