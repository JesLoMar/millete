# Router — Documentación técnica (Frontend)

## Estructura de archivos

- **ProtectedRoute.tsx** — Ruta protegida que requiere autenticación
- **PublicRoute.tsx** — Ruta pública que redirige si ya hay sesión

---

## ProtectedRoute.tsx

Componente de orden superior que protege las rutas que requieren autenticación. Actúa como wrapper usando el patrón layout route de React Router con `<Outlet />`.

### Hooks utilizados

- `useAuth()`: obtiene `isAuthenticated` e `isLoading` del AuthContext.
- `useLocation()`: obtiene la ubicación actual para redirigir tras login.
- `useTranslation()`: obtiene la función `t` para i18n.

### Comportamiento

1. **Carga (`isLoading === true`):** mientras se verifica la sesión (lectura de secureStorage + petición a `/auth/me/topnav`), muestra un texto centrado "Cargando sesión..." con color `text-muted-foreground`.
2. **No autenticado (`isAuthenticated === false`):** redirige a `/login` con `replace: true` para evitar que el usuario vuelva a la ruta protegida con el botón "atrás" del navegador. Pasa `state: { from: location }` para que el login pueda redirigir de vuelta a la ruta original tras autenticarse.
3. **Autenticado (`isAuthenticated === true`):** renderiza el contenido de la ruta hija mediante `<Outlet />`.

### Props

Ninguna. Obtiene todo el estado del contexto de autenticación.

---

## PublicRoute.tsx

Componente de orden superior para rutas públicas (login, registro, etc.). Evita que un usuario ya autenticado acceda a páginas de autenticación.

### Hooks utilizados

- `useAuth()`: obtiene `isAuthenticated` del AuthContext.

### Comportamiento

1. **Autenticado (`isAuthenticated === true`):** redirige a `/dashboard` con `replace: true`.
2. **No autenticado (`isAuthenticated === false`):** renderiza el contenido de la ruta hija mediante `<Outlet />`.

### Props

Ninguna. Obtiene todo el estado del contexto de autenticación.

---

## Flujo de enrutamiento

1. Usuario accede a una ruta protegida (`/dashboard`, `/transactions`, etc.).
2. ProtectedRoute verifica el estado de autenticación.
3. Si está cargando, muestra placeholder.
4. Si no está autenticado, redirige a `/login` guardando la ubicación intentada en `location.state.from`.
5. Tras login exitoso, `useLoginMutation` recupera `location.state.from` y redirige a la ruta original.
6. Si el usuario autenticado accede a `/login`, PublicRoute redirige a `/dashboard`.

---

## Integración con AuthContext

Ambos componentes dependen de `AuthContext`:

- **isLoading:** true durante la inicialización (lectura de secureStorage + fetch a `/auth/me/topnav`). Se controla con `useState(true)` y se pone a false al terminar el useEffect de montaje.
- **isAuthenticated:** derivado de `!!token`. Se actualiza al hacer login (setToken + setUser) o logout (setToken(null) + setUser(null)).

---

## Integración con useLoginMutation

La mutación de login en `useAuthMutations.ts` utiliza `location.state?.from` para la redirección post-login:

1. `onSuccess` de la mutación obtiene `location.state?.from?.pathname` (con search y hash si existen).
2. También considera el query param `?redirect=` de la URL actual.
3. Sanitiza la ruta con `sanitizeRedirect()` para prevenir open redirect.
4. Si existe una ruta válida, redirige allí; si no, a `/dashboard`.

Esto cierra el ciclo: ProtectedRoute guarda la ubicación → login redirige de vuelta.

---

## Mejoras implementadas (v0.0.1)

- **ProtectedRoute con estado de carga:** muestra placeholder mientras se verifica la sesión, evitando flash de redirección a `/login` al recargar la página.
- **ProtectedRoute con i18n:** texto "Cargando sesión..." traducido mediante `useTranslation()`.
- **PublicRoute simplificado:** redirige a `/dashboard` si ya hay sesión, evitando acceso a login/registro estando autenticado.
- **Integración con sanitizeRedirect:** la ubicación guardada por ProtectedRoute se valida en useLoginMutation para prevenir open redirect a URLs externas.