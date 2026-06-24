import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { User } from 'lucide-react';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import { Button } from '@/shared/components/core/button';
import { SettingsSection } from './SettingsSection';
import { useProfile } from '../hooks/useProfile';
import { useUpdateProfile } from '../hooks/useUpdateProfile';

export function PersonalInfoSection() {
  const { t } = useTranslation('userProfile');
  const { profile, isLoading } = useProfile();
  const { mutate: updateProfile, isPending } = useUpdateProfile();

  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState({
    username: '',
    email: '',
    currentPassword: '',
    error: '',
  });

  const startEditing = () => {
    setForm({
      username: profile?.username ?? '',
      email: profile?.email ?? '',
      currentPassword: '',
      error: '',
    });
    setIsEditing(true);
  };

  const cancelEditing = () => {
    setIsEditing(false);
    setForm((prev) => ({ ...prev, error: '' }));
  };

  const handleSave = () => {
    if (!form.currentPassword.trim()) {
      setForm((prev) => ({ ...prev, error: t('personalInfo.currentPassword') }));
      return;
    }
    setForm((prev) => ({ ...prev, error: '' }));
    updateProfile(
      {
        newUsername: form.username.trim() || undefined,
        newEmail: form.email.trim() || undefined,
        currentPassword: form.currentPassword,
      },
      {
        onSuccess: () => setIsEditing(false),
      }
    );
  };

  return (
    <SettingsSection
      icon={User}
      title={t('personalInfo.title')}
      description={t('personalInfo.description')}
    >
      {isLoading ? (
        <div className="text-sm text-muted-foreground">{t('common:loading')}</div>
      ) : isEditing ? (
        <div className="space-y-4">
          <div className="space-y-2">
            <Label>{t('personalInfo.username')}</Label>
            <Input
              value={form.username}
              onChange={(e) => setForm((prev) => ({ ...prev, username: e.target.value }))}
              placeholder={t('personalInfo.username')}
            />
          </div>
          <div className="space-y-2">
            <Label>{t('personalInfo.email')}</Label>
            <Input
              type="email"
              value={form.email}
              onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
              placeholder={t('personalInfo.email')}
            />
          </div>
          <div className="space-y-2">
            <Label>{t('personalInfo.currentPassword')}</Label>
            <Input
              type="password"
              value={form.currentPassword}
              onChange={(e) => setForm((prev) => ({ ...prev, currentPassword: e.target.value }))}
              placeholder={t('personalInfo.currentPassword')}
            />
            {form.error && (
              <p className="text-sm text-destructive">{form.error}</p>
            )}
          </div>
          <div className="flex gap-2">
            <Button onClick={handleSave} disabled={isPending}>
              {t('personalInfo.save')}
            </Button>
            <Button variant="outline" onClick={cancelEditing} disabled={isPending}>
              {t('personalInfo.cancel')}
            </Button>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">{t('personalInfo.username')}</p>
            <p className="text-sm font-medium">{profile?.username}</p>
          </div>
          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">{t('personalInfo.email')}</p>
            <p className="text-sm font-medium">{profile?.email}</p>
          </div>
          <Button variant="outline" onClick={startEditing}>
            {t('personalInfo.edit')}
          </Button>
        </div>
      )}
    </SettingsSection>
  );
}
