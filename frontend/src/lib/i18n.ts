import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import translationDE from '../assets/locales/de/translation.json';
import translationEN from '../assets/locales/en/translation.json';
import translationES from '../assets/locales/es/translation.json'
import translationFR from '../assets/locales/fr/translation.json';
import translationIT from '../assets/locales/it/translation.json';
import translationPT from '../assets/locales/pt/translation.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'en',
    supportedLngs: ['de', 'en', 'es', 'fr', 'it', 'pt'],
    debug: import.meta.env.DEV,
    interpolation: { escapeValue: false },
    resources: {
      de: { translation: translationDE },
      en: { translation: translationEN },
      es: { translation: translationES },
      fr: { translation: translationFR },
      it: { translation: translationIT },
      pt: { translation: translationPT },
    },
  });

export default i18n;