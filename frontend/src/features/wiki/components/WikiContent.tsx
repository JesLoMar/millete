import { useTranslation } from 'react-i18next';
import type { WikiSection } from '../types';

interface WikiContentProps {
  sectionKey: string;
}

export default function WikiContent({ sectionKey }: WikiContentProps) {
  const { t } = useTranslation('wiki');
  const sections = t('sections', { returnObjects: true }) as unknown as Record<string, WikiSection>;
  const section: WikiSection | undefined = sections?.[sectionKey];

  if (!section) {
    return (
      <div className="flex-1">
        <h1 className="text-2xl font-bold mb-4">
          {t('notFound.title', 'Sección no encontrada')}
        </h1>
        <p className="text-muted-foreground">
          {t('notFound.description', 'La sección que buscas no existe o no está disponible en este idioma.')}
        </p>
      </div>
    );
  }

  return (
    <div className="flex-1 min-w-0">
      <h1 className="text-2xl font-bold mb-2">{section.title}</h1>
      <p className="text-muted-foreground mb-8">{section.description}</p>

      <div className="space-y-10">
        {section.topics.map((topic, i) => (
          <div key={i}>
            <h2 className="text-lg font-semibold mb-2">{topic.title}</h2>
            <p className="text-muted-foreground whitespace-pre-line leading-relaxed">
              {topic.content}
            </p>
            {topic.image && (
              <img
                src={topic.image}
                alt={topic.title}
                className="mt-4 rounded-lg border max-w-full"
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );
}