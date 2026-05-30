# Raíz del proyecto — Documentación técnica (Frontend)

## Estructura de archivos

- **App.tsx** — Componente raíz con enrutador, providers y rutas
- **main.tsx** — Punto de entrada de la aplicación
- **index.css** — Estilos globales, variables CSS y directivas de Tailwind
- **vite.config.ts** — Configuración de Vite (no incluido)
- **package.json** — Dependencias y scripts (no incluido)
- **index.html** — Plantilla HTML principal (no incluido)
- **.env / .env.production** — Variables de entorno (no incluido)
- **tsconfig*.json** — Configuraciones de TypeScript (no incluido)
- **vite-env.d.ts** — Tipos de Vite (no incluido)

---

## App.tsx

Componente raíz de la aplicación. Define la estructura de enrutamiento y los providers globales.

### Providers (orden de anidamiento)

1. **AuthProvider:** provee el contexto de autenticación (`AuthContext`) a toda la aplicación. Se ejecuta antes que el router para que las rutas protegidas puedan verificar el estado de autenticación.
2. **BrowserRouter:** enrutador de React Router con modo history (URLs limpias sin `#`).
3. **Toaster:** componente de Sonner para renderizar notificaciones toast. Se coloca fuera de Routes para que esté disponible globalmente sin re-renderizarse al cambiar de ruta.

### Estructura de rutas

#### Rutas públicas (PublicRoute)

Envuelve rutas accesibles solo sin autenticación. Si el usuario ya tiene sesión, redirige a `/dashboard`.

| Ruta | Componente | Descripción |
|------|-----------|-------------|
| `/` | LoginPage | Página de inicio de sesión |
| `/login` | LoginPage | Página de inicio de sesión (alias) |

#### Rutas protegidas (ProtectedRoute)

Envuelve rutas que requieren autenticación. Si el usuario no tiene sesión, redirige a `/login` guardando la ubicación intentada.

| Ruta | Componente | Feature |
|------|-----------|---------|
| `/join-family` | JoinFamilyPage | Aceptar invitación a familia |
| `/dashboard` | DashboardPage | Panel principal |
| `/transactions` | TransactionsPage | Gestión de transacciones |
| `/categories` | CategoriesPage | Gestión de categorías |
| `/investments` | InvestmentsPage | Gestión de inversiones |
| `/family` | FamilyPage | Gestión familiar |

#### Ruta 404 (catch-all)

`<Route path="*">` dentro de ProtectedRoute redirige a DashboardPage. Si el usuario no está autenticado, ProtectedRoute lo redirigirá a `/login`. Si está autenticado, verá el dashboard.

### Flujo de autenticación en rutas

1. Usuario accede a cualquier ruta.
2. Si es pública (`/`, `/login`): PublicRoute verifica `isAuthenticated`. Si true, redirige a `/dashboard`. Si false, muestra la página.
3. Si es protegida: ProtectedRoute verifica `isLoading` (muestra placeholder) y `isAuthenticated`. Si false, redirige a `/login` con `state.from`. Si true, muestra la página.
4. Ruta no encontrada: ProtectedRoute envuelve DashboardPage como fallback.

---

## main.tsx

Punto de entrada de la aplicación. Se ejecuta al cargar la SPA en el navegador.

### Orden de inicialización

1. Importa `./lib/i18n` — inicializa i18next antes que cualquier componente para que las traducciones estén disponibles al renderizar.
2. Importa `./index.css` — carga los estilos globales de Tailwind.
3. Crea `QueryClient` con configuración global para React Query.

### Configuración de QueryClient

| Opción | Valor | Descripción |
|--------|-------|-------------|
| `refetchOnWindowFocus` | `false` | No re-fetch al reenfocar la ventana |
| `retry` | `1` | Un solo reintento en queries fallidas |
| `staleTime` | `5 * 60 * 1000` (5 min) | Datos frescos durante 5 minutos |
| `gcTime` | `10 * 60 * 1000` (10 min) | Caché eliminada tras 10 minutos de inactividad |

### Renderizado

- `React.StrictMode`: activa comprobaciones adicionales en desarrollo.
- `QueryClientProvider`: provee el cliente de React Query a toda la aplicación.
- Monta en `document.getElementById('root')`.

---

## index.css

Hoja de estilos global que configura Tailwind CSS, variables CSS y temas.

### Directiva de Tailwind v4

`@import "tailwindcss"` — importa Tailwind CSS con la nueva sintaxis de v4.

### Variante dark

`@custom-variant dark (&:is(.dark *))` — define la variante `dark:` para modo oscuro basada en la clase `.dark` en el elemento raíz.

### Tema personalizado (`@theme`)

Define variables de diseño con valores HSL:

- **Colores base:** border, input, ring, background, foreground.
- **Colores semánticos:** primary, secondary, destructive, muted, accent, popover, card (con sus foregrounds).
- **Colores de dashboard:** surface, surface-hover, subtle.
- **Colores de gráficos:** chart-1 a chart-5.
- **Radios:** radius-lg, radius-md, radius-sm.
- **Tipografía:** font-serif ("Playfair Display").

### Variables CSS (`@layer base`)

Define valores por defecto para modo claro (`:root`) y oscuro (`.dark`):

- **Modo claro:** fondos blancos, texto oscuro, primary azul.
- **Modo oscuro:** fondos oscuros, texto claro, primary azul claro.
- Ambos modos definen 28 variables CSS (las mismas que aplica `useTheme` dinámicamente como base).

### Estilos base

- `*` aplica `border-border` para bordes consistentes.
- `body` aplica colores de fondo y texto con transición suave (150ms) en propiedades de color.

### Variables de Sidebar

Define variables específicas para el sidebar en modo claro y oscuro:

- `--sidebar`, `--sidebar-foreground`, `--sidebar-primary`, `--sidebar-primary-foreground`
- `--sidebar-accent`, `--sidebar-accent-foreground`, `--sidebar-border`, `--sidebar-ring`

Estas variables se expone como colores de Tailwind mediante `@theme inline`.

### Integración con useTheme

- `index.css` proporciona los valores base (modo claro/oscuro estáticos).
- `useTheme` sobreescribe dinámicamente estas variables al cambiar de tema, aplicando los valores de la paleta seleccionada.
- Las variables de sidebar permanecen estáticas (no cambian con el tema).

---

## Variables de entorno

La aplicación usa dos archivos de entorno:

- **.env:** variables para desarrollo local.
- **.env.production:** variables para build de producción.

### Variable principal

`VITE_API_URL`: URL base del backend. Ejemplo: `http://localhost:8080/api/v1` en desarrollo. Consumida por `axiosClient.ts` para configurar `baseURL`.

---

## Flujo de inicialización

1. `index.html` carga el bundle de JavaScript.
2. `main.tsx` inicializa i18n, React Query y monta React.
3. `App.tsx` renderiza AuthProvider que lee secureStorage y verifica sesión.
4. BrowserRouter resuelve la ruta actual.
5. ProtectedRoute o PublicRoute deciden qué página mostrar según el estado de autenticación.
6. La página correspondiente se renderiza con sus queries y componentes.