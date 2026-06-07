import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { Info, ExternalLink } from "lucide-react"

export function FirstSteps() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const steps = t('info.steps.items', { returnObjects: true }) as Array<{
    number: string
    title: string
    description: string
  }>

  return (
    <div className="w-full max-w-xl mx-auto space-y-6 lg:space-y-8 px-4 sm:px-0 py-4">
      <div className="space-y-3">
        <h2 className="text-4xl sm:text-5xl font-serif text-white leading-tight">
          {t('info.steps.title')}
        </h2>
        <p className="text-muted-foreground text-base max-w-md">
          {t('info.steps.subtitle')}
        </p>
      </div>

      <div className="space-y-6 lg:space-y-8 py-2">
        {steps?.map((step) => (
          <div key={step.number} className="flex gap-4 sm:gap-6 items-start group">
            <div className="shrink-0 size-10 sm:size-12 rounded-full border-2 border-primary/50 flex items-center justify-center bg-primary/30 text-white font-mono text-xs sm:text-sm tracking-tighter font-bold shadow-[0_0_15px_rgba(var(--primary),0.2)]">
              {step.number}
            </div>
            <div className="space-y-1.5 min-w-0 flex-1">
              <h3 className="text-base sm:text-lg font-semibold text-white group-hover:text-primary transition-colors">
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
        <button
          onClick={() => navigate('/wiki')}
          className="w-full p-4 sm:p-5 rounded-xl bg-secondary/20 border border-border/50 flex items-center justify-between group cursor-pointer hover:bg-secondary/40 transition-all"
        >
          <div className="flex items-center gap-3 sm:gap-4">
            <div className="bg-primary/10 p-2 rounded-lg shrink-0">
              <Info className="size-4 sm:size-5 text-primary" />
            </div>
            <span className="text-sm sm:text-base font-medium text-primary">
              {t('info.wiki.link')}
            </span>
          </div>
          <ExternalLink className="size-4 sm:size-5 text-primary group-hover:translate-x-1 transition-transform shrink-0" />
        </button>
      </div>
    </div>
  )
}