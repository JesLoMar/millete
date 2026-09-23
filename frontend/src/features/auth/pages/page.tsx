import { Navigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import { ROUTES } from '@/app/router/routes';
import { AuthForm } from '@/features/auth/components/AuthForm';
import { BackgroundDecoration } from '@/features/auth/components/BackgroundDecoration';
import { BillConfetti } from '@/features/auth/components/BillConfetti';
import { InfoSection } from '@/features/auth/components/InfoSection';
import { useAuth } from '@/features/auth/context/AuthContext';

export const LoginPage = () => {
  const { t } = useTranslation();
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="animate-pulse text-muted-foreground">
          {t('common:status.loading')}
        </div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to={ROUTES.dashboard} replace />;
  }

  return (
    <main
      className="relative grid min-h-screen w-full grid-cols-1 overflow-x-hidden bg-background select-none lg:grid-cols-2"
      aria-label={t('auth:aria.pageLabel')}
    >
      <section
        className="relative z-10 flex w-full min-w-0 items-start justify-center p-6 sm:p-12 md:p-16"
        aria-label={t('auth:aria.formLabel')}
      >
        <div className="mx-auto w-full max-w-md">
          <AuthForm />
        </div>
      </section>

      <section
        className="relative z-10 flex w-full min-w-0"
        aria-label={t('auth:aria.infoLabel')}
      >
        <InfoSection />
      </section>

      <BackgroundDecoration />

      <BillConfetti
        maxBills={120}
        billsPerScroll={40}
        enabled={!isLoading}
      />
    </main>
  );
};