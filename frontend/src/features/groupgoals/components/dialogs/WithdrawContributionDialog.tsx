import {
  useId,
  useRef,
  useState,
  type FormEvent,
} from 'react'
import { useTranslation } from 'react-i18next'

import { Spinner } from '@/shared/components/Spinner'
import { Button } from '@/shared/components/core/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog'
import { Input } from '@/shared/components/core/input'
import { Label } from '@/shared/components/core/label'
import {
  formatCurrency,
  getLocalCurrencyCode,
} from '@/shared/utils/i18nFormat'

interface WithdrawContributionDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSave: (amount: number) => Promise<void>
  isSaving?: boolean
  /**
   * Saldo neto del miembro actual dentro del objetivo. `null` mientras no se
   * conoce (por ejemplo, si el perfil aún no ha cargado).
   */
  availableBalance: number | null
}

function toCents(value: number): number {
  return Math.round(value * 100)
}

type WithdrawError = 'invalid' | 'exceeds' | 'unavailable'

export function WithdrawContributionDialog({
  open,
  onOpenChange,
  onSave,
  isSaving = false,
  availableBalance,
}: WithdrawContributionDialogProps) {
  const { t } = useTranslation(['groupGoals', 'common'])
  const [amount, setAmount] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const inputId = useId()
  const hintId = useId()
  const errorId = useId()

  const parsedAmount = amount.trim().length > 0 ? Number(amount) : null
  const isParsedAmountValid =
    parsedAmount !== null &&
    Number.isFinite(parsedAmount) &&
    parsedAmount > 0

  const error: WithdrawError | null =
    availableBalance === null
      ? 'unavailable'
      : !isParsedAmountValid
        ? 'invalid'
        : toCents(parsedAmount) > toCents(availableBalance)
          ? 'exceeds'
          : null

  const canSubmit =
    !isSaving &&
    availableBalance !== null &&
    availableBalance > 0 &&
    error === null

  const showError =
    error !== null &&
    (submitted || error === 'exceeds')

  const resetForm = () => {
    setAmount('')
    setSubmitted(false)
  }

  const handleOpenChange = (isOpen: boolean) => {
    if (isSaving) {
      return
    }

    if (!isOpen) {
      resetForm()
    }

    onOpenChange(isOpen)
  }

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault()

    if (isSaving) {
      return
    }

    if (
      !isParsedAmountValid ||
      parsedAmount === null ||
      error !== null
    ) {
      setSubmitted(true)
      return
    }

    try {
      await onSave(parsedAmount)
      resetForm()
      onOpenChange(false)
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={handleOpenChange}
    >
      <DialogContent
        className="bg-card border-border sm:max-w-100"
        onOpenAutoFocus={(event) => {
          event.preventDefault()
          inputRef.current?.focus()
        }}
      >
        <DialogHeader>
          <DialogTitle>
            {t('groupGoals:withdrawTitle')}
          </DialogTitle>
        </DialogHeader>

        <form
          onSubmit={handleSubmit}
          noValidate
          className="py-4 space-y-4"
        >
          <div className="space-y-2">
            <Label
              htmlFor={inputId}
              className="text-sm font-semibold"
            >
              {t('groupGoals:amount')} ({getLocalCurrencyCode()})
            </Label>

            <Input
              id={inputId}
              ref={inputRef}
              type="number"
              inputMode="decimal"
              placeholder="0.00"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
              disabled={isSaving}
              className="bg-background border-border"
              min="0.01"
              max={availableBalance ?? undefined}
              step="0.01"
              aria-invalid={showError}
              aria-describedby={
                showError ? `${hintId} ${errorId}` : hintId
              }
            />

            <p
              id={hintId}
              className="text-xs text-muted-foreground"
            >
              {availableBalance === null ? (
                t('groupGoals:withdrawBalanceUnknown')
              ) : availableBalance > 0 ? (
                t('groupGoals:withdrawAvailable', {
                  amount: formatCurrency(availableBalance),
                })
              ) : (
                t('groupGoals:withdrawNothingAvailable')
              )}
            </p>

            {showError && (
              <p
                id={errorId}
                className="text-xs text-destructive"
              >
                {error === 'exceeds'
                  ? t('groupGoals:withdrawExceeds', {
                      amount: formatCurrency(
                        availableBalance ?? 0,
                      ),
                    })
                  : error === 'unavailable'
                    ? t('groupGoals:withdrawBalanceUnknown')
                    : t('groupGoals:withdrawAmountInvalid')}
              </p>
            )}
          </div>

          <DialogFooter className="gap-2 sm:gap-0">
            <Button
              type="button"
              variant="outline"
              onClick={() => handleOpenChange(false)}
              disabled={isSaving}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="submit"
              disabled={!canSubmit}
              className="min-w-24"
            >
              {isSaving ? (
                <Spinner size={20} />
              ) : (
                t('groupGoals:withdrawSubmit')
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
