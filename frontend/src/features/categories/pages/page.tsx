import { useState } from 'react';

import { CategoryTable } from '@/features/categories/components/CategoryTable';
import { Header } from '@/shared/components/Header';
import type { PeriodFilter } from '@/shared/components/PeriodSelector';
import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';

export const CategoriesPage = () => {
  const [period, setPeriod] = useState<PeriodFilter>('month');
  const periodControls = (
    <Header
      className="lg:max-xl:[&_.no-scrollbar]:flex-wrap lg:max-xl:[&_.no-scrollbar]:gap-x-1 lg:max-xl:[&_.no-scrollbar]:gap-y-0 lg:max-xl:[&_.no-scrollbar]:text-xs lg:max-xl:[&_fieldset_button]:min-w-14 lg:max-xl:[&_fieldset_button]:px-2 lg:max-xl:[&_fieldset_button]:text-xs"
      onPeriodChange={setPeriod}
      defaultPeriod={period}
    />
  );

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />

      <div className="flex flex-1 flex-col overflow-hidden pt-16">
        <TopNav className="border-b border-border/60" desktopCenter={periodControls} />

        <main className="flex-1 space-y-4 overflow-y-auto p-4 sm:px-6 sm:py-3">
          <div className="lg:hidden">{periodControls}</div>
          <CategoryTable period={period} />
        </main>
      </div>
    </div>
  );
};
