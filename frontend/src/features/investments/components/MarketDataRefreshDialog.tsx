import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle, Info, Loader2, Plus, X } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import { CURRENCIES } from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { isValidCurrencyCode } from '../utils';

interface MarketDataRefreshDialogProps {
  localCurrency: string;
  assetCurrencies: string[];
  onOpenChange: (open: boolean) => void;
}

function initialPairs(
  localCurrency: string,
  assetCurrencies: string[],
): string[] {
  const currencies = Array.from(
    new Set(
      assetCurrencies
        .map((currency) => currency.trim().toUpperCase())
        .filter(
          (currency) =>
            currency.length > 0 &&
            currency !== localCurrency,
        ),
    ),
  );

  return currencies.map(
    (currency) => `${currency}/${localCurrency}`,
  );
}

export function MarketDataRefreshDialog({
  localCurrency,
  assetCurrencies,
  onOpenChange,
}: MarketDataRefreshDialogProps) {
  const { t } = useTranslation('investments');
  const { refreshMarketData } = useInvestmentMutations();

  const [pairs, setPairs] = useState<string[]>(() =>
    initialPairs(localCurrency, assetCurrencies),
  );
  const [baseCurrency, setBaseCurrency] = useState('USD');
  const [quoteCurrency, setQuoteCurrency] = useState(
    localCurrency,
  );
  const [pairError, setPairError] = useState(false);

  const normalizedBase = baseCurrency.trim().toUpperCase();
  const normalizedQuote = quoteCurrency
    .trim()
    .toUpperCase();

  const handleAddPair = () => {
    if (
      !isValidCurrencyCode(normalizedBase) ||
      !isValidCurrencyCode(normalizedQuote) ||
      normalizedBase === normalizedQuote
    ) {
      setPairError(true);
      return;
    }

    const pair = `${normalizedBase}/${normalizedQuote}`;

    setPairs((previous) =>
      previous.some(
        (item) => item.toUpperCase() === pair,
      )
        ? previous
        : [...previous, pair],
    );
    setPairError(false);
  };

  const removePair = (pair: string) => {
    setPairs((previous) =>
      previous.filter((item) => item !== pair),
    );
  };

  const handleSubmit = async () => {
    try {
      await refreshMarketData.mutateAsync({
        currencyPairs: pairs,
      });
      onOpenChange(false);
    } catch {
      // La mutation muestra el error y el diálogo permanece abierto.
    }
  };

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-lg">
        <div className="space-y-4">
          <DialogHeader>
            <DialogTitle>{t('marketData.title')}</DialogTitle>
            <DialogDescription>
              {t('marketData.description')}
            </DialogDescription>
          </DialogHeader>

          <div className="flex items-start gap-2 rounded-md border border-border bg-muted/40 p-3 text-xs text-muted-foreground">
            <Info
              className="mt-0.5 size-4 shrink-0 text-primary"
              aria-hidden="true"
            />
            {t('marketData.autoRange')}
          </div>

          <div className="space-y-2">
            <Label>{t('marketData.currencyPairs')}</Label>

            {pairs.length === 0 ? (
              <p className="rounded-md border border-dashed border-border px-3 py-4 text-center text-xs text-muted-foreground">
                {t('marketData.noPairs')}
              </p>
            ) : (
              <ul className="flex flex-wrap gap-1.5">
                {pairs.map((pair) => (
                  <li key={pair}>
                    <Badge
                      variant="secondary"
                      className="gap-1 pr-1"
                    >
                      {pair}
                      <button
                        type="button"
                        onClick={() => removePair(pair)}
                        disabled={refreshMarketData.isPending}
                        className="rounded p-0.5 hover:bg-accent"
                        aria-label={t(
                          'marketData.removePair',
                          { pair },
                        )}
                      >
                        <X
                          className="size-3"
                          aria-hidden="true"
                        />
                      </button>
                    </Badge>
                  </li>
                ))}
              </ul>
            )}

            <div className="flex flex-col gap-2 sm:flex-row">
              <Input
                value={baseCurrency}
                list="investment-currencies"
                maxLength={3}
                onChange={(event) => {
                  setBaseCurrency(
                    event.target.value.toUpperCase(),
                  );
                  setPairError(false);
                }}
                placeholder={t('marketData.baseCurrency')}
                aria-label={t('marketData.baseCurrency')}
                className="uppercase"
                disabled={refreshMarketData.isPending}
              />
              <Input
                value={quoteCurrency}
                list="investment-currencies"
                maxLength={3}
                onChange={(event) => {
                  setQuoteCurrency(
                    event.target.value.toUpperCase(),
                  );
                  setPairError(false);
                }}
                placeholder={t('marketData.quoteCurrency')}
                aria-label={t('marketData.quoteCurrency')}
                className="uppercase"
                disabled={refreshMarketData.isPending}
              />
              <Button
                type="button"
                variant="outline"
                onClick={handleAddPair}
                disabled={refreshMarketData.isPending}
                className="shrink-0 gap-1"
              >
                <Plus className="size-4" aria-hidden="true" />
                {t('marketData.addPair')}
              </Button>
            </div>

            {pairError && (
              <p className="text-xs text-destructive">
                {t('marketData.invalidPair')}
              </p>
            )}

            <p className="text-xs text-muted-foreground">
              {t('marketData.pairsHelp')}
            </p>
          </div>

          <div className="flex items-start gap-2 rounded-md border border-warning/40 bg-warning/10 p-3 text-xs text-muted-foreground">
            <AlertTriangle
              className="mt-0.5 size-4 shrink-0 text-warning"
              aria-hidden="true"
            />
            {t('marketData.slowWarning')}
          </div>

          <datalist id="investment-currencies">
            {CURRENCIES.map((code) => (
              <option key={code} value={code} />
            ))}
          </datalist>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={refreshMarketData.isPending}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={refreshMarketData.isPending}
          >
            {refreshMarketData.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('marketData.refresh')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
