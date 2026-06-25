import { useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { User, LogOut, Menu } from "lucide-react"
import { Button } from "@/shared/components/core/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/shared/components/core/dropdown-menu"
import { useAuth } from "@/features/auth/context/AuthContext"
import { notify } from "@/shared/utils/notifications/notify"
import { cn } from "@/lib/utils"
import { LanguageSelector } from "./LanguageSelector"
import { ThemeSelector } from "./ThemeSelector"
import { NotificationBell } from "@/features/notifications/components/NotificationBell"

interface TopNavProps {
  className?: string
}

function getUserDisplay(
  user: { name?: string; email?: string } | null,
  fallbackGuest: string,
  fallbackUser: string
): {
  primary: string
  secondary: string | null
} {
  if (!user) {
    return { primary: fallbackGuest, secondary: null }
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
  return { primary: fallbackUser, secondary: null }
}

export function TopNav({ className }: TopNavProps) {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { t } = useTranslation(['nav', 'common'])

  const { primary, secondary } = getUserDisplay(user, t('guest'), t('user'))
  const hasOnlyOneField = !secondary

  const handleNavigate = useCallback((path: string) => {
    navigate(path)
  }, [navigate])

  const handleLogout = useCallback(() => {
    notify.success(t('logoutSuccess'))
    logout()
  }, [logout, t])

  const handleOpenSidebar = useCallback(() => {
    // Abrir sidebar mediante evento personalizado interno
    window.dispatchEvent(new CustomEvent('sidebar:open'))
  }, [])

  return (
    <header className={cn(
      "h-16 border-b bg-card/50 backdrop-blur-md px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30 transition-all",
      className
    )}>
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          className="md:hidden size-9 -ml-1"
          onClick={handleOpenSidebar}
          aria-label={t('open')}
        >
          <Menu size={20} aria-hidden="true" />
        </Button>

        <button
          type="button"
          onClick={() => handleNavigate("/dashboard")}
          className="flex items-center gap-2.5 select-none rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          aria-label={t('goToDashboard')}
        >
          <div className="bg-primary/10 p-0.5 rounded-xl text-primary border border-primary/20 shadow-sm shadow-primary/10 flex items-center justify-center">
            <img
              src="/web-app-icon.png"
              alt=""
              className="size-9 sm:size-10 object-contain"
              aria-hidden="true"
            />
          </div>
          <span className="font-bold text-lg tracking-tight text-foreground hidden sm:inline">
            {t('mobileTitle')}
          </span>
        </button>
      </div>

      <div className="flex items-center gap-1">
        <LanguageSelector />
        <ThemeSelector />
        <NotificationBell />
        <div className="h-8 w-px bg-border/60 mx-1 sm:mx-2" />
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="relative h-10 sm:h-auto flex items-center gap-2 px-2 sm:px-3 rounded-full sm:rounded-lg hover:bg-accent/50 transition-all py-1.5"
              aria-label={t('userMenu')}
            >
              <div className="size-8 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0 sm:hidden">
                <User size={16} aria-hidden="true" />
              </div>

              <div className="hidden sm:block text-left min-w-0">
                <p className={cn(
                  "font-medium leading-none text-sm text-foreground",
                  hasOnlyOneField && "text-sm"
                )}>
                  {primary}
                </p>
                {secondary && (
                  <p className="text-xs text-muted-foreground mt-1 truncate max-w-37.5">
                    {secondary}
                  </p>
                )}
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuItem onClick={() => handleNavigate("/profile")}>
              <User className="mr-2 size-4" aria-hidden="true" />
              {t('profile')}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleLogout}
              className="text-destructive focus:text-destructive"
            >
              <LogOut className="mr-2 size-4" aria-hidden="true" />
              {t('logout')}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}