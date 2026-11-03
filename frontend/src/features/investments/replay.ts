import type {
  HistoricalActivityType,
  SettlementCurrency,
} from './types';

export interface HistoryReplayRowInput {
  occurredAt: string;
  type: HistoricalActivityType;
  quantity?: number | null;
  unitPrice?: number | null;
  settlementCurrency?: SettlementCurrency | null;
  amount?: number | null;
  currency?: string | null;
  ratio?: number | null;
}

export interface HistoryReplayResult {
  quantity: number;
  acquisitionCost: number | null;
  oversold: boolean;
  invalidSplit: boolean;
  /**
   * Indica que alguna compra se introdujo en moneda local y el coste
   * reconstruido depende de tipos de cambio históricos que solo conoce el
   * backend.
   */
  hasUnresolvedLocalCost: boolean;
}

interface ReplayLot {
  originalQuantity: number;
  remainingQuantity: number;
  totalCost: number | null;
}

function finitePositive(value: number | null | undefined): number | null {
  if (
    value == null ||
    !Number.isFinite(value) ||
    value <= 0
  ) {
    return null;
  }

  return value;
}

/**
 * Reconstrucción FIFO equivalente a la del backend para el historial de un
 * Holding. Se usa únicamente como previsualización: el backend sigue siendo
 * la autoridad y puede rechazar el historial si no coincide exactamente.
 */
export function replayHistoryRows(
  rows: HistoryReplayRowInput[],
  assetCurrency: string,
  localCurrency: string,
): HistoryReplayResult {
  const ordered = rows
    .map((row, index) => ({ row, index }))
    .sort((left, right) => {
      const leftTime = new Date(left.row.occurredAt).getTime();
      const rightTime = new Date(right.row.occurredAt).getTime();

      if (leftTime !== rightTime) {
        return leftTime - rightTime;
      }

      return left.index - right.index;
    })
    .map(({ row }) => row);

  const lots: ReplayLot[] = [];
  let oversold = false;
  let invalidSplit = false;
  let hasUnresolvedLocalCost = false;

  for (const row of ordered) {
    if (row.type === 'BUY') {
      const quantity = finitePositive(row.quantity);
      const unitPrice = finitePositive(row.unitPrice);

      if (quantity == null || unitPrice == null) {
        continue;
      }

      const costInAssetCurrency =
        row.settlementCurrency === 'ASSET' ||
        assetCurrency === localCurrency;

      if (!costInAssetCurrency) {
        hasUnresolvedLocalCost = true;
      }

      lots.push({
        originalQuantity: quantity,
        remainingQuantity: quantity,
        totalCost: costInAssetCurrency
          ? quantity * unitPrice
          : null,
      });
      continue;
    }

    if (row.type === 'IN_KIND_DIVIDEND') {
      const quantity = finitePositive(row.quantity);

      if (quantity == null) {
        continue;
      }

      lots.push({
        originalQuantity: quantity,
        remainingQuantity: quantity,
        totalCost: 0,
      });
      continue;
    }

    if (row.type === 'SELL') {
      let pending = finitePositive(row.quantity) ?? 0;

      if (pending <= 0) {
        continue;
      }

      for (const lot of lots) {
        if (pending <= 0) {
          break;
        }

        if (lot.remainingQuantity <= 0) {
          continue;
        }

        const consumed = Math.min(
          pending,
          lot.remainingQuantity,
        );

        lot.remainingQuantity -= consumed;
        pending -= consumed;
      }

      if (pending > 1e-9) {
        oversold = true;
      }

      continue;
    }

    if (row.type === 'SPLIT') {
      const ratio = finitePositive(row.ratio);
      const openLots = lots.filter(
        (lot) => lot.remainingQuantity > 0,
      );

      if (ratio == null || openLots.length === 0) {
        invalidSplit = true;
        continue;
      }

      for (const lot of openLots) {
        lot.originalQuantity *= ratio;
        lot.remainingQuantity *= ratio;
      }
    }
  }

  const openLots = lots.filter(
    (lot) => lot.remainingQuantity > 1e-9,
  );
  const quantity = openLots.reduce(
    (total, lot) => total + lot.remainingQuantity,
    0,
  );

  const costKnown = openLots.every(
    (lot) => lot.totalCost != null,
  );

  const acquisitionCost = costKnown
    ? openLots.reduce(
        (total, lot) =>
          total +
          (lot.totalCost as number) *
            (lot.remainingQuantity / lot.originalQuantity),
        0,
      )
    : null;

  return {
    quantity,
    acquisitionCost,
    oversold,
    invalidSplit,
    hasUnresolvedLocalCost,
  };
}
