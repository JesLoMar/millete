import { useTranslation } from 'react-i18next';
import {
  Activity,
  BarChart3,
  BriefcaseBusiness,
  Database,
  Layers,
  WalletCards,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

import { cn } from '@/lib/utils';

export type InvestmentTab =
  | 'portfolio'
  | 'activity'
  | 'performance'
  | 'assets'
  | 'holdings'
  | 'status';

const TABS: Array<{
  id: InvestmentTab;
  labelKey: string;
  icon: LucideIcon;
}> = [
  {
    id: 'portfolio',
    labelKey: 'tabs.portfolio',
    icon: BriefcaseBusiness,
  },
  {
    id: 'activity',
    labelKey: 'tabs.activity',
    icon: Activity,
  },
  {
    id: 'performance',
    labelKey: 'tabs.performance',
    icon: BarChart3,
  },
  {
    id: 'assets',
    labelKey: 'tabs.assets',
    icon: Layers,
  },
  {
    id: 'holdings',
    labelKey: 'tabs.holdings',
    icon: WalletCards,
  },
  {
    id: 'status',
    labelKey: 'tabs.status',
    icon: Database,
  },
];

interface InvestmentTabsProps {
  active: InvestmentTab;
  onChange: (tab: InvestmentTab) => void;
}

export function InvestmentTabs({
  active,
  onChange,
}: InvestmentTabsProps) {
  const { t } = useTranslation('investments');

  return (
    <div
      className="flex gap-1 overflow-x-auto rounded-lg border border-border bg-card p-1 no-scrollbar"
      role="tablist"
      aria-label={t('title')}
    >
      {TABS.map((tab) => {
        const Icon = tab.icon;
        const isActive = active === tab.id;

        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            aria-selected={isActive}
            onClick={() => onChange(tab.id)}
            className={cn(
              'inline-flex shrink-0 items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors',
              isActive
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground',
            )}
          >
            <Icon className="size-4" aria-hidden="true" />
            {t(tab.labelKey)}
          </button>
        );
      })}
    </div>
  );
}
