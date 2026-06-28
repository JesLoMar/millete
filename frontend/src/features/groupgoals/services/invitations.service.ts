import { apiClient } from '@/shared/api/axiosClient';

const BASE = '/goals/invitations';

export const invitationsService = {
  accept: async (invitationId: string): Promise<void> => {
    await apiClient.post(`${BASE}/${invitationId}/accept`);
  },

  reject: async (invitationId: string): Promise<void> => {
    await apiClient.post(`${BASE}/${invitationId}/reject`);
  },
};
