import { useTranslation } from 'react-i18next';

export function AuthHeader() {
  const { t } = useTranslation('auth');

  return (
    <header className="flex min-w-0 items-center gap-3 sm:gap-4">
      <img
        src="/web-app-icon.webp"
        alt=""
        aria-hidden="true"
        className="size-11 shrink-0 object-contain md:size-20"
      />

      <span className="text-2xl font-bold tracking-tight text-foreground md:text-4xl">
        {t('auth:brand.name')}
      </span>
    </header>
  );
}
