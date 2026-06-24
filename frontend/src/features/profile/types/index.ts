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

export interface ProfileResponse {
  id: string
  username: string
  email: string
  active: boolean
  anonymized: boolean
  telegramChatId: number | null
}

export interface UpdateProfileRequest {
  newUsername?: string
  newEmail?: string
  currentPassword: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface UserPreferences {
  theme: 'light' | 'dark' | 'system'
  language: string
  dateFormat: string
  currencyFormat: {
    locale: string
    currency: string
  }
}

export interface SessionResponse {
  id: string
  channel: 'WEB' | 'TELEGRAM'
  active: boolean
  createdAt: string
}

export interface DeactivateAccountRequest {
  password: string
}
