import { useTranslation } from "react-i18next"
import { LanguageSelector } from "@/shared/components/LanguageSelector"
import { ThemeSelector } from "@/shared/components/ThemeSelector"

export function AuthHeader() {
  const { t } = useTranslation()

  return (
    <div className="flex items-center justify-between mb-12">
      <div className="flex items-center gap-3">
        <div className="bg-primary/20 p-1 rounded-xl flex items-center justify-center">
          <img 
            src="/web-app-icon.png" 
            alt={t("app.name")} 
            className="size-13 object-contain" 
          />
        </div>
        <span className="text-2xl font-bold tracking-tight text-white">
          {t("app.name")}
        </span>
      </div>
      <div className="flex items-center gap-2">
        <LanguageSelector />
        <ThemeSelector />
      </div>
    </div>
  )
}