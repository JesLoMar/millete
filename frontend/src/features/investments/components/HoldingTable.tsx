import { memo, useMemo } from 'react';
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
import {
  formatActivityDateTime,
  formatPercentage,
  formatQuantity,
  getPositionFlags,
  type PositionFlag,
} from '../utils';
import { useSectors } from '../hooks/useInvestmentQueries';
import type { Portfolio, Position } from '../types';

interface HoldingsTableProps {
  portfolio: Portfolio;
}

const FLAG_VARIANT: Record<
  PositionFlag,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  CALCULATED: 'outline',
  ESTIMATED: 'secondary',
  HISTORY_INCOMPLETE: 'secondary',
  MANUAL_PRICE: 'outline',
  NOT_CALCULABLE: 'destructive',
};

const FLAG_FALLBACK: Record<PositionFlag, string> = {
  CALCULATED: 'Calculado',
  ESTIMATED: 'Estimado',
  HISTORY_INCOMPLETE: 'Historial incompleto',
  MANUAL_PRICE: 'Precio manual',
  NOT_CALCULABLE: 'No calculable',
};

function PositionFlagBadges({ flags }: { flags: PositionFlag[] }) {
  const { t } = useTranslation('investments');
  return (
    <div className="flex flex-wrap gap-1">
      {flags.map((flag) => (
        <Badge key={flag} variant={FLAG_VARIANT[flag]} className="text-[11px]">
          {t(`positions.flags.${flag}`, { defaultValue: FLAG_FALLBACK[flag] })}
        </Badge>
      ))}
    </div>
  );
}

function PositionRow({
  position,
  localCurrency,
  sectorNames,
}: {
  position: Position;
  localCurrency: string;
  sectorNames: Map<string, string>;
}) {
  const { t } = useTranslation('investments');
  const flags = getPositionFlags(position);
  const showGain = position.calculable && position.unrealizedGain != null;
  const showReturn =
    position.calculable && position.unrealizedGainPercentage != null;
  const isPositive = (position.unrealizedGain ?? 0) >= 0;

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
          {position.assetSector
            ? (sectorNames.get(position.assetSector) ?? position.assetSector)
            : t('positions.unknownSector', { defaultValue: 'Sin sector' })}
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
        {position.localMarketValue != null ? (
          <div className="flex flex-col items-end">
            <span>{formatCurrency(position.localMarketValue, localCurrency)}</span>
            {position.priceTimestamp && (
              <span className="text-[11px] text-muted-foreground">
                {t('positions.priceAt', {
                  date: formatActivityDateTime(position.priceTimestamp),
                  defaultValue: 'Precio del {{date}}',
                })}
              </span>
            )}
          </div>
        ) : (
          <span className="text-muted-foreground">—</span>
        )}
      </TableCell>
      <TableCell className="text-right">
        {showGain ? (
          <div className="flex items-center justify-end gap-1.5">
            {isPositive ? (
              <TrendingUp className="size-3.5 text-emerald-500" aria-hidden="true" />
            ) : (
              <TrendingDown className="size-3.5 text-destructive" aria-hidden="true" />
            )}
            <span
              className={cn(
                'font-medium',
                isPositive ? 'text-emerald-500' : 'text-destructive',
              )}
            >
              {formatCurrency(position.unrealizedGain!, localCurrency)}
            </span>
          </div>
        ) : (
          <span className="text-muted-foreground">—</span>
        )}
      </TableCell>
      <TableCell className="text-right">
        {showReturn ? (
          <span
            className={cn(
              'font-medium',
              position.unrealizedGainPercentage! >= 0
                ? 'text-emerald-500'
                : 'text-destructive',
            )}
          >
            {formatPercentage(position.unrealizedGainPercentage)}
          </span>
        ) : (
          <span className="text-muted-foreground">—</span>
        )}
      </TableCell>
      <TableCell>
        <div className="flex flex-col gap-1">
          <PositionFlagBadges flags={flags} />
          {!position.calculable && position.notCalculableReason && (
            <span className="text-xs text-muted-foreground">
              {position.notCalculableReason}
            </span>
          )}
        </div>
      </TableCell>
    </TableRow>
  );
}

export const HoldingsTable = memo(function HoldingsTable({
  portfolio,
}: HoldingsTableProps) {
  const { t } = useTranslation('investments');
  const { data: sectors } = useSectors();

  const sectorNames = useMemo(
    () =>
      new Map((sectors ?? []).map((sector) => [sector.code, sector.displayName])),
    [sectors],
  );

  if (portfolio.positions.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-center">
        <p className="text-sm text-muted-foreground">{t('positions.empty')}</p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>{t('positions.asset')}</TableHead>
          <TableHead>{t('positions.sector')}</TableHead>
          <TableHead className="text-right">{t('positions.quantity')}</TableHead>
          <TableHead className="text-right">{t('positions.costBasis')}</TableHead>
          <TableHead className="text-right">{t('positions.marketValue')}</TableHead>
          <TableHead className="text-right">{t('positions.unrealizedGain')}</TableHead>
          <TableHead className="text-right">{t('positions.return')}</TableHead>
          <TableHead>{t('positions.status', { defaultValue: 'Estado' })}</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {portfolio.positions.map((position) => (
          <PositionRow
            key={`${position.assetReference.kind}-${position.assetReference.id}`}
            position={position}
            localCurrency={portfolio.localCurrency}
            sectorNames={sectorNames}
          />
        ))}
      </TableBody>
    </Table>
  );
});