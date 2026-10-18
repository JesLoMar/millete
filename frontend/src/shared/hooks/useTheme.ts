
import {
  useCallback,
  useState,
} from 'react';

import {
  THEMES,
  type Theme,
} from '@/shared/themes/palettes';

import {
  applyTheme,
  getInitialTheme,
  persistTheme,
} from '@/shared/themes/applyTheme';

export function useTheme() {
  const [theme, setThemeState] =
    useState<Theme>(getInitialTheme);

  const setTheme = useCallback((selectedTheme: Theme) => {
    applyTheme(
      document.documentElement,
      selectedTheme.colors,
    );

    persistTheme(selectedTheme);
    setThemeState(selectedTheme);
  }, []);

  const setThemeByName = useCallback(
    (name: string) => {
      const selectedTheme = THEMES.find(
        (availableTheme) => availableTheme.name === name,
      );

      if (!selectedTheme) {
        return;
      }

      setTheme(selectedTheme);
    },
    [setTheme],
  );

  return {
    theme,
    setTheme,
    setThemeByName,
    availableThemes: THEMES,
  };
}
