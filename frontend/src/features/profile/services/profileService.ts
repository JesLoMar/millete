import { apiClient } from '@/shared/api/axiosClient';

import type {
  ChangePasswordRequest,
  DeactivateAccountRequest,
  ProfileResponse,
  SessionResponse,
  UpdateProfileRequest,
  UserPreferences,
} from '../types';

const BASE = '/profile';

export const profileService = {
  getProfile: async (): Promise<ProfileResponse> => {
    const { data } = await apiClient.get<ProfileResponse>(BASE);
    return data;
  },

  updateProfile: async (
    data: UpdateProfileRequest,
  ): Promise<void> => {
    await apiClient.put<void>(BASE, data);
  },

  changePassword: async (
    data: ChangePasswordRequest,
  ): Promise<void> => {
    await apiClient.put<void>(`${BASE}/password`, data);
  },

  getPreferences: async (): Promise<UserPreferences> => {
    const { data } = await apiClient.get<UserPreferences>(
      `${BASE}/preferences`,
    );

    return data;
  },

  updatePreferences: async (
    prefs: Partial<UserPreferences>,
  ): Promise<void> => {
    await apiClient.put<void>(
      `${BASE}/preferences`,
      prefs,
    );
  },

  getSessions: async (): Promise<SessionResponse[]> => {
    const { data } = await apiClient.get<SessionResponse[]>(
      `${BASE}/sessions`,
    );

    return data;
  },

  deleteSession: async (
    sessionId: string,
  ): Promise<void> => {
    await apiClient.delete<void>(
      `${BASE}/sessions/${sessionId}`,
    );
  },

  deleteAllOtherSessions: async (): Promise<void> => {
    await apiClient.delete<void>(`${BASE}/sessions`);
  },

  deactivateAccount: async (
    data: DeactivateAccountRequest,
  ): Promise<void> => {
    await apiClient.post<void>(
      `${BASE}/deactivate`,
      data,
    );
  },
};