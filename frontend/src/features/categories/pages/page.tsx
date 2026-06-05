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
        <div className="flex min-h-dvh overflow-hidden bg-background">
            <Sidebar />
            <div className="flex-1 flex flex-col overflow-hidden">
                <TopNav />
                <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 sm:space-y-6">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
                        <Header
                            onPeriodChange={handlePeriodChange}
                            defaultPeriod={period}
                            hidePeriodSelector
                        />
                        <div className="flex items-center gap-2 sm:gap-3 w-full sm:w-auto">
                            <PeriodSelector 
                                period={period} 
                                onPeriodChange={handlePeriodChange}
                                className="flex-1 sm:flex-none"
                            />
                            <AddCategoryDialog />
                        </div>
                    </div>

                    <CategoryTable period={period} />
                </main>
            </div>
        </div>
    )
}