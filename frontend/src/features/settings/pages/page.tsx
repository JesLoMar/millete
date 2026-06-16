import { TopNav } from '@/shared/components/TopNav';
import { Sidebar } from '@/shared/components/Sidebar';
import { CurrencySelector } from '../components/CurrencySelector';
import { TelegramStatus } from '../components/TelegramStatus';

export const SettingsPage = () => {

  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <TopNav />
        <main className="p-6 space-y-6">
          <CurrencySelector />
          <TelegramStatus />
        </main>
      </div>
    </div>
  );
};