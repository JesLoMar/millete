import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import resourcesToBackend from 'i18next-resources-to-backend';

const localeModules = import.meta.glob<{
  default: Record<string, unknown>;
}>('../assets/locales/*/*.json');

const INITIAL_NAMESPACES = [
  'common',
  'api',
  'validations',
] as const;

const SUPPORTED_LANGUAGES = [
  'de',
  'en',
  'es',
  'fr',
  'it',
  'pt',
  'ja',
] as const;

const backend = resourcesToBackend(
  (language: string, namespace: string) => {

    const path = `../assets/locales/${language}/${namespace}.json`;
    const load = localeModules[path];

    if (!load) {
      return Promise.reject(
        new Error(
          `No se encuentra el archivo de traducción: ${path}`,
        ),
      );
    }

    return load().then((module) => module.default);
  },
);

const syncDocumentLang = (lng: string | undefined) => {
  if (lng) {
    document.documentElement.lang = lng;
  }
};

i18n
  .use(LanguageDetector)
  .use(backend)
  .use(initReactI18next)
  .init({
    fallbackLng: 'en',
    load: 'currentOnly',
    supportedLngs: [...SUPPORTED_LANGUAGES],
    ns: [...INITIAL_NAMESPACES],
    defaultNS: 'common',
    debug: import.meta.env.DEV,
    interpolation: {
      escapeValue: false,
    },
  })
  .then(() => {
    syncDocumentLang(i18n.resolvedLanguage ?? i18n.language);
  });

i18n.on('languageChanged', syncDocumentLang);

export default i18n;