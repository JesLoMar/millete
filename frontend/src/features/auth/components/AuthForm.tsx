import { useState } from "react"
import { useForm } from "react-hook-form"
import { useTranslation } from "react-i18next"
import { zodResolver } from "@hookform/resolvers/zod"
import { ArrowRight } from "lucide-react"
import { Spinner } from "@/shared/components/Spinner"
import { Button } from "@/shared/components/core/button"
import { useLoginMutation } from "../hooks/useAuthMutations"
import { useRegisterMutation } from "../hooks/useRegisterMutation"
import { AuthHeader } from "./AuthForm/AuthHeader"
import { AuthToggle } from "./AuthForm/AuthToggle"
import { LoginFields } from "./AuthForm/LoginFields"
import { RegisterFields } from "./AuthForm/RegisterFields"
import { PasswordField } from "./AuthForm/PasswordField"
import { AuthFooter } from "./AuthForm/AuthFooter"

import {
  loginSchema,
  registerSchema,
  type CombinedAuthFormData
} from "@/features/auth/schemas/auth.schema"
import type { RegisterUserRequest } from "../types"

import { useAutofillFix } from "@/shared/hooks/useAutofillFix"

export function AuthForm() {
  const [mode, setMode] = useState<"login" | "register">("login")
  const { t } = useTranslation(['auth', 'common'])

  useAutofillFix()

  const currentSchema = mode === "login" ? loginSchema : registerSchema

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors, isValid },
  } = useForm<CombinedAuthFormData>({
    resolver: zodResolver(currentSchema),
    mode: "onChange",
  })

  const { mutate: loginMutate, isPending: isLoginPending, isError: isLoginError } = useLoginMutation()
  const { mutate: registerMutate, isPending: isRegisterPending, isError: isRegisterError } = useRegisterMutation()

  const isPending = isLoginPending || isRegisterPending

  const usernameWatch = watch("usernameRegistro")
  const emailWatch = watch("emailRegistro")
  const hasIdentifier = !!usernameWatch?.trim() || !!emailWatch?.trim()

  const handleModeChange = (newMode: "login" | "register") => {
    setMode(newMode)
    reset()
  }

  const onSubmit = (data: CombinedAuthFormData) => {
    if (mode === "login") {
      loginMutate({ identifier: data.identifier!, password: data.password })
    } else {
      const registerData: RegisterUserRequest = {
        password: data.password,
        email: data.emailRegistro?.trim() || "",
        username: data.usernameRegistro?.trim() || "",
      }
      registerMutate(registerData)
    }
  }

  return (
    <div className="w-full flex flex-col justify-center space-y-8 sm:space-y-12 py-6">
      <AuthHeader />

      <div className="space-y-3 sm:space-y-4">
        <h1 className="text-4xl sm:text-5xl font-serif text-foreground leading-tight"
          dangerouslySetInnerHTML={{ __html: t('auth:greeting') }}
        />
        <p className="text-muted-foreground text-xs sm:text-sm font-medium uppercase tracking-[0.15em] sm:tracking-[0.2em]">
          {t('auth:subtitle')}
        </p>
      </div>

      <AuthToggle mode={mode} onToggle={handleModeChange} />

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 sm:space-y-6" noValidate>
        {mode === "login" ? (
          <LoginFields register={register} errors={errors} disabled={isPending} />
        ) : (
          <RegisterFields
            register={register}
            errors={errors}
            hasIdentifier={hasIdentifier}
            disabled={isPending}
          />
        )}

        <PasswordField register={register} errors={errors} disabled={isPending} mode={mode} />

        <Button
          type="submit"
          disabled={isPending || !isValid}
          className="w-full h-12 sm:h-14 text-base sm:text-lg font-semibold bg-primary hover:bg-primary/90 transition-all duration-200"
          aria-label={mode === "login" ? t('auth:submit.default') : t('auth:submit.register')}
        >
          {isPending ? (
            <Spinner size={24} />
          ) : (
            <>
              {mode === "login" ? t('auth:submit.default') : t('auth:submit.register')}
              <ArrowRight className="ml-2 size-5" aria-hidden="true" />
            </>
          )}
        </Button>

        {(isLoginError || isRegisterError) && (
          <p className="text-destructive text-sm text-center" role="alert">
            {mode === "login"
              ? t('auth:errors.login_failed')
              : t('auth:errors.register_failed')}
          </p>
        )}
      </form>

      <AuthFooter />
    </div>
  )
}
