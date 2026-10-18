
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import {
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query';
import axios, { type AxiosError } from 'axios';
import '@fontsource-variable/dm-sans';

import App from './App';
import './index.css';
import '@/lib/i18n';
import { MotionProvider } from '@/shared/components/MotionProvider';
import {
  applyTheme,
  getInitialTheme,
} from '@/shared/themes/applyTheme';

// Aplicar el tema guardado antes del primer render de React.
const initialTheme = getInitialTheme();

applyTheme(
  document.documentElement,
  initialTheme.colors,
);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        if (
          axios.isCancel(error) ||
          (error as AxiosError).code === 'ERR_CANCELED'
        ) {
          return false;
        }

        const status = (error as AxiosError).response?.status;

        return (status == null || status >= 500) && failureCount < 2;
      },
    },
  },
});

export const Root = () => (
  <QueryClientProvider client={queryClient}>
    <MotionProvider>
      <App />
    </MotionProvider>
  </QueryClientProvider>
);

const rootElement = document.getElementById('root');

if (rootElement) {
  createRoot(rootElement).render(
    <StrictMode>
      <Root />
    </StrictMode>,
  );
}
