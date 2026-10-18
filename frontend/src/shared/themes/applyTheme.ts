
import {
  DARK_MILLETE_THEME,
  MILLETE_THEME,
  THEMES,
  type Theme,
  type ThemeColors,
} from '@/shared/themes/palettes';

const THEME_STORAGE_KEY = 'millete-theme';

export function getInitialTheme(): Theme {
  if (typeof window === 'undefined') {
    return MILLETE_THEME;
  }

  try {
    const savedThemeName = window.localStorage.getItem(
      THEME_STORAGE_KEY,
    );

    const savedTheme = THEMES.find(
      (theme) => theme.name === savedThemeName,
    );

    if (savedTheme) {
      return savedTheme;
    }
  } catch {
    // Si localStorage no está disponible, usamos la preferencia del sistema.
  }

  const prefersDark =
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-color-scheme: dark)').matches;

  return prefersDark ? DARK_MILLETE_THEME : MILLETE_THEME;
}

export function persistTheme(theme: Theme): void {
  try {
    window.localStorage.setItem(
      THEME_STORAGE_KEY,
      theme.name,
    );
  } catch {
    // La persistencia es opcional.
  }
}

function getThemeCssVariables(
  colors: ThemeColors,
): Record<string, string> {
  return {
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
    '--destructive-foreground': colors.destructiveForeground,
    '--warning': colors.warning,
    '--warning-foreground': colors.warningForeground,
    '--border': colors.border,
    '--input': colors.input,
    '--ring': colors.ring,
    '--crust': colors.crust,
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
    '--sidebar-primary-foreground': colors.primaryForeground,
    '--sidebar-accent': colors.accent,
    '--sidebar-accent-foreground': colors.accentForeground,
    '--sidebar-border': colors.border,
    '--sidebar-ring': colors.ring,
  };
}

export function applyTheme(
  root: HTMLElement,
  colors: ThemeColors,
): void {
  const cssVariables = getThemeCssVariables(colors);

  Object.entries(cssVariables).forEach(([variable, value]) => {
    root.style.setProperty(variable, value);
  });

  const backgroundParts = colors.background.trim().split(/\s+/);
  const lightness = Number.parseFloat(
    backgroundParts[backgroundParts.length - 1] ?? '',
  );

  root.style.colorScheme =
    Number.isFinite(lightness) && lightness < 50
      ? 'dark'
      : 'light';

  const metaThemeColor =
    root.ownerDocument.querySelector<HTMLMetaElement>(
      'meta[name="theme-color"]',
    );

  metaThemeColor?.setAttribute(
    'content',
    `hsl(${colors.background})`,
  );
}
