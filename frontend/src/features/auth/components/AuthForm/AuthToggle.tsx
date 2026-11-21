import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

interface AuthToggleProps {
  mode: 'login' | 'register';
  onToggle: (mode: 'login' | 'register') => void;
}

export function AuthToggle({
  mode,
  onToggle,
}: AuthToggleProps) {
  const { t } = useTranslation('auth');

  return (
    <div
      className="grid grid-cols-2 rounded-md border border-border p-1"
      role="tablist"
      aria-label={t('form.toggle.label')}
    >
      <button
        type="button"
        role="tab"
        aria-selected={mode === 'login'}
        onClick={() => onToggle('login')}
        className={cn(
          'min-h-11 min-w-0 rounded-sm px-2 py-2 text-sm font-semibold break-words transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
          mode === 'login'
            ? 'bg-primary text-primary-foreground'
            : 'text-foreground hover:text-primary',
        )}
      >
        {t('auth:form.toggle.login')}
      </button>

      <button
        type="button"
        role="tab"
        aria-selected={mode === 'register'}
        onClick={() => onToggle('register')}
        className={cn(
          'min-h-11 min-w-0 rounded-sm px-2 py-2 text-sm font-semibold break-words transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
          mode === 'register'
            ? 'bg-primary text-primary-foreground'
            : 'text-foreground hover:text-primary',
        )}
      >
        {t('auth:form.toggle.register')}
      </button>
    </div>
  );
}
