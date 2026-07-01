import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import resourcesToBackend from "i18next-resources-to-backend";
import type { ResourceLanguage } from "i18next";

const localeLoaders: Record<
  string,
  () => Promise<{ default: ResourceLanguage }>
> = {
  de: () => import("../assets/locales/bundles/de.ts"),
  en: () => import("../assets/locales/bundles/en.ts"),
  es: () => import("../assets/locales/bundles/es.ts"),
  fr: () => import("../assets/locales/bundles/fr.ts"),
  it: () => import("../assets/locales/bundles/it.ts"),
  pt: () => import("../assets/locales/bundles/pt.ts"),
  ja: () => import("../assets/locales/bundles/ja.ts"),
};

const backend = resourcesToBackend(
  (language: string, namespace: string) =>
    localeLoaders[language]().then((m) => m.default[namespace])
);

const namespaces = [
  "common",
  "validations",
  "auth",
  "nav",
  "dashboard",
  "transactions",
  "categories",
  "investments",
  "groupGoals",
  "savingsGoals",
  "settings",
  "userProfile",
  "api",
  "wiki",
  "info",
  "notifications",
];

i18n
  .use(LanguageDetector)
  .use(backend)
  .use(initReactI18next)
  .init({
    fallbackLng: "en",
    load: "currentOnly",
    supportedLngs: ["de", "en", "es", "fr", "it", "pt", "ja"],
    ns: namespaces,
    defaultNS: "common",
    debug: import.meta.env.DEV,
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
