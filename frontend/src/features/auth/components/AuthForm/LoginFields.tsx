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
      <Label htmlFor="identifier" className="text-lg text-secondary-foreground/70 ml-1">
        {t("auth.form.fields.identifier.login")}
      </Label>
      <Input
        id="identifier"
        type="text"
        autoComplete="username"
        placeholder={t("auth.form.fields.placeholder.login")}
        disabled={disabled}
        className="bg-secondary/30 border-border/50 h-14 text-lg focus:ring-2 focus:ring-primary/50 transition-all rounded-xl px-5 text-white"
        {...register("identifier", { required: true })}
      />
      {errors.identifier && (
        <p className="text-red-400 text-xs ml-1">{t("auth.form.error.required")}</p>
      )}
    </div>
  )
}