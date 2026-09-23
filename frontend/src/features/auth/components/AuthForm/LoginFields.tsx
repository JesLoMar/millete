import { useTranslation } from 'react-i18next';
import type {
  FieldErrors,
  UseFormRegister,
} from 'react-hook-form';

import type { CombinedAuthFormData } from '@/features/auth/schemas/auth.schema';
import { cn } from '@/lib/utils';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

interface LoginFieldsProps {
  register: UseFormRegister<CombinedAuthFormData>;
  disabled: boolean;
  errors: FieldErrors<CombinedAuthFormData>;
}

export function LoginFields({
  register,
  disabled,
  errors,
}: LoginFieldsProps) {
  const { t } = useTranslation();

  return (
    <div className="space-y-3">
      <Label
        htmlFor="identifier"
        className="ml-1 text-lg text-secondary-foreground/70"
      >
        {t('auth:form.fields.identifier.login')}
      </Label>

      <Input
        id="identifier"
        type="text"
        autoComplete="username"
        placeholder={t('auth:form.fields.placeholder.login')}
        disabled={disabled}
        aria-invalid={errors.identifier ? 'true' : 'false'}
        className={cn(
          'h-14 rounded-xl border-border/50 bg-secondary/30 px-5 text-lg text-foreground transition-all focus:ring-2 focus:ring-primary/50',
          errors.identifier && 'animate-shake',
        )}
        {...register('identifier')}
      />

      {errors.identifier?.message && (
        <p className="ml-1 text-xs text-crust">
          {t(errors.identifier.message)}
        </p>
      )}
    </div>
  );
}