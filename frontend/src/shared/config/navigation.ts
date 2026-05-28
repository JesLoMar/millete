import type { LucideIcon } from "lucide-react"
import {
  LayoutDashboard,
  ArrowLeftRight,
  PieChart,
  FileText,
  Settings,
  HelpCircle,
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
  },
  {
    id: "reports",
    icon: FileText,
    labelKey: "sidebar.reports",
    path: "/reports",
    enabled: false,       // ⏳ Pendiente
    section: "main",
    order: 6,
  },

  // ─── Bottom Navigation ─────────────────────────────────
  {
    id: "settings",
    icon: Settings,
    labelKey: "sidebar.settings",
    path: "/settings",
    enabled: false,       // ⏳ Pendiente
    section: "bottom",
    order: 1,
  },
  {
    id: "help",
    icon: HelpCircle,
    labelKey: "sidebar.help",
    path: "/help",
    enabled: false,       // ⏳ Pendiente
    section: "bottom",
    order: 2,
  },
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