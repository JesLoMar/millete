import { Outlet } from 'react-router-dom';
import WikiSidebar from './WikiSidebar';
import { LanguageSelector } from '@/shared/components/LanguageSelector';
import { Button } from '@/shared/components/ui/button';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

export default function WikiLayout() {
  const { t } = useTranslation('wiki');

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b sticky top-0 bg-background/95 backdrop-blur z-50">
        <div className="max-w-7xl mx-auto flex items-center justify-between h-14 px-4">
          <Link to="/wiki" className="font-bold text-lg">
            Millete Wiki
          </Link>
          <div className="flex items-center gap-3">
            <LanguageSelector />
            <Button variant="outline" size="sm" asChild>
              <Link to="/login">
                {t('header.goToApp', 'Ir a la app')}
              </Link>
            </Button>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto flex gap-8 px-4 py-10">
        <WikiSidebar />
        <Outlet />
      </div>
    </div>
  );
}