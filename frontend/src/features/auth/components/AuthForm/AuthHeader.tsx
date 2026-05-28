import { useTranslation } from "react-i18next"
import { Home } from "lucide-react"
import { LanguageSelector } from "@/shared/components/LanguageSelector"
import { ThemeSelector } from "@/shared/components/ThemeSelector"

export function AuthHeader() {
  const { t } = useTranslation()

  return (
    <div className="flex items-center justify-between mb-12">
      <div className="flex items-center gap-3">
        <div className="bg-primary/20 p-2.5 rounded-xl">
          <Home className="size-6 text-primary" />
        </div>
        <span className="text-xl font-bold tracking-tight text-white">
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