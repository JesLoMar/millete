import { useTranslation } from 'react-i18next';

export function AuthFooter() {
  const { t } = useTranslation('auth');

  return (
    <footer className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1 border-t border-border px-5 py-4 text-xs text-muted-foreground sm:px-9">
      <span className="whitespace-nowrap">
        {t('auth:footer.appVersion')}
      </span>
      <span className="whitespace-nowrap">
        {t('auth:footer.copyright')}
      </span>
    </footer>
  );
}
