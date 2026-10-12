import { useTranslation } from 'react-i18next';

import { AuthForm } from '@/features/auth/components/AuthForm';
import { BackgroundDecoration } from '@/features/auth/components/BackgroundDecoration';
import { BillConfetti } from '@/features/auth/components/BillConfetti';
import { useAuth } from '@/features/auth/context/AuthContext';
import { Button } from '@/shared/components/core/button';

export const LoginPage = () => {
  const { t } = useTranslation();
  const { isLoading, isOffline, retryAuth } = useAuth();

  return (
    <main
      className="relative flex min-h-screen w-full items-center justify-center overflow-x-hidden bg-background"
      aria-label={t('auth:aria.pageLabel')}
    >
      <section
        className="relative z-10 flex w-full min-w-0 items-center justify-center p-6 sm:p-12 md:p-16"
        aria-label={t('auth:aria.formLabel')}
      >
        <div className="mx-auto w-full max-w-md">
          {isOffline && (
            <div
              className="mb-6 flex flex-col items-start gap-3 rounded-xl border border-border/50 p-4"
              role="status"
            >
              <p className="text-sm text-muted-foreground">
                {t('common:status.connectionError')}
              </p>

              <Button
                type="button"
                variant="outline"
                disabled={isLoading}
                onClick={retryAuth}
              >
                {t('common:actions.retry')}
              </Button>
            </div>
          )}

          <AuthForm />
        </div>
      </section>

      <BackgroundDecoration />

      <BillConfetti
        maxBills={24}
        billsPerScroll={8}
        enabled={!isLoading}
      />
    </main>
  );
};
