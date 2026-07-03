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

export const ROSE_MILLETE_THEME: Theme = {
  name: "rose-millete",
  label: "Rosé Millete",
  icon: "🌸",
  colors: {
    background: "0 100% 97%",
    foreground: "0 20% 15%",
    card: "0 100% 98.5%",
    cardForeground: "0 20% 15%",
    popover: "0 100% 98.5%",
    popoverForeground: "0 20% 15%",
    primary: "340 55% 39%",
    primaryForeground: "0 100% 97%",
    secondary: "340 40% 92%",
    secondaryForeground: "0 20% 15%",
    muted: "340 15% 85%",
    mutedForeground: "340 12% 45%",
    accent: "340 70% 63%",
    accentForeground: "0 20% 15%",
    destructive: "0 70% 52%",
    destructiveForeground: "0 100% 97%",
    warning: "25 80% 58%",
    warningForeground: "0 20% 15%",
    border: "340 20% 88%",
    input: "340 20% 88%",
    ring: "340 60% 56%",
    chart1: "340 55% 39%",
    chart2: "160 50% 53%",
    chart3: "30 80% 60%",
    chart4: "220 40% 65%",
    chart5: "0 70% 52%",
    surface: "340 30% 94%",
    surfaceHover: "340 35% 90%",
    subtle: "340 20% 88%",
  },
}

export const EMBER_MILLETE_THEME: Theme = {
  name: "ember-millete",
  label: "Ember Millete",
  icon: "🔥",
  colors: {
    background: "0 12% 3%",
    foreground: "0 40% 94%",
    card: "0 10% 5%",
    cardForeground: "0 40% 94%",
    popover: "0 10% 5%",
    popoverForeground: "0 40% 94%",
    primary: "355 80% 56%",
    primaryForeground: "0 12% 3%",
    secondary: "0 10% 10%",
    secondaryForeground: "0 40% 94%",
    muted: "0 8% 15%",
    mutedForeground: "0 12% 60%",
    accent: "25 90% 53%",
    accentForeground: "0 12% 3%",
    destructive: "0 100% 63%",
    destructiveForeground: "0 12% 3%",
    warning: "30 90% 55%",
    warningForeground: "0 12% 3%",
    border: "0 8% 20%",
    input: "0 8% 20%",
    ring: "0 100% 69%",
    chart1: "355 80% 56%",
    chart2: "160 50% 50%",
    chart3: "25 90% 53%",
    chart4: "210 50% 60%",
    chart5: "340 60% 65%",
    surface: "0 8% 8%",
    surfaceHover: "0 8% 12%",
    subtle: "0 8% 20%",
  },
}

export const THEMES: Theme[] = [MILLETE_THEME, DARK_MILLETE_THEME, ROSE_MILLETE_THEME, EMBER_MILLETE_THEME]
