import { useTranslation } from 'react-i18next';

import { ThemeSelector } from '@/shared/components/ThemeSelector';

export function AuthHeader() {
  const { t } = useTranslation();

  return (
    <header className="mb-12 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <img
          src="/web-app-icon.webp"
          alt=""
          aria-hidden="true"
          className="size-13 object-contain"
        />

        <span className="text-2xl font-bold tracking-tight text-foreground">
          {t('auth:brand.name')}
        </span>
      </div>

      <div className="flex items-center gap-2">
        <ThemeSelector />
      </div>
    </header>
  );
}