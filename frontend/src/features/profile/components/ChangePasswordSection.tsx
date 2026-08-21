import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Lock } from 'lucide-react';
import { Spinner } from "@/shared/components/Spinner";
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import { Button } from '@/shared/components/core/button';
import { SettingsSection } from './SettingsSection';
import { useChangePassword } from '../hooks/useChangePassword';
import { passwordSchema, PASSWORD_MIN_LENGTH } from '@/features/auth/schemas/auth.schema';

export function ChangePasswordSection() {
  const { t } = useTranslation('userProfile');
  const { mutate: changePassword, isPending } = useChangePassword();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [validationError, setValidationError] = useState('');

  const handleSubmit = () => {
    if (!passwordSchema.safeParse(newPassword).success) {
      setValidationError(t('validations:min_length', { min: PASSWORD_MIN_LENGTH }));
      return;
    }
    if (newPassword !== confirmNewPassword) {
      setValidationError(t('changePassword.mismatch'));
      return;
    }
    setValidationError('');
    changePassword(
      { currentPassword, newPassword },
      {
        onSuccess: () => {
          setCurrentPassword('');
          setNewPassword('');
          setConfirmNewPassword('');
        },
      }
    );
  };

  return (
    <SettingsSection
      icon={Lock}
      title={t('changePassword.title')}
      description={t('changePassword.description')}
    >
      <div className="space-y-4">
        <div className="space-y-2">
          <Label>{t('changePassword.currentPassword')}</Label>
          <Input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
          />
        </div>
        <div className="space-y-2">
          <Label>{t('changePassword.newPassword')}</Label>
          <Input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </div>
        <div className="space-y-2">
          <Label>{t('changePassword.confirmNewPassword')}</Label>
          <Input
            type="password"
            value={confirmNewPassword}
            onChange={(e) => setConfirmNewPassword(e.target.value)}
          />
          {validationError && (
            <p className="text-sm text-destructive">{validationError}</p>
          )}
        </div>
        <Button onClick={handleSubmit} disabled={isPending}>
          {isPending ? <Spinner size={20} /> : t('changePassword.save')}
        </Button>
      </div>
    </SettingsSection>
  );
}