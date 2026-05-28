import { apiClient } from '@/shared/api/axiosClient';
import type { LoginRequest, RegisterUserRequest, TokenResponse } from '../types';
export const authService = {
  login: async (credentials: LoginRequest): Promise<TokenResponse> => {
    const response = await apiClient.post<TokenResponse>('/auth/login', credentials);
    return response.data;
  },
  register: async (data: RegisterUserRequest): Promise<void> => {
    await apiClient.post('/auth/register', data);
  },
};