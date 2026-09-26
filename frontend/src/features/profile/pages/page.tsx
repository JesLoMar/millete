import { useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';

import { ChangePasswordSection } from '../components/ChangePasswordSection';
import { DeleteAccountSection } from '../components/DeleteAccountSection';
import { NotificationsTable } from '../components/NotificationsTable';
import { PersonalInfoSection } from '../components/PersonalInfoSection';
import { SessionsSection } from '../components/SessionsSection';
import { TimezoneSection } from '../components/TimezoneSection';

export const ProfilePage = () => {
  const { t } = useTranslation('userProfile');
  const [searchParams] = useSearchParams();

  const notificationsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (searchParams.get('section') !== 'notifications') {
      return;
    }

    notificationsRef.current?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    });
  }, [searchParams]);

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />

      <div className="flex flex-1 flex-col overflow-hidden pt-16">
        <TopNav />

        <main className="flex-1 overflow-y-auto p-6">
          <div className="mx-auto max-w-2xl space-y-6">
            <h1 className="text-2xl font-bold">
              {t('title')}
            </h1>

            <PersonalInfoSection />
            <TimezoneSection />
            <ChangePasswordSection />

            <div
              ref={notificationsRef}
              id="notifications-section"
              className="scroll-mt-20"
            >
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
