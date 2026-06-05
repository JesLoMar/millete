# Auth — Documentación técnica

## Estructura de archivos

- **components/AuthForm.tsx** — Orquestador del formulario con react-hook-form + zod
- **components/BackgroundDecoration.tsx** — Fondo decorativo con efectos de blur
- **components/InfoSection.tsx** — Panel informativo (wrapper)
- **components/AuthForm/AuthHeader.tsx** — Logo + selectores idioma/tema
- **components/AuthForm/AuthToggle.tsx** — Toggle Login / Register
- **components/AuthForm/LoginFields.tsx** — Campo identifier con autocomplete y mensaje de error
- **components/AuthForm/RegisterFields.tsx** — Campos username + email con validación
- **components/AuthForm/PasswordField.tsx** — Campo password con toggle mostrar/ocultar
- **components/AuthForm/AuthFooter.tsx** — Versiones + copyright
- **components/InfoSection/FirstSteps.tsx** — Pasos iniciales + enlace wiki
- **components/InfoSection/NewsList.tsx** — Lista de novedades
- **context/AuthContext.tsx** — Estado global de autenticación
- **hooks/useAuthMutations.ts** — Mutación de login con sanitización de redirect
- **hooks/useRegisterMutation.ts** — Mutación de registro con login automático
- **pages/page.tsx** — Página completa de login con redirección si autenticado
- **schemas/auth.schema.ts** — Esquemas de validación con Zod
- **services/auth.service.ts** — Llamadas HTTP a la API
- **types/index.ts** — Contratos de datos

---

## pages/page.tsx

Página de login que ocupa toda la pantalla. Estructura:

- Verifica `isAuthenticated` e `isLoading` desde `useAuth`.
- Si `isLoading` es true, muestra un placeholder con animación de pulso y texto "Cargando...".
- Si `isAuthenticated` es true, redirige a `/dashboard` con `<Navigate to="/dashboard" replace />`.
- Si no está autenticado, renderiza el layout completo:
  - **Izquierda (1/3):** AuthForm.
  - **Derecha (2/3):** InfoSection (solo visible en escritorio, `hidden lg:block`).
  - **Fondo:** BackgroundDecoration con `z-10` relativo.
- Atributos `aria-label` en main y secciones para accesibilidad.
- Clase `selection:bg-primary/30 selection:text-white` en el main.

---

## components/AuthForm.tsx — Orquestador

Controla el estado del formulario mediante react-hook-form integrado con zod (zodResolver).

### Estado local

- mode: "login" o "register" — determina qué campos se muestran y qué mutación se ejecuta.
- showPassword: boolean — controla visibilidad de la contraseña.

### Esquema de formulario

- Usa `useForm<CombinedAuthFormData>` con `zodResolver(authFormFieldsSchema)`.
- `authFormFieldsSchema` define la forma base con campos opcionales: identifier, usernameRegistro, emailRegistro y password obligatorio.
- `loginSchema` refina exigiendo identifier no vacío.
- `registerSchema` refina exigiendo al menos usernameRegistro o emailRegistro, y si emailRegistro tiene valor, valida formato email.

### Hooks utilizados

- useLoginMutation — expone `mutate` (renombrado a `loginMutate`), `isPending`.
- useRegisterMutation — expone `mutate` (renombrado a `registerMutate`), `isPending`.

### Flujo onSubmit

1. Si mode es "login" llama a `loginMutate` con `{ identifier, password }`.
2. Si mode es "register" llama a `registerMutate` con `{ username: usernameRegistro, email: emailRegistro, password }`.

### Validaciones

- **Login:** identifier requerido (refinamiento en loginSchema). Password mínimo 8 caracteres.
- **Register:** al menos username o email (refinamiento en registerSchema). Email válido si se introduce. Password mínimo 8 caracteres.
- **Botón submit:** deshabilitado si `!isValid` o `isPending`.

### Estados de UI

- **isPending:** deshabilita todos los campos y muestra un spinner con "Cargando..." en el botón.
- **isError (login):** muestra mensaje de credenciales inválidas desde el error de mutación.
- **isError (register):** muestra mensaje de error desde el error de mutación.
- **hasIdentifier (register):** advertencia si ambos campos (username y email) están vacíos.
- **isValid:** botón deshabilitado si el esquema no se cumple.

---

## Subcomponentes de AuthForm/

### AuthHeader.tsx

Logo de la app + LanguageSelector + ThemeSelector.

### AuthToggle.tsx

Dos botones que alternan mode entre "login" y "register". El botón activo usa `bg-primary`.

**Props:** mode, onToggle

### LoginFields.tsx

Campo identifier (acepta username o email). Requerido.

- Incluye `autoComplete="username"` para mejor experiencia del navegador.
- Muestra mensaje de error de validación si el campo está vacío.
- Registrado en el formulario como `identifier`.

**Props:** control (de react-hook-form), disabled, errors

### RegisterFields.tsx

Campos opcionales username y email, separados por un divisor visual "o".

- Registrados en el formulario como `usernameRegistro` y `emailRegistro`.
- Muestra advertencia si ambos están vacíos (hasIdentifier).
- Email vacío se ignora en el submit.

**Props:** control, errors, disabled, hasIdentifier

### PasswordField.tsx

Campo password común a login y register.

- Login: `autoComplete="current-password"`.
- Register: `autoComplete="new-password"`.
- Botón toggle (ojo) para mostrar/ocultar contraseña.
- Modo registro: hint de 8 caracteres mínimos.
- Muestra mensaje de error de validación si no cumple mínimo de caracteres.

**Props:** control, disabled, mode

### AuthFooter.tsx

Badges con versión web y API + texto de copyright.

---

## schemas/auth.schema.ts

Define los esquemas de validación con Zod:

- **passwordSchema:** string con mínimo 8 caracteres. Mensaje de error con clave i18n `auth.form.passwordHint`.
- **authFormFieldsSchema:** objeto base con identifier (opcional), usernameRegistro (opcional), emailRegistro (opcional) y password (obligatorio con passwordSchema).
- **CombinedAuthFormData:** tipo inferido del esquema base.
- **loginSchema:** refinamiento de authFormFieldsSchema que exige identifier no vacío. Mensaje de error `auth.form.error.invalidCredentials` en path identifier.
- **registerSchema:** dos refinamientos encadenados:
  1. Exige al menos usernameRegistro o emailRegistro no vacío. Mensaje `auth.form.registerHint` en path emailRegistro.
  2. Si emailRegistro tiene valor, valida formato email con regex. Mensaje `auth.forgotPassword.emailInvalid` en path emailRegistro.

---

## components/InfoSection.tsx

Panel informativo estático. Solo consume i18n, sin comunicación con backend.

### FirstSteps.tsx

Muestra 3 pasos numerados y un banner de enlace a la wiki.

### NewsList.tsx

Línea de tiempo vertical con novedades. La primera entrada lleva un punto brillante y un tag opcional.

---

## context/AuthContext.tsx

Estado global de autenticación. Provee token, user, isAuthenticated, isLoading, login y logout.

### User interface

- name: string
- email: string
- avatar?: string
- role?: string

### Inicialización (useEffect de montaje)

1. Lee token y usuario de secureStorage (métodos `getToken()` y `getUser<User>()`).
2. Si hay token:
   - Lo establece en estado.
   - Si no hay usuario cacheado, llama a `GET /auth/me/topnav` mediante `apiClient` (ya configurado con baseURL e interceptores).
   - Formatea el usuario: name = username || parte local del email || "Usuario".
   - Guarda el usuario formateado en estado y en secureStorage.
   - Si la llamada falla (token inválido/expirado), ejecuta `logout()`.
   - Si hay usuario cacheado, lo restaura directamente.
3. `isLoading` pasa a `false` al terminar.

### Listener de logout forzado

- Escucha el evento `auth:logout` en window (emitido por el interceptor de axios ante 401).
- Al recibirlo, ejecuta `logout()`.
- Limpia el listener en el cleanup del useEffect.

### login(token, userData?)

1. Guarda token en estado y secureStorage.
2. Si recibe `userData` lo guarda directamente en estado y secureStorage.
3. Si no, llama a `GET /auth/me/topnav` para obtener el perfil.
4. Si la llamada falla, ejecuta `logout()` y lanza error "Fallo al obtener perfil tras login".

### logout

- Limpia estado (token y user a null).
- Llama a `secureStorage.clear()`.
- Llama a `queryClient.clear()` para limpiar toda la caché de React Query.

### useAuth

Hook que consume AuthContext. Lanza error si se usa fuera del provider.

---

## hooks/useAuthMutations.ts — Login

Usa `useMutation` de React Query.

### mutationFn

Llama a `authService.login(credentials)` con identifier y password.

### onSuccess

1. Ejecuta `login(data.token)` del AuthContext (guarda token y obtiene perfil).
2. Invalida todas las queries pendientes con `queryClient.invalidateQueries()`.
3. Muestra notificación de éxito con `notify.success`.
4. Determina la ruta de redirección:
   - Prioridad 1: `location.state?.from?.pathname` (con search y hash si existen).
   - Prioridad 2: query param `?redirect=` de la URL.
   - Sanitiza la ruta con `sanitizeRedirect()`:
     - Rechaza URLs absolutas, protocolos `javascript:`, rutas que no empiezan por `/` o empiezan por `//`.
     - Solo permite rutas base predefinidas: `/dashboard`, `/transactions`, `/categories`, `/investments`, `/family`, `/join-family` y sus subrutas.
   - Si no hay redirect válido, navega a `/dashboard`.
5. Navega con `replace: true` para evitar volver al login con "atrás".

### onError

Registra en consola el mensaje de error (del backend o genérico).

---

## hooks/useRegisterMutation.ts — Registro

Usa `useMutation<TokenResponse, ApiError, RegisterUserRequest>`.

### mutationFn (2 pasos encadenados)

1. Llama a `authService.register(data)` — crea la cuenta (no devuelve token).
2. Construye identifier = data.username || data.email || "".
3. Si identifier está vacío, lanza error con mensaje i18n `auth.errors.no_identifier`.
4. Construye `LoginRequest` con identifier y password.
5. Llama a `authService.login(loginData)` — obtiene el JWT.
6. Si el login falla, lanza error con mensaje i18n `auth.errors.auto_login_failed`.

### onSuccess

1. Ejecuta `login(data.token)` del AuthContext.
2. Invalida queries con `queryClient.invalidateQueries()`.
3. Muestra notificación de éxito con clave i18n `auth.alerts.register_success`.
4. Navega a `/dashboard` con `replace: true`.

### onError

Muestra notificación de error con el mensaje del backend o clave i18n `auth.errors.register_failed`.

---

## services/auth.service.ts

Contiene dos métodos:

- **login(credentials):** POST a `/auth/login`. Recibe `LoginRequest`, devuelve `TokenResponse`.
- **register(data):** POST a `/auth/register`. Recibe `RegisterUserRequest`, no devuelve nada (void).

Ambos usan `apiClient`, instancia de axios configurada globalmente en `@/shared/api/axiosClient`:

- baseURL: `import.meta.env.VITE_API_URL`.
- Interceptor de petición: añade `Authorization: Bearer token`.
- Interceptor de respuesta: emite evento `auth:logout` ante errores 401.

---

## types/index.ts

Contratos de datos entre frontend y backend:

- **LoginRequest:** identifier (string), password (string).
- **RegisterUserRequest:** username (opcional), email (opcional), password (obligatorio).
- **TokenResponse:** token (string JWT).
- **UserTopnavResponse:** username, email, role, avatar (todos opcionales).
- **ApiErrorResponse:** status, message, error, timestamp (todos opcionales).

---

## Flujo completo

1. Usuario introduce credenciales en AuthForm.
2. `onSubmit` decide qué mutación llamar según `mode` (login o register).
3. El esquema Zod correspondiente (`loginSchema` o `registerSchema`) valida los campos.
4. `authService` llama a `POST /auth/login` o `POST /auth/register`.
5. En registro, tras crear la cuenta se hace login automático.
6. `onSuccess` guarda el token mediante `AuthContext.login()`, que persiste en secureStorage y obtiene el perfil del usuario.
7. Se invalida la caché de React Query, se muestra notificación y se redirige al dashboard (o a la ruta segura de redirect).

---

## Persistencia

- **secureStorage:** utilidad compartida que abstrae el almacenamiento seguro.
  - `secureStorage.getToken()` / `secureStorage.setToken(token)` para el JWT.
  - `secureStorage.getUser<User>()` / `secureStorage.setUser(user)` para el perfil.
  - `secureStorage.clear()` para limpieza total en logout.
- Al hacer login: se guarda token y usuario en secureStorage.
- Al hacer logout: se limpia secureStorage, estado y caché de React Query.
- Al inicializar: se restaura el token y usuario desde secureStorage; si no hay usuario cacheado se obtiene del backend con `GET /auth/me/topnav`.

---

## Conexión con el backend

| Método | Endpoint | Uso |
|--------|----------|-----|
| POST | /auth/login | Iniciar sesión, devuelve JWT |
| POST | /auth/register | Registrar usuario, sin token |
| GET | /auth/me/topnav | Obtener perfil del usuario autenticado |

---

## Mejoras implementadas (v0.0.1)

- **secureStorage centralizado:** reemplaza acceso directo a localStorage por utilidad compartida con métodos tipados.
- **apiClient en inicialización:** fetchUserData usa apiClient (axios) en lugar de fetch nativo, aprovechando interceptores y baseURL por entorno.
- **Listener de logout forzado:** AuthProvider escucha evento `auth:logout` emitido por el interceptor de axios ante 401, ejecutando limpieza completa.
- **Validación con Zod:** esquemas declarativos con refinamientos para login y registro, eliminando lógica de validación ad-hoc.
- **Sanitización de redirect:** función `sanitizeRedirect` que rechaza URLs externas, protocolos peligrosos y solo permite rutas internas predefinidas.
- **Página con estados de carga y redirección:** LoginPage muestra placeholder durante carga y redirige a /dashboard si ya está autenticado.
- **Registro robusto:** login automático tras registro con manejo de errores diferenciado y mensajes i18n.
- **Notificaciones toast:** éxito y error notificados con `notify.success` y `notify.error`.
- **Accesibilidad:** atributos `aria-label` en secciones principales y selección de texto estilizada.
- **Modo registro con email opcional:** email vacío se omite en el submit; solo se valida formato si el usuario introduce un valor.