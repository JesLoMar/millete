import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

// ============================================
// IMPORTAR ESPAÑOL (completo)
// ============================================
import commonES from "@/assets/locales/es/common.json";
import validationsES from "@/assets/locales/es/validations.json";
import authES from "@/assets/locales/es/auth.json";
import navES from "@/assets/locales/es/nav.json";
import dashboardES from "@/assets/locales/es/dashboard.json";
import transactionsES from "@/assets/locales/es/transactions.json";
import categoriesES from "@/assets/locales/es/categories.json";
import investmentsES from "@/assets/locales/es/investments.json";
import groupGoalsES from "@/assets/locales/es/groupGoals.json";
import savingsGoalsES from "@/assets/locales/es/savingsGoals.json";
import settingsES from "@/assets/locales/es/settings.json";
import userProfileES from "@/assets/locales/es/userProfile.json";
import apiES from "@/assets/locales/es/api.json";
import wikiES from "@/assets/locales/es/wiki.json";
import infoES from "@/assets/locales/es/info.json";
import notificationsES from "@/assets/locales/es/notifications.json";

// ============================================
// IMPORTAR INGLÉS (completo)
// ============================================
import enCommon from "@/assets/locales/en/common.json";
import enValidations from "@/assets/locales/en/validations.json";
import enAuth from "@/assets/locales/en/auth.json";
import enNav from "@/assets/locales/en/nav.json";
import enDashboard from "@/assets/locales/en/dashboard.json";
import enTransactions from "@/assets/locales/en/transactions.json";
import enCategories from "@/assets/locales/en/categories.json";
import enInvestments from "@/assets/locales/en/investments.json";
import enGroupGoals from "@/assets/locales/en/groupGoals.json";
import enSavingsGoals from "@/assets/locales/en/savingsGoals.json";
import enSettings from "@/assets/locales/en/settings.json";
import userProfileEN from "@/assets/locales/en/userProfile.json";
import enApi from "@/assets/locales/en/api.json";
import enWiki from "@/assets/locales/en/wiki.json";
import enInfo from "@/assets/locales/en/info.json";
import notificationsEN from "@/assets/locales/en/notifications.json";

// ============================================
// IMPORTAR ALEMÁN
// ============================================
import deCommon from "@/assets/locales/de/common.json";
import deValidations from "@/assets/locales/de/validations.json";
import deAuth from "@/assets/locales/de/auth.json";
import deNav from "@/assets/locales/de/nav.json";
import deDashboard from "@/assets/locales/de/dashboard.json";
import deTransactions from "@/assets/locales/de/transactions.json";
import deCategories from "@/assets/locales/de/categories.json";
import deInvestments from "@/assets/locales/de/investments.json";
import deGroupGoals from "@/assets/locales/de/groupGoals.json";
import deSavingsGoals from "@/assets/locales/de/savingsGoals.json";
import deSettings from "@/assets/locales/de/settings.json";
import deApi from "@/assets/locales/de/api.json";
import deWiki from "@/assets/locales/de/wiki.json";
import deInfo from "@/assets/locales/de/info.json";

// ============================================
// IMPORTAR FRANCÉS
// ============================================
import frCommon from "@/assets/locales/fr/common.json";
import frValidations from "@/assets/locales/fr/validations.json";
import frAuth from "@/assets/locales/fr/auth.json";
import frNav from "@/assets/locales/fr/nav.json";
import frDashboard from "@/assets/locales/fr/dashboard.json";
import frTransactions from "@/assets/locales/fr/transactions.json";
import frCategories from "@/assets/locales/fr/categories.json";
import frInvestments from "@/assets/locales/fr/investments.json";
import frGroupGoals from "@/assets/locales/fr/groupGoals.json";
import frSavingsGoals from "@/assets/locales/fr/savingsGoals.json";
import frSettings from "@/assets/locales/fr/settings.json";
import frApi from "@/assets/locales/fr/api.json";
import frWiki from "@/assets/locales/fr/wiki.json";
import frInfo from "@/assets/locales/fr/info.json";

// ============================================
// IMPORTAR ITALIANO
// ============================================
import itCommon from "@/assets/locales/it/common.json";
import itValidations from "@/assets/locales/it/validations.json";
import itAuth from "@/assets/locales/it/auth.json";
import itNav from "@/assets/locales/it/nav.json";
import itDashboard from "@/assets/locales/it/dashboard.json";
import itTransactions from "@/assets/locales/it/transactions.json";
import itCategories from "@/assets/locales/it/categories.json";
import itInvestments from "@/assets/locales/it/investments.json";
import itGroupGoals from "@/assets/locales/it/groupGoals.json";
import itSavingsGoals from "@/assets/locales/it/savingsGoals.json";
import itSettings from "@/assets/locales/it/settings.json";
import itApi from "@/assets/locales/it/api.json";
import itWiki from "@/assets/locales/it/wiki.json";
import itInfo from "@/assets/locales/it/info.json";

// ============================================
// IMPORTAR PORTUGUÉS
// ============================================
import ptCommon from "@/assets/locales/pt/common.json";
import ptValidations from "@/assets/locales/pt/validations.json";
import ptAuth from "@/assets/locales/pt/auth.json";
import ptNav from "@/assets/locales/pt/nav.json";
import ptDashboard from "@/assets/locales/pt/dashboard.json";
import ptTransactions from "@/assets/locales/pt/transactions.json";
import ptCategories from "@/assets/locales/pt/categories.json";
import ptInvestments from "@/assets/locales/pt/investments.json";
import ptGroupGoals from "@/assets/locales/pt/groupGoals.json";
import ptSavingsGoals from "@/assets/locales/pt/savingsGoals.json";
import ptSettings from "@/assets/locales/pt/settings.json";
import ptApi from "@/assets/locales/pt/api.json";
import ptWiki from "@/assets/locales/pt/wiki.json";
import ptInfo from "@/assets/locales/pt/info.json";

// ============================================
// IMPORTAR JAPONÉS
// ============================================
import jaCommon from "@/assets/locales/ja/common.json";
import jaValidations from "@/assets/locales/ja/validations.json";
import jaAuth from "@/assets/locales/ja/auth.json";
import jaNav from "@/assets/locales/ja/nav.json";
import jaDashboard from "@/assets/locales/ja/dashboard.json";
import jaTransactions from "@/assets/locales/ja/transactions.json";
import jaCategories from "@/assets/locales/ja/categories.json";
import jaInvestments from "@/assets/locales/ja/investments.json";
import jaGroupGoals from "@/assets/locales/ja/groupGoals.json";
import jaSavingsGoals from "@/assets/locales/ja/savingsGoals.json";
import jaSettings from "@/assets/locales/ja/settings.json";
import jaApi from "@/assets/locales/ja/api.json";
import jaWiki from "@/assets/locales/ja/wiki.json";
import jaInfo from "@/assets/locales/ja/info.json";

// ============================================
// CONFIGURACIÓN DE I18N
// ============================================
i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: "en",
    supportedLngs: ["de", "en", "es", "fr", "it", "pt", "ja"],
    ns: [
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
    ],
    defaultNS: "common",
    debug: import.meta.env.DEV,
    interpolation: {
      escapeValue: false,
    },
    resources: {
      // ==========================================
      // ESPAÑOL - COMPLETO
      // ==========================================
      es: {
        common: commonES,
        validations: validationsES,
        auth: authES,
        nav: navES,
        dashboard: dashboardES,
        transactions: transactionsES,
        categories: categoriesES,
        investments: investmentsES,
        groupGoals: groupGoalsES,
        savingsGoals: savingsGoalsES,
        settings: settingsES,
        userProfile: userProfileES,
        api: apiES,
        wiki: wikiES,
        info: infoES,
        notifications: notificationsES,
      },

      // ==========================================
      // INGLÉS - COMPLETO
      // ==========================================
      en: {
        common: enCommon,
        validations: enValidations,
        auth: enAuth,
        nav: enNav,
        dashboard: enDashboard,
        transactions: enTransactions,
        categories: enCategories,
        investments: enInvestments,
        groupGoals: enGroupGoals,
        savingsGoals: enSavingsGoals,
        settings: enSettings,
        userProfile: userProfileEN,
        api: enApi,
        wiki: enWiki,
        info: enInfo,
        notifications: notificationsEN,
      },

      // ==========================================
      // ALEMÁN - PENDIENTE DE TRADUCCIÓN
      // ==========================================
      de: {
        common: deCommon,
        validations: deValidations,
        auth: deAuth,
        nav: deNav,
        dashboard: deDashboard,
        transactions: deTransactions,
        categories: deCategories,
        investments: deInvestments,
        groupGoals: deGroupGoals,
        savingsGoals: deSavingsGoals,
        settings: deSettings,
        api: deApi,
        wiki: deWiki,
        info: deInfo,
      },

      // ==========================================
      // FRANCÉS - PENDIENTE DE TRADUCCIÓN
      // ==========================================
      fr: {
        common: frCommon,
        validations: frValidations,
        auth: frAuth,
        nav: frNav,
        dashboard: frDashboard,
        transactions: frTransactions,
        categories: frCategories,
        investments: frInvestments,
        groupGoals: frGroupGoals,
        savingsGoals: frSavingsGoals,
        settings: frSettings,
        api: frApi,
        wiki: frWiki,
        info: frInfo,
      },

      // ==========================================
      // ITALIANO - PENDIENTE DE TRADUCCIÓN
      // ==========================================
      it: {
        common: itCommon,
        validations: itValidations,
        auth: itAuth,
        nav: itNav,
        dashboard: itDashboard,
        transactions: itTransactions,
        categories: itCategories,
        investments: itInvestments,
        groupGoals: itGroupGoals,
        savingsGoals: itSavingsGoals,
        settings: itSettings,
        api: itApi,
        wiki: itWiki,
        info: itInfo,
      },

      // ==========================================
      // PORTUGUÉS - PENDIENTE DE TRADUCCIÓN
      // ==========================================
      pt: {
        common: ptCommon,
        validations: ptValidations,
        auth: ptAuth,
        nav: ptNav,
        dashboard: ptDashboard,
        transactions: ptTransactions,
        categories: ptCategories,
        investments: ptInvestments,
        groupGoals: ptGroupGoals,
        savingsGoals: ptSavingsGoals,
        settings: ptSettings,
        api: ptApi,
        wiki: ptWiki,
        info: ptInfo,
      },

      // ==========================================
      // JAPONÉS - PENDIENTE DE TRADUCCIÓN
      // ==========================================
      ja: {
        common: jaCommon,
        validations: jaValidations,
        auth: jaAuth,
        nav: jaNav,
        dashboard: jaDashboard,
        transactions: jaTransactions,
        categories: jaCategories,
        investments: jaInvestments,
        groupGoals: jaGroupGoals,
        savingsGoals: jaSavingsGoals,
        settings: jaSettings,
        api: jaApi,
        wiki: jaWiki,
        info: jaInfo,
      },
    },
  });

export default i18n;
