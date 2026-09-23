import { useCallback } from 'react';
import { ChevronDown, Languages } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';
import { Button } from '@/shared/components/core/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/core/dropdown-menu';
import { useAvailableLanguages } from '@/shared/hooks/useLanguages';
import { notify } from '@/shared/utils/notifications/notify';

interface LanguageSelectorProps {
  className?: string;
}

export function LanguageSelector({
  className,
}: LanguageSelectorProps) {
  const { t, i18n } = useTranslation('nav');
  const availableLanguages = useAvailableLanguages();

  const currentLanguage =
    availableLanguages.find(
      (language) => language.code === i18n.language,
    ) ?? availableLanguages[0];

  const handleChangeLanguage = useCallback(
    async (languageCode: string) => {
      try {
        await i18n.changeLanguage(languageCode);
      } catch {
        notify.error(
          t('nav:errors.languageChangeFailed'),
        );
      }
    },
    [i18n, t],
  );

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          className={cn(
            'flex items-center gap-2 px-3 text-muted-foreground hover:text-foreground',
            className,
          )}
          aria-label={t('nav:changeLanguage')}
        >
          <Languages
            className="size-5"
            aria-hidden="true"
          />

          <span className="hidden text-sm font-bold tracking-wider sm:inline">
            {currentLanguage?.code.toUpperCase() ?? '??'}
          </span>

          <ChevronDown
            className="size-4 opacity-50"
            aria-hidden="true"
          />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end" className="w-44">
        {availableLanguages.map((language) => {
          const isSelected =
            i18n.language === language.code;

          return (
            <DropdownMenuItem
              key={language.code}
              onClick={() =>
                handleChangeLanguage(language.code)
              }
              className={cn(
                'flex cursor-pointer items-center gap-2',
                isSelected && 'bg-accent font-medium',
              )}
            >
              <span
                className="text-base"
                aria-hidden="true"
              >
                {language.flag}
              </span>

              <span className="flex-1">
                {language.nativeName}
              </span>
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}