export type InvestmentAssetType = 'STOCK' | 'CRYPTO' | 'REAL_ESTATE' | 'OTHER';

export interface InvestmentResponse {
  id: string
  userId: string
  assetName: string
  ticker?: string
  type: string
  quantity: number
  purchasePrice: number
  currentPrice: number
  currentValue: number
  investedCapital: number
  profitOrLoss: number
  roiPercentage: number
  purchaseDate: string
  active: boolean
}

export interface RegisterInvestmentRequest {
  name: string
  tickerSymbol?: string
  assetType: InvestmentAssetType
  quantity: number
  purchasePrice: number
  purchaseDate: string
}

export interface UpdateInvestmentPriceRequest {
  currentPrice: number
}

export interface InvestmentMetricsData {
  portfolioValue: number
  monthlyReturn: number
  dividends: number
  portfolioTrend: number
  returnTrend: number
  dividendsTrend: number
}

export interface EvolutionResponse {
  period: string
  labels: string[]
  data: number[]
}

export interface DistributionData {
  name: string
  value: number
  percentage: number
  color: string
}

export interface DistributionResponse {
  totalValue: number
  distribution: DistributionData[]
}