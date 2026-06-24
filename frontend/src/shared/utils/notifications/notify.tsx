import { type ReactNode } from 'react';
import { toast } from 'sonner';
import { CircleCheck, AlertTriangle, Info, AlertCircle } from 'lucide-react';
import { ToastContent } from './ToastContent';

const config: Record<string, {
  icon: ReactNode;
  iconBg: string;
  iconShadow: string;
  borderColor: string;
  textTitle: string;
  textDescription: string;
  closeHover: string;
  defaultDuration: number;
}> = {
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

interface NotifyOptions {
  description?: string;
  duration?: number;
}

export const notify = {
  success: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent styles={config.success} message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.success.defaultDuration }),

  error: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent styles={config.error} message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.error.defaultDuration }),

  info: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent styles={config.info} message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.info.defaultDuration }),

  warning: (message: string, options?: NotifyOptions) =>
    toast.custom((t) => (
      <ToastContent styles={config.warning} message={message} description={options?.description} onClose={() => toast.dismiss(t)} />
    ), { duration: options?.duration ?? config.warning.defaultDuration }),
};
