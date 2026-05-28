export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface RegisterUserRequest {
  username?: string;
  email?: string;
  password: string;
}

export interface TokenResponse {
  token: string;
}

export interface UserTopnavResponse {
  username?: string;
  email?: string;
  role?: string;
  avatar?: string;
}

export interface ApiErrorResponse {
  status: number;
  message: string;
  error?: string;
  timestamp?: string;
}