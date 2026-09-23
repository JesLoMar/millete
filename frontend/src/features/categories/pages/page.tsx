import { useState } from 'react';

import { AddCategoryDialog } from '@/features/categories/components/AddCategoryDialog';
import { CategoryTable } from '@/features/categories/components/CategoryTable';
import { Header } from '@/shared/components/Header';
import {
  PeriodSelector,
  type PeriodFilter,
} from '@/shared/components/PeriodSelector';
import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';

export const CategoriesPage = () => {
  const [period, setPeriod] = useState<PeriodFilter>('month');

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />

      <div className="flex flex-1 flex-col overflow-hidden pt-16">
        <TopNav />

        <main className="flex-1 space-y-4 overflow-y-auto p-4 sm:space-y-6 sm:p-6">
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center sm:gap-4">
            <Header
              onPeriodChange={setPeriod}
              defaultPeriod={period}
              hidePeriodSelector
            />

            <div className="flex w-full flex-col items-stretch gap-2 sm:w-auto sm:flex-row sm:items-center sm:gap-3">
              <PeriodSelector
                period={period}
                onPeriodChange={setPeriod}
                className="w-full sm:flex-none"
              />

              <div className="flex w-full flex-col sm:w-auto">
                <AddCategoryDialog />
              </div>
            </div>
          </div>

          <CategoryTable period={period} />
        </main>
      </div>
    </div>
  );
};