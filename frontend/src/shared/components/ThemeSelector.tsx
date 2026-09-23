import { Check, Palette } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';
import { Button } from '@/shared/components/core/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from '@/shared/components/core/dropdown-menu';
import { useTheme } from '@/shared/hooks/useTheme';

interface ThemeSelectorProps {
  className?: string;
}

export function ThemeSelector({
  className,
}: ThemeSelectorProps) {
  const { t } = useTranslation('nav');
  const {
    theme,
    setTheme,
    availableThemes,
  } = useTheme();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            'size-10 text-muted-foreground hover:text-foreground',
            className,
          )}
          aria-label={t('nav:theme.selector')}
        >
          <Palette
            className="size-5"
            aria-hidden="true"
          />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel>
          {t('nav:theme.palette')}
        </DropdownMenuLabel>

        {availableThemes.map((availableTheme) => {
          const isSelected =
            theme.name === availableTheme.name;

          return (
            <DropdownMenuItem
              key={availableTheme.name}
              onClick={() => setTheme(availableTheme)}
              className={cn(
                'flex cursor-pointer items-center gap-2',
                isSelected && 'bg-accent font-medium',
              )}
            >
              <span
                className="text-base"
                aria-hidden="true"
              >
                {availableTheme.icon}
              </span>

              <span className="flex-1">
                {availableTheme.label}
              </span>

              {isSelected && (
                <Check
                  className="size-4 text-primary"
                  aria-hidden="true"
                />
              )}
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}