import { useTranslation } from 'react-i18next';

import { Header } from '@/shared/components/Header';

import { NotificationList } from '../components/NotificationList';

export default function NotificationsPage() {
  const { t } = useTranslation('notifications');

  return (
    <div className="min-h-dvh bg-background">
      <div className="space-y-6 p-6">
        <Header hidePeriodSelector />

        <h1 className="text-2xl font-semibold text-foreground">
          {t('title')}
        </h1>

        <NotificationList />
      </div>
    </div>
  );
}