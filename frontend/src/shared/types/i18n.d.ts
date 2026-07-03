import "i18next";
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
import enNotifications from "@/assets/locales/en/notifications.json";
import enUserProfile from "@/assets/locales/en/userProfile.json";
import enInfo from "@/assets/locales/en/info.json";

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
      "userProfile",
      "notifications",
      "info"
    ];
    resources: {
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
      userProfile: typeof enUserProfile;
      notifications: typeof enNotifications;
      wiki: typeof enWiki;
      info: typeof enInfo;
    };
    returnNull: false;
    returnEmptyString: false;
  }

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
