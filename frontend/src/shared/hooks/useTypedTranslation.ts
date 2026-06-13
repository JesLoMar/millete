import { useTranslation as useTranslationOriginal } from "react-i18next";
import type { TranslationKey } from "@/shared/types/i18n";

export function useTypedTranslation() {
  const { t, ...rest } = useTranslationOriginal();

  const typedT = (key: TranslationKey, options?: any) => {
    return t(key, options);
  };
  
  return {
    t: typedT,
    ...rest,
  };
}