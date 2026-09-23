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
  const { t } = useTranslation();

  return (
    <div
      className="flex w-fit rounded-2xl border border-border/50 bg-secondary/50 p-2 backdrop-blur-sm"
      role="tablist"
      aria-label={t('auth:form.toggle.label')}
    >
      <button
        type="button"
        role="tab"
        aria-selected={mode === 'login'}
        onClick={() => onToggle('login')}
        className={cn(
          'rounded-xl px-8 py-3 text-sm font-bold uppercase tracking-widest transition-all',
          mode === 'login'
            ? 'bg-primary text-primary-foreground shadow-lg'
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
          'rounded-xl px-8 py-3 text-sm font-bold uppercase tracking-widest transition-all',
          mode === 'register'
            ? 'bg-primary text-primary-foreground shadow-lg'
            : 'text-foreground hover:text-primary',
        )}
      >
        {t('auth:form.toggle.register')}
      </button>
    </div>
  );
}