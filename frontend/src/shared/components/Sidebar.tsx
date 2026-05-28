import { useCallback } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { Construction } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import { cn } from "@/lib/utils"
import { getEnabledNavItems, getDisabledNavItems, type NavItem } from "@/shared/config/navigation"
import { notify } from "@/shared/utils/notifications/notify"

interface SidebarProps {
  className?: string
  showDisabled?: boolean
}

export function Sidebar({ className, showDisabled = true }: SidebarProps) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()

  const mainItems = getEnabledNavItems("main")
  const bottomItems = getEnabledNavItems("bottom")
  const disabledItems = showDisabled ? getDisabledNavItems() : []

  const handleNavigate = useCallback((path: string) => {
    navigate(path)
  }, [navigate])

  const handleDisabledClick = useCallback((itemLabelKey: string) => {
    notify.info(
      t("sidebar.errors.featureComingSoon", { 
        feature: t(itemLabelKey) 
      })
    )
  }, [t])

  const isActive = (item: NavItem): boolean => {
    if (item.path === "/dashboard") {
      return location.pathname === "/dashboard"
    }
    return location.pathname.startsWith(item.path)
  }

  return (
    <aside className={cn("w-64 border-r bg-card/50 backdrop-blur-md flex flex-col h-[calc(100vh-4rem)] sticky top-16 select-none", className)}>
      <nav className="flex-1 p-4" aria-label={t("sidebar.mainNav")}>
        <ul className="space-y-1">
          {mainItems.map((item) => {
            const active = isActive(item)
            return (
              <li key={item.id}>
                <Button
                  variant={active ? "secondary" : "ghost"}
                  onClick={() => handleNavigate(item.path)}
                  className={cn(
                    "w-full justify-start gap-3 h-11 px-4 transition-all duration-200",
                    active
                      ? "bg-accent/50 text-foreground font-medium border-l-2 border-primary rounded-l-none pl-3.5"
                      : "text-muted-foreground hover:text-foreground hover:bg-accent/30"
                  )}
                >
                  <item.icon className={cn("size-5 transition-transform duration-200", active && "text-primary scale-105")} />
                  <span>{t(item.labelKey)}</span>
                </Button>
              </li>
            )
          })}
        </ul>

        {disabledItems.length > 0 && (
          <div className="mt-6 pt-6 border-t border-border/40">
            <p className="px-4 text-xs font-semibold text-muted-foreground/60 uppercase tracking-wider mb-2">
              {t("sidebar.comingSoon")}
            </p>
            <ul className="space-y-1">
              {disabledItems.map((item) => (
                <li key={item.id}>
                  <Button
                    variant="ghost"
                    onClick={() => handleDisabledClick(item.labelKey)}
                    className="w-full justify-start gap-3 h-11 px-4 text-muted-foreground/40 hover:bg-accent/10 hover:text-muted-foreground/60 transition-colors"
                  >
                    <Construction className="size-5 shrink-0 text-muted-foreground/30" />
                    <span className="truncate">{t(item.labelKey)}</span>
                  </Button>
                </li>
              ))}
            </ul>
          </div>
        )}
      </nav>

      {bottomItems.length > 0 && (
        <nav className="p-4 mt-auto border-t" aria-label={t("sidebar.bottomNav")}>
          <ul className="space-y-1">
            {bottomItems.map((item) => {
              const active = isActive(item)
              return (
                <li key={item.id}>
                  <Button
                    variant={active ? "secondary" : "ghost"}
                    onClick={() => handleNavigate(item.path)}
                    className={cn(
                      "w-full justify-start gap-3 h-11 px-4 transition-all duration-200",
                      active
                        ? "bg-accent/50 text-foreground font-medium"
                        : "text-muted-foreground hover:text-foreground hover:bg-accent/30"
                    )}
                  >
                    <item.icon className="size-5" />
                    <span>{t(item.labelKey)}</span>
                  </Button>
                </li>
              )
            })}
          </ul>
        </nav>
      )}
    </aside>
  )
}