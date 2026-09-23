import { useTranslation } from 'react-i18next';

interface NewsItem {
  tag?: string;
  title: string;
  description: string;
}

export function NewsList() {
  const { t } = useTranslation();

  const newsItemsValue = t('info:news.items', {
    returnObjects: true,
  });

  const newsItems: NewsItem[] = Array.isArray(newsItemsValue)
    ? (newsItemsValue as NewsItem[])
    : [];

  return (
    <div className="mx-auto w-full max-w-xl space-y-6 lg:space-y-8">
      <div className="w-[80%] max-w-2xl space-y-6 px-4 lg:space-y-10 sm:px-0">
        <div className="space-y-3">
          <h2 className="font-serif text-4xl leading-tight text-foreground sm:text-5xl">
            {t('info:news.title')}
          </h2>

          <p className="text-sm text-muted-foreground">
            {t('info:news.subtitle')}
          </p>
        </div>

        <ul className="list-none space-y-6 lg:space-y-8">
          {newsItems.map((item, index) => (
            <li
              key={`${item.title}-${index}`}
              className={`relative border-l-2 pl-10 ${
                index === 0
                  ? 'border-primary/30'
                  : 'border-border/50'
              }`}
            >
              <div
                className={`absolute -left-1.25 top-0 size-2 rounded-full ${
                  index === 0 ? 'bg-primary' : 'bg-border'
                }`}
                aria-hidden="true"
              />

              <div className="space-y-2">
                <div className="flex items-center gap-3">
                  <h3 className="text-base font-semibold text-foreground lg:text-lg">
                    {item.title}
                  </h3>

                  {item.tag && (
                    <span className="whitespace-nowrap rounded-full border border-primary/30 bg-primary/20 px-2.5 py-0.5 text-xs font-bold tracking-wider text-primary">
                      {item.tag}
                    </span>
                  )}
                </div>

                <p className="text-sm leading-relaxed text-foreground/70">
                  {item.description}
                </p>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}