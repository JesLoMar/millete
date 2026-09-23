import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

import {
  getLanguageFromCode,
  type Language,
} from '@/shared/utils/languages';

export function useAvailableLanguages(): Language[] {
  const { i18n } = useTranslation();

  return useMemo(() => {
    const supportedLanguages = i18n.options.supportedLngs;

    if (
      !Array.isArray(supportedLanguages) ||
      supportedLanguages.length === 0
    ) {
      return [getLanguageFromCode(i18n.language)];
    }

    const languages = supportedLanguages
      .filter((code) => code !== 'cimode')
      .map((code) => getLanguageFromCode(code));

    return languages.length > 0
      ? languages
      : [getLanguageFromCode(i18n.language)];
  }, [i18n.language, i18n.options.supportedLngs]);
}