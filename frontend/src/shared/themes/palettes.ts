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

export const MILLETE_THEME: Theme = {
  name: "millete",
  label: "Millete",
  icon: "🍞",
  colors: {

    background: "37.2 75.8% 87.1%",
    foreground: "24.0 32.6% 18.0%",


    card: "39.5 100.0% 92.5%",
    cardForeground: "24.0 32.6% 18.0%",
    popover: "37.2 75.8% 87.1%",
    popoverForeground: "24.0 32.6% 18.0%",


    primary: "162.0 48.1% 20.4%",
    primaryForeground: "39.5 100.0% 92.5%",


    secondary: "39.1 76.7% 83.1%",
    secondaryForeground: "24.0 32.6% 18.0%",


    muted: "42.1 42.9% 73.9%",
    mutedForeground: "29.1 13.4% 48.4%",


    accent: "47.0 91.7% 52.7%",
    accentForeground: "24.0 32.6% 18.0%",


    destructive: "9.4 70.2% 44.7%",
    destructiveForeground: "39.5 100.0% 92.5%",


    warning: "27.1 100.0% 32.5%",
    warningForeground: "39.5 100.0% 92.5%",


    border: "42.1 42.9% 73.9%",
    input: "42.1 42.9% 73.9%",
    ring: "25.7 77.8% 42.4%",


    chart1: "162.0 48.1% 20.4%",
    chart2: "144.1 47.1% 46.7%",
    chart3: "47.0 91.7% 52.7%",
    chart4: "215.6 38.1% 46.9%",
    chart5: "9.4 70.2% 44.7%",


    surface: "39.1 76.7% 83.1%",
    surfaceHover: "43.3 61.0% 76.9%",
    subtle: "42.1 42.9% 73.9%",
  },
}

export const DARK_MILLETE_THEME: Theme = {
  name: "dark-millete",
  label: "Dark Millete",
  icon: "🌙",
  colors: {
    background: "33.3 52.9% 6.7%",
    foreground: "37.4 72.6% 85.7%",
    card: "25.3 37.3% 10.0%",
    cardForeground: "37.4 72.6% 85.7%",
    popover: "25.3 37.3% 10.0%",
    popoverForeground: "37.4 72.6% 85.7%",
    primary: "161.2 47.9% 45.9%",
    primaryForeground: "33.3 52.9% 6.7%",
    secondary: "33.3 30.3% 17.5%",
    secondaryForeground: "37.4 72.6% 85.7%",
    muted: "30.0 21.6% 29.0%",
    mutedForeground: "39.0 18.7% 58.0%",
    accent: "44.4 87.1% 48.6%",
    accentForeground: "33.3 52.9% 6.7%",
    destructive: "9.5 73.9% 56.5%",
    destructiveForeground: "33.3 52.9% 6.7%",
    warning: "32.3 87.4% 53.3%",
    warningForeground: "33.3 52.9% 6.7%",
    border: "26.1 23.2% 19.4%",
    input: "26.1 23.2% 19.4%",
    ring: "25.0 80.7% 53.3%",
    chart1: "161.2 47.9% 45.9%",
    chart2: "142.3 54.1% 59.0%",
    chart3: "44.4 87.1% 48.6%",
    chart4: "215.6 44.0% 59.4%",
    chart5: "9.5 73.9% 56.5%",
    surface: "31.4 29.6% 13.9%",
    surfaceHover: "33.3 30.3% 17.5%",
    subtle: "26.1 23.2% 19.4%",
  },
}

export const THEMES: Theme[] = [MILLETE_THEME, DARK_MILLETE_THEME]
