import "i18next";
import enTranslation from "src/assets/locales/en/translation.json";

declare module "i18next" {
  interface CustomTypeOptions {
    defaultNS: "translation";
    resources: {
      translation: typeof enTranslation;
    };
  }
}