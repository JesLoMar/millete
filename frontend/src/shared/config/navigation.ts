import type { LucideIcon } from "lucide-react"
import {
  LayoutDashboard,
  ArrowLeftRight,
  PieChart,
  TrendingUp,
  LayoutGrid
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

// ─── Navigation Registry ──────────────────────────────────
// Para añadir una nueva pestaña:
// 1. Añade el objeto aquí
// 2. Cambia enabled: false → true cuando esté lista
// 3. El sidebar la mostrará automáticamente

export const NAVIGATION_REGISTRY: NavItem[] = [
  // ─── Main Navigation ───────────────────────────────────
  {
    id: "dashboard",
    icon: LayoutDashboard,
    labelKey: "sidebar.dashboard",
    path: "/dashboard",
    enabled: true,        // ✅ Implementado
    section: "main",
    order: 1,
  },
  {
    id: "transactions",
    icon: ArrowLeftRight,
    labelKey: "sidebar.transactions",
    path: "/transactions",
    enabled: true,       // ✅ Implementado
    section: "main",
    order: 2,
  },
  {
    id: "categories",
    icon: LayoutGrid,
    labelKey: "sidebar.categories",
    path: "/categories",
    enabled: true,      //✅ Implementado
    section: "main",
    order: 3,
  },
  {
    id: "investments",
    icon: TrendingUp,
    labelKey: "sidebar.investments",
    path: "/investments",
    enabled: true,       //✅ Implementado
    section: "main",
    order: 4,
  },
  {
    id: "family",
    icon: PieChart,
    labelKey: "sidebar.family",
    path: "/family",
    enabled: true,       //✅ Implementado
    section: "main",
    order: 5,
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