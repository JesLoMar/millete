export type TelegramConnectionStatus = 'connected' | 'disconnected' | 'loading' | 'error'

export interface TelegramStatusResponse {
  connected: boolean
  username?: string
  connectedAt?: string
}

export type CurrencyCode = 'EUR' | 'USD' | 'GBP' | 'JPY' | 'CHF' | 'CAD' | 'AUD'

export interface CurrencyOption {
  code: CurrencyCode
  symbol: string
  label: string
}