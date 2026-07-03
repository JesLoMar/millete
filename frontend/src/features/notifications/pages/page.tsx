import { useTranslation } from 'react-i18next';
import { Header } from '@/shared/components/Header';
import { NotificationList } from '../components/NotificationList';

export default function NotificationsPage() {
  const { t } = useTranslation('notifications');

  return (
    <div className="min-h-screen bg-background">
      <div className="p-6 space-y-6">
        <Header hidePeriodSelector />
        <h1 className="text-2xl font-semibold text-foreground">{t('title')}</h1>
        <NotificationList />
      </div>
    </div>
  );
}
