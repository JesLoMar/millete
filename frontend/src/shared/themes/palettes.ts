export interface ThemeColors {
  background: string
  foreground: string
  card: string
  cardForeground: string
  popover: string
  popoverForeground: string
  primary: string
  primaryForeground: string
  secondary: string
  secondaryForeground: string
  muted: string
  mutedForeground: string
  accent: string
  accentForeground: string
  destructive: string
  destructiveForeground: string
  warning: string
  warningForeground: string
  border: string
  input: string
  ring: string
  chart1: string
  chart2: string
  chart3: string
  chart4: string
  chart5: string
  surface: string
  surfaceHover: string
  subtle: string
}

export interface Theme {
  name: string
  label: string
  icon: string
  colors: ThemeColors
}

/**
 * Millete Design System v2.4 — Sendero Visual
 * Tema único con la paleta de marca propia.
 * Todos los colores derivados de la paleta Millete (light theme).
 */
export const MILLETE_THEME: Theme = {
  name: "millete",
  label: "Millete",
  icon: "🍞",
  colors: {
    // Base
    background: "37.2 75.8% 87.1%",       // --brand-crumb-cool #F7E4C5
    foreground: "24.0 32.6% 18.0%",        // --technical-charcoal #3D2B1F

    // Cards & Popovers
    card: "39.5 100.0% 92.5%",             // --brand-crumb-highlight #FFF2D9
    cardForeground: "24.0 32.6% 18.0%",    // charcoal
    popover: "37.2 75.8% 87.1%",           // crumb-cool
    popoverForeground: "24.0 32.6% 18.0%", // charcoal

    // Primary (Acción Principal) — Bill Ink
    primary: "162.0 48.1% 20.4%",          // --brand-bill-ink #1B4D3E
    primaryForeground: "39.5 100.0% 92.5%", // crumb-highlight

    // Secondary — Crumb
    secondary: "39.1 76.7% 83.1%",        // --brand-crumb #F5DEB3
    secondaryForeground: "24.0 32.6% 18.0%", // charcoal

    // Muted — Crumb Shadow / Stone
    muted: "42.1 42.9% 73.9%",            // --brand-crumb-shadow #D9C8A0
    mutedForeground: "29.1 13.4% 48.4%",  // --technical-stone #8C7B6B

    // Accent — Butter
    accent: "47.0 91.7% 52.7%",           // --brand-butter #F5C518
    accentForeground: "24.0 32.6% 18.0%", // charcoal

    // Destructive — Tomato
    destructive: "9.4 70.2% 44.7%",        // --brand-tomato #C23B22
    destructiveForeground: "39.5 100.0% 92.5%", // crumb-highlight

    // Warning — Burnt Orange (text on light, bg on dark)
    warning: "27.1 100.0% 32.5%",          // --technical-orange-text #A64B00
    warningForeground: "39.5 100.0% 92.5%", // crumb-highlight

    // Border & Input
    border: "42.1 42.9% 73.9%",           // crumb-shadow
    input: "42.1 42.9% 73.9%",            // crumb-shadow
    ring: "25.7 77.8% 42.4%",             // --brand-crust #C06018

    // Charts
    chart1: "162.0 48.1% 20.4%",          // bill-ink (primary)
    chart2: "144.1 47.1% 46.7%",          // success #3FAF6C
    chart3: "47.0 91.7% 52.7%",           // butter (accent)
    chart4: "215.6 38.1% 46.9%",          // slate-blue #4A6FA5
    chart5: "9.4 70.2% 44.7%",            // tomato (destructive)

    // Dashboard custom
    surface: "39.1 76.7% 83.1%",          // crumb
    surfaceHover: "43.3 61.0% 76.9%",      // crumb-warm
    subtle: "42.1 42.9% 73.9%",           // crumb-shadow
  },
}

export const MILLETE_DARK_THEME: Theme = {
  name: "D-Millete",
  label: "Dark Millete",
  icon: "🍞",
  colors: {
    // Base
    background: "37.2 75.8% 87.1%",       // --brand-crumb-cool #F7E4C5
    foreground: "24.0 32.6% 18.0%",        // --technical-charcoal #3D2B1F

    // Cards & Popovers
    card: "39.5 100.0% 92.5%",             // --brand-crumb-highlight #FFF2D9
    cardForeground: "24.0 32.6% 18.0%",    // charcoal
    popover: "37.2 75.8% 87.1%",           // crumb-cool
    popoverForeground: "24.0 32.6% 18.0%", // charcoal

    // Primary (Acción Principal) — Bill Ink
    primary: "162.0 48.1% 20.4%",          // --brand-bill-ink #1B4D3E
    primaryForeground: "39.5 100.0% 92.5%", // crumb-highlight

    // Secondary — Crumb
    secondary: "39.1 76.7% 83.1%",        // --brand-crumb #F5DEB3
    secondaryForeground: "24.0 32.6% 18.0%", // charcoal

    // Muted — Crumb Shadow / Stone
    muted: "42.1 42.9% 73.9%",            // --brand-crumb-shadow #D9C8A0
    mutedForeground: "29.1 13.4% 48.4%",  // --technical-stone #8C7B6B

    // Accent — Butter
    accent: "47.0 91.7% 52.7%",           // --brand-butter #F5C518
    accentForeground: "24.0 32.6% 18.0%", // charcoal

    // Destructive — Tomato
    destructive: "9.4 70.2% 44.7%",        // --brand-tomato #C23B22
    destructiveForeground: "39.5 100.0% 92.5%", // crumb-highlight

    // Warning — Burnt Orange (text on light, bg on dark)
    warning: "27.1 100.0% 32.5%",          // --technical-orange-text #A64B00
    warningForeground: "39.5 100.0% 92.5%", // crumb-highlight

    // Border & Input
    border: "42.1 42.9% 73.9%",           // crumb-shadow
    input: "42.1 42.9% 73.9%",            // crumb-shadow
    ring: "25.7 77.8% 42.4%",             // --brand-crust #C06018

    // Charts
    chart1: "162.0 48.1% 20.4%",          // bill-ink (primary)
    chart2: "144.1 47.1% 46.7%",          // success #3FAF6C
    chart3: "47.0 91.7% 52.7%",           // butter (accent)
    chart4: "215.6 38.1% 46.9%",          // slate-blue #4A6FA5
    chart5: "9.4 70.2% 44.7%",            // tomato (destructive)

    // Dashboard custom
    surface: "39.1 76.7% 83.1%",          // crumb
    surfaceHover: "43.3 61.0% 76.9%",      // crumb-warm
    subtle: "42.1 42.9% 73.9%",           // crumb-shadow
  },
}

/** Array exportado para compatibilidad (un solo tema). */
export const THEMES: Theme[] = [MILLETE_THEME, MILLETE_DARK_THEME]
