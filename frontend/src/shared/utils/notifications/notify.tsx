import { type ReactNode } from 'react';
import { toast } from 'sonner';
import { AlertTriangle, Info, AlertCircle } from 'lucide-react';
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
    icon: <img src="/web-app-icon.webp" alt="" className="h-5 w-5 object-contain" aria-hidden="true" />,
    iconBg: 'bg-primary text-primary-foreground',
    iconShadow: 'shadow-primary/20',
    borderColor: 'border-primary/30',
    textTitle: 'text-primary',
    textDescription: 'text-muted-foreground',
    closeHover: 'hover:bg-primary/10 text-primary/60 hover:text-primary',
    defaultDuration: 4000,
  },
  error: {
    icon: <AlertTriangle className="h-5 w-5" />,
    iconBg: 'bg-destructive text-destructive-foreground',
    iconShadow: 'shadow-destructive/20',
    borderColor: 'border-destructive/30',
    textTitle: 'text-destructive',
    textDescription: 'text-muted-foreground',
    closeHover: 'hover:bg-destructive/10 text-destructive/60 hover:text-destructive',
    defaultDuration: 6000,
  },
  info: {
    icon: <Info className="h-5 w-5" />,
    iconBg: 'bg-primary text-primary-foreground',
    iconShadow: 'shadow-primary/20',
    borderColor: 'border-primary/30',
    textTitle: 'text-primary',
    textDescription: 'text-muted-foreground',
    closeHover: 'hover:bg-primary/10 text-primary/60 hover:text-primary',
    defaultDuration: 4000,
  },
  warning: {
    icon: <AlertCircle className="h-5 w-5" />,
    iconBg: 'bg-accent text-accent-foreground',
    iconShadow: 'shadow-accent/20',
    borderColor: 'border-warning/30',
    textTitle: 'text-warning',
    textDescription: 'text-muted-foreground',
    closeHover: 'hover:bg-warning/10 text-warning/60 hover:text-warning',
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
