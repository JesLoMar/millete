import { Utensils, Home, Car, Zap, ShoppingCart, type LucideIcon } from "lucide-react"

export const CHART_COLORS = [
  "hsl(var(--chart-1))",
  "hsl(var(--chart-2))",
  "hsl(var(--chart-3))",
  "hsl(var(--chart-4))",
  "hsl(var(--chart-5))",
]

export const BUDGET_COLORS = [
  "bg-emerald-500",
  "bg-blue-500",
  "bg-pink-500",
  "bg-amber-500",
  "bg-purple-500",
  "bg-cyan-500",
]

export const CATEGORY_ICONS: Record<string, LucideIcon> = {
  Alimentación: Utensils,
  Hogar: Home,
  Transporte: Car,
  Suministros: Zap,
  Ocio: ShoppingCart,
}

export const CATEGORY_COLORS: Record<string, string> = {
  Alimentación: "text-emerald-500 bg-emerald-500/10",
  Hogar: "text-amber-500 bg-amber-500/10",
  Transporte: "text-blue-500 bg-blue-500/10",
  Suministros: "text-purple-500 bg-purple-500/10",
  Ocio: "text-pink-500 bg-pink-500/10",
}