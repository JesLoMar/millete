import { useCallback } from "react"
import { useTranslation } from "react-i18next"
import { Languages, ChevronDown } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { useAvailableLanguages } from "@/shared/hooks/useLanguages"
import { notify } from "@/shared/utils/notifications/notify"
import { cn } from "@/lib/utils"

interface LanguageSelectorProps {
  className?: string
}

export function LanguageSelector({ className }: LanguageSelectorProps) {
  const { t, i18n } = useTranslation()
  const availableLanguages = useAvailableLanguages()

  const currentLanguage = availableLanguages.find(
    lang => lang.code === i18n.language
  ) || availableLanguages[0]

  const handleChangeLanguage = useCallback(async (langCode: string) => {
    try {
      await i18n.changeLanguage(langCode)
    } catch (error) {
      notify.error(t("nav.errors.languageChangeFailed"))
    }
  }, [i18n, t])

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          className={cn(
            "text-muted-foreground hover:text-foreground flex items-center gap-2 px-3",
            className
          )}
        >
          <Languages className="size-5" />
          <span className="text-sm font-bold tracking-wider hidden sm:inline">
            {currentLanguage?.code.toUpperCase() || "??"}
          </span>
          <ChevronDown className="size-4 opacity-50" />
          <span className="sr-only">{t("nav.changeLanguage")}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-44">
        {availableLanguages.map((lang) => {
          const isSelected = i18n.language === lang.code
          return (
            <DropdownMenuItem
              key={lang.code}
              onClick={() => handleChangeLanguage(lang.code)}
              className={cn(
                "flex items-center gap-2 cursor-pointer",
                isSelected && "bg-accent font-medium"
              )}
            >
              <span className="text-base">{lang.flag}</span>
              <span className="flex-1">{lang.nativeName}</span>
            </DropdownMenuItem>
          )
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}