import { useTranslation } from "react-i18next"
import { Palette, Check } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { useTheme } from "@/shared/hooks/useTheme"
import { notify } from "@/shared/utils/notifications/notify"
import type { Theme } from "@/shared/themes/palettes"
import { cn } from "@/lib/utils"

interface ThemeSelectorProps {
  className?: string
}

export function ThemeSelector({ className }: ThemeSelectorProps) {
  const { t } = useTranslation()
  const { theme, setTheme, availableThemes } = useTheme()

  const handleThemeChange = (selectedTheme: Theme) => {
    try {
      setTheme(selectedTheme)
    } catch (error) {
      notify.error(t("theme.errors.themeChangeFailed"))
    }
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            "size-10 text-muted-foreground hover:text-foreground",
            className
          )}
        >
          <Palette className="size-5" />
          <span className="sr-only">{t("theme.selector")}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel>{t("theme.palette")}</DropdownMenuLabel>
        {availableThemes.map((t: Theme) => {
          const isSelected = theme.name === t.name
          return (
            <DropdownMenuItem
              key={t.name}
              onClick={() => handleThemeChange(t)}
              className={cn(
                "flex items-center gap-2 cursor-pointer",
                isSelected && "bg-accent font-medium"
              )}
            >
              <span className="text-base">{t.icon}</span>
              <span className="flex-1">{t.label}</span>
              {isSelected && <Check className="size-4 text-primary" />}
            </DropdownMenuItem>
          )
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}