# Librerías transversales

## Internacionalización

[`i18n.ts`](../../frontend/src/lib/i18n.ts) configura i18next, detector de idioma, React integration y carga dinámica de JSON por idioma y namespace mediante `import.meta.glob` y `i18next-resources-to-backend`. Los namespaces iniciales son `common`, `api` y `validations`; el namespace predeterminado es `common` y el idioma de respaldo `en`. Los idiomas declarados son `de`, `en`, `es`, `fr`, `it`, `pt` y `ja`. No se importan estáticamente siete recursos `translation.json`.

Los archivos de traducción residen en [`frontend/src/assets/locales`](../../frontend/src/assets/locales).

## Clases CSS

[`utils.ts`](../../frontend/src/lib/utils.ts) exporta `cn`, que combina entradas de clase mediante `clsx` y `tailwind-merge`.

## Dependencias y comandos

El manifiesto [`package.json`](../../frontend/package.json) declara React, React Router, TanStack Query, Axios, i18next, Zod, Tailwind y Vite. El gestor declarado es pnpm. Para verificar tipos/build los comandos disponibles son `pnpm type-check` y `pnpm build`; `build:check` usa expresamente `npm run` en su definición.

El detector se registra junto al backend de recursos y la integración React. Se establecen `load: currentOnly` y `document.documentElement.lang` al resolver/cambiar idioma. El loader construye la ruta `../assets/locales/${language}/${namespace}.json`; namespace inexistente rechaza con error explícito y el fallback global es `en`. Los namespaces específicos de cada feature se cargan bajo demanda además de `common`, `api` y `validations` iniciales.
