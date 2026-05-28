import { useTranslation } from "react-i18next"
import { Info, ExternalLink } from "lucide-react"

export function FirstSteps() {
  const { t } = useTranslation()

  const steps = t('info.steps.items', { returnObjects: true }) as Array<{
    number: string
    title: string
    description: string
  }>

  return (
    <div className="h-1/2 flex items-center justify-center border-b border-border/50 bg-card/50">
      <div className="w-[80%] max-w-2xl space-y-10">
        <div className="space-y-3">
          <h2 className="text-5xl font-serif text-white leading-tight">
            {t('info.steps.title')}
          </h2>
          <p className="text-muted-foreground text-base max-w-md">
            {t('info.steps.subtitle')}
          </p>
        </div>

        <div className="space-y-8 py-2">
          {steps?.map((step) => (
            <div key={step.number} className="flex gap-6 items-start group">
              <div className="shrink-0 size-12 rounded-full border border-primary/30 flex items-center justify-center bg-primary/5 text-primary font-mono text-xs tracking-tighter">
                {step.number}
              </div>
              <div className="space-y-1.5">
                <h3 className="text-lg font-semibold text-white group-hover:text-primary transition-colors">
                  {step.title}
                </h3>
                <p className="text-sm text-secondary-foreground/70 leading-relaxed">
                  {step.description}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className="pt-4">
          <div className="w-full p-5 rounded-xl bg-secondary/20 border border-border/50 flex items-center justify-between group cursor-pointer hover:bg-secondary/40 transition-all">
            <div className="flex items-center gap-4">
              <div className="bg-primary/10 p-2 rounded-lg">
                <Info className="size-5 text-primary" />
              </div>
              <span className="text-base font-medium text-primary">
                {t('info.wiki.link')}
              </span>
            </div>
            <ExternalLink className="size-5 text-primary group-hover:translate-x-1 transition-transform" />
          </div>
        </div>
      </div>
    </div>
  )
}