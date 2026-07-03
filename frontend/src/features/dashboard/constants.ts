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

export const CHART_COLORS = [
  "hsl(var(--chart-1))",
  "hsl(var(--chart-2))",
  "hsl(var(--chart-3))",
  "hsl(var(--chart-4))",
  "hsl(var(--chart-5))",
]

export const BUDGET_COLORS = [
  "bg-chart-2",
  "bg-chart-4",
  "bg-chart-5",
  "bg-chart-3",
  "bg-chart-5",
  "bg-chart-4",
]

export const CATEGORY_ICONS: Record<string, LucideIcon> = {
  Alimentación: Utensils,
  Hogar: Home,
  Transporte: Car,
  Suministros: Zap,
  Ocio: ShoppingCart,
}

export { CATEGORY_COLORS } from "@/shared/constants/categoryColors"

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
    color: "bg-warning/10 text-warning group-hover:bg-warning group-hover:text-warning-foreground",
    needsConfig: false,
  },
  {
    id: "csv",
    icon: FileSpreadsheet,
    labelKey: "export.formatCSV",
    descKey: "export.formatCSVDesc",
    color: "bg-primary/10 text-primary group-hover:bg-primary group-hover:text-primary-foreground",
    needsConfig: true,
  },
  {
    id: "pdf",
    icon: FileText,
    labelKey: "export.formatPDF",
    descKey: "export.formatPDFDesc",
    color: "bg-destructive/10 text-destructive group-hover:bg-destructive group-hover:text-destructive-foreground",
    needsConfig: true,
  },
]

export const EXPORT_ENTITY_TYPES = [
  { value: "categories", labelKey: "entities.categories" },
  { value: "transactions", labelKey: "entities.transactions" },
  { value: "investments", labelKey: "entities.investments" },
  { value: "savingsgoals", labelKey: "entities.savingsGoals" },
]

export const EXPORT_PERIOD_OPTIONS = [
  { value: "1m", labelKey: "export.period1m" },
  { value: "3m", labelKey: "export.period3m" },
  { value: "6m", labelKey: "export.period6m" },
  { value: "1y", labelKey: "export.period1y" },
]
