# Configuración raíz — Documentación técnica (Frontend)

## Estructura de archivos

- **vite.config.ts** — Configuración de Vite (plugins, alias, build, server)
- **vite-env.d.ts** — Tipos de variables de entorno de Vite
- **tsconfig.json** — Configuración raíz de TypeScript (referencias)
- **tsconfig.app.json** — Configuración de TypeScript para la aplicación
- **tsconfig.node.json** — Configuración de TypeScript para Node (Vite)
- **package.json** — Dependencias, scripts y metadatos del proyecto
- **index.html** — Plantilla HTML principal
- **eslint.config.js** — Configuración de ESLint
- **components.json** — Configuración de shadcn/ui

---

## vite.config.ts

Configuración principal de Vite, el bundler de la aplicación.

### Plugins

- **@vitejs/plugin-react:** soporte para React con Fast Refresh y JSX transform.
- **@tailwindcss/vite:** integración de Tailwind CSS v4 con Vite.

### Resolución de alias

`@` mapea a `./src`, permitiendo imports como `@/features/auth/...` en lugar de rutas relativas.

### Servidor de desarrollo

- **Puerto:** 5173.
- **open:** true — abre el navegador automáticamente al iniciar.

### Build de producción

- **Minificador:** Terser (con configuración explícita).
- **Compresión:**
  - `drop_console: true` en producción — elimina todos los console.*.
  - `drop_debugger: true` en producción — elimina debuggers.
  - `pure_funcs` en producción — elimina llamadas a console.log, console.info, console.debug y console.table.
- **Formato:** elimina comentarios del bundle.
- **Target:** `es2020` para compatibilidad con navegadores modernos.
- **Sourcemaps:** desactivados en producción.
- **Advertencia de chunk:** a partir de 1000 KB.

### Variables globales

`__APP_VERSION__`: versión del proyecto desde `package.json` disponible en el código.

### Carga de variables de entorno

`loadEnv(mode, process.cwd(), '')` carga variables de `.env` y `.env.production` según el modo.

---

## vite-env.d.ts

Declaración de tipos para variables de entorno de Vite.

### ImportMetaEnv

- `VITE_API_URL`: string — URL base del backend.
- `VITE_APP_NAME`: string — nombre de la aplicación.
- `VITE_APP_VERSION`: string — versión de la aplicación.

Extiende `ImportMeta` para que `import.meta.env` tenga tipado completo.

---

## tsconfig.json

Configuración raíz de TypeScript. Usa el sistema de referencias para dividir la configuración en dos proyectos:

- `tsconfig.app.json`: configuración para el código fuente de la aplicación (`src/`).
- `tsconfig.node.json`: configuración para archivos de Node (vite.config.ts).

El campo `files` vacío indica que este archivo solo sirve como punto de entrada para las referencias.

---

## tsconfig.app.json

Configuración de TypeScript para el código de la aplicación.

### Compilación

- **target:** `es2023` — JavaScript moderno.
- **lib:** `ES2023` y `DOM` — APIs del navegador incluidas.
- **module:** `esnext` — módulos ES nativos.
- **moduleResolution:** `bundler` — resolución de módulos para bundlers.
- **jsx:** `react-jsx` — transform automático de JSX (no requiere `import React`).

### Paths

`@/*` mapea a `./src/*` para imports absolutos.

### Opciones de bundler

- `allowImportingTsExtensions`: permite imports con extensión `.ts`/`.tsx`.
- `verbatimModuleSyntax`: obliga a usar `import type` para tipos.
- `moduleDetection`: `force` — trata todos los archivos como módulos.
- `noEmit`: true — no genera archivos de salida (solo type-checking).

### Linting

- `noUnusedLocals`: true — error en variables locales no usadas.
- `noUnusedParameters`: true — error en parámetros no usados.
- `erasableSyntaxOnly`: true — solo permite sintaxis que se puede borrar (compatible con bundlers).
- `noFallthroughCasesInSwitch`: true — evita fallthrough en switch.

### Include

`src/` — solo compila archivos dentro de la carpeta fuente.

---

## tsconfig.node.json

Configuración de TypeScript para archivos de Node (vite.config.ts).

### Compilación

- **target:** `es2023`.
- **lib:** `ES2023` (sin DOM).
- **types:** `["node"]` — incluye tipos de Node.js.

### Mismas opciones de bundler y linting que tsconfig.app.json.

### Include

Solo `vite.config.ts`.

---

## package.json

Metadatos y configuración de dependencias del proyecto.

### Información general

- **name:** `millete-front`.
- **private:** true — no se publica en npm.
- **version:** `0.0.1`.
- **type:** `module` — usa módulos ES.
- **packageManager:** `pnpm@11.3.0` — fuerza el uso de pnpm.

### Engines

- Node >= 20, pnpm >= 11.
- Bloquea npm y yarn con mensaje "please-use-pnpm".

### Scripts

| Script | Comando | Descripción |
|--------|---------|-------------|
| `preinstall` | `npx only-allow pnpm` | Bloquea instalación con otro gestor |
| `dev` | `vite` | Servidor de desarrollo |
| `build` | `vite build` | Build de producción |
| `type-check` | `tsc --noEmit` | Verificación de tipos |
| `build:check` | `type-check && build` | Verificación + build |
| `lint` | `eslint . --max-warnings 0` | Linting estricto |
| `lint:fix` | `eslint . --fix` | Linting con auto-corrección |
| `preview` | `vite preview` | Previsualizar build |

### Dependencias principales

| Categoría | Paquete | Uso |
|-----------|---------|-----|
| UI | `react`, `react-dom` v19 | Framework |
| UI | `lucide-react` | Iconos |
| UI | `recharts` | Gráficos |
| UI | `sonner` | Notificaciones toast |
| UI | `@radix-ui/*` | Componentes accesibles |
| UI | `embla-carousel-react` | Carrusel |
| Routing | `react-router-dom` v7 | Enrutador |
| Estado | `@tanstack/react-query` v5 | Queries y caché |
| HTTP | `axios` | Cliente HTTP |
| i18n | `i18next`, `react-i18next`, `i18next-browser-languagedetector` | Internacionalización |
| Form | `react-hook-form`, `@hookform/resolvers`, `zod` v4 | Formularios y validación |
| Estilos | `tailwindcss` v4, `@tailwindcss/vite` | CSS |
| Estilos | `clsx`, `class-variance-authority`, `tailwind-merge` | Utilidades de clases |
| Tema | `next-themes` | Gestión de temas |

### Dependencias de desarrollo

TypeScript v6, Vite v8, ESLint v10, Terser, PostCSS, Autoprefixer, tipos de React y Node.

---

## index.html

Plantilla HTML principal. Punto de entrada del navegador.

### Meta tags

- **charset:** UTF-8.
- **viewport:** responsive con escala inicial 1.0.
- **description:** "Aplicación de gestión financiera familiar".
- **theme-color:** `#3b82f6` (azul primary).

### Fuentes

Preconexión a Google Fonts para optimizar carga. Carga la fuente "Playfair Display" en pesos 400, 600 y 700 para uso en serif.

### Estructura

- `<div id="root">`: punto de montaje de React.
- `<script type="module" src="/src/main.tsx">`: entrada del bundle.

### Título

"Millete" — nombre de la aplicación.

---

## eslint.config.js

Configuración de ESLint con el nuevo formato plano (flat config).

### Ignorados globales

- `dist/` y `node_modules/`.
- 4 componentes de UI (badge, button, carousel, form) — generados por shadcn/ui.

### Configuración para archivos TypeScript

#### Extensiones

- `js.configs.recommended` — reglas base de JavaScript.
- `tseslint.configs.recommended` — reglas de TypeScript.
- `reactHooks.configs.flat.recommended` — reglas de hooks de React.
- `reactRefresh.configs.vite` — integración con HMR de Vite.

#### Globals

`globals.browser` — APIs del navegador disponibles globalmente.

#### Reglas personalizadas

| Regla | Nivel | Descripción |
|-------|-------|-------------|
| `no-console` | warn | Permite solo `console.warn` y `console.error` |
| `no-debugger` | warn | Advierte sobre debuggers |
| `@typescript-eslint/no-unused-vars` | warn | Ignora variables que empiezan con `_` |
| `@typescript-eslint/no-explicit-any` | warn | Advierte sobre uso de `any` |
| `react-refresh/only-export-components` | warn | Solo exportar componentes para HMR |
| `react-hooks/set-state-in-effect` | warn | Advierte sobre setState en efectos |

---

## components.json

Configuración de shadcn/ui para generación de componentes.

### Opciones

- **style:** `new-york` — estilo visual de los componentes.
- **rsc:** false — no usa React Server Components.
- **tsx:** true — genera archivos TypeScript.
- **iconLibrary:** `lucide` — usa Lucide para iconos.

### Tailwind

- **config:** vacío (usa config automática de Tailwind v4).
- **css:** `src/index.css` — archivo de estilos globales.
- **baseColor:** `neutral` — color base para el tema.
- **cssVariables:** true — usa variables CSS para el tema.
- **prefix:** vacío — sin prefijo en clases.

### Aliases

| Alias | Ruta | Descripción |
|-------|------|-------------|
| `components` | `@/shared/components` | Componentes compartidos |
| `utils` | `@/lib/utils` | Función `cn` |
| `ui` | `@/shared/components/ui` | Componentes de shadcn/ui |
| `lib` | `@/lib` | Utilidades generales |
| `hooks` | `@/shared/hooks` | Hooks compartidos |