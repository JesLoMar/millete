import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  ArrowDownLeft,
  ArrowUpRight,
  Coins,
  Flag,
  Landmark,
  Lock,
  Repeat,
  Scissors,
  type LucideIcon,
} from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { Badge } from '@/shared/components/core/badge';
import {
  describeActivityDetails,
  formatActivityDateTime,
  getActivityAssetLabel,
} from '../utils';
import type { Activity, ActivityType } from '../types';

interface ActivityHistoryProps {
  activities: Activity[];
}

const ACTIVITY_ICON: Record<ActivityType, LucideIcon> = {
  BUY: ArrowDownLeft,
  SELL: ArrowUpRight,
  DIVIDEND: Coins,
  DEPOSIT: Landmark,
  WITHDRAW: Landmark,
  OPENING_CASH: Landmark,
  OPENING_POSITION: Flag,
  SPLIT: Scissors,
  EXCHANGE: Repeat,
};

function getActivityBadgeVariant(
  type: ActivityType,
): 'default' | 'secondary' | 'outline' | 'destructive' {
  switch (type) {
    case 'BUY':
    case 'DEPOSIT':
    case 'DIVIDEND':
    case 'OPENING_CASH':
    case 'OPENING_POSITION':
      return 'default';
    case 'SELL':
    case 'WITHDRAW':
      return 'destructive';
    default:
      return 'outline';
  }
}

function ActivityRow({ activity }: { activity: Activity }) {
  const { t } = useTranslation('investments');
  const Icon = ACTIVITY_ICON[activity.type] ?? Landmark;
  const isLinked = activity.linkedTransactionId != null;

  return (
    <TableRow>
      <TableCell>
        <div className="flex items-center gap-3">
          <div className="rounded-md bg-muted p-2">
            <Icon className="size-4 text-muted-foreground" aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-foreground">
              {getActivityAssetLabel(activity)}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {formatActivityDateTime(activity.occurredAt)}
            </p>
          </div>
        </div>
      </TableCell>
      <TableCell>
        <div className="flex flex-col items-start gap-1">
          <Badge variant={getActivityBadgeVariant(activity.type)}>
            {t(`activities.types.${activity.type}`)}
          </Badge>
          {isLinked && (
            <span
              className="inline-flex items-center gap-1 text-[11px] text-muted-foreground"
              title={t('activities.linkedHint', {
                defaultValue:
                  'Vinculada a una transacción. Se modifica desde Transacciones.',
              })}
            >
              <Lock className="size-3" aria-hidden="true" />
              {t('activities.linked', { defaultValue: 'Vinculada a transacciones' })}
            </span>
          )}
        </div>
      </TableCell>
      <TableCell className="text-right text-sm">
        {describeActivityDetails(activity.type, activity.details)}
      </TableCell>
      <TableCell className="max-w-50 truncate text-right text-xs text-muted-foreground">
        {activity.comment ?? '—'}
      </TableCell>
    </TableRow>
  );
}

export const ActivityHistory = memo(function ActivityHistory({
  activities,
}: ActivityHistoryProps) {
  const { t } = useTranslation('investments');

  if (activities.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-center">
        <p className="text-sm text-muted-foreground">{t('activities.empty')}</p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>{t('activities.asset')}</TableHead>
          <TableHead>{t('activities.type')}</TableHead>
          <TableHead className="text-right">{t('activities.details')}</TableHead>
          <TableHead className="text-right">{t('activities.comment')}</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {activities.map((activity) => (
          <ActivityRow key={activity.id} activity={activity} />
        ))}
      </TableBody>
    </Table>
  );
});