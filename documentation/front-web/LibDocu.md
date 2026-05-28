# Lib — Documentación técnica (Frontend)

## Estructura de archivos

- **i18n.ts** — Configuración de internacionalización con i18next
- **utils.ts** — Utilidad de combinación de clases Tailwind

---

## i18n.ts

Configuración centralizada de internacionalización (i18n) para toda la aplicación.

### Plugins utilizados

- **LanguageDetector:** detecta automáticamente el idioma del usuario desde el navegador, localStorage o query string.
- **initReactI18next:** integración con React mediante el provider `I18nextProvider`.

### Configuración

- **fallbackLng:** `'en'` — idioma de respaldo si la traducción no existe en el idioma detectado.
- **supportedLngs:** 6 idiomas: alemán (de), inglés (en), español (es), francés (fr), italiano (it), portugués (pt).
- **debug:** activado solo en desarrollo (`import.meta.env.DEV`).
- **interpolation.escapeValue:** `false` — React ya escapa valores por defecto.

### Recursos

6 archivos JSON de traducción importados estáticamente:

| Código | Archivo | Idioma |
|--------|---------|--------|
| de | `src/assets/locales/de/translation.json` | Alemán |
| en | `src/assets/locales/en/translation.json` | Inglés |
| es | `src/assets/locales/es/translation.json` | Español |
| fr | `src/assets/locales/fr/translation.json` | Francés |
| it | `src/assets/locales/it/translation.json` | Italiano |
| pt | `src/assets/locales/pt/translation.json` | Portugués |

Cada archivo se asigna al namespace `translation` (por defecto).

### Exportación

Exporta la instancia configurada de i18n como default export. Es consumida por:
- `@/shared/hooks/useLanguages.ts` para obtener idiomas disponibles.
- `@/shared/utils/i18nFormat.ts` para detectar el locale activo.
- `@/shared/utils/notifications/notify.tsx` para traducciones en notificaciones.
- Todos los componentes que usan `useTranslation()` de react-i18next.

### Detección de idioma

El plugin LanguageDetector busca el idioma en este orden:
1. Query string (`?lng=es`)
2. localStorage (`i18nextLng`)
3. Cookie (`i18next`)
4. Navegador (`navigator.language`)

---

## utils.ts

Utilidad de combinación de clases CSS para Tailwind CSS.

### cn()

Función que combina múltiples clases CSS usando `clsx` y `tailwind-merge`.

#### Parámetros

- `...inputs: ClassValue[]` — clases CSS en cualquier formato aceptado por clsx (strings, objetos, arrays, undefined, null, false).

#### Funcionamiento

1. `clsx(inputs)`: combina todas las entradas en una sola string de clases, resolviendo condicionales (objetos), filtrando valores falsy y aplanando arrays.
2. `twMerge(resultado)`: resuelve conflictos de clases Tailwind. Si hay clases duplicadas o conflictivas, mantiene solo la última según las reglas de precedencia de Tailwind.

#### Uso en el proyecto

Esta función se usa en prácticamente todos los componentes de la aplicación para combinar clases condicionales sin conflictos. Es el método estándar para gestionar clases dinámicas con Tailwind.

---

## Integración con el resto de la aplicación

- **i18n.ts** es el punto de entrada de internacionalización. Se importa una sola vez en la raíz de la aplicación y provee traducciones a todos los componentes mediante `useTranslation()`.
- **utils.ts** es una utilidad transversal usada por todos los componentes que necesitan clases condicionales. Centraliza la lógica de combinación de clases evitando conflictos visuales.