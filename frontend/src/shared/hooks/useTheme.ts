import {
  useCallback,
  useEffect,
  useState,
} from 'react';

import {
  DARK_MILLETE_THEME,
  MILLETE_THEME,
  THEMES,
  type Theme,
  type ThemeColors,
} from '@/shared/themes/palettes';

const THEME_STORAGE_KEY = 'millete-theme';

const getSavedTheme = (): Theme | null => {
  try {
    const savedThemeName = localStorage.getItem(
      THEME_STORAGE_KEY,
    );

    if (!savedThemeName) {
      return null;
    }

    return (
      THEMES.find(
        (theme) => theme.name === savedThemeName,
      ) ?? null
    );
  } catch {
    return null;
  }
};

const getInitialTheme = (): Theme => {
  if (typeof window === 'undefined') {
    return MILLETE_THEME;
  }

  const savedTheme = getSavedTheme();

  if (savedTheme) {
    return savedTheme;
  }

  return window.matchMedia(
    '(prefers-color-scheme: dark)',
  ).matches
    ? DARK_MILLETE_THEME
    : MILLETE_THEME;
};

const persistTheme = (theme: Theme): void => {
  try {
    localStorage.setItem(
      THEME_STORAGE_KEY,
      theme.name,
    );
  } catch {
    // La persistencia es opcional; el tema ya se ha aplicado al estado.
  }
};

const getThemeCssVariables = (
  colors: ThemeColors,
): Record<string, string> => ({
  '--background': colors.background,
  '--foreground': colors.foreground,
  '--card': colors.card,
  '--card-foreground': colors.cardForeground,
  '--popover': colors.popover,
  '--popover-foreground': colors.popoverForeground,
  '--primary': colors.primary,
  '--primary-foreground': colors.primaryForeground,
  '--secondary': colors.secondary,
  '--secondary-foreground': colors.secondaryForeground,
  '--muted': colors.muted,
  '--muted-foreground': colors.mutedForeground,
  '--accent': colors.accent,
  '--accent-foreground': colors.accentForeground,
  '--destructive': colors.destructive,
  '--destructive-foreground':
    colors.destructiveForeground,
  '--warning': colors.warning,
  '--warning-foreground':
    colors.warningForeground,
  '--border': colors.border,
  '--input': colors.input,
  '--ring': colors.ring,
  '--chart-1': colors.chart1,
  '--chart-2': colors.chart2,
  '--chart-3': colors.chart3,
  '--chart-4': colors.chart4,
  '--chart-5': colors.chart5,
  '--surface': colors.surface,
  '--surface-hover': colors.surfaceHover,
  '--subtle': colors.subtle,
  '--sidebar': colors.card,
  '--sidebar-foreground': colors.foreground,
  '--sidebar-primary': colors.primary,
  '--sidebar-primary-foreground':
    colors.primaryForeground,
  '--sidebar-accent': colors.accent,
  '--sidebar-accent-foreground':
    colors.accentForeground,
  '--sidebar-border': colors.border,
  '--sidebar-ring': colors.ring,
});

const applyTheme = (
  root: HTMLElement,
  colors: ThemeColors,
): void => {
  const cssVariables = getThemeCssVariables(colors);

  Object.entries(cssVariables).forEach(
    ([variable, value]) => {
      root.style.setProperty(variable, value);
    },
  );
};

export function useTheme() {
  const [theme, setThemeState] =
    useState<Theme>(getInitialTheme);

  const setTheme = useCallback((selectedTheme: Theme) => {
    setThemeState(selectedTheme);
    persistTheme(selectedTheme);
  }, []);

  const setThemeByName = useCallback(
    (name: string) => {
      const selectedTheme = THEMES.find(
        (availableTheme) => availableTheme.name === name,
      );

      if (!selectedTheme) {
        return;
      }

      setTheme(selectedTheme);
    },
    [setTheme],
  );

  useEffect(() => {
    applyTheme(
      document.documentElement,
      theme.colors,
    );
  }, [theme]);

  return {
    theme,
    setTheme,
    setThemeByName,
    availableThemes: THEMES,
  };
}