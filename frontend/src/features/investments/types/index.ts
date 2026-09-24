export type InvestmentAssetType =
  | 'STOCK'
  | 'CRYPTO'
  | 'FUND'
  | 'REAL_ESTATE'
  | 'OTHER';

export interface InvestmentResponse {
  id: string;
  userId: string;
  assetName: string;
  ticker?: string;
  type: InvestmentAssetType;
  quantity: number;
  purchasePrice: number;
  currentPrice: number;
  currentValue: number;
  investedCapital: number;
  profitOrLoss: number;
  roiPercentage: number;
  purchaseDate: string;
  active: boolean;
}

export interface RegisterInvestmentRequest {
  assetName: string;
  ticker?: string;
  quantity: number;
  purchasePrice: number;
  type: InvestmentAssetType;
  purchaseDate: string;
}

export interface UpdateInvestmentPriceRequest {
  id: string;
  currentPrice: number;
}

export interface InvestmentMetricsData {
  portfolioValue: number;
  monthlyReturn: number;
  dividends: number;
  portfolioTrend: number;
  returnTrend: number;
  dividendsTrend: number;
}

export interface EvolutionResponse {
  period: string;
  labels: string[];
  data: number[];
}

export interface DistributionData {
  name: string;
  value: number;
  percentage: number;
  color: string;
}

export interface DistributionResponse {
  totalValue: number;
  distribution: DistributionData[];
}