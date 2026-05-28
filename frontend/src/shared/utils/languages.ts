export type SupportedLanguageCode = "de" | "en" | "es" | "fr" | "it" | "pt"

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
}

const displayNamesCache = new Map<string, Intl.DisplayNames>()

function getDisplayNames(code: string): Intl.DisplayNames {
  if (!displayNamesCache.has(code)) {
    displayNamesCache.set(code, new Intl.DisplayNames([code], { type: "language" }))
  }
  return displayNamesCache.get(code)!
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
  try {
    const displayNames = getDisplayNames(code)
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