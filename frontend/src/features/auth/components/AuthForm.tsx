import { useState } from 'react';
import {
  useForm,
  useWatch,
} from 'react-hook-form';
import { Trans, useTranslation } from 'react-i18next';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowRight } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import { loginSchema, registerSchema } from '@/features/auth/schemas/auth.schema';
import type { CombinedAuthFormData } from '@/features/auth/schemas/auth.schema';

import { useLoginMutation } from '../hooks/useLoginMutation.ts';
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
  const { t } = useTranslation(['auth', 'common']);

  const currentSchema =
    mode === 'login' ? loginSchema : registerSchema;

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isValid },
  } = useForm<CombinedAuthFormData>({
    resolver: zodResolver(currentSchema),
    mode: 'onChange',
  });

  const {
    mutate: loginMutate,
    isPending: isLoginPending,
    isError: isLoginError,
  } = useLoginMutation();

  const {
    mutate: registerMutate,
    isPending: isRegisterPending,
    isError: isRegisterError,
  } = useRegisterMutation();

  const isPending = isLoginPending || isRegisterPending;

  const usernameWatch = useWatch({
    control,
    name: 'usernameRegistro',
  });

  const emailWatch = useWatch({
    control,
    name: 'emailRegistro',
  });

  const hasIdentifier =
    !!usernameWatch?.trim() || !!emailWatch?.trim();

  const handleModeChange = (newMode: 'login' | 'register') => {
    setMode(newMode);
    reset();
  };

  const onSubmit = (data: CombinedAuthFormData) => {
    if (mode === 'login') {
      loginMutate({
        identifier: data.identifier!,
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
            components={{ br: <br /> }}
          />
        </h1>

        <p className="text-xs font-medium uppercase tracking-[0.15em] text-muted-foreground sm:text-sm sm:tracking-[0.2em]">
          {t('auth:subtitle')}
        </p>
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
            hasIdentifier={hasIdentifier}
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
          disabled={isPending || !isValid}
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