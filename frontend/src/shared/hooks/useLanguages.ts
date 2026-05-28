import { useMemo } from "react"
import { useTranslation } from "react-i18next"
import { getLanguageFromCode } from "@/shared/utils/languages"
import type { Language } from "@/shared/utils/languages"

export function useAvailableLanguages(): Language[] {
  const { i18n } = useTranslation()
  
  return useMemo(() => {
    const supportedLngs = i18n.options.supportedLngs as string[] | undefined
    
    if (!supportedLngs || supportedLngs.length === 0) {
      return [getLanguageFromCode(i18n.language)]
    }
    
    // Combina filter + map en una sola iteración
    return supportedLngs.reduce<Language[]>((acc, code) => {
      if (typeof code === "string" && code !== "cimode") {
        acc.push(getLanguageFromCode(code))
      }
      return acc
    }, [])
  }, [i18n.options.supportedLngs, i18n.language])
}