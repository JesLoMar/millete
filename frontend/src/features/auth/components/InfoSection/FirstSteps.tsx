import { Info, ExternalLink } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { ROUTES } from '@/app/router/routes';

interface Step {
  number: string;
  title: string;
  description: string;
}

export function FirstSteps() {
  const { t } = useTranslation();

  const stepsValue = t('info:steps.items', {
    returnObjects: true,
  });

  const steps: Step[] = Array.isArray(stepsValue)
    ? (stepsValue as Step[])
    : [];

  return (
    <div className="mx-auto w-full max-w-xl space-y-6 px-4 py-4 sm:px-0 lg:space-y-8">
      <div className="space-y-3">
        <h2 className="font-serif text-4xl leading-tight text-foreground sm:text-5xl">
          {t('info:steps.title')}
        </h2>

        <p className="max-w-md text-base text-muted-foreground">
          {t('info:steps.subtitle')}
        </p>
      </div>

      <ol className="list-none space-y-6 py-2 lg:space-y-8">
        {steps.map((step) => (
          <li
            key={step.number}
            className="group flex items-start gap-4 sm:gap-6"
          >
            <div className="flex size-10 shrink-0 items-center justify-center rounded-full border-2 border-primary/50 bg-primary/30 text-xs font-bold tracking-tighter text-foreground font-mono sm:size-12 sm:text-sm">
              {step.number}
            </div>

            <div className="min-w-0 flex-1 space-y-1.5">
              <h3 className="text-base font-semibold text-foreground transition-colors group-hover:text-primary sm:text-lg">
                {step.title}
              </h3>

              <p className="text-sm leading-relaxed text-secondary-foreground/70">
                {step.description}
              </p>
            </div>
          </li>
        ))}
      </ol>

      <div className="pt-4">
        <Link
          to={ROUTES.wiki}
          className="group flex w-full cursor-pointer items-center justify-between rounded-xl border border-border/50 bg-secondary/20 p-4 transition-all hover:bg-secondary/40 sm:p-5"
        >
          <div className="flex items-center gap-3 sm:gap-4">
            <div className="shrink-0 rounded-lg bg-primary/10 p-2">
              <Info
                className="size-4 text-primary sm:size-5"
                aria-hidden="true"
              />
            </div>

            <span className="text-sm font-medium text-primary sm:text-base">
              {t('info:wiki.link')}
            </span>
          </div>

          <ExternalLink
            className="size-4 shrink-0 text-primary transition-transform group-hover:translate-x-1 sm:size-5"
            aria-hidden="true"
          />
        </Link>
      </div>
    </div>
  );
}