export type CurrencyCode = string;

export interface CurrencyOption {
  readonly code: CurrencyCode;
  readonly symbol: string;
  readonly label: string;
}

export interface ProfileResponse {
  readonly id: string;
  readonly username: string | null;
  readonly email: string | null;
  readonly active: boolean;
  readonly anonymized: boolean;
  readonly telegramChatId: number | null;
}

export interface UpdateProfileRequest {
  newUsername: string | null;
  newEmail: string | null;
  currentPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserPreferences {
  readonly timezone?: string;
  readonly localCurrency?: CurrencyCode;
  readonly theme?: 'light' | 'dark' | 'system';
  readonly language?: string;
  readonly dateFormat?: string;
  readonly currencyFormat?: {
    readonly locale: string;
    readonly currency: string;
  };
  readonly [key: string]: unknown;
}

export interface SessionResponse {
  readonly id: string;
  readonly channel: 'WEB' | 'TELEGRAM';
  readonly active: boolean;
  readonly createdAt: string;
}

export interface DeactivateAccountRequest {
  password: string;
}
