import type { LucideIcon } from 'lucide-react';

import {
  LayoutDashboard,
  ArrowLeftRight,
  PieChart,
  TrendingUp,
  LayoutGrid,
  PiggyBank,
} from 'lucide-react';

import { ROUTES } from './routes';

export interface NavItem {
  id: string;
  icon: LucideIcon;
  labelKey: string;
  path: string;
  enabled: boolean;
  section: 'main' | 'bottom';
  order: number;
}

const NAVIGATION_REGISTRY: NavItem[] = [
  {
    id: 'dashboard',
    icon: LayoutDashboard,
    labelKey: 'dashboard',
    path: ROUTES.dashboard,
    enabled: true,
    section: 'main',
    order: 1,
  },
  {
    id: 'categories',
    icon: LayoutGrid,
    labelKey: 'categories',
    path: ROUTES.categories,
    enabled: true,
    section: 'main',
    order: 2,
  },
  {
    id: 'transactions',
    icon: ArrowLeftRight,
    labelKey: 'transactions',
    path: ROUTES.transactions,
    enabled: true,
    section: 'main',
    order: 3,
  },
  {
    id: 'investments',
    icon: TrendingUp,
    labelKey: 'investments',
    path: ROUTES.investments,
    enabled: true,
    section: 'main',
    order: 4,
  },
  {
    id: 'savingsgoals',
    icon: PiggyBank,
    labelKey: 'savingsgoals',
    path: ROUTES.savingsGoals,
    enabled: true,
    section: 'main',
    order: 5,
  },
  {
    id: 'groupgoals',
    icon: PieChart,
    labelKey: 'groupgoals',
    path: ROUTES.groupGoals,
    enabled: true,
    section: 'main',
    order: 6,
  },
];

export function getEnabledNavItems(
  section: 'main' | 'bottom',
): NavItem[] {
  return NAVIGATION_REGISTRY
    .filter(
      (item) =>
        item.section === section &&
        item.enabled,
    )
    .sort((a, b) => a.order - b.order);
}

export function getDisabledNavItems(): NavItem[] {
  return NAVIGATION_REGISTRY.filter(
    (item) => !item.enabled,
  );
}