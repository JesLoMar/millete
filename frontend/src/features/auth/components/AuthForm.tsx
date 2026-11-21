import { useState } from 'react';
import {
  useForm,
} from 'react-hook-form';
import { useTranslation } from 'react-i18next';
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
    <>
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="grid md:grid-cols-2"
        noValidate
      >
        <div className="min-w-0 md:border-r md:border-border">
          <div className="flex flex-col gap-5 border-b border-border px-5 py-6 sm:px-9 md:border-b-0 md:px-10 lg:px-14">
            <AuthHeader />
            <AuthToggle
              mode={mode}
              onToggle={handleModeChange}
            />
          </div>

          <div className="min-w-0 space-y-5 px-5 pt-8 pb-3 sm:px-9 md:px-10 md:pt-0 md:pb-7 lg:px-14">
            <h1 className="mb-7 text-3xl leading-tight font-bold tracking-tight text-balance sm:text-4xl">
              {t('greeting').replace(/<br\s*\/?>/g, ' ')}
            </h1>

            {mode === 'login' ? (
              <LoginFields register={register} errors={errors} disabled={isPending} />
            ) : (
              <RegisterFields register={register} errors={errors} control={control} disabled={isPending} />
            )}

            <PasswordField
              register={register}
              errors={errors}
              disabled={isPending}
              mode={mode}
            />
          </div>
        </div>

        <div className="grid justify-items-center gap-3 px-5 pt-5 pb-7 sm:px-9 md:col-span-2 md:pt-0 md:pb-10">
          <Button
            type="submit"
            disabled={isPending}
            className="h-12 w-full rounded-md bg-primary px-6 text-base font-semibold sm:w-auto sm:min-w-52"
            aria-label={mode === 'login' ? t('auth:submit.default') : t('auth:submit.register')}
          >
            {isPending ? (
              <Spinner size={24} />
            ) : (
              <>
                {mode === 'login' ? t('auth:submit.default') : t('auth:submit.register')}
                <ArrowRight className="size-4" aria-hidden="true" />
              </>
            )}
          </Button>

          {hasSubmissionError && (
            <p className="text-center text-sm text-destructive" role="alert">
              {mode === 'login' ? t('auth:errors.loginFailed') : t('auth:errors.registerFailed')}
            </p>
          )}
        </div>
      </form>

      <AuthFooter />
    </>
  );
}
