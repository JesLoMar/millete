import React, { Suspense, lazy } from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import './app/globals.css';
import './index.css';
import './lib/i18n';

const MotionProvider = lazy(() => import('@/shared/components/MotionProvider').then(m => ({ default: m.MotionProvider })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      refetchOnWindowFocus: false,
    },
  },
});

export function Root() {
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

const rootElement = document.getElementById('root');
if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <Root />
    </React.StrictMode>
  );
}
