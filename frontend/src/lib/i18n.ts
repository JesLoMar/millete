import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import translationDE from '@/assets/locales/de/translation.json';
import translationEN from '@/assets/locales/en/translation.json';
import translationES from '@/assets/locales/es/translation.json';
import translationFR from '@/assets/locales/fr/translation.json';
import translationIT from '@/assets/locales/it/translation.json';
import translationPT from '@/assets/locales/pt/translation.json';
import translationJA from '@/assets/locales/ja/translation.json';

import wikiDE from '@/assets/locales/de/wiki.json';
import wikiEN from '@/assets/locales/en/wiki.json';
import wikiES from '@/assets/locales/es/wiki.json';
import wikiFR from '@/assets/locales/fr/wiki.json';
import wikiIT from '@/assets/locales/it/wiki.json';
import wikiPT from '@/assets/locales/pt/wiki.json';
import wikiJA from '@/assets/locales/ja/wiki.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'en',
    supportedLngs: ['de', 'en', 'es', 'fr', 'it', 'pt', 'ja'],
    ns: ['translation', 'wiki'],
    defaultNS: 'translation',
    debug: import.meta.env.DEV,
    interpolation: { escapeValue: false },
    resources: {
      de: { translation: translationDE, wiki: wikiDE },
      en: { translation: translationEN, wiki: wikiEN },
      es: { translation: translationES, wiki: wikiES },
      fr: { translation: translationFR, wiki: wikiFR },
      it: { translation: translationIT, wiki: wikiIT },
      pt: { translation: translationPT, wiki: wikiPT },
      ja: { translation: translationJA, wiki: wikiJA },
    },
  });

export default i18n;