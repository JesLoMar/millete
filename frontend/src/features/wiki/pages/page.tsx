import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import WikiContent from '../components/WikiContent';

export default function WikiPage() {
  const { section } = useParams<{ section?: string }>();
  const { t } = useTranslation('wiki');

  if (!section) {
    return (
      <div className="flex-1">
        <h1 className="text-2xl font-bold mb-4">
          {t('index.title', 'Guía de uso')}
        </h1>
        <p className="text-muted-foreground">
          {t('index.description', 'Selecciona una sección en el menú lateral para ver su contenido.')}
        </p>
      </div>
    );
  }

  return <WikiContent sectionKey={section} />;
}