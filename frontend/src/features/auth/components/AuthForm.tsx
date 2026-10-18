import { useState } from 'react';
import {
  useForm,
} from 'react-hook-form';
import { Trans, useTranslation } from 'react-i18next';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowRight } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  loginSchema,
  registerSchema,
} from '@/features/auth/schemas/auth.schema';
import type {
  CombinedAuthFormData,
} from '@/features/auth/schemas/auth.schema';

import { useLoginMutation } from '../hooks/useLoginMutation';
import { useRegisterMutation } from '../hooks/useRegisterMutation';
import type { RegisterUserRequest } from '../types';

import { AuthHeader } from './AuthForm/AuthHeader';
import { AuthToggle } from './AuthForm/AuthToggle';
import { LoginFields } from './AuthForm/LoginFields';
import { RegisterFields } from './AuthForm/RegisterFields';
import { PasswordField } from './AuthForm/PasswordField';
import { AuthFooter } from './AuthForm/AuthFooter';

export function AuthForm() {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const { t } = useTranslation('auth');

  const currentSchema =
    mode === 'login' ? loginSchema : registerSchema;

  const {
    register,
    handleSubmit,
    control,
    getValues,
    setValue,
    clearErrors,
    formState: { errors },
  } = useForm<CombinedAuthFormData>({
    resolver: zodResolver(currentSchema),
    mode: 'onChange',
  });

  const {
    mutate: loginMutate,
    isPending: isLoginPending,
    isError: isLoginError,
  } = useLoginMutation();

  const handleAutoLoginFailed = (identifier: string) => {
    clearErrors();

    setValue('identifier', identifier, {
      shouldDirty: true,
      shouldValidate: false,
    });

    setMode('login');
  };

  const {
    mutate: registerMutate,
    isPending: isRegisterPending,
    isError: isRegisterError,
  } = useRegisterMutation({
    onAutoLoginFailed: handleAutoLoginFailed,
  });

  const isPending = isLoginPending || isRegisterPending;

  const handleModeChange = (
    newMode: 'login' | 'register',
  ) => {
    if (newMode === mode) {
      return;
    }

    if (newMode === 'login') {
      const identifier =
        getValues('usernameRegistro')?.trim() ||
        getValues('emailRegistro')?.trim() ||
        getValues('identifier')?.trim() ||
        '';

      setValue('identifier', identifier, {
        shouldDirty: false,
        shouldValidate: false,
      });
    } else {
      const identifier =
        getValues('identifier')?.trim() || '';

      if (identifier) {
        const looksLikeEmail =
          /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(identifier);

        if (looksLikeEmail) {
          setValue('emailRegistro', identifier, {
            shouldDirty: false,
            shouldValidate: false,
          });
        } else {
          setValue('usernameRegistro', identifier, {
            shouldDirty: false,
            shouldValidate: false,
          });
        }
      }
    }

    clearErrors();
    setMode(newMode);
  };

  const onSubmit = (data: CombinedAuthFormData) => {
    if (mode === 'login') {
      const identifier = data.identifier?.trim();

      if (!identifier) {
        return;
      }

      loginMutate({
        identifier,
        password: data.password,
      });

      return;
    }

    const registerData: RegisterUserRequest = {
      password: data.password,
      email: data.emailRegistro?.trim() || '',
      username: data.usernameRegistro?.trim() || '',
    };

    registerMutate(registerData);
  };

  const hasSubmissionError =
    (mode === 'login' && isLoginError) ||
    (mode === 'register' && isRegisterError);

  return (
    <div className="flex w-full flex-col justify-center space-y-8 py-6 sm:space-y-12">
      <AuthHeader />

      <div className="space-y-3 sm:space-y-4">
        <h1 className="font-serif text-4xl leading-tight text-foreground sm:text-5xl">
          <Trans
            i18nKey="greeting"
            ns="auth"
          />
        </h1>
      </div>

      <AuthToggle
        mode={mode}
        onToggle={handleModeChange}
      />

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="space-y-5 sm:space-y-6"
        noValidate
      >
        {mode === 'login' ? (
          <LoginFields
            register={register}
            errors={errors}
            disabled={isPending}
          />
        ) : (
          <RegisterFields
            register={register}
            errors={errors}
            control={control}
            disabled={isPending}
          />
        )}

        <PasswordField
          register={register}
          errors={errors}
          disabled={isPending}
          mode={mode}
        />

        <Button
          type="submit"
          disabled={isPending}
          className="h-12 w-full bg-primary text-base font-semibold transition-all duration-200 hover:bg-primary/90 sm:h-14 sm:text-lg"
          aria-label={
            mode === 'login'
              ? t('auth:submit.default')
              : t('auth:submit.register')
          }
        >
          {isPending ? (
            <Spinner size={24} />
          ) : (
            <>
              {mode === 'login'
                ? t('auth:submit.default')
                : t('auth:submit.register')}

              <ArrowRight
                className="ml-2 size-5"
                aria-hidden="true"
              />
            </>
          )}
        </Button>

        {hasSubmissionError && (
          <p
            className="text-center text-sm text-destructive"
            role="alert"
          >
            {mode === 'login'
              ? t('auth:errors.login_failed')
              : t('auth:errors.register_failed')}
          </p>
        )}
      </form>

      <AuthFooter />
    </div>
  );
}