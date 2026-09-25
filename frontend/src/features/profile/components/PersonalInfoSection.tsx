import { useId, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { User } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import { useProfile } from '../hooks/useProfile';
import { useUpdateProfile } from '../hooks/useUpdateProfile';
import { SettingsSection } from './SettingsSection';

interface FormState {
  username: string;
  email: string;
  currentPassword: string;
  error: string;
}

export function PersonalInfoSection() {
  const { t } = useTranslation('userProfile');

  const { profile, isLoading } = useProfile();
  const {
    mutate: updateProfile,
    isPending: isUpdating,
  } = useUpdateProfile();

  const [isEditing, setIsEditing] = useState(false);

  const [form, setForm] = useState<FormState>({
    username: '',
    email: '',
    currentPassword: '',
    error: '',
  });

  const usernameId = useId();
  const emailId = useId();
  const passwordId = useId();
  const errorId = useId();

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

    setForm((previous) => ({
      ...previous,
      error: '',
    }));
  };

  const handleSave = () => {
    const username = form.username.trim();
    const email = form.email.trim();

    if (!username && !email) {
      setForm((previous) => ({
        ...previous,
        error: t('personalInfo.identifierRequired'),
      }));
      return;
    }

    if (!form.currentPassword.trim()) {
      setForm((previous) => ({
        ...previous,
        error: t('personalInfo.currentPassword'),
      }));
      return;
    }

    setForm((previous) => ({
      ...previous,
      error: '',
    }));

    updateProfile(
      {
        newUsername: username || null,
        newEmail: email || null,
        currentPassword: form.currentPassword,
      },
      {
        onSuccess: () => {
          setIsEditing(false);
        },
      },
    );
  };

  return (
    <SettingsSection
      icon={User}
      title={t('personalInfo.title')}
      description={t('personalInfo.description')}
    >
      {isLoading ? (
        <div className="text-sm text-muted-foreground">
          {t('common:loading')}
        </div>
      ) : isEditing ? (
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor={usernameId}>
              {t('personalInfo.username')}
            </Label>

            <Input
              id={usernameId}
              value={form.username}
              onChange={(event) =>
                setForm((previous) => ({
                  ...previous,
                  username: event.target.value,
                }))
              }
              placeholder={t('personalInfo.username')}
              disabled={isUpdating}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor={emailId}>
              {t('personalInfo.email')}
            </Label>

            <Input
              id={emailId}
              type="email"
              value={form.email}
              onChange={(event) =>
                setForm((previous) => ({
                  ...previous,
                  email: event.target.value,
                }))
              }
              placeholder={t('personalInfo.email')}
              disabled={isUpdating}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor={passwordId}>
              {t('personalInfo.currentPassword')}
            </Label>

            <Input
              id={passwordId}
              type="password"
              value={form.currentPassword}
              onChange={(event) =>
                setForm((previous) => ({
                  ...previous,
                  currentPassword: event.target.value,
                }))
              }
              placeholder={t('personalInfo.currentPassword')}
              disabled={isUpdating}
              aria-invalid={Boolean(form.error)}
              aria-describedby={
                form.error ? errorId : undefined
              }
            />

            {form.error && (
              <p
                id={errorId}
                role="alert"
                className="text-sm text-destructive"
              >
                {form.error}
              </p>
            )}
          </div>

          <div className="flex gap-2">
            <Button
              type="button"
              onClick={handleSave}
              disabled={isUpdating}
            >
              {isUpdating ? (
                <Spinner size={20} />
              ) : (
                t('personalInfo.save')
              )}
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={cancelEditing}
              disabled={isUpdating}
            >
              {t('personalInfo.cancel')}
            </Button>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">
              {t('personalInfo.username')}
            </p>

            <p className="text-sm font-medium">
              {profile?.username}
            </p>
          </div>

          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">
              {t('personalInfo.email')}
            </p>

            <p className="text-sm font-medium">
              {profile?.email}
            </p>
          </div>

          <Button
            type="button"
            variant="outline"
            onClick={startEditing}
          >
            {t('personalInfo.edit')}
          </Button>
        </div>
      )}
    </SettingsSection>
  );
}