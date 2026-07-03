export type SupportedLanguageCode = "de" | "en" | "es" | "fr" | "it" | "pt" | "ja"

export interface Language {
  code: string
  nativeName: string
  englishName: string
  flag: string
}

const LANGUAGE_MAP: Record<SupportedLanguageCode, Omit<Language, "code">> = {
  es: { nativeName: "Español", englishName: "Spanish", flag: "🇪🇸" },
  en: { nativeName: "English", englishName: "English", flag: "🇬🇧" },
  fr: { nativeName: "Français", englishName: "French", flag: "🇫🇷" },
  de: { nativeName: "Deutsch", englishName: "German", flag: "🇩🇪" },
  it: { nativeName: "Italiano", englishName: "Italian", flag: "🇮🇹" },
  pt: { nativeName: "Português", englishName: "Portuguese", flag: "🇵🇹" },
  ja: { nativeName: "日本語", englishName: "Japanese", flag: "🇯🇵" },
}

// Pre-create Intl.DisplayNames for known locales at module scope
const precreatedDisplayNames: Record<SupportedLanguageCode, Intl.DisplayNames> = {
  es: new Intl.DisplayNames(["es"], { type: "language" }),
  en: new Intl.DisplayNames(["en"], { type: "language" }),
  fr: new Intl.DisplayNames(["fr"], { type: "language" }),
  de: new Intl.DisplayNames(["de"], { type: "language" }),
  it: new Intl.DisplayNames(["it"], { type: "language" }),
  pt: new Intl.DisplayNames(["pt"], { type: "language" }),
  ja: new Intl.DisplayNames(["ja"], { type: "language" }),
}

function getDisplayNames(code: string): Intl.DisplayNames | undefined {
  return precreatedDisplayNames[code as SupportedLanguageCode];
}

function getFlagFromCode(code: string): string {
  if (code.length === 2) {
    const base = 0x1F1E6 - 65
    const first = code[0].toUpperCase().charCodeAt(0) + base
    const second = code[1].toUpperCase().charCodeAt(0) + base
    return String.fromCodePoint(first, second)
  }
  return "🌐"
}

function getNativeNameFromCode(code: string): string {
  const displayNames = getDisplayNames(code)
  if (!displayNames) {
    return code.toUpperCase()
  }
  try {
    return displayNames.of(code) || code.toUpperCase()
  } catch {
    return code.toUpperCase()
  }
}

export function getLanguageFromCode(code: string): Language {
  if (code in LANGUAGE_MAP) {
    const mapped = LANGUAGE_MAP[code as SupportedLanguageCode]
    return {
      code,
      ...mapped,
    }
  }
  
  return {
    code,
    nativeName: getNativeNameFromCode(code),
    englishName: code.toUpperCase(),
    flag: getFlagFromCode(code),
  }
}

export function getSupportedLanguages(): Language[] {
  return (Object.keys(LANGUAGE_MAP) as SupportedLanguageCode[]).map((code) => ({
    code,
    ...LANGUAGE_MAP[code],
  }))
}
