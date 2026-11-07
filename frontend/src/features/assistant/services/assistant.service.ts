import { apiClient } from '@/shared/api/axiosClient';

import type { AssistantInterpretation } from '../types';

export const assistantService = {
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
