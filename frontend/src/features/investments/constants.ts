import {
  BarChart3,
  Bitcoin,
  Building2,
  HelpCircle,
  Wallet,
  type LucideIcon,
} from 'lucide-react'

import type { InvestmentAssetType } from './types'

export const TYPE_COLORS: Record<InvestmentAssetType, string> = {
  STOCK: 'bg-chart-4',
  CRYPTO: 'bg-chart-3',
  FUND: 'bg-chart-2',
  REAL_ESTATE: 'bg-destructive',
  OTHER: 'bg-muted-foreground',
}

export const INVESTMENT_TYPES: Array<{
  value: InvestmentAssetType
  labelKey: string
  icon: LucideIcon
  color: string
}> = [
  {
    value: 'STOCK',
    labelKey: 'investments:types.stock',
    icon: BarChart3,
    color: 'text-chart-4',
  },
  {
    value: 'CRYPTO',
    labelKey: 'investments:types.crypto',
    icon: Bitcoin,
    color: 'text-chart-3',
  },
  {
    value: 'FUND',
    labelKey: 'investments:types.fund',
    icon: Wallet,
    color: 'text-chart-2',
  },
  {
    value: 'REAL_ESTATE',
    labelKey: 'investments:types.realEstate',
    icon: Building2,
    color: 'text-destructive',
  },
  {
    value: 'OTHER',
    labelKey: 'investments:types.other',
    icon: HelpCircle,
    color: 'text-muted-foreground',
  },
]