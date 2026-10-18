import { useTranslation } from 'react-i18next';

export function AuthFooter() {
  const { t } = useTranslation('auth');

  return (
    <footer className="mt-auto flex w-full flex-col items-start justify-between gap-4 border-t border-border/30 pt-8 sm:flex-row sm:items-center sm:gap-0 sm:pt-12">
      <span className="whitespace-nowrap text-sm font-medium text-muted-foreground/40 sm:text-base">
        {t('auth:footer.appVersion')}
      </span>
      <span className="whitespace-nowrap text-sm font-medium text-muted-foreground/40 sm:text-base">
        {t('auth:footer.copyright')}
      </span>
    </footer>
  );
}