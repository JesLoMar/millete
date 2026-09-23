import { useTranslation } from 'react-i18next';

import { Badge } from '@/shared/components/core/badge';

export function AuthFooter() {
  const { t } = useTranslation();

  return (
    <footer className="mt-auto flex w-full flex-col items-start justify-between gap-4 border-t border-border/30 pt-8 sm:flex-row sm:items-center sm:gap-0 sm:pt-12">
      <div className="flex flex-wrap items-center gap-3 sm:gap-6">
        <Badge
          variant="outline"
          className="whitespace-nowrap border-border/50 bg-secondary/10 px-3 py-1.5 font-mono text-sm text-muted-foreground/60 sm:px-4"
        >
          {t('auth:footer.webVersion')}
        </Badge>

        <Badge
          variant="outline"
          className="whitespace-nowrap border-border/50 bg-secondary/10 px-3 py-1.5 font-mono text-sm text-muted-foreground/60 sm:px-4"
        >
          {t('auth:footer.apiVersion')}
        </Badge>
      </div>

      <span className="whitespace-nowrap text-sm font-medium text-muted-foreground/40 sm:text-base">
        {t('auth:footer.copyright')}
      </span>
    </footer>
  );
}