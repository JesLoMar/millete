import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { useTranslation } from 'react-i18next';
import {
  AlertTriangle,
  FileJson,
  Loader2,
  Upload,
} from 'lucide-react';

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

import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import type { InvestmentLedgerSnapshot } from '../types';
import { formatDateTime, readJsonFile } from '../utils';

interface RestoreLedgerDialogProps {
  onOpenChange: (open: boolean) => void;
}

function isValidSnapshot(
  value: unknown,
): value is InvestmentLedgerSnapshot {
  if (value == null || typeof value !== 'object') {
    return false;
  }

  const candidate = value as Record<string, unknown>;

  return (
    typeof candidate.version === 'number' &&
    typeof candidate.trackingStartAt === 'string' &&
    Array.isArray(candidate.userAssets) &&
    Array.isArray(candidate.userAssetPrices) &&
    Array.isArray(candidate.holdings) &&
    Array.isArray(candidate.activities) &&
    Array.isArray(candidate.audits)
  );
}

export function RestoreLedgerDialog({
  onOpenChange,
}: RestoreLedgerDialogProps) {
  const { t } = useTranslation('investments');
  const { restoreLedger } = useInvestmentMutations();
  const [snapshot, setSnapshot] =
    useState<InvestmentLedgerSnapshot | null>(null);
  const [fileName, setFileName] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [confirmed, setConfirmed] = useState(false);

  const handleFile = async (
    event: ChangeEvent<HTMLInputElement>,
  ) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    try {
      const parsed = await readJsonFile<unknown>(file);

      if (!isValidSnapshot(parsed)) {
        setSnapshot(null);
        setFileName(file.name);
        setError(t('ledger.restore.invalidFile'));
        return;
      }

      setSnapshot(parsed);
      setFileName(file.name);
      setError(null);
      setConfirmed(false);
    } catch {
      setSnapshot(null);
      setFileName(file.name);
      setError(t('ledger.restore.parseError'));
    }
  };

  const handleSubmit = async () => {
    if (!snapshot || !confirmed) {
      return;
    }

    try {
      await restoreLedger.mutateAsync(snapshot);
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
            <DialogTitle>{t('ledger.restore.title')}</DialogTitle>
            <DialogDescription>
              {t('ledger.restore.description')}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Label htmlFor="ledger-file">
              {t('ledger.restore.file')}
            </Label>
            <Input
              id="ledger-file"
              type="file"
              accept="application/json,.json"
              onChange={(event) => void handleFile(event)}
              disabled={restoreLedger.isPending}
            />
            <p className="text-xs text-muted-foreground">
              {t('ledger.restore.fileHelp')}
            </p>
          </div>

          {fileName && !snapshot && !error && (
            <p className="text-sm text-muted-foreground">
              {fileName}
            </p>
          )}

          {error && (
            <p className="text-sm text-destructive">{error}</p>
          )}

          {snapshot && (
            <div className="space-y-2 rounded-lg border border-border p-3">
              <div className="flex items-center gap-2">
                <FileJson
                  className="size-4 text-muted-foreground"
                  aria-hidden="true"
                />
                <span className="truncate text-sm font-medium text-foreground">
                  {fileName}
                </span>
              </div>

              <dl className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs text-muted-foreground">
                <dt>{t('ledger.restore.version')}</dt>
                <dd className="text-right tabular-nums text-foreground">
                  {snapshot.version}
                </dd>
                <dt>{t('ledger.restore.trackingStartAt')}</dt>
                <dd className="text-right text-foreground">
                  {formatDateTime(snapshot.trackingStartAt)}
                </dd>
                <dt>{t('ledger.restore.userAssets')}</dt>
                <dd className="text-right tabular-nums text-foreground">
                  {snapshot.userAssets.length}
                </dd>
                <dt>{t('ledger.restore.holdings')}</dt>
                <dd className="text-right tabular-nums text-foreground">
                  {snapshot.holdings.length}
                </dd>
                <dt>{t('ledger.restore.activities')}</dt>
                <dd className="text-right tabular-nums text-foreground">
                  {snapshot.activities.length}
                </dd>
                <dt>{t('ledger.restore.audits')}</dt>
                <dd className="text-right tabular-nums text-foreground">
                  {snapshot.audits.length}
                </dd>
              </dl>
            </div>
          )}

          <div className="flex items-start gap-2 rounded-md border border-destructive/40 bg-destructive/5 p-3 text-xs text-muted-foreground">
            <AlertTriangle
              className="mt-0.5 size-4 shrink-0 text-destructive"
              aria-hidden="true"
            />
            {t('ledger.restore.warning')}
          </div>

          <label className="flex items-start gap-2 text-sm text-foreground">
            <input
              type="checkbox"
              checked={confirmed}
              onChange={(event) =>
                setConfirmed(event.target.checked)
              }
              disabled={!snapshot || restoreLedger.isPending}
              className="mt-0.5 size-4 rounded border-input accent-primary"
            />
            {t('ledger.restore.confirm')}
          </label>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={restoreLedger.isPending}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={() => void handleSubmit()}
            disabled={
              !snapshot || !confirmed || restoreLedger.isPending
            }
            className="gap-1.5"
          >
            {restoreLedger.isPending ? (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            ) : (
              <Upload className="size-4" aria-hidden="true" />
            )}
            {t('ledger.restore.submit')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
