import {
  useId,
  useState,
  type FormEvent,
} from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2, Sparkles } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { cn } from '@/lib/utils';

import { assistantService } from '../services/assistant.service';
import type { AssistantInterpretation } from '../types';

import { AssistantActionDialog } from './AssistantActionDialog';

const EXAMPLE_KEYS = [
  'expense',
  'recurring',
  'savings',
  'contribution',
  'category',
] as const;

export function AssistantCard() {
  const { t } = useTranslation('dashboard');

  const [input, setInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [interpretation, setInterpretation] =
    useState<AssistantInterpretation | null>(null);

  const inputId = useId();

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault();

    const trimmed = input.trim();

    if (!trimmed) {
      setError(String(t('assistant.errors.empty')));
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const result =
        await assistantService.interpret(trimmed);

      if (result.status === 'UNKNOWN') {
        setError(String(t('assistant.errors.unknown')));
        return;
      }

      if (
        !result.action ||
        !result.data ||
        result.action === 'EDIT_CATEGORY'
      ) {
        setError(
          String(t('assistant.errors.unsupported')),
        );
        return;
      }

      setInterpretation(result);
    } catch {
      setError(String(t('assistant.errors.generic')));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleExampleClick = (key: (typeof EXAMPLE_KEYS)[number]) => {
    setInput(String(t(`assistant.examples.${key}`)));
    setError(null);
  };

  return (
    <>
      <Card className="gap-4 overflow-hidden border-border/60 bg-linear-to-br from-card via-card to-primary/5 shadow-sm">
        <CardHeader className="gap-1 pb-0">
          <CardTitle className="flex items-center gap-2.5 text-base sm:text-lg">
            <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <Sparkles
                className="size-5"
                aria-hidden="true"
              />
            </span>
            {t('assistant.title')}
          </CardTitle>

          <CardDescription className="leading-relaxed">
            {t('assistant.subtitle')}
          </CardDescription>
        </CardHeader>

        <CardContent>
          <form
            onSubmit={handleSubmit}
            className="flex flex-col gap-3"
          >
            <label
              htmlFor={inputId}
              className="sr-only"
            >
              {t('assistant.title')}
            </label>

            <div className="flex flex-col gap-3 sm:flex-row sm:items-stretch">
              <textarea
                id={inputId}
                rows={2}
                value={input}
                onChange={(event) => {
                  setInput(event.target.value);
                  setError(null);
                }}
                placeholder={String(
                  t('assistant.placeholder'),
                )}
                disabled={isSubmitting}
                className={cn(
                  'min-h-19 w-full resize-none rounded-lg border border-border bg-background px-3 py-2 text-sm shadow-xs',
                  'outline-none transition-[color,box-shadow] placeholder:text-muted-foreground',
                  'focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50',
                  'disabled:cursor-not-allowed disabled:opacity-50',
                )}
              />

              <Button
                type="submit"
                disabled={isSubmitting}
                className="h-auto min-h-11 shrink-0 gap-2 bg-primary px-5 font-semibold hover:bg-primary/90 sm:self-stretch"
              >
                {isSubmitting ? (
                  <Loader2
                    className="size-4 animate-spin"
                    aria-hidden="true"
                  />
                ) : (
                  <Sparkles
                    className="size-4"
                    aria-hidden="true"
                  />
                )}

                {isSubmitting
                  ? t('assistant.interpreting')
                  : t('assistant.submit')}
              </Button>
            </div>

            <div className="flex flex-wrap items-center gap-1.5">
              <span className="mr-1 text-xs font-medium text-muted-foreground">
                {t('assistant.examplesLabel')}
              </span>

              {EXAMPLE_KEYS.map((key) => (
                <button
                  key={key}
                  type="button"
                  onClick={() => handleExampleClick(key)}
                  disabled={isSubmitting}
                  className="rounded-full border border-border/60 bg-background/70 px-2.5 py-1 text-xs text-muted-foreground transition-colors hover:border-primary/40 hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {t(`assistant.examples.${key}`)}
                </button>
              ))}
            </div>

            {error && (
              <p
                className="text-sm text-destructive"
                role="alert"
              >
                {error}
              </p>
            )}
          </form>
        </CardContent>
      </Card>

      {interpretation && (
        <AssistantActionDialog
          interpretation={interpretation}
          onClose={() => setInterpretation(null)}
        />
      )}
    </>
  );
}
