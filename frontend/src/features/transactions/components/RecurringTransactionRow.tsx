import { useTranslation } from 'react-i18next';
import {
  ArrowDownLeft,
  ArrowUpRight,
  Calendar,
  Repeat,
} from 'lucide-react';

import type { PlannedTransaction } from '@/features/transactions/hooks/usePlannedTransactions';
import { cn } from '@/lib/utils';

import {
  calculateNextExecution,
  formatDate,
  formatFrequency,
} from '../utils';
import { RecurringTransactionActions } from './RecurringTransactionActions';

interface RecurringTransactionRowProps {
  transaction: PlannedTransaction;
  onEdit: (tx: PlannedTransaction) => void;
  onDelete: (tx: PlannedTransaction) => void;
}

export function RecurringTransactionRow({
  transaction,
  onEdit,
  onDelete,
}: RecurringTransactionRowProps) {
  const { t } = useTranslation('transactions');

  const isIncome = transaction.type === 'INCOME';

  const formattedAmount = Math.abs(
    transaction.amount,
  ).toLocaleString('es-ES', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  const frequency = formatFrequency(transaction, t);
  const nextExecution = calculateNextExecution(transaction);

  return (
    <>
      <div className="group hidden items-center gap-4 border-b p-4 transition-colors last:border-0 hover:bg-accent/30 sm:flex">
        <div
          className={cn(
            'shrink-0 rounded-full p-2.5',
            isIncome
              ? 'bg-primary/10 text-primary'
              : 'bg-destructive/10 text-destructive',
          )}
          aria-hidden="true"
        >
          {isIncome ? (
            <ArrowUpRight size={16} />
          ) : (
            <ArrowDownLeft size={16} />
          )}
        </div>

        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold">
            {transaction.description}
          </p>

          <div className="mt-0.5 flex flex-wrap items-center gap-2">
            <Calendar
              size={12}
              className="shrink-0 text-muted-foreground"
              aria-hidden="true"
            />

            <span className="text-xs text-muted-foreground">
              {frequency}
            </span>

            <span
              className="size-1 rounded-full bg-border"
              aria-hidden="true"
            />

            <span className="text-xs text-muted-foreground">
              {formatDate(transaction.startDate)}
            </span>

            {transaction.endDate && (
              <>
                <span
                  className="text-xs text-muted-foreground"
                  aria-hidden="true"
                >
                  →
                </span>

                <span className="text-xs text-muted-foreground">
                  {formatDate(transaction.endDate)}
                </span>
              </>
            )}

            <span
              className="size-1 rounded-full bg-border"
              aria-hidden="true"
            />

            <span className="flex items-center gap-1 text-xs text-muted-foreground">
              <Repeat
                size={10}
                className="shrink-0"
                aria-hidden="true"
              />
              {nextExecution}
            </span>
          </div>
        </div>

        <div className="shrink-0 text-right">
          <p
            className={cn(
              'text-sm font-bold tabular-nums',
              isIncome
                ? 'text-primary'
                : 'text-foreground',
            )}
          >
            {isIncome ? '+' : '-'}
            {formattedAmount} €
          </p>
        </div>

        <RecurringTransactionActions
          transaction={transaction}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      </div>

      <div className="border-b p-4 transition-colors last:border-0 hover:bg-accent/30 sm:hidden">
        <div className="mb-2 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div
              className={cn(
                'shrink-0 rounded-full p-2',
                isIncome
                  ? 'bg-primary/10 text-primary'
                  : 'bg-destructive/10 text-destructive',
              )}
              aria-hidden="true"
            >
              {isIncome ? (
                <ArrowUpRight size={15} />
              ) : (
                <ArrowDownLeft size={15} />
              )}
            </div>

            <p
              className={cn(
                'text-base font-bold tabular-nums',
                isIncome
                  ? 'text-primary'
                  : 'text-foreground',
              )}
            >
              {isIncome ? '+' : '-'}
              {formattedAmount} €
            </p>
          </div>

          <RecurringTransactionActions
            transaction={transaction}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        </div>

        <p className="mb-1.5 truncate text-sm font-medium">
          {transaction.description}
        </p>

        <div className="flex flex-wrap items-center gap-1.5 text-xs text-muted-foreground">
          <Calendar
            size={11}
            className="shrink-0"
            aria-hidden="true"
          />

          <span>{frequency}</span>

          <span
            aria-hidden="true"
            className="text-muted-foreground/50"
          >
            ·
          </span>

          <span>
            {formatDate(transaction.startDate)}
          </span>

          {transaction.endDate && (
            <>
              <span aria-hidden="true">→</span>
              <span>
                {formatDate(transaction.endDate)}
              </span>
            </>
          )}

          <span
            aria-hidden="true"
            className="text-muted-foreground/50"
          >
            ·
          </span>

          <span className="flex items-center gap-1">
            <Repeat
              size={10}
              className="shrink-0"
              aria-hidden="true"
            />
            {nextExecution}
          </span>
        </div>
      </div>
    </>
  );
}