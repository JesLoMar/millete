import "i18next";

import enTranslation from "@/assets/locales/en/translation.json";
import enWiki from "@/assets/locales/en/wiki.json";

export type TranslationResources = typeof enTranslation;
export type WikiResources = typeof enWiki;

export type NestedKeyOf<T> = T extends object
  ? {
      [K in keyof T]: K extends string
        ? T[K] extends string | number | boolean
          ? K
          : T[K] extends object
          ? `${K}.${NestedKeyOf<T[K]>}`
          : never
        : never;
    }[keyof T]
  : never;

export type TranslationKey = NestedKeyOf<TranslationResources>;

declare module "i18next" {
  interface CustomTypeOptions {
    defaultNS: "translation";
    ns: ["translation", "wiki"];
    resources: {
      translation: TranslationResources;
      wiki: WikiResources;
    };
    returnNull: false;
    returnEmptyString: false;
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
}