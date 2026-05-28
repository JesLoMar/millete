import { useTranslation } from "react-i18next"
import { cn } from "@/lib/utils"

interface AuthToggleProps {
  mode: "login" | "register"
  onToggle: (mode: "login" | "register") => void
}
export function AuthToggle({ mode, onToggle }: AuthToggleProps) {
  const { t } = useTranslation()

  return (
    <div className="flex p-2 bg-secondary/50 backdrop-blur-sm rounded-2xl w-fit border border-border/50">
      <button
        type="button"
        onClick={() => onToggle("login")}
        className={cn(
          "px-8 py-3 text-sm font-bold uppercase tracking-widest rounded-xl transition-all",
          mode === "login"
            ? "bg-primary text-white shadow-lg"
            : "text-muted-foreground hover:text-white"
        )}
      >
        {t("auth.form.toggle.login")}
      </button>
      <button
        type="button"
        onClick={() => onToggle("register")}
        className={cn(
          "px-8 py-3 text-sm font-bold uppercase tracking-widest rounded-xl transition-all",
          mode === "register"
            ? "bg-primary text-white shadow-lg"
            : "text-muted-foreground hover:text-white"
        )}
      >
        {t("auth.form.toggle.register")}
      </button>
    </div>
  )
}