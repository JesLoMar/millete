# Aplicación y arranque

[`main.tsx`](../../frontend/src/main.tsx) carga estilos e internacionalización, configura React Query y monta la aplicación. [`App.tsx`](../../frontend/src/App.tsx) compone `AuthProvider`, `BrowserRouter`, rutas cargadas de forma diferida y el toaster.

## Rutas montadas

Las rutas y su protección efectiva se definen en `App.tsx`. Las rutas `/` y `/login` son públicas. Dashboard, transacciones, categorías, inversiones, metas grupales, incorporación a meta, perfil, metas de ahorro y notificaciones están dentro de `ProtectedRoute`. El comodín protegido redirige al dashboard.

`ROUTES` y `PROTECTED_ROUTE_PATHS` en [`routes.ts`](../../frontend/src/shared/config/routes.ts) son constantes, no una prueba de que cada valor esté montado. En particular, no existe una ruta Wiki en `App.tsx`.

## Contexto compartido

`QueryClientProvider` se inicializa en `main.tsx`. Las opciones globales vigentes están en ese archivo; cada hook puede definir sus propias opciones. `index.css` aporta los estilos y tokens globales. El tema se aplica mediante `useTheme` y las paletas de `shared/themes`; no inferir modos o preferencias a partir de documentación anterior.

La configuración de Vite y TypeScript está en [`vite.config.ts`](../../frontend/vite.config.ts), [`tsconfig.app.json`](../../frontend/tsconfig.app.json) y [`tsconfig.node.json`](../../frontend/tsconfig.node.json).

## Providers y caché

`main.tsx` monta, de fuera hacia dentro, `StrictMode`, `QueryClientProvider`, `MotionProvider` y `App`. Antes del primer render aplica el tema recuperado por `getInitialTheme`. `App` añade `AuthProvider`, `BrowserRouter`, `Suspense` con spinner y `Toaster`.

Defaults del QueryClient: stale time de cinco minutos, no refetch al enfocar ventana, y retry condicionado: cancelaciones no reintentan; errores de red sin status o respuestas 5xx reintentan mientras `failureCount < 2`; otros status no. Hooks individuales pueden reemplazar estos defaults.
