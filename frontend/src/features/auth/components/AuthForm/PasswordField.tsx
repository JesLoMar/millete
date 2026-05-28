import { useState } from "react"
import { useTranslation } from "react-i18next"
import type { UseFormRegister } from "react-hook-form"
import type { CombinedAuthFormData } from "@/features/auth/schemas/auth.schema"
import { Eye, EyeOff } from "lucide-react"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import { Button } from "@/shared/components/ui/button"

interface PasswordFieldProps {
  register: UseFormRegister<CombinedAuthFormData>
  disabled: boolean
  mode: "login" | "register"
}

export function PasswordField({ register, disabled, mode }: PasswordFieldProps) {
  const { t } = useTranslation()
  const [showPassword, setShowPassword] = useState(false)

  return (
    <div className="space-y-3">
      <Label htmlFor="password" className="text-lg text-secondary-foreground/70 ml-1">
        {t("auth.form.fields.password.label")}
      </Label>
      <div className="relative">
        <Input
          id="password"
          type={showPassword ? "text" : "password"}
          autoComplete={mode === "login" ? "current-password" : "new-password"}
          placeholder={t("auth.form.fields.password.placeholder")}
          disabled={disabled}
          className="bg-secondary/30 border-border/50 h-14 text-lg focus:ring-2 focus:ring-primary/50 transition-all rounded-xl px-5 pr-12 text-white"
          {...register("password", {
            required: true,
            minLength: mode === "register" ? 8 : undefined,
          })}
        />
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
          onClick={() => setShowPassword(!showPassword)}
          tabIndex={-1}
        >
          {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
        </Button>
      </div>
      {mode === "register" && (
        <p className="text-xs text-muted-foreground ml-1">
          {t("auth.form.passwordHint")}
        </p>
      )}
    </div>
  )
}