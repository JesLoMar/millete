import { useCallback } from "react"
import { useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { LanguageSelector } from "@/shared/components/LanguageSelector"
import { ThemeSelector } from "@/shared/components/ThemeSelector"
import { User, LogOut, Menu } from "lucide-react"
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

  // ✅ Función para abrir el sidebar en móvil
  const handleOpenSidebar = useCallback(() => {
    ;(window as any).__sidebarOpen?.()
  }, [])

  return (
    <header className={cn(
      "h-16 border-b bg-card/50 backdrop-blur-md px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30 transition-all",
      className
    )}>
      {/* ============ LADO IZQUIERDO ============ */}
      <div className="flex items-center gap-2">
        {/* BOTÓN HAMBURGUESA - Solo visible en móvil */}
        <Button
          variant="ghost"
          size="icon"
          className="md:hidden size-9 -ml-1"
          onClick={handleOpenSidebar}
          aria-label={t("sidebar.open")}
        >
          <Menu size={20} aria-hidden="true" />
        </Button>

        {/* Logo convertido en botón accesible */}
        <button
          onClick={() => handleNavigate("/dashboard")}
          className="flex items-center gap-2.5 select-none rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          aria-label={t("nav.goToDashboard")}
        >
          <div className="bg-primary/10 p-0.5 rounded-xl text-primary border border-primary/20 shadow-sm shadow-primary/10 flex items-center justify-center">
            <img 
              src="/web-app-icon.png" 
              alt="" 
              className="size-9 sm:size-10 object-contain" 
              aria-hidden="true"
            />
          </div>
          <span className="font-bold text-base sm:text-lg tracking-tight bg-linear-to-r from-foreground to-foreground/70 bg-clip-text text-transparent hidden xs:inline">
            {t("app.name")}
          </span>
        </button>
      </div>

      {/* ============ LADO DERECHO ============ */}
      <div className="flex items-center gap-1">
        <LanguageSelector />
        <ThemeSelector />
        
        <div className="h-8 w-px bg-border/60 mx-1 sm:mx-2" />
        
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="relative h-10 flex items-center gap-2 px-2 sm:px-3 rounded-full hover:bg-accent/50"
              aria-label={t("nav.userMenu")}
            >
              {/* Avatar circular con iniciales */}
              <div className="size-8 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0">
                <User size={16} aria-hidden="true" />
              </div>
              {/* Texto - oculto en móviles muy pequeños */}
              <div className="hidden sm:block text-right">
                <p className={cn(
                  "font-medium leading-none text-sm",
                  hasOnlyOneField && "text-sm"
                )}>
                  {primary}
                </p>
                {secondary && (
                  <p className="text-xs text-muted-foreground mt-0.5 truncate max-w-30">
                    {secondary}
                  </p>
                )}
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuItem onClick={() => handleNavigate("/profile")}>
              <User className="mr-2 size-4" aria-hidden="true" />
              {t("nav.profile")}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleLogout}
              className="text-destructive focus:text-destructive"
            >
              <LogOut className="mr-2 size-4" aria-hidden="true" />
              {t("nav.logout")}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}