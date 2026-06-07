import { useState, useCallback } from 'react';
import { Outlet } from 'react-router-dom';
import WikiSidebar from './WikiSidebar';
import { LanguageSelector } from '@/shared/components/LanguageSelector';
import { Button } from '@/shared/components/ui/button';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { ThemeSelector } from '@/shared/components/ThemeSelector';

export default function WikiLayout() {
  const { t } = useTranslation('wiki');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const openSidebar = useCallback(() => setIsSidebarOpen(true), []);
  const closeSidebar = useCallback(() => setIsSidebarOpen(false), []);

  return (
    <div className="min-h-dvh bg-background">
      <header className="border-b sticky top-0 bg-background/95 backdrop-blur z-50">
        <div className="max-w-7xl mx-auto flex items-center justify-between h-14 px-4">
          <div className="flex items-center gap-3">
            <Button
              variant="ghost"
              size="icon"
              className="md:hidden size-9 -ml-1"
              onClick={openSidebar}
              aria-label={t('header.openMenu', 'Abrir menú de navegación')}
            >
              <Menu size={20} aria-hidden="true" />
            </Button>
            <Link to="/wiki" className="font-bold text-lg">
              Millete Wiki
            </Link>
          </div>
          <div className="flex items-center gap-2 sm:gap-3">
            <LanguageSelector />
            <ThemeSelector />
            <Button variant="outline" size="sm" asChild className="text-xs sm:text-sm h-8 sm:h-9">
              <Link to="/login">
                {t('header.goToApp', 'Ir a la app')}
              </Link>
            </Button>
          </div>
        </div>
      </header>

      {isSidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm md:hidden"
          onClick={closeSidebar}
          aria-hidden="true"
        />
      )}

      <div className="max-w-7xl mx-auto flex gap-6 lg:gap-8 px-4 py-6 sm:py-10">
        <aside
          className={`
            fixed inset-y-0 left-0 z-50 w-64 bg-background border-r
            transition-transform duration-300 ease-in-out
            md:static md:z-auto md:translate-x-0 md:border-0 md:bg-transparent
            ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}
          `}
        >
          <div className="flex items-center justify-between p-4 border-b md:hidden">
            <span className="font-semibold text-sm">
              {t('header.navigation', 'Navegación')}
            </span>
            <Button
              variant="ghost"
              size="icon"
              onClick={closeSidebar}
              className="size-8"
              aria-label={t('header.closeMenu', 'Cerrar menú')}
            >
              <X size={18} />
            </Button>
          </div>
          {/* Contenido del sidebar con scroll */}
          <div className="h-full overflow-y-auto p-4 md:p-0">
            <WikiSidebar onNavigate={closeSidebar} />
          </div>
        </aside>

        <main className="flex-1 min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  );
}