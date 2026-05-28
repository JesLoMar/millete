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
export const THEMES: Theme[] = [
  {
    name: "default",
    label: "Azul Profesional",
    icon: "💼",
    colors: {
      background: "222.2 84% 4.9%",
      foreground: "210 40% 98%",
      card: "222.2 84% 4.9%",
      cardForeground: "210 40% 98%",
      popover: "222.2 84% 4.9%",
      popoverForeground: "210 40% 98%",
      primary: "217.2 91.2% 59.8%",
      primaryForeground: "222.2 47.4% 11.2%",
      secondary: "217.2 32.6% 17.5%",
      secondaryForeground: "210 40% 98%",
      muted: "217.2 32.6% 17.5%",
      mutedForeground: "215 20.2% 65.1%",
      accent: "217.2 32.6% 17.5%",
      accentForeground: "210 40% 98%",
      destructive: "0 62.8% 30.6%",
      destructiveForeground: "210 40% 98%",
      border: "217.2 32.6% 17.5%",
      input: "217.2 32.6% 17.5%",
      ring: "224.3 76.3% 48%",
      chart1: "217.2 91.2% 59.8%",
      chart2: "142.1 70.6% 45.3%",
      chart3: "47.9 95.8% 53.1%",
      chart4: "262.1 83.3% 67.8%",
      chart5: "0 72.2% 60.6%",
      surface: "217.2 32.6% 12%",
      surfaceHover: "217.2 32.6% 17.5%",
      subtle: "217.2 32.6% 20%",
    },
  },
  {
    name: "ocean",
    label: "Océano Sereno",
    icon: "🌊",
    colors: {
      background: "200 50% 6%",
      foreground: "187 30% 90%",
      card: "200 50% 8%",
      cardForeground: "187 30% 90%",
      popover: "200 50% 8%",
      popoverForeground: "187 30% 90%",
      primary: "187 85% 50%",
      primaryForeground: "200 50% 6%",
      secondary: "187 30% 15%",
      secondaryForeground: "187 30% 90%",
      muted: "187 30% 15%",
      mutedForeground: "187 25% 60%",
      accent: "187 30% 15%",
      accentForeground: "187 30% 90%",
      destructive: "0 62.8% 30.6%",
      destructiveForeground: "187 30% 90%",
      border: "187 25% 20%",
      input: "187 25% 20%",
      ring: "187 85% 50%",
      chart1: "187 85% 50%",
      chart2: "199 89% 40%",
      chart3: "170 70% 55%",
      chart4: "210 60% 65%",
      chart5: "160 50% 50%",
      surface: "187 25% 12%",
      surfaceHover: "187 25% 18%",
      subtle: "187 25% 22%",
    },
  },
  {
    name: "forest",
    label: "Bosque Fresco",
    icon: "🌿",
    colors: {
      background: "140 30% 6%",
      foreground: "142 20% 90%",
      card: "140 30% 8%",
      cardForeground: "142 20% 90%",
      popover: "140 30% 8%",
      popoverForeground: "142 20% 90%",
      primary: "142 60% 50%",
      primaryForeground: "140 30% 6%",
      secondary: "142 25% 15%",
      secondaryForeground: "142 20% 90%",
      muted: "142 25% 15%",
      mutedForeground: "142 20% 60%",
      accent: "142 25% 15%",
      accentForeground: "142 20% 90%",
      destructive: "0 62.8% 30.6%",
      destructiveForeground: "142 20% 90%",
      border: "142 20% 20%",
      input: "142 20% 20%",
      ring: "142 60% 50%",
      chart1: "142 60% 50%",
      chart2: "120 40% 60%",
      chart3: "85 50% 55%",
      chart4: "160 45% 50%",
      chart5: "45 60% 60%",
      surface: "142 20% 12%",
      surfaceHover: "142 20% 18%",
      subtle: "142 20% 22%",
    },
  },
  {
    name: "sunset",
    label: "Atardecer Cálido",
    icon: "🌅",
    colors: {
      background: "20 30% 6%",
      foreground: "25 30% 90%",
      card: "20 30% 8%",
      cardForeground: "25 30% 90%",
      popover: "20 30% 8%",
      popoverForeground: "25 30% 90%",
      primary: "25 90% 60%",
      primaryForeground: "20 30% 6%",
      secondary: "25 30% 15%",
      secondaryForeground: "25 30% 90%",
      muted: "25 30% 15%",
      mutedForeground: "25 25% 60%",
      accent: "25 30% 15%",
      accentForeground: "25 30% 90%",
      destructive: "0 62.8% 30.6%",
      destructiveForeground: "25 30% 90%",
      border: "25 25% 20%",
      input: "25 25% 20%",
      ring: "25 90% 60%",
      chart1: "25 90% 60%",
      chart2: "15 85% 60%",
      chart3: "45 80% 65%",
      chart4: "0 70% 70%",
      chart5: "55 75% 60%",
      surface: "25 20% 12%",
      surfaceHover: "25 20% 18%",
      subtle: "25 20% 22%",
    },
  },
]