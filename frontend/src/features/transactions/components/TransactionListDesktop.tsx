
import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  ArrowDownLeft,
  ArrowUpRight,
  HelpCircle,
  MoreHorizontal,
} from 'lucide-react';
import * as m from 'framer-motion/m';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/core/dropdown-menu';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import { formatDate } from '../utils';
import type { Transaction } from './types';

interface TransactionListDesktopProps {
  transactions: Transaction[];
  onEdit: (tx: Transaction) => void;
  onDelete: (tx: Transaction) => void;
}

export const TransactionListDesktop = memo(function TransactionListDesktop({
  transactions,
  onEdit,
  onDelete,
}: TransactionListDesktopProps) {
  const { t } = useTranslation('transactions');

  return (
    <div className="hidden flex-col sm:flex">
      {transactions.map((tx) => {
        const isIncome = tx.type === 'INCOME';
        const isOrphan = !tx.category?.trim();

        const categoryName = isOrphan
          ? t('uncategorized')
          : tx.category;

        return (
          <m.div
            key={tx.id}
            className="group flex min-h-11 items-center gap-2.5 border-b border-border px-1 py-1.5 transition-colors last:border-0 hover:bg-accent/30"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.3 }}
          >
            <div
              className={cn(
                'shrink-0',
                isIncome
                  ? 'text-financial-positive'
                  : 'text-financial-negative',
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
              <p className="truncate text-sm font-semibold transition-colors group-hover:text-primary">
                {tx.description}
              </p>

              <div className="mt-0.5 flex min-w-0 flex-wrap items-center gap-x-2 gap-y-0.5">
                <span className="shrink-0 text-xs text-muted-foreground">
                  {formatDate(tx.date)}
                </span>

                <span
                  className="size-1 shrink-0 rounded-full bg-border"
                  aria-hidden="true"
                />

                {isOrphan ? (
                  <span className="inline-flex items-center gap-1 text-xs text-muted-foreground">
                    <HelpCircle size={12} aria-hidden="true" />
                    <span>{categoryName}</span>
                  </span>
                ) : (
                  <Badge
                    variant="outline"
                    className="h-auto max-w-full whitespace-normal wrap-anywhere rounded-sm border-none px-1 text-xs font-medium"
                    style={
                      tx.categoryColor
                        ? {
                            color: tx.categoryColor,
                            backgroundColor: `${tx.categoryColor}20`,
                          }
                        : undefined
                    }
                  >
                    {categoryName}
                  </Badge>
                )}
              </div>
            </div>

            <div className="shrink-0 text-right">
              <p
                className={cn(
                  'text-sm font-bold tabular-nums',
                  isIncome
                    ? 'text-financial-positive'
                    : 'text-financial-negative',
                )}
              >
                {isIncome ? '+' : '-'}
                {formatCurrency(Math.abs(tx.amount))}
              </p>
            </div>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="size-8"
                  aria-label={t('moreOptions')}
                >
                  <MoreHorizontal
                    size={16}
                    aria-hidden="true"
                  />
                </Button>
              </DropdownMenuTrigger>

              <DropdownMenuContent
                align="end"
                className="border-border bg-card"
              >
                <DropdownMenuItem
                  className="cursor-pointer"
                  onClick={() => onEdit(tx)}
                >
                  {t('edit')}
                </DropdownMenuItem>

                <DropdownMenuItem
                  className="cursor-pointer text-destructive"
                  onClick={() => onDelete(tx)}
                >
                  {t('delete')}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </m.div>
        );
      })}
    </div>
  );
});
