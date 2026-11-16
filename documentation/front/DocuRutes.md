# Enrutamiento

[`App.tsx`](../../frontend/src/App.tsx) registra rutas públicas, protegidas y un comodín. Este archivo es la fuente de verdad de páginas montadas; [`routes.ts`](../../frontend/src/shared/config/routes.ts) contiene constantes y el registro de navegación es otro concepto distinto.

## Protección

`PublicRoute` contiene las páginas de inicio y login, y redirige a usuarios autenticados. `ProtectedRoute` espera la inicialización del contexto y exige una sesión para mostrar las rutas hijas; las rutas protegidas incluyen `/notifications` y `/join-group-goal`. El comportamiento de autenticación y recuperación se detalla en [AuthDocu.md](AuthDocu.md).

## Wiki y fallback

Aunque existe una constante `ROUTES.wiki`, `App.tsx` no monta una ruta Wiki. Por tanto `/wiki` no es una página pública disponible; el comodín dentro del grupo protegido redirige al dashboard (o primero se procesa la protección). No describir rutas declaradas pero no montadas como funciones actuales.

## Registro montado

| Patrón | Acceso | Página |
|---|---|---|
| `/`, `/login` | Público mediante `PublicRoute`; una sesión autenticada redirige | Login |
| `/join-group-goal` | Protegido | Aceptar invitación |
| `/dashboard` | Protegido | Dashboard |
| `/transactions` | Protegido | Transacciones |
| `/categories` | Protegido | Categorías |
| `/investments` | Protegido | Inversiones |
| `/group-goals` | Protegido | Metas grupales |
| `/profile` | Protegido | Perfil |
| `/savings-goals` | Protegido | Metas personales |
| `/notifications` | Protegido | Centro de notificaciones |
| `*` | Bajo grupo protegido | Navega a `/dashboard` |

`ProtectedRoute` muestra spinner mientras `isLoading`; si offline mantiene el estado de acceso que entrega AuthContext y la UI de autenticación ofrece reintento donde está implementada. Las pantallas se cargan con `React.lazy`; `Suspense` presenta spinner mientras llega el módulo.
