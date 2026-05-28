import { useState } from "react"
import { useForm } from "react-hook-form"
import { useTranslation, Trans } from "react-i18next"
import { zodResolver } from "@hookform/resolvers/zod"
import { ArrowRight } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
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

export function AuthForm() {
  const [mode, setMode] = useState<"login" | "register">("login")
  const { t } = useTranslation()

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
        email: data.emailRegistro?.trim() || undefined,
        username: data.usernameRegistro?.trim() || undefined,
      }
      registerMutate(registerData)
    }
  }

  return (
    <div className="flex flex-col h-screen p-12 w-full bg-background relative z-20">
      <AuthHeader />

      <div className="flex-1 flex flex-col justify-center max-w-md w-full mx-auto">
        <div className="space-y-12">
          <div className="space-y-4">
            <h1 className="text-6xl font-serif text-white leading-tight">
              <Trans i18nKey="auth.greeting" />
            </h1>
            <p className="text-muted-foreground text-sm font-medium uppercase tracking-[0.2em]">
              {t("auth.brand.tagline")}
            </p>
          </div>

          <div className="space-y-10">
            <AuthToggle mode={mode} onToggle={handleModeChange} />

            <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
              {mode === "register" && (
                <RegisterFields
                  register={register}
                  errors={errors}
                  disabled={isPending}
                  hasIdentifier={hasIdentifier}
                />
              )}

              {mode === "login" && (
                <LoginFields register={register} disabled={isPending} errors={errors} />
              )}

              <PasswordField register={register} disabled={isPending} mode={mode} />

              {isLoginError && mode === "login" && (
                <p className="text-red-400 text-sm font-medium">
                  {t("auth.form.error.invalidCredentials")}
                </p>
              )}
              {isRegisterError && mode === "register" && (
                <p className="text-red-400 text-sm font-medium">
                  {t("auth.form.error.registerFailed")}
                </p>
              )}

              <Button
                type="submit"
                disabled={isPending || !isValid}
                className="w-full h-14 bg-primary hover:bg-primary/90 text-white text-lg font-bold rounded-xl transition-all group mt-4"
              >
                {isPending
                  ? t("auth.submit.loading")
                  : mode === "login"
                    ? t("auth.submit.default")
                    : t("auth.submit.register")}
                <ArrowRight className="ml-2 size-5 group-hover:translate-x-1 transition-transform" />
              </Button>
            </form>
          </div>
        </div>
      </div>

      <AuthFooter />
    </div>
  )
}