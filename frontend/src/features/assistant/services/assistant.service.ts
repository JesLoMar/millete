import { apiClient } from '@/shared/api/axiosClient';

import type { AssistantInterpretation } from '../types';

export const assistantService = {
  availability: async (signal?: AbortSignal): Promise<boolean> => {
    const response = await apiClient.get<{ available: boolean }>(
      '/assistant/availability',
      { signal, skipGlobalErrorNotify: true },
    );
    return response.data?.available === true;
  },

  interpret: async (
    input: string,
  ): Promise<AssistantInterpretation> => {
    const response =
      await apiClient.post<AssistantInterpretation>(
        '/assistant/interpret',
        { input },
        {
          // The local model can take a while to extract structured data.
          timeout: 90_000,
          skipGlobalErrorNotify: true,
        },
      );

    return response.data;
  },
};
