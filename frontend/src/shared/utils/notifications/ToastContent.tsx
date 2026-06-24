import { type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { X } from 'lucide-react';

export interface ToastStyles {
  icon: ReactNode;
  iconBg: string;
  iconShadow: string;
  borderColor: string;
  textTitle: string;
  textDescription: string;
  closeHover: string;
}

export const ToastContent = ({
  styles,
  message,
  description,
  onClose,
}: {
  styles: ToastStyles;
  message: string;
  description?: string;
  onClose: () => void;
}) => {
  const { t } = useTranslation()

  return (
    <div
      className={`w-full max-w-sm pointer-events-auto flex items-center gap-4 p-5 rounded-4xl border backdrop-blur-md transition-all shadow-xl bg-card/90 dark:bg-surface/80 ${styles.borderColor}`}
    >
      <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full shadow-md ${styles.iconBg} ${styles.iconShadow}`}>
        {styles.icon}
      </div>
      <div className="flex-1 space-y-1 min-w-0">
        <p className={`font-semibold text-sm leading-snug ${styles.textTitle}`}>{message}</p>
        {description && (
          <p className={`text-xs leading-relaxed ${styles.textDescription}`}>{description}</p>
        )}
      </div>
      <button
        type="button"
        onClick={onClose}
        className={`p-1 rounded-full transition-colors shrink-0 ${styles.closeHover}`}
        aria-label={t('common:actions.close')}
      >
        <X className="size-3.5" />
      </button>
    </div>
  );
};
