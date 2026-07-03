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

export const DARK_MILLETE_THEME: Theme = {
  name: "dark-millete",
  label: "Dark Millete",
  icon: "🌙",
  colors: {
    background: "33.3 52.9% 6.7%",            // #1A1208 — Madera oscura del piso
    foreground: "37.4 72.6% 85.7%",            // #F5E1C0 — Crema cálida
    card: "25.3 37.3% 10.0%",                // #231810 — Mesa de trabajo
    cardForeground: "37.4 72.6% 85.7%",        // #F5E1C0 — Texto sobre card
    popover: "25.3 37.3% 10.0%",              // #231810 — Igual que card
    popoverForeground: "37.4 72.6% 85.7%",    // #F5E1C0 — Texto sobre popover
    primary: "161.2 47.9% 45.9%",              // #3DAD8A — Verde brillante (iluminado)
    primaryForeground: "33.3 52.9% 6.7%",    // #1A1208 — Texto sobre primary
    secondary: "33.3 30.3% 17.5%",             // #3A2E1F — Marrón oscuro
    secondaryForeground: "37.4 72.6% 85.7%",  // #F5E1C0 — Texto sobre secondary
    muted: "30.0 21.6% 29.0%",                // #5A4A3A — Marrón grisáceo
    mutedForeground: "39.0 18.7% 58.0%",      // #A89A80 — Texto secundario
    accent: "44.4 87.1% 48.6%",               // #E8B010 — Dorado envejecido
    accentForeground: "33.3 52.9% 6.7%",     // #1A1208 — Texto sobre accent
    destructive: "9.5 73.9% 56.5%",           // #E2583E — Tomato brillante
    destructiveForeground: "33.3 52.9% 6.7%", // #1A1208 — Texto sobre destructive
    warning: "32.3 87.4% 53.3%",               // #F09020 — Naranja brillante
    warningForeground: "33.3 52.9% 6.7%",      // #1A1208 — Texto sobre warning
    border: "26.1 23.2% 19.4%",               // #3D3026 — Borde marrón oscuro
    input: "26.1 23.2% 19.4%",                // #3D3026 — Input border
    ring: "25.0 80.7% 53.3%",                 // #E87828 — Crust brillante (foco)
    chart1: "161.2 47.9% 45.9%",               // #3DAD8A — Verde brillante
    chart2: "142.3 54.1% 59.0%",               // #5ECF88 — Verde claro
    chart3: "44.4 87.1% 48.6%",               // #E8B010 — Dorado
    chart4: "215.6 44.0% 59.4%",               // #6A8FC5 — Azul claro
    chart5: "9.5 73.9% 56.5%",               // #E2583E — Tomato brillante
    surface: "31.4 29.6% 13.9%",              // #2E2419 — Superficie elevada
    surfaceHover: "33.3 30.3% 17.5%",         // #3A2E1F — Hover sobre surface
    subtle: "26.1 23.2% 19.4%",               // #3D3026 — Separador sutil
  },
}

/** Array exportado con todos los temas disponibles. */
export const THEMES: Theme[] = [MILLETE_THEME, DARK_MILLETE_THEME]
