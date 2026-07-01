import React, { Suspense, lazy } from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import './app/globals.css';
import './index.css';
import './lib/i18n';

const MotionProvider = lazy(() => import('@/shared/components/MotionProvider').then(m => ({ default: m.MotionProvider })));

function Root() {
  return (
    <QueryClientProvider client={queryClient}>
      <Suspense fallback={null}>
        <MotionProvider>
          <App />
        </MotionProvider>
      </Suspense>
    </QueryClientProvider>
  );
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000,
      gcTime: 10 * 60 * 1000,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>
);
