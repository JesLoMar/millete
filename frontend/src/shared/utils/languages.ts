export type SupportedLanguageCode =
  | 'de'
  | 'en'
  | 'es'
  | 'fr'
  | 'it'
  | 'pt'
  | 'ja';

export interface Language {
  code: string;
  nativeName: string;
  englishName: string;
  flag: string;
}

const LANGUAGE_MAP: Record<
  SupportedLanguageCode,
  Omit<Language, 'code'>
> = {
  es: {
    nativeName: 'Español',
    englishName: 'Spanish',
    flag: '🇪🇸',
  },
  en: {
    nativeName: 'English',
    englishName: 'English',
    flag: '🇬🇧',
  },
  fr: {
    nativeName: 'Français',
    englishName: 'French',
    flag: '🇫🇷',
  },
  de: {
    nativeName: 'Deutsch',
    englishName: 'German',
    flag: '🇩🇪',
  },
  it: {
    nativeName: 'Italiano',
    englishName: 'Italian',
    flag: '🇮🇹',
  },
  pt: {
    nativeName: 'Português',
    englishName: 'Portuguese',
    flag: '🇵🇹',
  },
  ja: {
    nativeName: '日本語',
    englishName: 'Japanese',
    flag: '🇯🇵',
  },
};

const DISPLAY_NAMES: Record<
  SupportedLanguageCode,
  Intl.DisplayNames
> = {
  es: new Intl.DisplayNames(['es'], {
    type: 'language',
  }),
  en: new Intl.DisplayNames(['en'], {
    type: 'language',
  }),
  fr: new Intl.DisplayNames(['fr'], {
    type: 'language',
  }),
  de: new Intl.DisplayNames(['de'], {
    type: 'language',
  }),
  it: new Intl.DisplayNames(['it'], {
    type: 'language',
  }),
  pt: new Intl.DisplayNames(['pt'], {
    type: 'language',
  }),
  ja: new Intl.DisplayNames(['ja'], {
    type: 'language',
  }),
};

const getDisplayNames = (
  code: string,
): Intl.DisplayNames | undefined =>
  DISPLAY_NAMES[code as SupportedLanguageCode];

const getFlagFromCode = (code: string): string => {
  if (!/^[A-Za-z]{2}$/.test(code)) {
    return '🌐';
  }

  const firstCodePoint =
    code.toUpperCase().charCodeAt(0) +
    0x1f1e6 -
    65;

  const secondCodePoint =
    code.toUpperCase().charCodeAt(1) +
    0x1f1e6 -
    65;

  return String.fromCodePoint(
    firstCodePoint,
    secondCodePoint,
  );
};

const getNativeNameFromCode = (
  code: string,
): string => {
  const displayNames = getDisplayNames(code);

  if (!displayNames) {
    return code.toUpperCase();
  }

  try {
    return (
      displayNames.of(code) ||
      code.toUpperCase()
    );
  } catch {
    return code.toUpperCase();
  }
};

export function getLanguageFromCode(
  code: string,
): Language {
  if (Object.hasOwn(LANGUAGE_MAP, code)) {
    const mapped =
      LANGUAGE_MAP[
        code as SupportedLanguageCode
      ];

    return {
      code,
      ...mapped,
    };
  }

  return {
    code,
    nativeName: getNativeNameFromCode(code),
    englishName: code.toUpperCase(),
    flag: getFlagFromCode(code),
  };
}