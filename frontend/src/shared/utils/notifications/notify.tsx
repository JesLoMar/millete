import { type ReactNode } from 'react';
import { toast } from 'sonner';
import { CircleCheck, AlertTriangle, Info, AlertCircle, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

interface NotifyOptions {
  description?: string;
  duration?: number;
}

const config: Record<
  ToastType,
  {
    icon: ReactNode;
    iconBg: string;
    iconShadow: string;
    borderColor: string;
    textTitle: string;
    textDescription: string;
    closeHover: string;
    defaultDuration: number;
  }
> = {
  success: {
    icon: <CircleCheck className="h-5 w-5" />,
    iconBg: 'bg-emerald-500 text-white',
    iconShadow: 'shadow-emerald-500/20',
    borderColor: 'border-emerald-500/30 dark:border-emerald-500/20',
    textTitle: 'text-emerald-600 dark:text-emerald-400',
    textDescription: 'text-emerald-700/80 dark:text-emerald-300/70',
    closeHover: 'hover:bg-emerald-500/10 text-emerald-500/60 hover:text-emerald-500',
    defaultDuration: 4000,
  },
  error: {
    icon: <AlertTriangle className="h-5 w-5" />,
    iconBg: 'bg-destructive text-destructive-foreground',
    iconShadow: 'shadow-destructive/20',
    borderColor: 'border-destructive/30 dark:border-destructive/20',
    textTitle: 'text-red-500 dark:text-red-400',
    textDescription: 'text-red-700/80 dark:text-red-300/70',
    closeHover: 'hover:bg-destructive/10 text-destructive/60 hover:text-destructive',
    defaultDuration: 6000,
  },
  info: {
    icon: <Info className="h-5 w-5" />,
    iconBg: 'bg-primary text-primary-foreground',
    iconShadow: 'shadow-primary/20',
    borderColor: 'border-primary/30 dark:border-primary/20',
    textTitle: 'text-primary dark:text-primary',
    textDescription: 'text-muted-foreground',
    closeHover: 'hover:bg-primary/10 text-primary/60 hover:text-primary',
    defaultDuration: 4000,
  },
  warning: {
    icon: <AlertCircle className="h-5 w-5" />,
    iconBg: 'bg-amber-500 text-white',
    iconShadow: 'shadow-amber-500/20',
    borderColor: 'border-amber-500/30 dark:border-amber-500/20',
    textTitle: 'text-amber-600 dark:text-amber-400',
    textDescription: 'text-amber-700/80 dark:text-amber-300/70',
    closeHover: 'hover:bg-amber-500/10 text-amber-500/60 hover:text-amber-500',
    defaultDuration: 5000,
  },
};

const ToastContent = ({
  type,
  message,
  description,
  onClose,
}: {
  type: ToastType;
  message: string;
  description?: string;
  onClose: () => void;
}) => {
  const styles = config[type];
  const { t } = useTranslation()

  return (
    <div
      className={`w-full max-w-sm pointer-events-auto flex items-center gap-4 p-5 rounded-4xl border backdrop-blur-md transition-all shadow-xl bg-card/90 dark:bg-surface/80 ${styles.borderColor}`}
    >
      {/* Icono de Estado */}
      <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full shadow-md ${styles.iconBg} ${styles.iconShadow}`}>
        {styles.icon}
      </div>

      {/* Contenido de texto informativo */}
      <div className="flex-1 space-y-1 min-w-0">
        <p className={`font-semibold text-sm leading-snug ${styles.textTitle}`}>{message}</p>
        {description && (
          <p className={`text-xs leading-relaxed ${styles.textDescription}`}>{description}</p>
        )}
      </div>

      {/* Botón interactivo de cierre rápido */}
      <button
        type="button"
        onClick={onClose}
        className={`p-1 rounded-full transition-colors shrink-0 ${styles.closeHover}`}
        aria-label={t("common.close")}
      >
        <X className="size-3.5" />
      </button>
    </div>
  );
};

export const notify = {
  success: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent type="success" message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.success.defaultDuration }),

  error: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent type="error" message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.error.defaultDuration }),

  info: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent type="info" message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.info.defaultDuration }),

  warning: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent type="warning" message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.warning.defaultDuration }),
};