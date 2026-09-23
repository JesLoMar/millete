import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

import App from './App';
import './app/globals.css';
import './index.css';
import '@/lib/i18n';
import { MotionProvider } from '@/shared/components/MotionProvider';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      refetchOnWindowFocus: false,
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