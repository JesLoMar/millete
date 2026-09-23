import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type {
  FieldErrors,
  UseFormRegister,
} from 'react-hook-form';

import type { CombinedAuthFormData } from '@/features/auth/schemas/auth.schema';
import { cn } from '@/lib/utils';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

interface PasswordFieldProps {
  register: UseFormRegister<CombinedAuthFormData>;
  disabled: boolean;
  mode: 'login' | 'register';
  errors: FieldErrors<CombinedAuthFormData>;
}

export function PasswordField({
  register,
  disabled,
  mode,
  errors,
}: PasswordFieldProps) {
  const { t } = useTranslation();
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="space-y-3">
      <Label
        htmlFor="password"
        className="ml-1 text-lg text-secondary-foreground/70"
      >
        {t('auth:form.fields.password.label')}
      </Label>

      <div className="relative">
        <Input
          id="password"
          type={showPassword ? 'text' : 'password'}
          autoComplete={
            mode === 'login' ? 'current-password' : 'new-password'
          }
          placeholder={t('auth:form.fields.password.placeholder')}
          disabled={disabled}
          aria-invalid={errors.password ? 'true' : 'false'}
          className={cn(
            'h-14 rounded-xl border-border/50 bg-secondary/30 px-5 pr-12 text-lg text-foreground transition-all focus:ring-2 focus:ring-primary/50',
            errors.password && 'animate-shake',
          )}
          {...register('password')}
        />

        <Button
          type="button"
          variant="ghost"
          size="icon"
          disabled={disabled}
          className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
          onClick={() => setShowPassword((visible) => !visible)}
        >
          {showPassword ? (
            <EyeOff size={20} aria-hidden="true" />
          ) : (
            <Eye size={20} aria-hidden="true" />
          )}
        </Button>
      </div>

      {errors.password?.message && (
        <p className="ml-1 text-xs text-crust">
          {t(errors.password.message)}
        </p>
      )}

      {mode === 'register' && (
        <p className="ml-1 text-xs text-muted-foreground">
          {t('auth:form.passwordHint')}
        </p>
      )}
    </div>
  );
}