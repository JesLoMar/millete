import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, TrendingUp } from 'lucide-react';

import {
  INVESTMENT_TYPES,
} from '../constants';
import {
  useInvestmentMutations,
} from '../hooks/useInvestmentMutations';
import type {
  InvestmentAssetType,
  RegisterInvestmentRequest,
} from '../types';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';

interface InvestmentForm {
  assetName: string;
  ticker: string;
  quantity: string;
  purchasePrice: string;
  type: InvestmentAssetType;
  purchaseDate: string;
}

const getInitialForm = (): InvestmentForm => ({
  assetName: '',
  ticker: '',
  quantity: '',
  purchasePrice: '',
  type: 'STOCK',
  purchaseDate: new Date().toISOString().split('T')[0],
});

export function NewInvestmentDialog() {
  const { t } = useTranslation();

  const {
    createInvestment,
    isCreating,
  } = useInvestmentMutations();

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<InvestmentForm>(
    getInitialForm,
  );

  const inputRef = useRef<HTMLInputElement>(null);

  const resetForm = () => {
    setForm(getInitialForm());
  };

  const handleSave = async () => {
    if (
      !form.assetName.trim() ||
      !form.quantity ||
      !form.purchasePrice
    ) {
      return;
    }

    const request: RegisterInvestmentRequest = {
      assetName: form.assetName.trim(),
      ticker:
        form.ticker.toUpperCase().trim() || undefined,
      quantity: Number(form.quantity),
      purchasePrice: Number(form.purchasePrice),
      type: form.type,
      purchaseDate: form.purchaseDate,
    };

    try {
      await createInvestment.mutateAsync(request);

      setOpen(false);
      resetForm();
    } catch {
      // El feedback del error lo gestiona la mutation.
    }
  };

  const isValid =
    form.assetName.trim() &&
    form.quantity &&
    Number(form.quantity) > 0 &&
    form.purchasePrice &&
    Number(form.purchasePrice) > 0;

  return (
    <Dialog
      open={open}
      onOpenChange={(isOpen) => {
        setOpen(isOpen);

        if (!isOpen) {
          resetForm();
        }
      }}
    >
      <DialogTrigger asChild>
        <Button className="h-9 shrink-0 gap-2 bg-primary px-4 font-semibold hover:bg-primary/90">
          <Plus size={16} />
          {t('investments:new')}
        </Button>
      </DialogTrigger>

      <DialogContent
        className="border-border bg-card sm:max-w-lg"
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-xl font-semibold">
              <TrendingUp className="size-5 text-primary" />
              {t('investments:newTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="grid grid-cols-3 gap-3 sm:gap-4">
              <div className="col-span-2 space-y-2">
                <Label className="text-sm font-semibold">
                  {t('investments:assetName')}
                </Label>

                <Input
                  ref={inputRef}
                  placeholder={t(
                    'investments:assetNamePlaceholder',
                  )}
                  value={form.assetName}
                  onChange={(event) =>
                    setForm((previous) => ({
                      ...previous,
                      assetName: event.target.value,
                    }))
                  }
                  disabled={isCreating}
                  className="border-border bg-background"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-sm font-semibold">
                  {t('investments:ticker')}
                </Label>

                <Input
                  placeholder="AAPL"
                  value={form.ticker}
                  onChange={(event) =>
                    setForm((previous) => ({
                      ...previous,
                      ticker:
                        event.target.value.toUpperCase(),
                    }))
                  }
                  disabled={isCreating}
                  className="border-border bg-background"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">
                {t('investments:type')}
              </Label>

              <Select
                value={form.type}
                onValueChange={(value) => {
                  const selectedType =
                    INVESTMENT_TYPES.find(
                      (investmentType) =>
                        investmentType.value === value,
                    );

                  if (!selectedType) {
                    return;
                  }

                  setForm((previous) => ({
                    ...previous,
                    type: selectedType.value,
                  }));
                }}
              >
                <SelectTrigger className="border-border bg-background">
                  <SelectValue />
                </SelectTrigger>

                <SelectContent className="border-border bg-card">
                  {INVESTMENT_TYPES.map((investmentType) => (
                    <SelectItem
                      key={investmentType.value}
                      value={investmentType.value}
                    >
                      <div className="flex items-center gap-2">
                        <investmentType.icon
                          size={14}
                          className={investmentType.color}
                        />
                        {t(investmentType.labelKey)}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">
                  {t('investments:quantity')}
                </Label>

                <Input
                  type="number"
                  placeholder="0.00"
                  value={form.quantity}
                  onChange={(event) =>
                    setForm((previous) => ({
                      ...previous,
                      quantity: event.target.value,
                    }))
                  }
                  disabled={isCreating}
                  className="border-border bg-background"
                  min="0.0001"
                  step="any"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-sm font-semibold">
                  {t('investments:purchasePrice')}
                </Label>

                <Input
                  type="number"
                  placeholder="0.00"
                  value={form.purchasePrice}
                  onChange={(event) =>
                    setForm((previous) => ({
                      ...previous,
                      purchasePrice: event.target.value,
                    }))
                  }
                  disabled={isCreating}
                  className="border-border bg-background"
                  min="0.01"
                  step="0.01"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">
                {t('investments:purchaseDate')}
              </Label>

              <Input
                type="date"
                value={form.purchaseDate}
                onChange={(event) =>
                  setForm((previous) => ({
                    ...previous,
                    purchaseDate: event.target.value,
                  }))
                }
                disabled={isCreating}
                className="border-border bg-background"
              />
            </div>
          </div>

          <DialogFooter className="sticky bottom-0 gap-2 bg-card pb-1 pt-2">
            <Button
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={isCreating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              onClick={handleSave}
              disabled={isCreating || !isValid}
              className="bg-primary px-6 hover:bg-primary/90"
            >
              {isCreating ? (
                <Spinner size={20} />
              ) : (
                t('investments:save')
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}