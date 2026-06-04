import { useTranslation } from "react-i18next"
import type { UseFormRegister, FieldErrors } from "react-hook-form"
import type { CombinedAuthFormData } from "@/features/auth/schemas/auth.schema"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"

interface LoginFieldsProps {
  register: UseFormRegister<CombinedAuthFormData>
  disabled: boolean
  errors: FieldErrors<CombinedAuthFormData>
}

export function LoginFields({ register, disabled, errors }: LoginFieldsProps) {
  const { t } = useTranslation()

  return (
    <div className="space-y-3">
      <Label htmlFor="identifier" className="text-sm font-medium text-foreground/80 ml-1">
        {t("auth.form.fields.identifier.login")}
      </Label>
      <Input
        id="identifier"
        type="text"
        autoComplete="username"
        placeholder={t("auth.form.fields.placeholder.login")}
        disabled={disabled}
        className="bg-background border-border h-14 text-base sm:text-lg focus:ring-2 focus:ring-primary/50 transition-all rounded-xl px-5 text-foreground"
        {...register("identifier", { required: true })}
      />
      {errors.identifier && (
        <p className="text-destructive text-xs ml-1">{t("auth.form.error.required")}</p>
      )}
    </div>
  )
}