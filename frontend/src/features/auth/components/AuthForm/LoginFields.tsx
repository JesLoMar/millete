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
  const { t } = useTranslation(['auth', 'validations']);

  return (
    <div className="space-y-1.5">
      <Label
        htmlFor="identifier"
        className="text-sm font-semibold text-foreground"
      >
        {t('auth:form.fields.identifier.login')}
      </Label>

      <Input
        id="identifier"
        type="text"
        autoComplete="username"
        autoCapitalize="none"
        autoCorrect="off"
        spellCheck={false}
        placeholder={t('auth:form.fields.placeholder.login')}
        disabled={disabled}
        aria-invalid={errors.identifier ? 'true' : 'false'}
        className={cn(
          'h-11 rounded-none border-0 border-b border-input bg-transparent px-0 text-base text-foreground shadow-none focus-visible:border-ring focus-visible:ring-0',
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
