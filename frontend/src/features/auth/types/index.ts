export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface RegisterUserRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  status: string;
  username?: string | null;
  email?: string | null;
  sessionId?: string | null;
}