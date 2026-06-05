import "i18next";
import enTranslation from "src/assets/locales/en/translation.json";
import enWiki from "src/assets/locales/en/wiki.json";

declare module "i18next" {
  interface CustomTypeOptions {
    defaultNS: "translation";
    ns: ["translation", "wiki"];
    resources: {
      translation: typeof enTranslation;
      wiki: typeof enWiki;
    };
  }
}