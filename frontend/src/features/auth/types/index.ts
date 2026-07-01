export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface RegisterUserRequest {
  username: string;
  email: string;
  password: string;
}

export interface TokenResponse {
  token: string;
}

