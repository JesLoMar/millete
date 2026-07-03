import { useState, useEffect, useCallback } from "react"
import { MILLETE_THEME, DARK_MILLETE_THEME, THEMES, type Theme, type ThemeColors } from "@/shared/themes/palettes"

const THEME_STORAGE_KEY = "millete-theme"

function getInitialTheme(): Theme {
  if (typeof window === "undefined") return MILLETE_THEME
  const saved = localStorage.getItem(THEME_STORAGE_KEY)
  if (saved) {
    const found = THEMES.find((t) => t.name === saved)
    if (found) return found
  }
  // Detectar preferencia del sistema (opcional)
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
  if (prefersDark) return DARK_MILLETE_THEME
  return MILLETE_THEME
}

export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme)

  const setTheme = useCallback((t: Theme) => {
    setThemeState(t)
    localStorage.setItem(THEME_STORAGE_KEY, t.name)
  }, [])

  const setThemeByName = useCallback(
    (name: string) => {
      const found = THEMES.find((t) => t.name === name)
      if (found) setTheme(found)
    },
    [setTheme]
  )

  // Aplicar todas las variables CSS al DOM (incluyendo sidebar)
  useEffect(() => {
    const root = document.documentElement
    const colors: ThemeColors = theme.colors
    const cssVars: Record<string, string> = {
      "--background": colors.background,
      "--foreground": colors.foreground,
      "--card": colors.card,
      "--card-foreground": colors.cardForeground,
      "--popover": colors.popover,
      "--popover-foreground": colors.popoverForeground,
      "--primary": colors.primary,
      "--primary-foreground": colors.primaryForeground,
      "--secondary": colors.secondary,
      "--secondary-foreground": colors.secondaryForeground,
      "--muted": colors.muted,
      "--muted-foreground": colors.mutedForeground,
      "--accent": colors.accent,
      "--accent-foreground": colors.accentForeground,
      "--destructive": colors.destructive,
      "--destructive-foreground": colors.destructiveForeground,
      "--warning": colors.warning,
      "--warning-foreground": colors.warningForeground,
      "--border": colors.border,
      "--input": colors.input,
      "--ring": colors.ring,
      "--chart-1": colors.chart1,
      "--chart-2": colors.chart2,
      "--chart-3": colors.chart3,
      "--chart-4": colors.chart4,
      "--chart-5": colors.chart5,
      "--surface": colors.surface,
      "--surface-hover": colors.surfaceHover,
      "--subtle": colors.subtle,
      // Sidebar variables — dinámicas, derivadas del tema
      "--sidebar": colors.card,
      "--sidebar-foreground": colors.foreground,
      "--sidebar-primary": colors.primary,
      "--sidebar-primary-foreground": colors.primaryForeground,
      "--sidebar-accent": colors.accent,
      "--sidebar-accent-foreground": colors.accentForeground,
      "--sidebar-border": colors.border,
      "--sidebar-ring": colors.ring,
    }
    Object.entries(cssVars).forEach(([key, value]) => {
      root.style.setProperty(key, value)
    })
  }, [theme])

  return {
    theme,
    setTheme,
    setThemeByName,
    availableThemes: THEMES,
  }
}
