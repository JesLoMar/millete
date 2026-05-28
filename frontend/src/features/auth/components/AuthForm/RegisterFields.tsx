import { useTranslation } from "react-i18next"
import type { UseFormRegister, FieldErrors } from "react-hook-form"
import type { CombinedAuthFormData } from "@/features/auth/schemas/auth.schema"
import { Mail, User } from "lucide-react"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"

interface RegisterFieldsProps {
  register: UseFormRegister<CombinedAuthFormData>
  errors: FieldErrors<CombinedAuthFormData>
  disabled: boolean
  hasIdentifier: boolean
}

export function RegisterFields({ register, errors, disabled, hasIdentifier }: RegisterFieldsProps) {
  const { t } = useTranslation()

  return (
    <div className="space-y-4 animate-in fade-in slide-in-from-top-2 duration-300">
      <div className="space-y-2">
        <Label htmlFor="usernameRegistro" className="text-sm text-secondary-foreground/70 ml-1 flex items-center gap-1">
          <User className="size-4" />
          {t("auth.form.fields.username.label")}
          <span className="text-xs text-muted-foreground">({t("auth.form.optional")})</span>
        </Label>
        <Input
          id="usernameRegistro"
          type="text"
          placeholder={t("auth.form.fields.username.placeholder")}
          disabled={disabled}
          className="bg-secondary/30 border-border/50 h-12 text-base focus:ring-2 focus:ring-primary/50 transition-all rounded-xl px-4 text-white"
          {...register("usernameRegistro")}
        />
        {errors.usernameRegistro?.message && (
          <p className="text-red-400 text-xs ml-1">{t(errors.usernameRegistro.message)}</p>
        )}
      </div>

      <div className="flex items-center gap-3">
        <div className="h-px flex-1 bg-border/30" />
        <span className="text-xs text-muted-foreground">{t("auth.form.andOr")}</span>
        <div className="h-px flex-1 bg-border/30" />
      </div>

      <div className="space-y-2">
        <Label htmlFor="emailRegistro" className="text-sm text-secondary-foreground/70 ml-1 flex items-center gap-1">
          <Mail className="size-4" />
          {t("auth.form.fields.identifier.register")}
          <span className="text-xs text-muted-foreground">({t("auth.form.optional")})</span>
        </Label>
        <Input
          id="emailRegistro"
          type="email"
          placeholder={t("auth.form.fields.placeholder.register")}
          disabled={disabled}
          className="bg-secondary/30 border-border/50 h-12 text-base focus:ring-2 focus:ring-primary/50 transition-all rounded-xl px-4 text-white"
          {...register("emailRegistro")}
        />
        {errors.emailRegistro?.message && (
          <p className="text-red-400 text-xs ml-1">{t(errors.emailRegistro.message)}</p>
        )}
      </div>

      {!hasIdentifier && !errors.emailRegistro && (
        <p className="text-amber-400 text-xs ml-1 flex items-center gap-1">
          <span>⚠️</span>
          {t("auth.form.registerHint")}
        </p>
      )}
    </div>
  )
}