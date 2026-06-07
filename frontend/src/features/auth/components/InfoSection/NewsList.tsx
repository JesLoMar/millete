import { useTranslation } from "react-i18next"

export function NewsList() {
  const { t } = useTranslation()

  const newsItems = t('info.news.items', { returnObjects: true }) as Array<{
    tag?: string
    title: string
    description: string
  }>

  return (
    <div className="w-full max-w-xl mx-auto space-y-6 lg:space-y-8">
      <div className="w-[80%] max-w-2xl space-y-6 lg:space-y-10 px-4 sm:px-0">
        <div className="space-y-3">
          <h2 className="text-4xl sm:text-5xl font-serif text-white leading-tight">
            {t('info.news.title')}
          </h2>
          <p className="text-muted-foreground text-sm">
            {t('info.news.subtitle')}
          </p>
        </div>

        <div className="space-y-6 lg:space-y-8">
          {newsItems?.map((item, index) => (
            <div
              key={item.title}
              className={`relative pl-10 border-l-2 ${index === 0 ? 'border-primary/30' : 'border-border/50'}`}
            >
              <div className={`absolute -left-1.25 top-0 w-2 h-2 rounded-full ${index === 0 ? 'bg-primary shadow-[0_0_10px_var(--primary)]' : 'bg-border'}`} />
              <div className="space-y-2">
                <div className="flex items-center gap-3">
                  <h4 className="text-white font-semibold text-base lg:text-lg">
                    {item.title}
                  </h4>
                  {item.tag && (
                    <span className="text-[10px] px-2.5 py-0.5 rounded-full bg-primary/20 text-primary border border-primary/30 font-bold tracking-wider whitespace-nowrap">
                      {item.tag}
                    </span>
                  )}
                </div>
                <p className="text-sm text-secondary-foreground/60 leading-relaxed">
                  {item.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}