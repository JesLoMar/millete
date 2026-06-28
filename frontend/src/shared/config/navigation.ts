import type { LucideIcon } from "lucide-react"
import {
  LayoutDashboard,
  ArrowLeftRight,
  PieChart,
  TrendingUp,
  LayoutGrid,
  PiggyBank
} from "lucide-react"

// ─── Types ────────────────────────────────────────────────
export interface NavItem {
  id: string
  icon: LucideIcon
  labelKey: string
  path: string
  enabled: boolean
  section: "main" | "bottom"
  order: number
}

const NAVIGATION_REGISTRY: NavItem[] = [
  // ─── Main Navigation ───────────────────────────────────
  {
    id: "dashboard",
    icon: LayoutDashboard,
    labelKey: "dashboard",
    path: "/dashboard",
    enabled: true,
    section: "main",
    order: 1,
  },
  {
    id: "categories",
    icon: LayoutGrid,
    labelKey: "categories",
    path: "/categories",
    enabled: true,
    section: "main",
    order: 2,
  },
  {
    id: "transactions",
    icon: ArrowLeftRight,
    labelKey: "transactions",
    path: "/transactions",
    enabled: true,
    section: "main",
    order: 3,
  },
  {
    id: "investments",
    icon: TrendingUp,
    labelKey: "investments",
    path: "/investments",
    enabled: true,
    section: "main",
    order: 4,
  },
  {
    id: "savingsgoals",
    icon: PiggyBank,
    labelKey: "savingsgoals",
    path: "/savings-goals",
    enabled: true,
    section: "main",
    order: 5,
  },
  {
    id: "groupgoals",
    icon: PieChart,
    labelKey: "groupgoals",
    path: "/group-goals",
    enabled: true,
    section: "main",
    order: 6,
  }
]

// ─── Helpers ──────────────────────────────────────────────
export function getEnabledNavItems(section: "main" | "bottom"): NavItem[] {
  return NAVIGATION_REGISTRY
    .filter(item => item.section === section && item.enabled)
    .sort((a, b) => a.order - b.order)
}

export function getDisabledNavItems(): NavItem[] {
  return NAVIGATION_REGISTRY.filter(item => !item.enabled)
}
