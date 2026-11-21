import {
  useEffect,
  useId,
  useRef,
  useState,
  type FormEvent,
  type ReactElement,
} from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2, Sparkles } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/components/core/dialog';
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

export function AssistantDialog({ trigger }: { trigger: ReactElement }) {
  const { t } = useTranslation('dashboard');
  const [open, setOpen] = useState(false);
  const [interpretation, setInterpretation] = useState<AssistantInterpretation | null>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger ref={triggerRef} asChild>{trigger}</DialogTrigger>
        <DialogContent
          className="max-h-[calc(100dvh-2rem)] overflow-y-auto bg-card text-card-foreground"
          onCloseAutoFocus={(event) => {
            if (interpretation) event.preventDefault();
          }}
        >
          <DialogHeader className="pr-6 text-left">
            <DialogTitle className="flex items-center gap-2">
              <Sparkles className="size-5 text-primary" aria-hidden="true" />
              {t('assistant.title')}
            </DialogTitle>
            <DialogDescription>{t('assistant.subtitle')}</DialogDescription>
          </DialogHeader>
          <AssistantForm onInterpret={(result) => {
            setInterpretation(result);
            setOpen(false);
          }} />
        </DialogContent>
      </Dialog>
      {interpretation && (
        <AssistantActionDialog
          interpretation={interpretation}
          onClose={() => {
            setInterpretation(null);
            requestAnimationFrame(() => triggerRef.current?.focus());
          }}
        />
      )}
    </>
  );
}

function AssistantForm({ onInterpret }: { onInterpret: (result: AssistantInterpretation) => void }) {
  const { t } = useTranslation('dashboard');

  const [input, setInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inputId = useId();
  const mounted = useRef(false);

  useEffect(() => {
    mounted.current = true;
    return () => { mounted.current = false; };
  }, []);

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
      if (!mounted.current) return;

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

      onInterpret(result);
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
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <label htmlFor={inputId} className="sr-only">
          {t('assistant.title')}
        </label>
        <textarea
          id={inputId}
          rows={3}
          value={input}
          onChange={(event) => {
            setInput(event.target.value);
            setError(null);
          }}
          placeholder={String(t('assistant.placeholder'))}
          disabled={isSubmitting}
          className={cn(
            'w-full resize-y rounded-lg border border-border bg-background px-3 py-2 text-sm shadow-xs',
            'outline-none transition-[color,box-shadow] placeholder:text-muted-foreground',
            'focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50',
            'disabled:cursor-not-allowed disabled:opacity-50',
          )}
        />

        {error && <p className="text-sm text-destructive" role="alert">{error}</p>}

        <Button
          type="submit"
          disabled={isSubmitting}
          className="min-h-11 self-end gap-2 px-5 font-semibold"
        >
          {isSubmitting ? (
            <Loader2 className="size-4 animate-spin" aria-hidden="true" />
          ) : (
            <Sparkles className="size-4" aria-hidden="true" />
          )}
          {isSubmitting ? t('assistant.interpreting') : t('assistant.submit')}
        </Button>

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
              className="rounded-full border border-border/60 bg-background/70 px-2.5 py-1 text-left text-xs text-muted-foreground transition-colors hover:border-primary/40 hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
            >
              {t(`assistant.examples.${key}`)}
            </button>
          ))}
        </div>
      </form>

  );
}
