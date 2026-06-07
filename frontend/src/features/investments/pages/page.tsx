import { useState, useCallback } from "react"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { Header } from "@/shared/components/Header"
import { PeriodSelector, type PeriodFilter } from "@/shared/components/PeriodSelector"
import { InvestmentMetrics } from "../components/InvestmentMetrics"
import { EvolutionChart } from "../components/EvolutionChart"
import { DistributionChart } from "../components/DistributionChart"
import { AssetList } from "../components/AssetList"
import { NewInvestmentDialog } from "../components/NewInvestmentDialog"
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { useInvestmentQueries } from "../hooks/useInvestmentQueries"
import { useInvestmentMutations } from "../hooks/useInvestmentMutations"
import type { InvestmentResponse } from "../types"

export const InvestmentsPage = () => {
  const [period, setPeriod] = useState<PeriodFilter>("month")
  const [deletingInvestment, setDeletingInvestment] = useState<InvestmentResponse | null>(null)

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  const { investments, metrics, evolution, distribution } = useInvestmentQueries(period)
  const { deleteInvestment } = useInvestmentMutations()

  const handleDelete = async () => {
    if (!deletingInvestment) return

    try {
      await deleteInvestment.mutateAsync(deletingInvestment.id)
      setDeletingInvestment(null)
    } catch (err) {
      console.error("Error al eliminar inversión:", err)
    }
  }

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 sm:space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
            <Header onPeriodChange={handlePeriodChange} defaultPeriod={period} hidePeriodSelector />
            
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 sm:gap-3 w-full sm:w-auto">
              <PeriodSelector 
                period={period} 
                onPeriodChange={handlePeriodChange}
                className="w-full sm:flex-none"
              />
              <div className="w-full sm:w-auto flex flex-col">
                <NewInvestmentDialog />
              </div>
            </div>
          </div>

          <InvestmentMetrics data={metrics.data} isLoading={metrics.isLoading} period={period} />
          
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6">
            <div className="lg:col-span-8">
              <EvolutionChart data={evolution.data} isLoading={evolution.isLoading} />
            </div>
            <div className="lg:col-span-4">
              <DistributionChart data={distribution.data} isLoading={distribution.isLoading} />
            </div>
          </div>
          
          <AssetList
            investments={investments.data || []}
            isLoading={investments.isLoading}
            onDelete={setDeletingInvestment}
          />
        </main>
      </div>

      <ConfirmDeletionDialog
        open={!!deletingInvestment}
        onOpenChange={(open) => { if (!open) setDeletingInvestment(null) }}
        itemName={deletingInvestment?.assetName || ""}
        onConfirm={handleDelete}
      />
    </div>
  )
}