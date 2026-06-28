import { apiClient } from '@/shared/api/axiosClient';
import type {
  ProfileResponse,
  UpdateProfileRequest,
  ChangePasswordRequest,
  UserPreferences,
  SessionResponse,
  DeactivateAccountRequest,
} from '../types';

const BASE = '/profile';

export const profileService = {
  getProfile: () =>
    apiClient.get<ProfileResponse>(BASE).then(res => res.data),

  updateProfile: (data: UpdateProfileRequest) =>
    apiClient.put<void>(BASE, data),

  changePassword: (data: ChangePasswordRequest) =>
    apiClient.put<void>(`${BASE}/password`, data),

  getPreferences: async (): Promise<UserPreferences> => {
    const { data } = await apiClient.get<string>(`${BASE}/preferences`);
    return JSON.parse(data);
  },

  updatePreferences: (prefs: Partial<UserPreferences>) =>
    apiClient.put<void>(`${BASE}/preferences`, JSON.stringify(prefs), {
      headers: { 'Content-Type': 'application/json' },
    }),

  unlinkTelegram: () =>
    apiClient.delete<void>(`${BASE}/telegram`),

  getSessions: () =>
    apiClient.get<SessionResponse[]>(`${BASE}/sessions`).then(res => res.data),

  deleteSession: (sessionId: string) =>
    apiClient.delete<void>(`${BASE}/sessions/${sessionId}`),

  deleteAllOtherSessions: (currentSessionId: string) =>
    apiClient.delete<void>(`${BASE}/sessions`, { params: { currentSessionId } }),

  deactivateAccount: (data: DeactivateAccountRequest) =>
    apiClient.post<void>(`${BASE}/deactivate`, data),
};
