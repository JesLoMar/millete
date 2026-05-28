import { useState, useCallback } from "react"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { AddCategoryDialog } from "@/features/categories/components/AddCategoryDialog"
import { CategoryTable } from "@/features/categories/components/CategoryTable"
import { Header } from "@/shared/components/Header"
import { PeriodSelector, type PeriodFilter } from "@/shared/components/PeriodSelector"

export const CategoriesPage = () => {
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
                    <div className="flex items-center justify-between gap-4">
                        <Header
                            onPeriodChange={handlePeriodChange}
                            defaultPeriod={period}
                            hidePeriodSelector
                        />
                        <PeriodSelector period={period} onPeriodChange={handlePeriodChange} />
                        <AddCategoryDialog />
                    </div>

                    <CategoryTable period={period} />
                </main>
            </div>
        </div>
    )
}