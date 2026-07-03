import { apiClient } from '@/shared/api/axiosClient';
import type { LoginRequest, RegisterUserRequest, LoginResponse } from '../types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
    return response.data;
  },
  register: async (data: RegisterUserRequest): Promise<void> => {
    await apiClient.post('/auth/register', data);
  },
};
