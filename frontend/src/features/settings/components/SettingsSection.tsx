import type { LucideIcon } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { cn } from '@/lib/utils';

interface SettingsSectionProps {
  icon: LucideIcon;
  title: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
  badge?: React.ReactNode;
}

export function SettingsSection({
  icon: Icon,
  title,
  description,
  children,
  className,
  badge,
}: SettingsSectionProps) {
  return (
    <Card className={cn('w-full', className)}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-lg">
          <Icon className="h-5 w-5 text-muted-foreground" />
          {title}
          {badge && <span className="ml-auto">{badge}</span>}
        </CardTitle>
        {description && (
          <p className="text-sm text-muted-foreground">{description}</p>
        )}
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  );
}