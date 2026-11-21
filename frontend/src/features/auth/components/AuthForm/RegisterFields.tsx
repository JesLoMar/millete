import { AlertTriangle, Mail, User } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useWatch } from 'react-hook-form';
import type {
  Control,
  FieldErrors,
  UseFormRegister,
} from 'react-hook-form';

import type { CombinedAuthFormData } from '@/features/auth/schemas/auth.schema';
import { cn } from '@/lib/utils';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

interface RegisterFieldsProps {
  register: UseFormRegister<CombinedAuthFormData>;
  errors: FieldErrors<CombinedAuthFormData>;
  control: Control<CombinedAuthFormData>;
  disabled: boolean;
}

export function RegisterFields({
  register,
  errors,
  control,
  disabled,
}: RegisterFieldsProps) {
  const { t } = useTranslation(['auth', 'validations', 'common']);

  const usernameWatch = useWatch({
    control,
    name: 'usernameRegistro',
  });

  const emailWatch = useWatch({
    control,
    name: 'emailRegistro',
  });

  const hasIdentifier =
    !!usernameWatch?.trim() ||
    !!emailWatch?.trim();

  return (
    <div className="space-y-3">
      <div className="space-y-1.5">
        <Label
          htmlFor="usernameRegistro"
          className="flex flex-wrap items-center gap-1 text-sm font-semibold text-foreground"
        >
          <User className="size-4" />
          {t('auth:form.fields.username.label')}
          <span className="text-xs text-muted-foreground">
            ({t('common:prompts.optional')})
          </span>
        </Label>

        <Input
          id="usernameRegistro"
          type="text"
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          placeholder={t('auth:form.fields.username.placeholder')}
          disabled={disabled}
          aria-invalid={errors.usernameRegistro ? 'true' : 'false'}
          className={cn(
            'h-11 rounded-none border-0 border-b border-input bg-transparent px-0 text-base text-foreground shadow-none focus-visible:border-ring focus-visible:ring-0',
            errors.usernameRegistro && 'animate-shake',
          )}
          {...register('usernameRegistro')}
        />

        {errors.usernameRegistro?.message && (
          <p className="ml-1 text-xs text-crust">
            {t(errors.usernameRegistro.message)}
          </p>
        )}
      </div>

      <div className="flex items-center gap-3">
        <div className="h-px flex-1 bg-border/30" />

        <span className="text-xs text-muted-foreground">
          {t('auth:form.andOr')}
        </span>

        <div className="h-px flex-1 bg-border/30" />
      </div>

      <div className="space-y-1.5">
        <Label
          htmlFor="emailRegistro"
          className="flex flex-wrap items-center gap-1 text-sm font-semibold text-foreground"
        >
          <Mail className="size-4" />
          {t('auth:form.fields.identifier.register')}
          <span className="text-xs text-muted-foreground">
            ({t('common:prompts.optional')})
          </span>
        </Label>

        <Input
          id="emailRegistro"
          type="email"
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          placeholder={t('auth:form.fields.placeholder.register')}
          disabled={disabled}
          aria-invalid={errors.emailRegistro ? 'true' : 'false'}
          className={cn(
            'h-11 rounded-none border-0 border-b border-input bg-transparent px-0 text-base text-foreground shadow-none focus-visible:border-ring focus-visible:ring-0',
            errors.emailRegistro && 'animate-shake',
          )}
          {...register('emailRegistro')}
        />

        {errors.emailRegistro?.message && (
          <p className="ml-1 text-xs text-crust">
            {t(errors.emailRegistro.message)}
          </p>
        )}
      </div>

      {!hasIdentifier &&
        !errors.emailRegistro &&
        !errors.usernameRegistro && (
          <p className="ml-1 flex items-center gap-1 text-xs text-warning">
            <AlertTriangle
              className="size-3.5"
              aria-hidden="true"
            />
            {t('auth:form.registerHint')}
          </p>
        )}
    </div>
  );
}
