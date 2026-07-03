import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';
import { useEffect, useRef } from 'react';
import { TopNav } from '@/shared/components/TopNav';
import { Sidebar } from '@/shared/components/Sidebar';
import { PersonalInfoSection } from '../components/PersonalInfoSection';
import { ChangePasswordSection } from '../components/ChangePasswordSection';
import { TelegramSection } from '../components/TelegramSection';
import { SessionsSection } from '../components/SessionsSection';
import { DeleteAccountSection } from '../components/DeleteAccountSection';
import { NotificationsTable } from '../components/NotificationsTable';

export const ProfilePage = () => {
  const { t } = useTranslation('userProfile');
  const [searchParams] = useSearchParams();
  const notificationsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const section = searchParams.get('section');
    if (section === 'notifications' && notificationsRef.current) {
      notificationsRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, [searchParams]);

  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden pt-16">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-6">
          <div className="max-w-2xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold">{t('title')}</h1>
            <PersonalInfoSection />
            <ChangePasswordSection />
            <TelegramSection />
            <div ref={notificationsRef} id="notifications-section">
              <NotificationsTable />
            </div>
            <SessionsSection />
            <DeleteAccountSection />
          </div>
        </main>
      </div>
    </div>
  );
};
