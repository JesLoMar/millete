import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { TrendingDown, TrendingUp } from 'lucide-react';
import { cn } from '@/lib/utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { Badge } from '@/shared/components/core/badge';
import { formatCurrency } from '@/shared/utils/i18nFormat';
import { formatPercentage, formatQuantity } from '../utils';
import type { Portfolio, Position } from '../types';

interface HoldingsTableProps {
  portfolio: Portfolio;
}

function PositionRow({
  position,
  localCurrency,
}: {
  position: Position;
  localCurrency: string;
}) {
  const { t } = useTranslation('investments');
  const isPositive = (position.unrealizedGain ?? 0) >= 0;
  const gainPercentage = position.unrealizedGainPercentage;

  return (
    <TableRow>
      <TableCell className="font-medium">
        <div className="flex flex-col">
          <span className="text-foreground">{position.assetName}</span>
          {position.assetSymbol && (
            <span className="text-xs text-muted-foreground">
              {position.assetSymbol}
            </span>
          )}
        </div>
      </TableCell>
      <TableCell>
        <Badge variant="outline" className="text-xs">
          {position.assetSector ?? t('positions.unknownSector')}
        </Badge>
      </TableCell>
      <TableCell className="text-right">
        {formatQuantity(position.quantity)}
      </TableCell>
      <TableCell className="text-right">
        {position.localCostBasis != null
          ? formatCurrency(position.localCostBasis, localCurrency)
          : '—'}
      </TableCell>
      <TableCell className="text-right">
        {position.localMarketValue != null
          ? formatCurrency(position.localMarketValue, localCurrency)
          : '—'}
      </TableCell>
      <TableCell className="text-right">
        {position.unrealizedGain != null ? (
          <div className="flex items-center justify-end gap-1.5">
            {isPositive ? (
              <TrendingUp
                className="size-3.5 text-emerald-500"
                aria-hidden="true"
              />
            ) : (
              <TrendingDown
                className="size-3.5 text-destructive"
                aria-hidden="true"
              />
            )}
            <span
              className={cn(
                'font-medium',
                isPositive ? 'text-emerald-500' : 'text-destructive',
              )}
            >
              {formatCurrency(position.unrealizedGain, localCurrency)}
            </span>
          </div>
        ) : (
          <span className="text-muted-foreground">—</span>
        )}
      </TableCell>
      <TableCell className="text-right">
        {gainPercentage != null ? (
          <span
            className={cn(
              'font-medium',
              gainPercentage >= 0
                ? 'text-emerald-500'
                : 'text-destructive',
            )}
          >
            {formatPercentage(gainPercentage)}
          </span>
        ) : (
          <span className="text-muted-foreground">
            {position.notCalculableReason ?? '—'}
          </span>
        )}
      </TableCell>
    </TableRow>
  );
}

export const HoldingsTable = memo(function HoldingsTable({
  portfolio,
}: HoldingsTableProps) {
  const { t } = useTranslation('investments');

  if (portfolio.positions.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-center">
        <p className="text-sm text-muted-foreground">
          {t('positions.empty')}
        </p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>{t('positions.asset')}</TableHead>
          <TableHead>{t('positions.sector')}</TableHead>
          <TableHead className="text-right">
            {t('positions.quantity')}
          </TableHead>
          <TableHead className="text-right">
            {t('positions.costBasis')}
          </TableHead>
          <TableHead className="text-right">
            {t('positions.marketValue')}
          </TableHead>
          <TableHead className="text-right">
            {t('positions.unrealizedGain')}
          </TableHead>
          <TableHead className="text-right">
            {t('positions.return')}
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {portfolio.positions.map((position) => (
          <PositionRow
            key={`${position.assetReference.kind}-${position.assetReference.id}`}
            position={position}
            localCurrency={portfolio.localCurrency}
          />
        ))}
      </TableBody>
    </Table>
  );
});