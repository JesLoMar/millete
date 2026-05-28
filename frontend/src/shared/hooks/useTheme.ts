import { useState, useEffect, useCallback } from "react"
import { THEMES, type Theme, type ThemeColors } from "@/shared/themes/palettes"
export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(() => {
    if (typeof window !== "undefined") {
      const stored = localStorage.getItem("theme-name")
      if (stored) {
        const found = THEMES.find((t: Theme) => t.name === stored)
        if (found) return found
      }
    }
    return THEMES[0]
  })
  // Aplicar todas las variables CSS al DOM
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
    }
    Object.entries(cssVars).forEach(([key, value]) => {
      root.style.setProperty(key, value)
    })
    // Siempre modo oscuro
    root.classList.remove("light")
    root.classList.add("dark")
    localStorage.setItem("theme-name", theme.name)
  }, [theme])
  const setTheme = useCallback((newTheme: Theme) => {
    setThemeState(newTheme)
  }, [])
  const setThemeByName = useCallback((name: string) => {
    const found = THEMES.find((t: Theme) => t.name === name)
    if (found) setThemeState(found)
  }, [])
  return {
    theme,
    setTheme,
    setThemeByName,
    availableThemes: THEMES,
  }
}