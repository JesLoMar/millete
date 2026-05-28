import { useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { LanguageSelector } from "@/shared/components/LanguageSelector"
import { ThemeSelector } from "@/shared/components/ThemeSelector"
import { Wallet2, User, LogOut } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { useAuth } from "@/features/auth/context/AuthContext"
import { notify } from "@/shared/utils/notifications/notify"
import { cn } from "@/lib/utils"

interface TopNavProps {
  className?: string
}

function getUserDisplay(
  user: { name?: string; email?: string } | null, 
  t: (key: string) => string
): {
  primary: string
  secondary: string | null
} {
  if (!user) {
    return { primary: t("nav.guest"), secondary: null }
  }
  const hasName = !!user.name
  const hasEmail = !!user.email
  if (hasName && hasEmail) {
    return { primary: user.name!, secondary: user.email! }
  }
  if (hasName) {
    return { primary: user.name!, secondary: null }
  }
  if (hasEmail) {
    return { primary: user.email!.split("@")[0], secondary: user.email! }
  }
  return { primary: t("nav.user"), secondary: null }
}

export function TopNav({ className }: TopNavProps) {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { t } = useTranslation()

  const { primary, secondary } = getUserDisplay(user, t)
  const hasOnlyOneField = !secondary

  const handleNavigate = useCallback((path: string) => {
    navigate(path)
  }, [navigate])

  const handleLogout = useCallback(() => {
    notify.success(t("nav.logoutSuccess"))
    logout()
  }, [logout, t])

  return (
    <header className={cn(
      "h-16 border-b bg-card/50 backdrop-blur-md px-6 flex items-center justify-between sticky top-0 z-40 transition-all",
      className
    )}>
      <div className="flex items-center gap-2.5 cursor-pointer select-none" onClick={() => handleNavigate("/dashboard")}>
        <div className="bg-primary/10 p-2 rounded-xl text-primary border border-primary/20 shadow-sm shadow-primary/10">
          <Wallet2 className="size-5" />
        </div>
        <span className="font-bold text-lg tracking-tight bg-linear-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
          {t("app.name")}
        </span>
      </div>

      <div className="flex items-center gap-1">
        <LanguageSelector />
        <ThemeSelector />
        <div className="h-8 w-px bg-border mx-2" />
        
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="relative h-10 flex items-center gap-2 px-3 rounded-full hover:bg-accent/50"
            >
              <div className="text-right">
                <p className={cn(
                  "font-medium leading-none",
                  hasOnlyOneField ? "text-sm" : "text-sm"
                )}>
                  {primary}
                </p>
                {secondary && (
                  <p className="text-xs text-muted-foreground mt-0.5">
                    {secondary}
                  </p>
                )}
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuItem onClick={() => handleNavigate("/profile")}>
              <User className="mr-2 size-4" />
              {t("nav.profile")}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleLogout}
              className="text-destructive focus:text-destructive"
            >
              <LogOut className="mr-2 size-4" />
              {t("nav.logout")}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}