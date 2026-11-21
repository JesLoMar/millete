import { useCallback } from "react"
import type { ReactNode } from "react"
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
  desktopCenter?: ReactNode
}

export function TopNav({ className, desktopCenter }: TopNavProps) {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const { t } = useTranslation(['nav', 'common'])

  const handleNavigate = useCallback((path: string) => {
    navigate(path)
  }, [navigate])

  const handleLogout = useCallback(() => {
    notify.success(t('logoutSuccess'))
    logout()
  }, [logout, t])

  const handleOpenSidebar = useCallback(() => {

    window.dispatchEvent(new CustomEvent('sidebar:open'))
  }, [])

  return (
    <header className={cn(
      "h-16 bg-transparent px-4 sm:px-6 flex items-center justify-between fixed top-0 left-0 w-full z-30",
      desktopCenter && "lg:grid lg:grid-cols-[minmax(16.5rem,1fr)_minmax(0,auto)_minmax(16.5rem,1fr)] lg:gap-4",
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
          <img
            src="/web-app-icon.webp"
            alt=""
            className="size-9 sm:size-10 object-contain drop-shadow-[0_2px_2px_rgba(192,96,24,0.25)] hover:rotate-[5deg] transition-transform duration-200"
            aria-hidden="true"
          />
          <span className="font-bold text-lg tracking-tight text-foreground hidden sm:inline md:hidden">
            {t('mobileTitle')}
          </span>
        </button>
      </div>

      {desktopCenter && (
        <div className="hidden min-w-0 lg:block">
          {desktopCenter}
        </div>
      )}

      <div className={cn("flex items-center gap-1", desktopCenter && "lg:justify-self-end")}>
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
              <div className="size-8 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0">
                <User size={16} aria-hidden="true" />
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
