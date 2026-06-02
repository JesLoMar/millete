import { useTranslation } from 'react-i18next';
import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/utils';

export default function WikiSidebar() {
  const { t } = useTranslation('wiki');
  const wiki = t('sections', { returnObjects: true }) as unknown as Record<string, { title: string }>;
  const sections = wiki ?? {};

  return (
    <nav className="w-64 shrink-0">
      <ul className="space-y-1 sticky top-24">
        {Object.entries(sections).map(([key, section]) => (
          <li key={key}>
            <NavLink
              to={`/wiki/${key}`}
              className={({ isActive }) =>
                cn(
                  'block px-4 py-2 rounded-md text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary/10 text-primary'
                    : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                )
              }
            >
              {section.title}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}