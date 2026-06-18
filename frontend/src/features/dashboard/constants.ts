import {
  Utensils,
  Home,
  Car,
  Zap,
  ShoppingCart,
  FileDown,
  FolderArchive,
  FileSpreadsheet,
  FileText,
  type LucideIcon,
} from "lucide-react"

// ─── Gráficos ─────────────────────────────────────────────
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

// ─── Categorías ───────────────────────────────────────────
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

// ─── Exportación ──────────────────────────────────────────
export type ExportFormat = "json" | "zip" | "csv" | "pdf"

export interface ExportFormatOption {
  id: ExportFormat
  icon: LucideIcon
  labelKey: string
  descKey: string
  color: string
  needsConfig: boolean
}

export const EXPORT_FORMATS: ExportFormatOption[] = [
  {
    id: "json",
    icon: FileDown,
    labelKey: "export.formatJSON",
    descKey: "export.formatJSONDesc",
    color: "bg-primary/10 text-primary group-hover:bg-primary group-hover:text-primary-foreground",
    needsConfig: false,
  },
  {
    id: "zip",
    icon: FolderArchive,
    labelKey: "export.formatZIP",
    descKey: "export.formatZIPDesc",
    color: "bg-amber-500/10 text-amber-500 group-hover:bg-amber-500 group-hover:text-white",
    needsConfig: false,
  },
  {
    id: "csv",
    icon: FileSpreadsheet,
    labelKey: "export.formatCSV",
    descKey: "export.formatCSVDesc",
    color: "bg-emerald-500/10 text-emerald-500 group-hover:bg-emerald-500 group-hover:text-white",
    needsConfig: true,
  },
  {
    id: "pdf",
    icon: FileText,
    labelKey: "export.formatPDF",
    descKey: "export.formatPDFDesc",
    color: "bg-rose-500/10 text-rose-500 group-hover:bg-rose-500 group-hover:text-white",
    needsConfig: true,
  },
]

export const EXPORT_ENTITY_TYPES = [
  { value: "categories", labelKey: "entities.categories" },
  { value: "transactions", labelKey: "entities.transactions" },
  { value: "investments", labelKey: "entities.investments" },
  { value: "savingsgoals", labelKey: "entities.savingsGoals" },
  { value: "groupgoals", labelKey: "entities.groupGoals" },
]

export const EXPORT_PERIOD_OPTIONS = [
  { value: "1m", labelKey: "export.period1m" },
  { value: "3m", labelKey: "export.period3m" },
  { value: "6m", labelKey: "export.period6m" },
  { value: "1y", labelKey: "export.period1y" },
  { value: "all", labelKey: "export.periodAll" },
]