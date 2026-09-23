import { AlertTriangle, Mail, User } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type {
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
  disabled: boolean;
  hasIdentifier: boolean;
}

export function RegisterFields({
  register,
  errors,
  disabled,
  hasIdentifier,
}: RegisterFieldsProps) {
  const { t } = useTranslation();

  return (
    <div className="animate-in fade-in slide-in-from-top-2 space-y-4 duration-300">
      <div className="space-y-2">
        <Label
          htmlFor="usernameRegistro"
          className="ml-1 flex items-center gap-1 text-sm text-secondary-foreground/70"
        >
          <User className="size-4" />
          {t('auth:form.fields.username.label')}
          <span className="text-xs text-muted-foreground">
            ({t('auth:form.optional')})
          </span>
        </Label>

        <Input
          id="usernameRegistro"
          type="text"
          placeholder={t('auth:form.fields.username.placeholder')}
          disabled={disabled}
          aria-invalid={errors.usernameRegistro ? 'true' : 'false'}
          className={cn(
            'h-12 rounded-xl border-border/50 bg-secondary/30 px-4 text-base text-foreground transition-all focus:ring-2 focus:ring-primary/50',
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

      <div className="space-y-2">
        <Label
          htmlFor="emailRegistro"
          className="ml-1 flex items-center gap-1 text-sm text-secondary-foreground/70"
        >
          <Mail className="size-4" />
          {t('auth:form.fields.identifier.register')}
          <span className="text-xs text-muted-foreground">
            ({t('auth:form.optional')})
          </span>
        </Label>

        <Input
          id="emailRegistro"
          type="email"
          placeholder={t('auth:form.fields.placeholder.register')}
          disabled={disabled}
          aria-invalid={errors.emailRegistro ? 'true' : 'false'}
          className={cn(
            'h-12 rounded-xl border-border/50 bg-secondary/30 px-4 text-base text-foreground transition-all focus:ring-2 focus:ring-primary/50',
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

      {!hasIdentifier && !errors.emailRegistro && (
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