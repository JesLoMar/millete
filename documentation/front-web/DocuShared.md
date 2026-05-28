# Shared — Documentación técnica (Frontend)

## Estructura de archivos

### api/
- **axiosClient.ts** — Cliente HTTP con interceptores de autenticación y errores

### components/
- **FormattedMetricCard.tsx** — Tarjeta de métrica con formato automático (moneda/número) y tendencia
- **Header.tsx** — Cabecera con saludo dinámico, fecha y selector de período opcional
- **LanguageSelector.tsx** — Selector de idioma con dropdown
- **MetricCard.tsx** — Tarjeta de métrica base con skeleton de carga
- **PeriodSelector.tsx** — Selector de período semana/mes/año
- **Sidebar.tsx** — Barra lateral de navegación con items activos/deshabilitados
- **ThemeSelector.tsx** — Selector de tema visual con paletas
- **TopNav.tsx** — Barra superior con logo, idioma, tema y menú de usuario

### components/ui/
- **badge.tsx** — Componente Badge
- **button.tsx** — Componente Button
- **card.tsx** — Componentes Card, CardHeader, CardContent, CardTitle, CardDescription, CardFooter
- **carousel.tsx** — Componente Carousel
- **chart.tsx** — Componentes ChartContainer, ChartTooltip, ChartTooltipContent, ChartConfig
- **dialog.tsx** — Componentes Dialog, DialogTrigger, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter
- **dropdown-menu.tsx** — Componentes DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator
- **form.tsx** — Componentes de formulario con react-hook-form
- **input.tsx** — Componente Input
- **label.tsx** — Componente Label
- **progress.tsx** — Componente Progress
- **select.tsx** — Componentes Select, SelectTrigger, SelectContent, SelectItem, SelectValue
- **sheet.tsx** — Componente Sheet
- **skeleton.tsx** — Componente Skeleton
- **sonner.tsx** — Componente Toaster de Sonner
- **tooltip.tsx** — Componente Tooltip

### config/
- **navigation.ts** — Registro centralizado de navegación con items habilitados/deshabilitados

### hooks/
- **useCategories.ts** — Query de categorías con filtro de activas
- **useLanguages.ts** — Hook de idiomas disponibles desde i18next
- **usePlannedTransactions.ts** — Query de transacciones recurrentes
- **useTheme.ts** — Gestión de temas con variables CSS y persistencia

### themes/
- **palettes.ts** — Definición de 4 temas (default, ocean, forest, sunset) con 28 variables CSS cada uno

### types/
- **api.ts** — Interfaz ApiError
- **i18n.d.ts** — Aumento de tipos de i18next con recursos de traducción
- **index.ts** — Interfaz PageResponse<T> para respuestas paginadas

### utils/
- **i18nFormat.ts** — Formateo de moneda y números con Intl según idioma activo
- **languages.ts** — Utilidades de idiomas: detección de banderas, nombres nativos y caché
- **secureStorage.ts** — Almacenamiento seguro con ofuscación por fingerprint
- **notifications/notify.tsx** — Sistema de notificaciones toast con 4 variantes

---

## api/axiosClient.ts

Cliente HTTP centralizado para toda la aplicación. Instancia de axios con configuración global.

### Configuración base

- **baseURL:** `import.meta.env.VITE_API_URL` con fallback a `http://localhost:8080/api/v1`.
- **timeout:** 30 segundos.
- **Content-Type:** `application/json`.

### Interceptor de request

Añade el header `Authorization: Bearer <token>` automáticamente si hay un token en `secureStorage`.

### Interceptor de response

- **Éxito:** devuelve la respuesta sin modificar.
- **Error 401:** limpia `secureStorage` y emite evento `auth:logout` en window (escuchado por AuthContext para ejecutar logout).
- **Error con `skipGlobalErrorNotify: true`:** no muestra notificación toast (usado en importación de datos para evitar duplicados).
- **Errores sin skipGlobalErrorNotify:** muestra notificación toast con `notify.error`, incluyendo descripción contextual según código de estado (401, 403, 404, 5xx) o timeout.
- **Mensaje de error:** prioriza `response.data.message`, luego `response.data.error`, luego `error.message`, finalmente clave i18n genérica.

### Tipos extendidos

Extiende `AxiosRequestConfig` para incluir `skipGlobalErrorNotify?: boolean`.

---

## components/TopNav.tsx

Barra de navegación superior. Común a todas las páginas.

### Estructura

- **Logo:** icono Wallet2 con fondo primary/10 y nombre de la app. Clic redirige a `/dashboard`.
- **Controles:** LanguageSelector + ThemeSelector.
- **Menú de usuario:** DropdownMenu con nombre/email del usuario.
  - `getUserDisplay`: función que determina qué mostrar:
    - Si hay name y email: name como primary, email como secondary.
    - Si solo hay name: name como primary, sin secondary.
    - Si solo hay email: parte local como primary, email completo como secondary.
    - Si no hay nada: "Invitado" como primary.
  - Opciones: Perfil (navega a `/profile`) y Cerrar sesión (ejecuta logout con notificación).

### Hooks utilizados

- `useAuth()`: obtiene `user` y `logout`.
- `useNavigate()`: navegación programática.
- `useTranslation()`: i18n.

### Estilo

- Altura fija 64px (`h-16`), sticky en top con z-40.
- Fondo `bg-card/50` con `backdrop-blur-md`.

---

## components/Sidebar.tsx

Barra lateral de navegación. Común a todas las páginas.

### Props

- **className?:** clases adicionales.
- **showDisabled?:** mostrar items deshabilitados (default true).

### Estructura

- **Navegación principal:** items de `getEnabledNavItems("main")` ordenados. Cada item muestra icono y texto traducido. El item activo tiene indicador visual: borde izquierdo primary, fondo accent/50, icono con scale-105 y color primary.
- **Items deshabilitados:** sección "Próximamente" con icono Construction, opacidad reducida y cursor pointer que muestra notificación info al hacer clic.
- **Navegación inferior:** items de `getEnabledNavItems("bottom")` anclados abajo con `mt-auto` y separador `border-t`.

### Detección de item activo

Función `isActive`: para `/dashboard` compara exacta, para el resto usa `startsWith`. Esto evita que `/dashboard` coincida con rutas que empiecen igual.

### Hooks utilizados

- `useNavigate()` y `useLocation()`: navegación y detección de ruta actual.
- `useTranslation()`: i18n.

### Dimensiones

- Ancho fijo 256px (`w-64`), altura `calc(100vh - 4rem)`, sticky en top-16.

---

## components/Header.tsx

Cabecera de página con saludo dinámico y fecha actual.

### Props

- **className?:** clases adicionales.
- **onPeriodChange?:** callback al cambiar período.
- **defaultPeriod?:** período inicial (default "month").
- **hidePeriodSelector?:** oculta el selector de período (default false).

### Saludo dinámico

`getGreetingKey()` devuelve clave i18n según la hora:
- Antes de 12: `dashboard.header.greeting.morning`
- Antes de 20: `dashboard.header.greeting.afternoon`
- Después: `dashboard.header.greeting.evening`

### Fecha formateada

`formatDate()` devuelve día de la semana, mes, año y número de semana (calculado desde el 1 de enero). Se renderiza con icono Calendar y textos traducidos.

### Nombre de usuario

Prioriza `user.name`, luego parte local de `user.email`, finalmente "Invitado".

### Selector de período

Si `onPeriodChange` existe y `hidePeriodSelector` es false, muestra PeriodSelector con el período actual.

---

## components/PeriodSelector.tsx

Selector de período con tres opciones: semana, mes, año.

### Props

- **period:** "week" | "month" | "year".
- **onPeriodChange:** callback al seleccionar.
- **className?:** clases adicionales.

### Estructura

Tres botones en grupo con `role="group"` y `aria-label`. El botón activo tiene fondo primary, texto blanco y sombra. Texto en uppercase con tracking-wider.

### Tipo exportado

`PeriodFilter = "week" | "month" | "year"` (re-exportado por Header.tsx).

---

## components/MetricCard.tsx

Tarjeta de métrica base con soporte para skeleton de carga.

### Props

- **title:** título de la métrica.
- **value:** valor (string o number).
- **icon:** componente LucideIcon.
- **color:** clases Tailwind para el contenedor del icono.
- **loading?:** muestra skeleton (default false).
- **children?:** contenido adicional bajo el valor.
- **className?:** clases adicionales.

### Estados

- **Carga:** placeholder con 4 barras animadas (título, icono, valor, tendencia).
- **Normal:** título uppercase con tracking-wider, icono en contenedor coloreado, valor en texto grande (3xl) con `tabular-nums`, y children opcional.

---

## components/FormattedMetricCard.tsx

Extiende MetricCard con formato automático de moneda/número y visualización de tendencia.

### Props

- **title:** título.
- **value:** number.
- **format?:** "currency" (default) o "number".
- **trend:** porcentaje de tendencia.
- **trendValue?:** valor opcional de tendencia formateado.
- **icon:** LucideIcon.
- **color:** clases Tailwind.
- **periodLabel?:** texto del período para el tooltip.
- **loading?:** skeleton.
- **invertedTrend?:** invierte colores de tendencia (gastos: tendencia negativa es buena).
- **className?:** clases adicionales.

### Funcionalidad

- `isValidNumber`: filtra NaN e Infinity.
- `formattedValue`: usa `formatCurrency` o `formatNumber` de i18nFormat.
- `trendColor`: verde para positivo (o negativo si invertedTrend), rosa para negativo.
- Icono de tendencia: ArrowUpRight para subida, ArrowDownRight para bajada.
- Si no hay tendencia válida: texto "Sin datos disponibles".

---

## components/LanguageSelector.tsx

Selector de idioma con dropdown que muestra banderas y nombres nativos.

### Hooks utilizados

- `useAvailableLanguages()`: obtiene lista de idiomas disponibles desde i18next.
- `useTranslation()`: accede a `i18n.changeLanguage()`.

### Estructura

- Botón trigger con icono Languages, código de idioma actual en uppercase (oculto en móvil) y ChevronDown.
- Dropdown con lista de idiomas: bandera (emoji), nombre nativo. El idioma activo tiene fondo accent y font-medium.
- Al cambiar: llama a `i18n.changeLanguage()`. Si falla, notificación de error.

---

## components/ThemeSelector.tsx

Selector de tema visual con 4 paletas predefinidas.

### Hooks utilizados

- `useTheme()`: obtiene `theme`, `setTheme` y `availableThemes`.

### Estructura

- Botón trigger con icono Palette.
- Dropdown con etiqueta "Paleta de colores" y lista de temas: icono (emoji), nombre. El tema activo tiene fondo accent y check verde.
- Al cambiar: llama a `setTheme()`. Si falla, notificación de error.

---

## config/navigation.ts

Registro centralizado de navegación de la aplicación.

### NavItem

- id, icon (LucideIcon), labelKey (clave i18n), path, enabled, section ("main" | "bottom"), order.

### NAVIGATION_REGISTRY

Array con 8 items:

**Main (orden 1-6):**
1. Dashboard (LayoutDashboard, `/dashboard`, enabled)
2. Transacciones (ArrowLeftRight, `/transactions`, enabled)
3. Categorías (LayoutGrid, `/categories`, enabled)
4. Inversiones (TrendingUp, `/investments`, enabled)
5. Familia (PieChart, `/family`, enabled)
6. Reportes (FileText, `/reports`, disabled)

**Bottom (orden 1-2):**
1. Configuración (Settings, `/settings`, disabled)
2. Ayuda (HelpCircle, `/help`, disabled)

### Helpers

- `getEnabledNavItems(section)`: devuelve items habilitados de una sección, ordenados.
- `getDisabledNavItems()`: devuelve todos los items deshabilitados.

---

## hooks/useCategories.ts

Hook que obtiene las categorías del usuario.

### Query

- **queryKey:** `['categories']`.
- **queryFn:** `GET /categories`, filtra solo activas (`active !== false`).
- **staleTime:** 5 minutos (las categorías cambian poco).

### Retorno

Query de React Query con `Category[]`. Interfaz Category: id, userId, name, color, budgetLimit, createdAt, modifiedAt, active.

---

## hooks/useLanguages.ts

Hook que obtiene la lista de idiomas disponibles desde i18next.

### Funcionamiento

- Lee `i18n.options.supportedLngs`.
- Si no hay idiomas configurados, devuelve array con el idioma actual.
- Filtra "cimode" y mapea cada código a un objeto `Language` usando `getLanguageFromCode`.
- Usa `useMemo` para evitar recálculos.

### Retorno

`Language[]` con code, nativeName, englishName y flag.

---

## hooks/usePlannedTransactions.ts

Hook que obtiene las transacciones recurrentes del usuario.

### Query

- **queryKey:** `['plannedTransactions']`.
- **queryFn:** `GET /planned-transactions`.

### Interfaz PlannedTransaction

id, description, categoryName, categoryId, amount, type, frequencyType, frequencyInterval, startDate, endDate, active.

---

## hooks/useTheme.ts

Hook de gestión de temas visuales con persistencia en localStorage.

### Estado inicial

Lee `theme-name` de localStorage. Si existe y coincide con un tema de `THEMES`, lo usa. Si no, usa THEMES[0] (default).

### Efecto

Al cambiar `theme`, aplica 28 variables CSS al `:root`:
- --background, --foreground, --card, --card-foreground, --popover, --popover-foreground
- --primary, --primary-foreground, --secondary, --secondary-foreground
- --muted, --muted-foreground, --accent, --accent-foreground
- --destructive, --destructive-foreground, --border, --input, --ring
- --chart-1 a --chart-5
- --surface, --surface-hover, --subtle

Fuerza modo oscuro (`classList.add("dark")`, `classList.remove("light")`). Guarda nombre del tema en localStorage.

### API

- `theme`: tema actual.
- `setTheme(theme)`: cambia a un tema específico.
- `setThemeByName(name)`: cambia por nombre.
- `availableThemes`: array THEMES.

---

## themes/palettes.ts

Define 4 temas visuales, cada uno con 28 valores HSL.

### Temas

1. **default** (Azul Profesional 💼): tonos azules, primary `217.2 91.2% 59.8%`.
2. **ocean** (Océano Sereno 🌊): tonos cian, primary `187 85% 50%`.
3. **forest** (Bosque Fresco 🌿): tonos verdes, primary `142 60% 50%`.
4. **sunset** (Atardecer Cálido 🌅): tonos naranjas, primary `25 90% 60%`.

### Interfaces

- **ThemeColors:** 28 propiedades HSL.
- **Theme:** name, label, icon, colors.

---

## types/api.ts

Interfaz para errores de la API:

- **ApiError:** `response?.data?.message` y `response?.data?.error` opcionales.

---

## types/index.ts

Interfaz para respuestas paginadas del backend:

- **PageResponse<T>:** content, pageable, last, totalPages, totalElements, size, number, sort, first, numberOfElements, empty.

---

## types/i18n.d.ts

Aumento de tipos de i18next para definir `defaultNS: "translation"` y recursos tipados desde `enTranslation`.

---

## utils/secureStorage.ts

Almacenamiento seguro con ofuscación por fingerprint del dispositivo.

### Prefijo

`ms_` para todas las claves en localStorage.

### Fingerprint

`deriveFingerprint()` genera un hash numérico a partir de:
- userAgent
- colorDepth, width, height de pantalla
- language
- hardwareConcurrency

Convierte el hash a base 36 para usar como clave de cifrado.

### Cifrado

`xorTransform(text, key)`: XOR carácter por carácter entre el texto y la clave (cíclica).

### Métodos

- **setToken(token):** cifra `{token}::{timestamp}` con XOR + base64 y guarda en localStorage.
- **getToken():** descifra, extrae el token (antes de `::`) y lo devuelve. Si falla, elimina la clave.
- **setUser(user):** cifra `JSON.stringify(user)` y guarda.
- **getUser<T>():** descifra, parsea JSON y devuelve tipado. Si falla, elimina la clave.
- **clear():** elimina ambas claves (token y user).

---

## utils/languages.ts

Utilidades para gestión de idiomas.

### Tipos

- **SupportedLanguageCode:** "de" | "en" | "es" | "fr" | "it" | "pt".
- **Language:** code, nativeName, englishName, flag.

### LANGUAGE_MAP

Mapeo estático de 6 idiomas con nombre nativo, nombre en inglés y bandera (emoji).

### Funciones

- **getLanguageFromCode(code):** si el código está en LANGUAGE_MAP, devuelve los datos predefinidos. Si no, genera nombre nativo con `Intl.DisplayNames`, bandera con código Unicode y nombre en inglés con código uppercase.
- **getSupportedLanguages():** devuelve array de Language con los 6 idiomas soportados.
- **getDisplayNames(code):** caché de instancias `Intl.DisplayNames` por código de idioma.
- **getFlagFromCode(code):** convierte código de 2 letras a emoji de bandera (ej: "es" → "🇪🇸").
- **getNativeNameFromCode(code):** obtiene nombre nativo con `Intl.DisplayNames`.

---

## utils/i18nFormat.ts

Formateo de moneda y números con `Intl.NumberFormat` según el idioma activo de i18next.

### Mapeo de locales

6 idiomas mapeados a locale: es → es-ES, en → en-GB, fr → fr-FR, de → de-DE, it → it-IT, pt → pt-PT. Default: es-ES.

### Moneda

Siempre EUR. `formatCurrency(value, currency?)` formatea con 2 decimales fijos.

### Números

`formatNumber(value, options?)` formatea con 2 decimales por defecto. Valida que `minimumFractionDigits` no supere `maximumFractionDigits` (corrige automáticamente). Acepta opciones de `Intl.NumberFormatOptions`.

---

## utils/notifications/notify.tsx

Sistema de notificaciones toast basado en Sonner con 4 variantes visuales.

### Tipos

- **ToastType:** "success" | "error" | "info" | "warning".
- **NotifyOptions:** description (opcional), duration (opcional, en ms).

### Configuración por variante

| Tipo | Icono | Color icono | Duración default | Borde |
|------|-------|-------------|------------------|-------|
| success | CircleCheck | emerald | 4000ms | emerald |
| error | AlertTriangle | destructive | 6000ms | destructive |
| info | Info | primary | 4000ms | primary |
| warning | AlertCircle | amber | 5000ms | amber |

### ToastContent

Componente interno que renderiza cada toast con:
- Icono en círculo con sombra.
- Mensaje principal (semibold) y descripción opcional.
- Botón de cierre (X) con hover coloreado según tipo.
- Fondo `bg-card/90` con `backdrop-blur-md` y `shadow-xl`.
- Borde redondeado `rounded-4xl`.

### API

- `notify.success(message, options?)`
- `notify.error(message, options?)`
- `notify.info(message, options?)`
- `notify.warning(message, options?)`

Cada método llama a `toast.custom()` con el componente ToastContent y duración configurable.

---

## Mejoras implementadas (v0.0.1)

- **secureStorage con ofuscación:** tokens y datos de usuario se almacenan cifrados con XOR + base64 usando fingerprint del dispositivo como clave. Previene lectura directa de localStorage.
- **apiClient con skipGlobalErrorNotify:** opción para omitir notificaciones globales en peticiones específicas (ej: importación de datos que maneja sus propias notificaciones).
- **Interceptor 401 con evento global:** emite `auth:logout` en window para que AuthContext ejecute limpieza sin dependencia circular.
- **Sistema de notificaciones unificado:** 4 variantes (success, error, info, warning) con estilos consistentes, descripciones opcionales y duraciones configurables.
- **Formateo i18n de moneda/números:** `formatCurrency` y `formatNumber` usan `Intl.NumberFormat` con locale detectado de i18next. Corrección automática de decimales inconsistentes.
- **Temas dinámicos con 28 variables CSS:** 4 paletas completas aplicadas mediante `useTheme` con persistencia en localStorage. Fuerza modo oscuro.
- **Navigation registry centralizado:** items de navegación con enabled/disabled, orden y sección. Sidebar renderiza automáticamente desde el registro.
- **Greeting dinámico en Header:** saludo según hora del día (mañana/tarde/noche) con fecha formateada y número de semana.
- **MetricCard con skeleton:** placeholder animado que coincide con la estructura real de la tarjeta.
- **FormattedMetricCard con tendencia invertida:** soporte para `invertedTrend` en gastos (tendencia negativa se muestra en verde).
- **Detección de idiomas robusta:** fallback a bandera Unicode y nombre nativo con `Intl.DisplayNames` para idiomas no predefinidos.
- **Sidebar con items deshabilitados:** sección "Próximamente" con notificación info al hacer clic.
- **LanguageSelector con cambio seguro:** try/catch con notificación de error si falla el cambio de idioma.