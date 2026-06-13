import "i18next";
<<<<<<< HEAD
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
import enApi from "@/assets/locales/en/api.json";
import enWiki from "@/assets/locales/en/wiki.json";
import enInfo from "@/assets/locales/en/info.json";
=======
import enTranslation from "src/assets/locales/en/translation.json";
import enWiki from "src/assets/locales/en/wiki.json";
>>>>>>> 0e12808 (Revert "V0.0.4 (#5)")

declare module "i18next" {
  interface CustomTypeOptions {
    defaultNS: "common";
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
      "api",
      "wiki",
      "info"
    ];
    resources: {
<<<<<<< HEAD
      common: typeof enCommon;
      validations: typeof enValidations;
      auth: typeof enAuth;
      nav: typeof enNav;
      dashboard: typeof enDashboard;
      transactions: typeof enTransactions;
      categories: typeof enCategories;
      investments: typeof enInvestments;
      groupGoals: typeof enGroupGoals;
      savingsGoals: typeof enSavingsGoals;
      settings: typeof enSettings;
      api: typeof enApi;
      wiki: typeof enWiki;
      info: typeof enInfo;
=======
      translation: typeof enTranslation;
      wiki: typeof enWiki;
>>>>>>> 0e12808 (Revert "V0.0.4 (#5)")
    };
  }
<<<<<<< HEAD

  interface TFunction {
    <T extends string = string>(key: T, options?: Record<string, unknown>): string;
  }
}

declare module "react-i18next" {
  interface UseTranslationOptions {
    ns?: CustomTypeOptions["ns"][number] | CustomTypeOptions["ns"];
  }

  interface WithTranslation {
    t: <TKeys extends string = string>(
      key: TKeys,
      options?: Record<string, unknown>
    ) => string;
  }
}

// Alemán
declare module "@/assets/locales/de/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/de/wiki.json" {
  const value: WikiResources;
  export default value;
}

// Inglés
declare module "@/assets/locales/en/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/en/wiki.json" {
  const value: WikiResources;
  export default value;
}

// Español
declare module "@/assets/locales/es/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/es/wiki.json" {
  const value: WikiResources;
  export default value;
}

// Francés
declare module "@/assets/locales/fr/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/fr/wiki.json" {
  const value: WikiResources;
  export default value;
}

// Italiano
declare module "@/assets/locales/it/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/it/wiki.json" {
  const value: WikiResources;
  export default value;
}

// Portugués
declare module "@/assets/locales/pt/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/pt/wiki.json" {
  const value: WikiResources;
  export default value;
}

// Japonés
declare module "@/assets/locales/ja/translation.json" {
  const value: TranslationResources;
  export default value;
}
declare module "@/assets/locales/ja/wiki.json" {
  const value: WikiResources;
  export default value;
=======
>>>>>>> 0e12808 (Revert "V0.0.4 (#5)")
}