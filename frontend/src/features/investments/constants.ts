import { BarChart3, Bitcoin, Wallet, Building2, HelpCircle, type LucideIcon } from "lucide-react"

export const TYPE_COLORS: Record<string, string> = {
  STOCK: "bg-blue-500",
  CRYPTO: "bg-amber-500",
  FUND: "bg-emerald-500",
  REAL_ESTATE: "bg-rose-500",
  OTHER: "bg-slate-500",
}

export const INVESTMENT_TYPES: Array<{
  value: string
  labelKey: string
  icon: LucideIcon
  color: string
}> = [
  { value: "STOCK", labelKey: "investments.types.stock", icon: BarChart3, color: "text-blue-400" },
  { value: "CRYPTO", labelKey: "investments.types.crypto", icon: Bitcoin, color: "text-amber-400" },
  { value: "FUND", labelKey: "investments.types.fund", icon: Wallet, color: "text-emerald-400" },
  { value: "REAL_ESTATE", labelKey: "investments.types.realEstate", icon: Building2, color: "text-rose-400" },
  { value: "OTHER", labelKey: "investments.types.other", icon: HelpCircle, color: "text-slate-400" },
]