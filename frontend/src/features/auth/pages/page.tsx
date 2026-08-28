import { AuthForm } from '@/features/auth/components/AuthForm';
import { InfoSection } from '@/features/auth/components/InfoSection';
import { BackgroundDecoration } from '@/features/auth/components/BackgroundDecoration';
import { BillConfetti } from '@/features/auth/components/BillConfetti';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export const LoginPage = () => {
  const { t } = useTranslation();
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="animate-pulse text-muted-foreground">{t('common:status.loading')}</div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <main
      className="min-h-screen w-full grid grid-cols-1 lg:grid-cols-2 bg-background relative overflow-x-hidden select-none"
      aria-label={t('auth:aria.pageLabel')}
    >
      <section
        className="w-full flex items-start justify-center p-6 sm:p-12 md:p-16 relative z-10 min-w-0"
        aria-label={t('auth:aria.formLabel')}
      >
        <div className="w-full max-w-md mx-auto">
          <AuthForm />
        </div>
      </section>
      <section
        className="flex w-full min-w-0 relative z-10"
        aria-label={t('auth:aria.infoLabel')}
      >
        <InfoSection />
      </section>
      <BackgroundDecoration />
      <BillConfetti maxBills={120} billsPerScroll={40} enabled={!isLoading} />
    </main>
  );
};