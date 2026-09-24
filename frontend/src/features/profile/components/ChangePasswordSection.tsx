import { useId, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Lock } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import {
  PASSWORD_MIN_LENGTH,
  passwordSchema,
} from '@/features/auth/schemas/auth.schema';

import { useChangePassword } from '../hooks/useChangePassword';
import { SettingsSection } from './SettingsSection';

export function ChangePasswordSection() {
  const { t } = useTranslation([
    'userProfile',
    'common',
  ]);

  const {
    mutate: changePassword,
    isPending: isChangingPassword,
  } = useChangePassword();

  const [currentPassword, setCurrentPassword] =
    useState('');

  const [newPassword, setNewPassword] =
    useState('');

  const [confirmNewPassword, setConfirmNewPassword] =
    useState('');

  const [validationError, setValidationError] =
    useState('');

  const currentPasswordId = useId();
  const newPasswordId = useId();
  const confirmPasswordId = useId();
  const errorId = useId();

  const handleSubmit = () => {
    if (
      !passwordSchema.safeParse(newPassword).success
    ) {
      setValidationError(
        t('validations:min_length', {
          min: PASSWORD_MIN_LENGTH,
        }),
      );
      return;
    }

    if (newPassword !== confirmNewPassword) {
      setValidationError(t('changePassword.mismatch'));
      return;
    }

    setValidationError('');

    changePassword(
      {
        currentPassword,
        newPassword,
      },
      {
        onSuccess: () => {
          setCurrentPassword('');
          setNewPassword('');
          setConfirmNewPassword('');
          setValidationError('');
        },
      },
    );
  };

  return (
    <SettingsSection
      icon={Lock}
      title={t('changePassword.title')}
      description={t('changePassword.description')}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          handleSubmit();
        }}
        className="space-y-4"
      >
        <div className="space-y-2">
          <Label htmlFor={currentPasswordId}>
            {t('changePassword.currentPassword')}
          </Label>

          <Input
            id={currentPasswordId}
            type="password"
            value={currentPassword}
            onChange={(event) =>
              setCurrentPassword(event.target.value)
            }
            disabled={isChangingPassword}
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor={newPasswordId}>
            {t('changePassword.newPassword')}
          </Label>

          <Input
            id={newPasswordId}
            type="password"
            value={newPassword}
            onChange={(event) =>
              setNewPassword(event.target.value)
            }
            disabled={isChangingPassword}
            aria-invalid={Boolean(validationError)}
            aria-describedby={
              validationError ? errorId : undefined
            }
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor={confirmPasswordId}>
            {t('changePassword.confirmNewPassword')}
          </Label>

          <Input
            id={confirmPasswordId}
            type="password"
            value={confirmNewPassword}
            onChange={(event) =>
              setConfirmNewPassword(event.target.value)
            }
            disabled={isChangingPassword}
            aria-invalid={Boolean(validationError)}
            aria-describedby={
              validationError ? errorId : undefined
            }
          />

          {validationError && (
            <p
              id={errorId}
              role="alert"
              className="text-sm text-destructive"
            >
              {validationError}
            </p>
          )}
        </div>

        <Button
          type="submit"
          disabled={isChangingPassword}
        >
          {isChangingPassword ? (
            <Spinner size={20} />
          ) : (
            t('changePassword.save')
          )}
        </Button>
      </form>
    </SettingsSection>
  );
}