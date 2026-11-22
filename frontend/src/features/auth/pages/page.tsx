import { useTranslation } from 'react-i18next';

import { AuthForm } from '@/features/auth/components/AuthForm';
import { BillConfetti } from '@/features/auth/components/BillConfetti';
import { useAuth } from '@/features/auth/context/AuthContext';
import { LanguageSelector } from '@/shared/components/LanguageSelector';
import { ThemeSelector } from '@/shared/components/ThemeSelector';
import { Button } from '@/shared/components/core/button';

export const LoginPage = () => {
  const { t } = useTranslation(['auth', 'common']);
  const { isLoading, isServerOffline, retryAuth } = useAuth();

  return (
    <main
      className="relative grid min-h-dvh w-full place-items-center bg-background bg-[radial-gradient(ellipse_at_center,color-mix(in_oklab,var(--color-card),white_16%)_16%,var(--color-card)_32%,var(--color-background)_68%,color-mix(in_oklab,var(--color-background),black_12%)_100%)] px-4 py-8 sm:px-8 sm:py-12"
      aria-label={t('auth:aria.pageLabel')}
    >
      <section
        className="z-10 w-full min-w-0 max-w-5xl text-card-foreground"
        aria-label={t('auth:aria.formLabel')}
      >
        <div className="flex min-h-[calc(2.5rem+env(safe-area-inset-top))] justify-center md:min-h-10">
          <div className="absolute inset-x-0 top-[calc(env(safe-area-inset-top)+0.75rem)] mx-auto flex w-fit items-center justify-center gap-3 md:static">
            <LanguageSelector className="size-11 p-0 md:size-10 [&_span]:hidden [&_.lucide-chevron-down]:hidden" />
            <ThemeSelector className="size-11 md:size-10" />
          </div>
        </div>

        <AuthForm />

        {isServerOffline && (
          <div className="mt-3 flex flex-wrap items-center justify-center gap-x-2 text-center">
            <p className="text-sm text-muted-foreground" role="status">
              {t('common:status.connectionError')}
            </p>
            <Button
              type="button"
              variant="ghost"
              className="min-h-10 text-primary underline underline-offset-4"
              disabled={isLoading}
              onClick={retryAuth}
            >
              {t('common:actions.retry')}
            </Button>
          </div>
        )}
      </section>

      <BillConfetti
        maxBills={24}
        billsPerScroll={8}
        enabled={!isLoading}
      />
    </main>
  );
};
