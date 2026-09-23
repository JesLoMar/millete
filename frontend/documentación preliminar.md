## App

**Responsabilidad:**

* Composición global de la aplicación.
* Configuración de navegación.
* Protección de rutas según el estado de autenticación.
* Comportamiento transversal relacionado con la presentación global.
* Configuración de providers globales necesarios para la aplicación.

**No contiene:**

* Lógica de negocio.
* Acceso directo a la API.
* Estado específico de una feature.
* Modelos de dominio.
* Lógica específica de una feature.

### main.tsx

Punto de entrada de la aplicación.

Responsabilidades:

* Inicializar React.
* Registrar `StrictMode`.
* Proporcionar el `QueryClient` global de TanStack Query.
* Inicializar la internacionalización.
* Proporcionar `MotionProvider`.
* Montar `App`.

`QueryClient` utiliza una configuración global con:

* `staleTime` de 5 minutos.
* `refetchOnWindowFocus: false`.

TanStack Query es la infraestructura utilizada para gestionar el estado remoto de la aplicación.

No existe una capa adicional de gestión de server state entre las features y TanStack Query salvo que una feature tenga una necesidad concreta que lo justifique.

### App.tsx

Responsabilidad:

* Composición del router.
* Registro de las páginas de la aplicación.
* Definición de qué páginas son públicas y cuáles requieren autenticación.
* Lazy loading de las páginas.
* Montaje del `Toaster` global.

Las páginas de las features se cargan mediante `React.lazy` para evitar cargar todas las funcionalidades en el bundle inicial.

Estructura conceptual:

```text
App
├── PublicRoute
│   ├── /
│   └── /login
│
├── Wiki
│   ├── /wiki
│   └── /wiki/:section
│
└── ProtectedRoute
    ├── /join-group-goal
    ├── /dashboard
    ├── /transactions
    ├── /categories
    ├── /investments
    ├── /group-goals
    ├── /profile
    ├── /savings-goals
    └── /notifications
```

### router/routes.ts

Define las rutas canónicas de la aplicación mediante `ROUTES`.

Las rutas deben referenciarse mediante `ROUTES` en lugar de repetir strings de URL directamente en los componentes.

`PROTECTED_ROUTE_PATHS` mantiene una relación explícita de rutas protegidas.

Debe verificarse si esta constante tiene un uso real o si duplica información que ya queda expresada mediante la estructura de `React Router`. Si no tiene una responsabilidad adicional, puede eliminarse para evitar mantener dos fuentes de verdad sobre qué rutas están protegidas.

### router/PublicRoute.tsx

Guard para rutas destinadas a usuarios no autenticados.

Si existe una sesión autenticada, redirige al dashboard.

En caso contrario renderiza el `Outlet`.

No contiene lógica de autenticación propia; consume el estado proporcionado por `AuthContext`.

### router/ProtectedRoute.tsx

Guard para rutas autenticadas.

Estados:

1. Inicialización de sesión → muestra estado de carga.
2. Imposibilidad de comprobar la sesión → muestra error y permite reintentar.
3. Usuario no autenticado → redirige a login conservando la ubicación original.
4. Usuario autenticado → renderiza el `Outlet`.

La lógica pertenece a la infraestructura de navegación del frontend, no al dominio de negocio.

`ProtectedRoute` consume exclusivamente la información necesaria de `AuthContext` para tomar la decisión de navegación y presentar los estados correspondientes.

### CSS global

`src/index.css` contiene los estilos globales de la aplicación:

* Importación de Tailwind.
* Design tokens y variables de tema.
* Estilos base.
* Estilos globales del `body`.
* Scrollbars.
* Animaciones globales.
* Compatibilidad con `prefers-reduced-motion`.
* Corrección del autofill del navegador.
* Estilos específicos de determinados elementos globales.

`src/app/globals.css` contiene actualmente únicamente la regla global de `prefers-reduced-motion`.

La misma regla está duplicada en `src/index.css`, por lo que `globals.css` no aporta actualmente una responsabilidad propia y puede eliminarse, dejando la regla en `index.css`.

Los estilos pertenecientes exclusivamente a una feature no deberían mantenerse en el CSS global salvo que exista una razón real para ello.

### Lazy loading y aislamiento de features

El lazy loading de páginas permite mantener separadas las features y evitar que todas sus páginas se carguen inicialmente.

Debe evitarse introducir desde componentes globales imports innecesarios de funcionalidades concretas que puedan provocar que código o estilos específicos de una feature entren en el bundle inicial.

## Mejoras detectadas

### 1. `PROTECTED_ROUTE_PATHS` puede ser redundante

Actualmente existe una lista manual de rutas protegidas además de la estructura de rutas protegidas de `App.tsx`.

Debe comprobarse su uso.

Si únicamente replica información ya conocida por `React Router`, eliminarla para mantener una única fuente de verdad.

### 2. Uso inconsistente de `ROUTES`

`PublicRoute` utiliza actualmente `/dashboard` directamente mientras que `ProtectedRoute` utiliza `ROUTES.login`.

Debe utilizarse `ROUTES.dashboard` para evitar strings de rutas hardcodeados.

### 3. Catch-all de rutas protegidas simplificable

Actualmente el fallback utiliza una estructura con dos rutas `*`.

La configuración puede simplificarse integrando el fallback dentro de la rama protegida para que exista una única estructura de protección y una ruta `*` que redirija/renderice el dashboard.

### 4. `globals.css` innecesario

Actualmente `src/app/globals.css` únicamente contiene una regla que también existe en `src/index.css`.

Debe eliminarse la duplicación y, salvo que aparezca una nueva responsabilidad para este archivo, eliminar `globals.css`.

### 5. CSS específico de `BillConfetti` dentro de estilos globales

`index.css` contiene estilos específicos de `BillConfetti`, componente perteneciente a `features/auth`.

Este CSS debería pertenecer a la propia feature o al propio componente para evitar introducir estilos de una funcionalidad concreta en la hoja global.

Una posible organización:

```text
features/auth/components/
├── BillConfetti.tsx
└── BillConfetti.css
```

El objetivo es mantener cada feature autocontenida y facilitar también el code splitting de sus recursos.

### 6. `MotionProvider` cargado mediante lazy loading

`MotionProvider` se carga de forma diferida desde `main.tsx`.

Debe comprobarse su implementación para determinar si esta optimización realmente aporta valor o simplemente retrasa el montaje inicial de `App`.

Actualmente utiliza `fallback={null}`, por lo que durante la carga del provider puede existir un breve estado de pantalla vacía.

No modificar hasta comprobar la responsabilidad real de `MotionProvider`.

### 7. Revisar tokens y estilos globales no utilizados

`index.css` contiene un número elevado de variables de tema y tokens globales.

No deben eliminarse por apariencia o por cantidad.

Debe comprobarse su utilización real en `shared/components` y en las features antes de eliminar cualquier token. Los tokens sin uso serán candidatos a limpieza.

## Criterios arquitectónicos para esta capa

* `app` compone la aplicación, no implementa funcionalidades de negocio.
* Las rutas deben tener una única fuente de verdad.
* Los providers globales solo deben existir cuando proporcionan una capacidad realmente transversal.
* Las features deben mantenerse aisladas siempre que sea posible.
* El estado remoto debe delegarse en TanStack Query sin crear capas intermedias innecesarias.
* El CSS global debe contener únicamente estilos realmente globales.
* La optimización de bundle no debe conseguirse a costa de introducir abstracciones innecesarias.
* No crear capas o servicios adicionales únicamente por motivos arquitectónicos si React Router, React Query o la propia composición de React ya resuelven el problema.

----

## Auth

### Responsabilidad

El feature `auth` contiene dos responsabilidades relacionadas pero diferenciables:

1. **Gestión global de sesión**

   * comprobación inicial de sesión;
   * estado de autenticación;
   * usuario autenticado;
   * logout;
   * retry de comprobación de sesión;
   * respuesta a eventos globales de logout.

2. **Interfaz pública de autenticación**

   * login;
   * registro;
   * validación de formularios;
   * navegación posterior a autenticación;
   * contenido informativo de la página pública;
   * elementos visuales y decorativos de la pantalla de acceso.

El `AuthContext` es la pieza transversal del módulo: aunque se encuentre dentro de `features/auth`, es consumido por otras partes de la aplicación como `ProtectedRoute` y `PublicRoute`.

No contiene:

* lógica de negocio financiera;
* reglas de dominio de otras features;
* acceso directo a Axios desde los componentes;
* lógica de persistencia de datos financieros.

---

## Estructura

```text
features/auth/
├── components/
│   ├── AuthForm.tsx
│   ├── BackgroundDecoration.tsx
│   ├── BillConfetti.tsx
│   ├── InfoSection.tsx
│   ├── AuthForm/
│   │   ├── AuthFooter.tsx
│   │   ├── AuthHeader.tsx
│   │   ├── AuthToggle.tsx
│   │   ├── LoginFields.tsx
│   │   ├── PasswordField.tsx
│   │   └── RegisterFields.tsx
│   └── InfoSection/
│       ├── FirstSteps.tsx
│       └── NewsList.tsx
├── context/
│   └── AuthContext.tsx
├── hooks/
│   ├── useAuthMutations.ts
│   ├── useBillConfetti.ts
│   └── useRegisterMutation.ts
├── pages/
│   └── page.tsx
├── schemas/
│   └── auth.schema.ts
├── services/
│   └── auth.service.ts
└── types/
    └── index.ts
```

---

# AuthContext

### Responsabilidad

`AuthContext` mantiene el estado global de sesión y proporciona las operaciones necesarias para que la aplicación pueda determinar si existe una sesión autenticada.

Estado mantenido:

```text
user
sessionId
isLoading
isOffline
```

Estado derivado:

```text
isAuthenticated = !!user
```

Funciones expuestas:

```text
login()
logout()
retryAuth()
```

### Inicialización

Al montarse el provider se comprueba la sesión mediante:

```text
GET /auth/me/topnav
```

Resultados:

```text
200
 ↓
usuario autenticado

401
 ↓
usuario no autenticado

error de red
 ↓
isOffline = true
```

`isLoading` representa el estado de comprobación inicial de la sesión.

### Login

El método `login()` del contexto no autentica las credenciales.

La autenticación de credenciales ocurre en la mutación de login.

`AuthContext.login()` sirve realmente para volver a obtener el usuario autenticado después de que el backend haya aceptado las credenciales:

```text
POST /auth/login
      ↓
AuthContext.login()
      ↓
GET /auth/me/topnav
      ↓
actualizar estado global
```

### Logout

`logout()`:

* llama a `/auth/logout`;
* limpia `user`;
* limpia `sessionId`;
* limpia `sessionCache`;
* limpia la caché de React Query;
* restablece `isOffline`.

Además, el contexto escucha el evento global `auth:logout`.

### Retry

`retryAuth()` vuelve a ejecutar la comprobación inicial de sesión.

### Valoración

El `AuthContext` concentra varias responsabilidades, pero todas están relacionadas con la gestión global de sesión.

No se considera necesario separarlo todavía en varios contextos o servicios.

El hecho de que viva dentro de `features/auth` es aceptable mientras continúe siendo el único módulo que centraliza la autenticación y no provoque dependencias problemáticas.

---

# Login

### Flujo

```text
AuthForm
   ↓
useLoginMutation
   ↓
authService.login()
   ↓
POST /auth/login
   ↓
AuthContext.login()
   ↓
GET /auth/me/topnav
   ↓
estado autenticado
   ↓
notificación + navegación
```

### Responsabilidades del formulario

`AuthForm` coordina:

* modo login/registro;
* React Hook Form;
* selección del schema;
* submit;
* render de los campos correspondientes;
* estados de error.

`useLoginMutation` coordina la operación asíncrona y sus efectos posteriores.

---

# Registro

### Flujo actual

```text
AuthForm
   ↓
useRegisterMutation
   ↓
POST /auth/register
   ↓
POST /auth/login
   ↓
AuthContext.login()
   ↓
GET /auth/me/topnav
   ↓
estado autenticado
   ↓
notificación + navegación
```

El registro realiza actualmente tres operaciones de red antes de considerar completada la autenticación.

Esto no debe considerarse automáticamente un error de frontend: depende de si el contrato actual del backend requiere registro y autenticación como operaciones separadas.

Sí constituye un punto a revisar posteriormente porque introduce más latencia y más puntos de fallo.

---

# Hooks

## `useAuthMutations.ts`

Actualmente contiene `useLoginMutation`.

Responsabilidad:

* ejecutar login;
* sincronizar el `AuthContext`;
* limpiar caché cuando corresponda;
* mostrar notificaciones;
* resolver la redirección posterior.

No es un wrapper trivial: contiene suficiente orquestación como para justificar el hook.

### Mejora pendiente

Parte de la lógica posterior al login se repite con `useRegisterMutation`.

Debe revisarse si existe una forma sencilla de centralizar esa fase sin crear una abstracción innecesaria.

---

## `useRegisterMutation.ts`

Responsabilidad:

* ejecutar registro;
* realizar auto-login;
* sincronizar el `AuthContext`;
* limpiar caché;
* notificar;
* navegar.

Contiene más lógica que una simple llamada al service, por lo que mantenerlo como hook independiente es razonable.

### Mejora pendiente

Comparte parte del flujo posterior a autenticación con `useLoginMutation`.

También existe una diferencia de tratamiento de errores cuando el auto-login genera un `Error` manual frente a los errores procedentes de Axios.

Debe revisarse cuando se vea el contrato de errores global de la aplicación.

---

## `useBillConfetti.ts`

Responsabilidad exclusiva:

* efecto visual de confeti;
* listeners de interacción;
* creación/reutilización de elementos;
* animación;
* soporte de `prefers-reduced-motion`.

No contiene lógica de autenticación.

La existencia de este hook está justificada porque mantiene fuera del componente la complejidad del efecto visual.

---

# Service

## `auth.service.ts`

Contiene actualmente las operaciones HTTP de:

```text
login()
register()
```

El service no realiza transformación ni orquestación.

Su función principal es centralizar los endpoints y evitar que los hooks dependan directamente del cliente HTTP.

### Valoración

Es una abstracción fina.

No contiene lógica de negocio ni lógica compleja.

No debe considerarse automáticamente innecesaria: debe valorarse en conjunto con la convención utilizada por el resto de features.

Si todas las features utilizan services equivalentes para encapsular su acceso HTTP, puede mantenerse por consistencia.

Si otros módulos terminan eliminando esta capa y utilizan directamente una capa `api`, `auth.service.ts` sería candidato a simplificación.

---

# Schemas

## `auth.schema.ts`

Contiene las validaciones de formulario de login y registro.

Las validaciones frontend tienen principalmente una responsabilidad de UX:

* campos obligatorios;
* longitud mínima;
* formato de email;
* presencia de al menos un identificador.

El backend continúa siendo la fuente de verdad para:

* credenciales válidas;
* existencia/uniqueness de usuarios;
* reglas definitivas de contraseña;
* autorización;
* creación de sesión.

No se debe eliminar una validación frontend únicamente porque también exista en backend.

---

# Types

Contiene contratos específicos de auth como:

```text
LoginRequest
RegisterUserRequest
LoginResponse
```

### Mejora pendiente

Revisar posteriormente:

* alineación entre `RegisterUserRequest` y el estado real del formulario;
* utilidad real de `LoginResponse`;
* duplicación innecesaria entre schemas y tipos manuales.

No crear tipos separados únicamente por tener una capa de tipos: deben aportar un contrato real.

---

# Componentes

## `AuthForm.tsx`

Componente orquestador del formulario.

Es responsable de:

* seleccionar modo;
* crear el formulario;
* seleccionar schema;
* ejecutar mutaciones;
* renderizar los campos correspondientes.

Su tamaño y responsabilidad son razonables actualmente.

---

## `PasswordField.tsx`

Componente reutilizable dentro de login y registro.

Responsabilidad:

* campo de contraseña;
* mostrar/ocultar contraseña;
* integración con React Hook Form.

### Mejora detectada

Existe validación duplicada:

```text
Zod schema
+
reglas propias de React Hook Form
```

Debe existir una única fuente de reglas de validación del formulario siempre que sea posible.

La validación UX debe concentrarse en el schema y React Hook Form debería encargarse de integrarlo.

---

## `LoginFields.tsx`

Componente presentacional para los campos específicos de login.

Es pequeño, pero mantiene separada la presentación del formulario.

No se considera necesario fusionarlo mientras la separación mejore la legibilidad.

---

## `RegisterFields.tsx`

Componente presentacional de los campos de registro.

Incluye la lógica de presentación necesaria para mostrar la condición de identificador.

Su responsabilidad es clara.

---

## `AuthToggle.tsx`

Permite cambiar entre login y registro.

Responsabilidad única y clara.

### Mejora pendiente

Revisar si debe bloquearse durante una mutación pendiente para evitar cambiar de formulario mientras existe una petición activa.

---

## `AuthHeader.tsx`

Contiene marca y elementos de configuración de idioma/tema.

Su dependencia de componentes compartidos está justificada porque idioma y tema son funcionalidades globales que también tienen sentido antes de iniciar sesión.

---

## `AuthFooter.tsx`

Componente puramente presentacional.

No se detecta ninguna responsabilidad excesiva.

---

## `InfoSection`

Contiene contenido informativo de la pantalla pública:

* primeros pasos;
* novedades.

Este contenido está relacionado con la página de acceso pero no con autenticación propiamente dicha.

No supone un problema mientras siga siendo exclusivo de esta pantalla.

---

## `BackgroundDecoration.tsx`

Decoración visual.

No contiene lógica de negocio.

---

## `BillConfetti.tsx`

Componente visual que delega la complejidad del efecto en `useBillConfetti`.

La separación es adecuada.

---

# Dependencias con shared

Dependencias consideradas razonables:

```text
shared/api
    ↓
cliente HTTP común

shared/components/core
    ↓
primitivas UI

shared/components/LanguageSelector
shared/components/ThemeSelector
    ↓
funcionalidades globales

shared/utils/notifications
    ↓
feedback al usuario

shared/types/api
    ↓
errores API comunes
```

### Punto a revisar

`shared/utils/sessionCache` merece una revisión transversal.

Dentro de `auth` se observa escritura y limpieza, pero no está claro todavía qué consumidores externos dependen de ella.

Antes de eliminarla hay que comprobar todos sus usos en el proyecto.

---

# Mejoras detectadas

## ALTO

### 1. Flujo de registro excesivamente orquestado

Actualmente:

```text
register
→ login
→ fetch current user
```

Debe comprobarse si el backend puede proporcionar una respuesta que permita simplificar el flujo.

No modificar desde el frontend sin revisar primero el contrato del backend.

### 2. Duplicación del post-login

`useLoginMutation` y `useRegisterMutation` comparten parte de:

```text
sincronización de sesión
limpieza de caché
notificación
navegación
```

Debe revisarse si puede reducirse la duplicación sin introducir otra capa innecesaria.

### 3. Validación duplicada de contraseña

`PasswordField` añade reglas propias además del schema Zod.

Debe eliminarse la duplicación y mantener una única fuente de reglas.

---

## MEDIO

### 4. `sessionCache` necesita justificación

Se escribe y limpia desde `AuthContext`, pero su utilidad no está clara a partir del módulo.

Revisar todos sus consumidores antes de decidir si conservarla o eliminarla.

### 5. `AuthContext` contiene varias responsabilidades relacionadas

No es necesario dividirlo inmediatamente.

Primero debe comprobarse si la complejidad real sigue siendo manejable y si existe duplicación con otras partes del frontend.

### 6. `queryClient.clear()` aparece en varios puntos

El login, registro y logout intervienen sobre la caché global.

Debe revisarse si todas esas limpiezas son necesarias o si algunas son redundantes.

### 7. Contrato de tipos de registro

Revisar que `RegisterUserRequest` represente exactamente los datos que puede generar el formulario.

### 8. `LoginResponse` tiene uso limitado

Revisar si aporta un contrato necesario o si existe únicamente por tipar una respuesta que actualmente no se consume.

### 9. Errores de registro

Revisar la diferencia entre errores Axios y errores manuales generados durante el auto-login.

---

## BAJO

### 10. `useAuthMutations.ts` contiene una única mutación

El nombre en plural puede revisarse por claridad, aunque no constituye un problema arquitectónico.

### 11. `AuthToggle` puede cambiar de modo durante una petición

Revisar UX y comportamiento real.

### 12. Algunos componentes son muy pequeños

`LoginFields` y otros componentes pequeños pueden mantenerse mientras mejoren la lectura.

No se deben fusionar únicamente para reducir el número de archivos.

---

# Elementos que se consideran correctos

* `AuthContext` como punto global de sesión.
* React Query para mutaciones asíncronas.
* React Hook Form + Zod para formularios.
* Separación de login y registro.
* `PasswordField` como componente independiente.
* `useBillConfetti` como aislamiento de lógica visual.
* Cliente HTTP compartido.
* Componentes UI compartidos.
* Sanitización de redirecciones.
* No reutilizar datos cacheados para declarar automáticamente una sesión válida ante un fallo de red.
* Lazy loading de la página de autenticación.
* Mantener la lógica de presentación separada de las operaciones HTTP.

# Decisiones que no justifican cambios por ahora

No se considera necesario:

* mover `AuthContext` a `app` inmediatamente;
* eliminar `auth.service.ts` sin comparar con el resto de features;
* crear nuevas capas para “ordenar” el módulo;
* fusionar todos los componentes pequeños;
* eliminar validaciones frontend porque también existan en backend;
* eliminar `useRegisterMutation` o `useLoginMutation`;
* introducir otro sistema de gestión de estado.

# Relación con `app`

`ProtectedRoute` y `PublicRoute` utilizan `AuthContext`, por lo que `auth` constituye una dependencia transversal de la aplicación.

Esto es una característica normal del módulo y no implica que deba salir de `features/auth`.

La decisión de moverlo solo tendría sentido si aparecen problemas reales de dependencias o ciclos.

# Pendiente de comprobación

Antes de hacer cambios relevantes en `auth`, comprobar:

1. Todos los usos de `sessionCache`.
2. Todos los usos de `sessionId`.
3. Todas las llamadas a `queryClient.clear()`.
4. Contrato real de `/auth/register`.
5. Contrato real de `/auth/login`.
6. Contrato y consumidores de `/auth/me/topnav`.
7. Sistema global de errores API.
8. Imports reales de `auth` desde el resto de la aplicación.

---

## Categories

### Responsabilidad

El feature `categories` permite al usuario:

* consultar sus categorías;
* buscar categorías por nombre;
* navegar por categorías mediante paginación;
* crear categorías;
* editar categorías;
* eliminar categorías;
* visualizar el progreso presupuestario asociado a las categorías.

La responsabilidad principal del módulo es la gestión y presentación de categorías.

La visualización del progreso presupuestario introduce además una segunda responsabilidad relacionada con datos de gasto y presupuesto que debe mantenerse bajo revisión, ya que actualmente el frontend realiza parte del cálculo.

---

# Estructura

```text
features/categories/
├── constants.ts
├── components/
│   ├── AddCategoryDialog.tsx
│   ├── CategoryRow.tsx
│   ├── CategoryTable.tsx
│   ├── CategoryTableSkeleton.tsx
│   ├── ColorPicker.tsx
│   ├── ConfirmDeletionDialog.tsx
│   └── EditCategoryDialog.tsx
├── hooks/
│   └── useCategoryMutation.ts
├── pages/
│   └── page.tsx
└── types/
    └── index.ts
```

No existe actualmente una capa `services` específica de `categories`.

Las operaciones HTTP de escritura se realizan desde `useCategoryMutation`, mientras que la consulta de gasto por categoría se realiza directamente desde `CategoryTable`.

---

# Responsabilidades

## Responsabilidades propias de categories

* listado de categorías;
* búsqueda;
* paginación;
* creación;
* edición;
* eliminación;
* selección de color;
* presentación de estados de carga;
* presentación de diálogos de edición/eliminación;
* feedback visual de las operaciones.

## Responsabilidades delegadas a shared

* cliente HTTP;
* consulta reutilizable de categorías;
* componentes visuales;
* notificaciones;
* formato monetario;
* paginación/layout;
* traducciones y utilidades comunes.

---

# Estado del módulo

## Server state

* categorías;
* gastos por categoría y periodo;
* estado de las mutations.

El listado principal de categorías se gestiona mediante `useCategories`.

La información de gasto por periodo se obtiene mediante una query independiente desde `CategoryTable`.

No se observa una copia de la lista de categorías en `useState`, por lo que no existe una duplicación evidente del server state principal.

## UI state

`page.tsx`:

* periodo seleccionado.

`CategoryTable.tsx`:

* término de búsqueda;
* categoría seleccionada para edición;
* categoría seleccionada para eliminación.

`AddCategoryDialog.tsx`:

* estado del formulario;
* estado de apertura cuando funciona en modo no controlado.

## Form state

Los diálogos de creación y edición mantienen el formulario mediante `useState`.

No se utiliza React Hook Form ni Zod en este feature.

## Derived state

`CategoryTable` calcula:

* gastos por categoría;
* presupuesto ajustado al periodo;
* porcentaje de gasto;
* estado de presupuesto excedido.

Esta parte merece especial atención porque parte de ella puede representar lógica de negocio y no únicamente presentación.

---

# Listado

## Flujo

```text
CategoryPage
    ↓
CategoryTable
    ↓
useCategories
    ↓
API
    ↓
React Query
    ↓
CategoryRow
```

`page.tsx` mantiene únicamente el periodo seleccionado y delega el listado a `CategoryTable`.

`CategoryTable` mantiene búsqueda y selección de categorías para las operaciones posteriores.

---

# Búsqueda y paginación

La búsqueda se mantiene en `CategoryTable` mediante `searchTerm`.

La lógica de consulta y paginación está delegada a `shared/hooks/useCategories`.

El módulo no mantiene manualmente en estado la lista de categorías ni implementa su propia gestión de paginación.

### Pendiente de comprobar

Debe revisarse `shared/hooks/useCategories.ts` para confirmar:

* query key;
* relación con `useServerPagination`;
* comportamiento al cambiar búsqueda;
* reinicio de página;
* manejo de errores;
* configuración de cache.

No se debe decidir si esta abstracción está correctamente ubicada hasta comprobar sus consumidores reales.

---

# Creación

## Flujo

```text
AddCategoryDialog
    ↓
useCategoryMutation
    ↓
POST /categories
    ↓
invalidateQueries
```

`AddCategoryDialog` mantiene localmente:

* nombre;
* color;
* presupuesto;
* mensaje de error.

La mutación se encarga de:

* petición HTTP;
* invalidación de cache;
* notificaciones.

## Validación actual

El nombre se comprueba manualmente mediante `trim()`.

El presupuesto se transforma antes de enviarse.

No existe actualmente una validación centralizada común entre creación y edición.

### Mejora detectada

La validación de creación no refleja completamente las reglas del backend.

Debe alinearse con:

* nombre obligatorio;
* máximo 20 caracteres;
* presupuesto no negativo.

No es necesario introducir Zod únicamente por uniformidad: primero debe determinarse si una validación manual sencilla es suficiente para este feature.

---

# Edición

## Flujo

```text
CategoryRow
    ↓
CategoryTable
    ↓
EditCategoryDialog
    ↓
useCategoryMutation
    ↓
PUT /categories/{id}
```

`CategoryTable` mantiene la categoría seleccionada y utiliza el `id` como referencia.

`EditCategoryDialog` mantiene localmente:

* nombre;
* color;
* presupuesto;
* error.

## Problema detectado

La edición utiliza `mutate()` y cierra el diálogo inmediatamente después.

Esto significa que el diálogo no espera a conocer el resultado de la operación.

Consecuencias:

* el usuario pierde el contexto del formulario si falla la petición;
* el `try/catch` del componente no representa correctamente el error de la mutation;
* el error termina gestionándose principalmente mediante el `onError` del hook.

### Mejora

Revisar el flujo para que el cierre del diálogo ocurra cuando la operación haya terminado correctamente, manteniéndolo abierto en caso de error.

---

# Eliminación

## Flujo

```text
CategoryRow
    ↓
ConfirmDeletionDialog
    ↓
useCategoryMutation
    ↓
DELETE /categories/{id}
    ↓
invalidateQueries
```

El frontend no modifica `active` directamente.

La eliminación lógica es responsabilidad del backend.

Desde la perspectiva de la UI, la categoría desaparece posteriormente de la lista porque las consultas se vuelven a validar.

Esto es correcto: el frontend no necesita reproducir el mecanismo de soft delete.

### Mejora detectada

Existe más de un punto de manejo de errores:

* `onError` de la mutation;
* `catch` adicional en `CategoryTable`.

Debe evitarse mostrar dos notificaciones para la misma operación.

---

# `useCategoryMutation.ts`

Contiene las mutaciones de:

* creación;
* actualización;
* eliminación.

Además gestiona:

* peticiones HTTP;
* estados de pending;
* invalidación de queries;
* notificaciones;
* extracción de mensajes de error.

No es un simple wrapper de Axios: utiliza React Query y contiene comportamiento de cache y feedback.

Por tanto, **mantenerlo como hook está justificado**.

No se considera necesario crear además un `CategoryService` salvo que aparezca una necesidad real.

### Mejora pendiente

Revisar el número y alcance de las queries que invalida.

La invalidación amplia puede estar perfectamente justificada si los datos de categorías afectan a:

* dashboard;
* transacciones;
* presupuestos;
* informes.

No debe reducirse sin comprobar primero qué consultas dependen realmente de categorías.

---

# Comunicación con backend

Las mutations utilizan el cliente HTTP compartido:

```text
shared/api/axiosClient
```

No existe una cadena innecesaria de:

```text
component
→ service
→ repository
→ api
```

La estructura actual es relativamente directa:

```text
component
    ↓
hook / query
    ↓
apiClient
    ↓
backend
```

Esto es positivo y no debería complicarse sin una razón concreta.

---

# Tipos

`types/index.ts` contiene los tipos de categoría y de los datos asociados al gasto.

Algunos tipos reflejan directamente la respuesta del backend.

### Importante

No considero automáticamente problemático que `Category` contenga campos que una pantalla concreta no utiliza.

Los tipos TypeScript se eliminan en runtime, por lo que tener:

```text
userId
createdAt
modifiedAt
active
```

en una interfaz no aumenta el bundle ni implica necesariamente una mala arquitectura.

La cuestión relevante es determinar si:

* son realmente datos del contrato de API;
* se comparten con otros consumidores;
* existe un modelo de UI distinto que justifique separación.

Por tanto, no eliminar campos del tipo únicamente porque `CategoryRow` no los utilice.

### Pendiente

Revisar el contrato real de las respuestas de categories y todos los consumidores antes de decidir si conviene separar:

```text
API response type
```

de:

```text
UI model
```

---

# Validación

Actualmente la validación es manual y está distribuida entre creación y edición.

### Backend

Reglas conocidas:

* nombre obligatorio;
* máximo 20 caracteres;
* color hexadecimal;
* presupuesto nulo o no negativo.

### Frontend

Se anticipan algunas de estas reglas, pero no de forma uniforme.

Problemas observados:

* creación no refleja claramente el máximo de nombre;
* edición utiliza `maxLength=50` en lugar de 20;
* la validación de presupuesto difiere entre creación y edición.

### Mejora

Centralizar las reglas de validación del formulario para evitar que Add y Edit evolucionen de manera distinta.

La solución concreta puede ser:

* un schema común;
* funciones de validación compartidas;
* o una implementación manual común.

No es necesario añadir una librería nueva únicamente para solucionar este problema.

---

# Componentes

## `CategoryTable`

Es actualmente el componente con mayor responsabilidad.

Gestiona:

* listado;
* búsqueda;
* selección de categoría;
* eliminación;
* consulta de gastos;
* cálculo de presupuesto;
* cálculo de porcentaje;
* composición de diálogos;
* loading/empty state;
* animaciones.

### Problema principal

No es únicamente una tabla presentacional.

Contiene lógica de datos y lógica de cálculo que podrían separarse.

Especialmente preocupante:

```text
consulta de gastos
+
presupuesto ajustado por periodo
+
porcentaje gastado
```

### Mejora

Separar las responsabilidades en función de lo que descubramos al contrastarlo con el backend.

No significa necesariamente crear cinco hooks y tres services.

El objetivo es conseguir algo conceptualmente más cercano a:

```text
CategoryTable
    ↓
datos ya preparados
    ↓
CategoryRow
```

sin convertir la feature en una arquitectura de muchas capas.

---

## `CategoryRow`

Responsabilidad principalmente presentacional.

Recibe:

* categoría;
* gasto;
* presupuesto;
* porcentaje;
* callbacks de edición/eliminación.

La separación es adecuada.

No contiene lógica compleja.

---

## `CategoryTableSkeleton`

Responsabilidad única de loading.

Correcto.

---

## `ColorPicker`

Responsabilidad única y reutilizada por creación y edición.

Correcto.

No parece necesario simplificarlo.

---

## `ConfirmDeletionDialog`

Responsabilidad única:

* pedir confirmación;
* informar del elemento;
* ejecutar callback.

Correcto.

---

## `AddCategoryDialog`

Componente razonablemente contenido.

Gestiona formulario y creación.

### Mejora

Alinear validación con edición y backend.

Revisar si la capacidad controlado/no controlado aporta realmente valor, dado que el consumidor visible utiliza la versión no controlada.

No eliminarla sin comprobar otros consumidores.

---

## `EditCategoryDialog`

Responsabilidad razonable.

### Mejora principal

No cerrar el diálogo antes de conocer el resultado de la mutation.

---

# Presupuesto y gastos

Esta es actualmente la parte más delicada del módulo.

`CategoryTable` consulta datos procedentes de:

```text
/dashboard/categories
```

y los cruza con las categorías.

Posteriormente calcula:

* gasto;
* presupuesto ajustado;
* porcentaje.

Esto crea una dependencia conceptual entre `categories` y datos de dashboard.

No es necesariamente incorrecto que una pantalla de categorías consulte un endpoint especializado para obtener información adicional.

La cuestión que debemos resolver es:

> ¿El frontend está calculando una regla que ya debería proporcionar el backend?

Especialmente debe revisarse:

* cálculo semanal;
* cálculo mensual;
* cálculo anual;
* porcentaje gastado;
* tratamiento de presupuesto cero.

### Antes de refactorizar

Hay que contrastar esta lógica con el backend encargado de proporcionar los datos de gasto/presupuesto.

---

# Asociación de gastos

El frontend asocia los gastos a las categorías utilizando el nombre de categoría.

Esto es una señal a revisar.

Idealmente, cuando existe una relación entre recursos, debería existir un identificador estable, por ejemplo:

```text
categoryId
```

en lugar de utilizar:

```text
category.name
```

como clave de unión.

No significa necesariamente que el código actual sea incorrecto: primero hay que comprobar el contrato real del endpoint `/dashboard/categories` y si el nombre está garantizado como identificador válido para este propósito.

---

# Dependencias con shared

## Dependencias claramente justificadas

* `shared/api/axiosClient`
* `shared/components/core/*`
* `shared/utils/notifications`
* `shared/utils/i18nFormat`
* `shared/types/api`
* componentes globales de layout.

## Dependencias pendientes de revisar

### `shared/hooks/useCategories`

Debe comprobarse si:

* se utiliza fuera de categories;
* contiene lógica genérica;
* está acoplado al endpoint de categorías.

Si únicamente sirve a `categories`, podría pertenecer a la propia feature.

Si es consumido por dashboard, transactions u otras features, mantenerlo en `shared` puede estar justificado.

### `shared/hooks/useServerPagination`

Debe revisarse junto con `useCategories` para determinar qué parte de paginación es realmente genérica.

---

# Dependencias con otras features

No se observa una dependencia directa de código hacia otras features.

Sí existe una dependencia conceptual con datos de dashboard mediante:

```text
/dashboard/categories
```

y existen referencias a namespaces de traducción de otras features.

Las traducciones cruzadas no constituyen por sí mismas un problema arquitectónico: primero hay que comprobar si esas claves representan realmente contenido específico de otra feature o simplemente vocabulario reutilizado.

---

# Comparación con backend

## Correctamente delegado al backend

* ownership de categoría;
* persistencia;
* búsqueda;
* paginación;
* existencia de recursos;
* eliminación lógica;
* autoridad final de las validaciones.

## Validación duplicada por UX

Es correcto anticipar en frontend:

* nombre obligatorio;
* límite de longitud;
* presupuesto no negativo;
* formato de color.

Estas validaciones no sustituyen las del backend.

## Punto a revisar

La lógica de:

```text
presupuesto por periodo
+
porcentaje gastado
```

puede constituir lógica de negocio duplicada.

Debe contrastarse con el backend antes de decidir dónde debería realizarse el cálculo.

---

# Problemas detectados

## ALTO

### 1. `CategoryTable` concentra demasiadas responsabilidades

Actualmente combina:

* server state;
* consulta adicional de gastos;
* cálculos;
* búsqueda;
* selección;
* diálogos;
* presentación.

Es el principal candidato a simplificación estructural del módulo.

### 2. Cálculo de presupuesto/gasto en frontend

`getAdjustedBudgetLimit` y `getSpentPercentage` deben contrastarse con el backend.

El objetivo es evitar que una regla de negocio exista simultáneamente en backend y frontend.

### 3. Validación diferente entre Add y Edit

Las restricciones del formulario no son coherentes:

* máximo de nombre diferente;
* presupuesto validado de forma distinta.

Debe existir una política común.

### 4. Edición cierra el diálogo antes del resultado

El flujo debería distinguir entre:

```text
mutation pendiente
→ mutation exitosa
→ cerrar
```

y:

```text
mutation falla
→ mantener contexto
→ mostrar error
```

---

## MEDIO

### 5. Posible acoplamiento a endpoint de dashboard

La tabla de categorías consume datos de un endpoint conceptualmente asociado a dashboard.

Debe determinarse si este endpoint representa realmente un recurso reutilizable o si la UI está acoplando dos contextos que deberían estar mejor separados.

### 6. Asociación por nombre de categoría

El cruce de gastos mediante `name` merece revisión.

Un identificador estable sería normalmente más robusto.

### 7. Manejo de errores inconsistente

Existe mezcla de:

* `onError`;
* `catch`;
* errores inline;
* toast.

Debe establecerse una estrategia consistente.

### 8. Query de gastos sin estado de error visible

Si falla la consulta de gasto, la UI puede presentar datos de presupuesto incompletos sin explicar el problema.

### 9. `useCategories` pendiente de revisión

No debe modificarse hasta comprobar su implementación y consumidores.

### 10. Invalidación de queries pendiente de revisión

No asumir que es demasiado amplia hasta identificar exactamente las queries invalidadas y por qué dependen de categorías.

---

## BAJO

### 11. AddCategoryDialog soporta controlado/no controlado

Revisar si la capacidad controlada tiene consumidores reales.

### 12. Inconsistencias menores de naming

* `useCategoryMutation.ts` vs `useCategoryMutations`;
* posibles nombres heredados.

### 13. Mensajes hardcoded

Revisar cualquier error que no utilice i18n.

### 14. Animaciones de filas

No son un problema arquitectónico, pero pueden evaluarse posteriormente dentro de la estrategia global de bundle/performance.

---

# Elementos que están bien y conservaría

* TanStack Query como fuente del server state.
* Ausencia de una copia manual de las categorías en `useState`.
* `useCategoryMutation` como punto único de las mutations.
* Cliente HTTP compartido.
* `CategoryRow` como componente principalmente presentacional.
* `ColorPicker` independiente y reutilizado.
* `ConfirmDeletionDialog` independiente.
* `CategoryTableSkeleton`.
* No enviar `userId` desde el formulario.
* Delegar la eliminación lógica completamente al backend.
* No crear un `CategoryService` simplemente para añadir otra capa.
* No introducir React Hook Form/Zod automáticamente solo por homogeneidad.

---

# Pendiente de comprobar antes de refactorizar

1. `shared/hooks/useCategories.ts`
2. `shared/hooks/useServerPagination.ts`
3. Todos los consumidores de `useCategories`
4. Todas las query keys invalidadas por `useCategoryMutation`
5. Contrato exacto de `/dashboard/categories`
6. Módulo backend que proporciona `/dashboard/categories`
7. Cálculo equivalente de presupuesto/gasto en backend
8. Contrato exacto de `CategoryExpense`
9. Todos los consumidores de los tipos de `categories`

Estas comprobaciones son necesarias antes de eliminar abstracciones o mover responsabilidades.

---

## Dashboard

### Responsabilidad

El feature `dashboard` actúa como panel de control principal de la aplicación.

Su responsabilidad principal es:

* consultar información financiera agregada;
* presentar métricas y tendencias;
* mostrar resúmenes de categorías, presupuestos y transacciones;
* ofrecer acciones rápidas relacionadas con otras features;
* proporcionar operaciones de importación/exportación desde la interfaz del dashboard.

El dashboard funciona principalmente como **compositor y presentador de información**, no como propietario de las entidades que muestra.

No debería convertirse en una segunda implementación de:

* categorías;
* transacciones;
* inversiones;
* objetivos;
* reglas financieras de otras features.

---

# Estructura

```text id="mkc14y"
features/dashboard/
├── constants.ts
├── utils.ts
├── components/
│   ├── BudgetBars.tsx
│   ├── CategoryDonut.tsx
│   ├── ExportCard.tsx
│   ├── ExportModal.tsx
│   ├── HistoryChart.tsx
│   ├── ImportModal.tsx
│   ├── QuickActions.tsx
│   └── RecentTransactions.tsx
├── hooks/
│   ├── useDashboardQueries.ts
│   └── useExport.ts
├── pages/
│   └── page.tsx
└── types/
    └── index.ts
```

La estructura es sencilla y no contiene capas innecesarias de `services`, `repositories` o similares.

Esto es positivo.

---

# Responsabilidades reales

## Información financiera

El dashboard presenta:

* métricas financieras globales;
* gastos por categoría;
* presupuestos;
* evolución histórica;
* transacciones recientes.

## Acciones

También ofrece:

* creación rápida de transacciones;
* creación rápida de categorías;
* importación;
* exportación.

## Estado de UI

Gestiona:

* periodo seleccionado;
* apertura/cierre de diálogos;
* estado visual de determinadas acciones.

## Presentación

Incluye:

* tarjetas métricas;
* gráficos;
* barras de presupuesto;
* listado resumido de transacciones.

---

# Composición frente a lógica propia

El dashboard debe distinguir claramente entre:

### A. Datos específicos de dashboard

Datos agregados o resumidos preparados para la vista.

### B. Datos de otras features

Por ejemplo:

* categorías;
* transacciones recientes;
* presupuestos.

Que el dashboard muestre estos datos no significa que pase a ser dueño de esas entidades.

### C. Transformaciones de presentación

Ejemplos legítimos:

* adaptar datos a la estructura de un gráfico;
* formatear fechas;
* formatear monedas;
* construir labels;
* ordenar datos para una visualización.

### D. Reglas de negocio

Deben revisarse cuidadosamente.

El hecho de que un cálculo aparezca en un componente financiero no significa automáticamente que tenga que estar en backend. La pregunta es si existen reglas de negocio no triviales que el backend deba garantizar.

---

# Página

## `page.tsx`

La página actúa principalmente como orquestador.

Responsabilidades:

* composición del layout;
* consumo agrupado de las queries;
* mantenimiento del estado de los modales;
* paso de callbacks a `QuickActions`;
* composición de las distintas secciones.

### Estado

#### Server state

No se gestiona directamente en la página.

Se delega principalmente a `useDashboardQueries`.

#### UI state

Incluye estados como:

* `isImportOpen`;
* `isExportOpen`;
* `isAddTransactionOpen`;
* `isAddCategoryOpen`.

#### Form state

No pertenece a la página.

Los formularios viven en sus respectivos modales.

### Valoración

`page.tsx` tiene una responsabilidad razonable para una página de composición.

No se observa una necesidad inmediata de extraer un "DashboardController" o una capa intermedia similar.

---

# `useDashboardQueries.ts`

Agrupa las queries utilizadas por el dashboard.

Su objetivo es evitar que `page.tsx` tenga que coordinar individualmente todas las consultas.

Según la auditoría:

* histórico;
* categorías;
* presupuestos;
* transacciones recientes.

### Valoración

La existencia de un hook agrupador está justificada si únicamente:

* compone queries;
* proporciona una interfaz cómoda a la página;
* no contiene reglas de negocio.

No es necesario dividirlo simplemente porque contenga varias queries.

### Pendiente

Comprobar su implementación para determinar:

* parámetros de periodo;
* query keys;
* endpoints;
* `staleTime`;
* queries habilitadas/deshabilitadas;
* transformaciones;
* dependencia entre consultas.

Especialmente debe verificarse cómo llega el `period` seleccionado a las queries.

---

# Periodos

El dashboard utiliza un periodo que afecta a la información mostrada.

Debe quedar claro el flujo:

```text id="2kzjtr"
PeriodSelector / Header
        ↓
Dashboard page
        ↓
useDashboardQueries
        ↓
queries
        ↓
backend
```

### Mejora pendiente

Comprobar que el periodo forme parte de:

* parámetros de las peticiones cuando corresponda;
* query keys cuando cambie el resultado.

Si el periodo cambia el resultado de una query pero no cambia su key ni sus parámetros, sí existiría un problema funcional.

No debe marcarse como bug hasta comprobar la implementación real.

---

# React Query

El dashboard utiliza TanStack Query como mecanismo de server state.

La estrategia aparente es ejecutar varias queries independientes en paralelo.

Esto es apropiado para una página compuesta por widgets independientes siempre que no exista una dependencia real entre consultas.

## Evitar duplicación

Es normal que dashboard y otras features soliciten información relacionada.

No debe considerarse automáticamente duplicación problemática.

Hay que distinguir entre:

```text id="w9y0zu"
GET /categories
```

y:

```text id="3t99cb"
GET /dashboard/categories
```

aunque ambos contengan categorías.

Podrían representar contratos distintos:

* recurso de dominio;
* dato agregado preparado para dashboard.

### Pendiente

Comparar las query keys y endpoints de dashboard con:

* categories;
* transactions;
* inversiones;
* etc.

---

# `useExport.ts`

Contiene la lógica necesaria para descargar archivos:

* petición HTTP;
* obtención del archivo;
* creación de `ObjectURL`;
* creación temporal de un enlace;
* descarga;
* liberación posterior del objeto.

Esta es lógica de infraestructura del navegador, no lógica financiera.

## Valoración

Mantenerla como hook está razonablemente justificado porque necesita interactuar con React y el DOM.

No debe moverse a `shared` únicamente porque sea genérica en abstracto.

### Mejora pendiente

Comprobar si existe realmente otra feature que necesite la misma funcionalidad.

Solo si aparece reutilización real tendría sentido extraer una utilidad/hook genérico de descarga.

---

# `constants.ts`

Contiene dos tipos principales de constantes.

## Constantes puramente visuales

* `CHART_COLORS`;
* `BUDGET_COLORS`.

Estas pertenecen claramente al dashboard o a su capa de presentación.

No contienen lógica de negocio.

## Configuración de exportación

* formatos;
* tipos de entidad;
* opciones de periodo.

También es apropiado que permanezcan cerca de la funcionalidad de exportación.

---

# `CATEGORY_ICONS`

Existe un mapa basado en nombres de categorías.

Conceptualmente:

```text id="7yq6k2"
nombre de categoría
        ↓
icono fijo
```

Ejemplo:

```text id="g9tmj7"
"Alimentación"
    ↓
icono X
```

### Problema

Las categorías son personalizables por el usuario.

Por tanto, asociar un icono mediante nombres concretos:

* depende de strings;
* depende de idioma;
* no funciona bien con categorías personalizadas;
* puede romperse al cambiar el nombre de una categoría;
* no escala a categorías creadas por usuarios.

Esto sí es una mala solución de representación.

### Importante

No se concluye automáticamente que el backend deba añadir un `icon`.

Primero hay que decidir si el producto quiere que:

* las categorías tengan icono propio;
* el icono sea puramente visual y genérico;
* exista una selección de icono;
* se utilicen iconos derivados únicamente para categorías predefinidas.

Si la intención es que cada categoría pueda tener un icono estable, entonces sí habría que estudiar un contrato explícito con backend.

Mientras tanto, el frontend debería tener un fallback neutro para categorías personalizadas.

---

# `utils.ts`

`formatDate` es una utilidad de presentación.

Su existencia está justificada mientras sea específica de la visualización del dashboard.

No existe motivo para moverla a `shared` únicamente porque sea una función pequeña.

Debe moverse solo si la misma transformación se necesita realmente en otras features.

---

# Componentes

## `BudgetBars.tsx`

Presenta información de presupuesto mediante barras.

Realiza operaciones simples de presentación, como calcular cuánto queda:

```text id="zkr1j9"
limit - spent
```

### Valoración

La resta por sí sola no debe considerarse lógica de negocio problemática.

Es una derivación trivial de datos ya proporcionados.

Solo sería necesario trasladarla al backend si:

* existen reglas adicionales;
* el cálculo depende de conceptos financieros complejos;
* backend y frontend están implementando interpretaciones diferentes.

---

## `CategoryDonut.tsx`

Responsabilidad:

* recibir datos;
* adaptarlos al gráfico;
* renderizar `DonutChart`.

Es un componente principalmente presentacional.

### Valoración

Correcto.

---

## `HistoryChart.tsx`

Responsabilidad:

* representar evolución temporal.

Debe evitar incluir cálculos financieros complejos.

La adaptación de los datos a la librería gráfica sí pertenece a presentación.

---

## `ExportCard.tsx`

Presenta la funcionalidad de exportación y actúa como punto de entrada visual.

Responsabilidad adecuada.

---

## `ExportModal.tsx`

Gestiona la configuración de la exportación:

* formato;
* tipo;
* periodo;
* ejecución.

La lógica de descarga está delegada al hook.

La separación es razonable.

---

## `ImportModal.tsx`

Gestiona:

* selección/arrastre del archivo;
* lectura;
* envío;
* estado de la operación;
* feedback.

### JSON.parse

Actualmente se utiliza `JSON.parse` antes del envío.

Esto no es necesariamente incorrecto.

Una comprobación sintáctica rápida puede mejorar UX.

El problema aparece únicamente con archivos suficientemente grandes como para bloquear apreciablemente el hilo principal.

Además, la validación estructural definitiva debe seguir estando en backend.

### Pendiente

Comprobar:

* tamaño máximo real de importación;
* formato recibido por backend;
* si se necesita parsear el archivo completo antes de enviarlo;
* límites actuales de infraestructura.

No eliminar `JSON.parse` automáticamente.

---

## `QuickActions.tsx`

Componente presentacional.

Recibe callbacks y ejecuta acciones.

No conoce detalles de negocio.

### Valoración

Muy buena separación.

Conviene conservar este patrón.

---

## `RecentTransactions.tsx`

Responsabilidad:

* representar transacciones recientes;
* formatear información;
* mostrar iconos y colores.

No hace fetch directamente.

Esto mantiene el componente relativamente sencillo.

### Punto a revisar

Utiliza `CATEGORY_ICONS` basado en nombre de categoría.

Por tanto, comparte el mismo problema de representación descrito anteriormente.

---

# Import / Export

## Export

Flujo conceptual:

```text id="4gp2j3"
ExportCard / ExportModal
        ↓
useExport
        ↓
apiClient
        ↓
archivo
        ↓
descarga navegador
```

La separación es razonable.

## Import

Flujo conceptual:

```text id="8v3zkn"
ImportModal
        ↓
File
        ↓
lectura / JSON.parse
        ↓
apiClient
        ↓
backend
```

La validación definitiva del contenido corresponde al backend.

El frontend puede realizar validaciones tempranas de UX:

* extensión;
* MIME;
* tamaño;
* JSON sintácticamente válido.

---

# QuickActions

`QuickActions` no contiene lógica propia de negocio.

Actúa como componente tonto:

```text id="u7a2qb"
callback
   ↓
ejecución
```

Esto es correcto y debería mantenerse.

No necesita conocer directamente las rutas si el padre ya le proporciona acciones.

---

# RecentTransactions

Es principalmente presentacional.

Recibe los datos y los transforma para mostrarlos.

### No debería hacer

* crear/eliminar transacciones;
* modificar categorías;
* ejecutar reglas financieras;
* gestionar directamente el estado de transactions.

El dashboard debe tratar estas transacciones como información resumida.

---

# Tipos

El dashboard tiene tipos específicos para los datos que consume.

Esto no es automáticamente duplicación.

Puede ser correcto tener:

```text id="o4hj43"
DashboardTransactionItem
```

aunque exista:

```text id="v8x7dd"
Transaction
```

en `transactions`, si el contrato del dashboard representa solo una proyección/resumen diferente.

### Regla

No compartir tipos solo para eliminar líneas de código.

Debe compartirse un tipo cuando representa realmente el mismo contrato.

### Pendiente

Comparar:

* `TransactionItem`;
* `CategoryData`;

con los contratos de:

* `transactions`;
* `categories`;
* backend dashboard.

Solo entonces decidir si existe duplicación real.

---

# Dependencias con otras features

El dashboard utiliza:

```text id="q0zmyc"
features/transactions
features/categories
```

para mostrar o abrir funcionalidades propias de esas áreas.

## Situación actual

`page.tsx` importa directamente:

* `NewTransactionDialog`;
* `AddCategoryDialog`.

Esto crea una dependencia directa de dashboard hacia la implementación de esos componentes.

### Problema

El dashboard conoce rutas internas de otras features:

```text id="y3h1d4"
dashboard
   ↓
features/transactions/components/dialogs/NewTransactionDialog
```

y:

```text id="g8g9v1"
dashboard
   ↓
features/categories/components/AddCategoryDialog
```

Eso hace que cambios internos de una feature puedan obligar a modificar dashboard.

### Mejora pendiente

No sustituir automáticamente esto por un event bus.

Primero debe valorarse una solución sencilla:

* exponer un componente público de la feature;
* mantener un pequeño punto de entrada de la feature;
* o mover la composición común a una capa superior si realmente es compartida.

El objetivo es reducir el conocimiento de estructura interna, no añadir otra infraestructura.

---

# Dependencias con shared

El uso de `shared` está mayoritariamente justificado.

Especialmente:

* layout;
* métricas;
* gráficos;
* diálogos;
* botones;
* API client;
* notificaciones.

La centralización de gráficos en `shared/components/core` evita repetir la configuración de la librería de visualización.

Esto es una buena decisión.

### Pendiente

Comprobar si algún componente compartido utilizado por dashboard arrastra dependencias innecesarias al chunk.

---

# Periodos y fechas

El dashboard maneja información condicionada por periodos.

Debe evitarse que los componentes gráficos o barras sean responsables de decidir reglas sobre:

* qué periodo representa una consulta;
* cómo debe calcularse un periodo financiero;
* qué fechas son contables.

Esas decisiones deben venir de la consulta o de datos ya preparados.

Sí es legítimo que la UI:

* formatee fechas;
* cree labels;
* convierta datos al formato requerido por un gráfico.

---

# Bundle y rendimiento

## Lazy loading

La página de dashboard se carga mediante `React.lazy`.

Esto permite mantener sus dependencias dentro de su chunk siempre que no sean importadas desde infraestructura global.

## Dependencias potencialmente relevantes

* librería de charts;
* Framer Motion;
* Radix utilizado por componentes;
* iconos.

No se debe asumir que por estar en `shared` formen parte del bundle inicial.

Hay que analizar el grafo real de imports.

## Import / Export

No introduce dependencias externas especialmente pesadas: utiliza APIs nativas del navegador para archivos/descarga.

## `ImportModal`

El posible coste relevante es de CPU por `JSON.parse` de archivos grandes, no por bundle.

---

# Problemas detectados

## ALTO

### 1. `CATEGORY_ICONS` depende del nombre de la categoría

**Problema:**

El icono se decide mediante nombres de categorías concretos.

**Consecuencia:**

No funciona de forma robusta con:

* categorías personalizadas;
* nombres modificados;
* diferentes idiomas.

**Qué comprobar antes de modificarlo:**

Determinar qué comportamiento de producto se desea para categorías personalizadas.

No asumir que la solución debe ser añadir un campo `icon` al backend.

---

### 2. Dependencia directa de dashboard hacia diálogos internos de otras features

**Problema:**

Dashboard importa directamente componentes internos de:

* `transactions`;
* `categories`.

**Consecuencia:**

Aumenta el acoplamiento estructural entre features.

**Qué comprobar antes de modificarlo:**

Determinar si estos componentes tienen consumidores adicionales y si podemos definir puntos de entrada más claros sin introducir infraestructura global innecesaria.

---

### 3. Verificar sincronización del periodo con `useDashboardQueries`

**Problema potencial:**

No queda claro en la auditoría cómo el periodo seleccionado llega a las queries.

**Consecuencia si realmente está mal:**

El dashboard podría mostrar datos del periodo anterior.

**Qué comprobar antes de modificarlo:**

Implementación exacta de:

* `page.tsx`;
* `useDashboardQueries`;
* Header/PeriodSelector;
* query keys.

---

# MEDIO

### 4. `CategoryDonut`/`RecentTransactions` dependen de `CATEGORY_ICONS`

**Problema:**

La presentación depende indirectamente de nombres concretos de categorías.

**Consecuencia:**

Las categorías personalizadas necesitan fallback.

**Qué comprobar antes de modificarlo:**

Definir estrategia de iconografía para categorías personalizadas.

---

### 5. Tipos del dashboard potencialmente duplicados

**Problema:**

`TransactionItem` y `CategoryData` podrían solaparse con tipos de otras features.

**Consecuencia:**

Mayor mantenimiento y posibles contratos divergentes.

**Qué comprobar antes de modificarlo:**

Comparar los tipos con los de:

* transactions;
* categories;
* respuestas específicas de dashboard.

---

### 6. `ImportModal` realiza `JSON.parse` en cliente

**Problema:**

El parseo es síncrono.

**Consecuencia:**

Puede bloquear el hilo si el archivo alcanza un tamaño relevante.

**Qué comprobar antes de modificarlo:**

Tamaño máximo real de archivos y formato del endpoint de importación.

---

### 7. `useExport` puede acabar siendo una utilidad transversal

**Problema:**

La lógica de descarga está dentro de dashboard.

**Consecuencia:**

Otras features podrían depender del dashboard si necesitan la misma mecánica.

**Qué comprobar antes de modificarlo:**

Buscar otros consumidores reales.

No moverla a `shared` mientras no exista reutilización.

---

### 8. Revisar amplitud de las queries del dashboard

**Problema:**

El dashboard puede estar pidiendo varias proyecciones que también consultan otras features.

**Consecuencia:**

Posibles peticiones redundantes, pero no confirmadas.

**Qué comprobar antes de modificarlo:**

Comparar endpoints y query keys reales.

---

# BAJO

### 9. Cálculos simples en componentes

Operaciones como:

```text id="qb8hyg"
limit - spent
```

no necesitan extraerse únicamente por tratarse de datos financieros.

Deben permanecer donde sean más claras salvo que aparezcan reglas adicionales.

---

### 10. `formatDate` dentro de dashboard

No es necesario convertirla en utilidad global salvo que tenga consumidores reales fuera del dashboard.

---

### 11. Constantes de gráficos en dashboard

`CHART_COLORS` y `BUDGET_COLORS` están correctamente acotadas mientras no sean utilizadas por otras features.

---

# Elementos que están bien y conservaría

* Dashboard como feature compositiva.
* `page.tsx` como orquestador.
* `useDashboardQueries` como agrupador de queries.
* React Query como gestor de server state.
* Queries independientes ejecutadas en paralelo cuando no existen dependencias entre ellas.
* `QuickActions` como componente puramente presentacional.
* Separación de `ImportModal` y `ExportModal`.
* `useExport` como encapsulación de la interacción con descarga.
* Gráficos presentacionales.
* Utilización de componentes gráficos compartidos.
* Utilización del cliente HTTP compartido.
* No crear una capa de `service` si únicamente duplicaría llamadas a API.
* No mover tipos a `shared` únicamente por evitar duplicación textual.
* No enviar cálculos triviales al backend solamente porque representen datos financieros.

# Pendiente de comprobación

1. Implementación completa de `useDashboardQueries`.
2. Cómo llega `period` a cada query.
3. Query keys reales.
4. Endpoints reales de dashboard.
5. Contratos del backend utilizados por dashboard.
6. Tipos de `transactions` y `categories` para comparar con los tipos locales.
7. Todos los consumidores de `useExport`.
8. Implementación de `ImportModal` y límite real de archivos.
9. Componentes de gráficos compartidos para determinar su coste real.
10. Todas las dependencias de `page.tsx` hacia otras features.

---

## Group Goals

### Responsabilidad

El feature `groupgoals` gestiona las metas económicas compartidas entre varias personas.

Incluye:

* creación y edición de metas;
* selección de meta;
* visualización del detalle;
* gestión de miembros;
* invitaciones;
* aceptación/rechazo mediante enlace;
* contribuciones;
* historial de contribuciones;
* distribución de contribuciones;
* progreso de la meta;
* permisos visuales asociados a los roles.

La feature es suficientemente cohesionada como para mantenerse como un único módulo.

Internamente existen varias responsabilidades relacionadas:

* metas;
* miembros;
* invitaciones;
* contribuciones;
* distribución.

No existe actualmente evidencia suficiente para separarlas en features distintas.

---

# Estructura

```text id="qg0g68"
features/groupgoals/
├── constants.ts
├── utils.ts
├── components/
│   ├── ContributionHistory.tsx
│   ├── DistributionCard.tsx
│   ├── GroupGoalDetail.tsx
│   ├── GroupGoalSelector.tsx
│   ├── MemberCard.tsx
│   └── dialogs/
│       ├── AddContributionDialog.tsx
│       ├── CreateGroupGoalDialog.tsx
│       ├── EditGoalNameDialog.tsx
│       ├── EditMemberDialog.tsx
│       ├── InviteMemberDialog.tsx
│       └── UpdateGoalDialog.tsx
├── hooks/
│   ├── useGroupGoalMutations.ts
│   ├── useGroupGoalQueries.ts
│   └── useInvitations.ts
├── pages/
│   ├── JoinGroupGoalPage.tsx
│   └── page.tsx
├── services/
│   └── invitations.service.ts
└── types/
    └── index.ts
```

La estructura general es razonable.

No existen capas artificiales de repositories o servicios para cada operación.

---

# Responsabilidades reales

## Gestión de metas

* listado;
* selección;
* detalle;
* creación;
* edición;
* eliminación.

## Gestión de miembros

* visualización;
* edición;
* eliminación;
* tratamiento visual de roles.

## Invitaciones

* creación de invitación;
* aceptación;
* rechazo;
* entrada mediante enlace.

## Contribuciones

* creación;
* historial;
* cálculo de progreso;
* distribución.

## Presentación

* progreso;
* distribución;
* miembros;
* historial;
* estados de loading;
* diálogos.

---

# Estado del módulo

## Server state

* lista de metas;
* detalle de la meta seleccionada;
* historial paginado de contribuciones;
* estado de las mutations.

React Query actúa como fuente principal del server state.

## UI state

Principalmente en `page.tsx`:

* meta seleccionada;
* diálogos abiertos;
* meta que se está editando;
* meta que se está eliminando;
* miembro que se está editando;
* miembro que se está eliminando.

En `JoinGroupGoalPage`:

* resultado del procesamiento de invitación;
* estados de éxito/error;
* estado de procesamiento.

## Form state

Cada diálogo mantiene su propio estado de formulario.

No se utiliza React Hook Form ni Zod.

## Derived state

La página calcula:

* total de contribuciones;
* porcentaje de progreso;
* contribución esperada por miembro;
* contribución real;
* porcentaje individual;
* total de porcentajes personalizados;
* comprobaciones relacionadas con el último administrador.

Parte de estos valores son simples datos derivados y parte pueden representar reglas de negocio.

---

# Página principal

## `pages/page.tsx`

Actúa como principal orquestador del módulo.

Responsabilidades:

* obtener la lista de metas;
* determinar la meta seleccionada;
* sincronizar selección y URL;
* obtener detalle;
* coordinar diálogos;
* ejecutar mutations;
* preparar datos para el detalle;
* coordinar acciones de miembros, invitaciones y contribuciones.

### Estado

La página mantiene bastante UI state, pero esto es consecuencia directa de ser el punto de composición de una feature con muchos diálogos.

No se considera automáticamente un problema que tenga muchos estados.

### Problema principal

La página también contiene lógica derivada de distribución/progreso y una llamada HTTP directa para la edición del nombre.

Por tanto, no es exclusivamente composición.

### Valoración

Es el componente que más merece vigilancia, pero no debe dividirse automáticamente en múltiples hooks o servicios.

El objetivo debería ser reducir responsabilidades únicamente donde exista una ganancia clara.

---

# Selección de meta

La selección se representa mediante:

* estado interno;
* parámetro de URL.

La URL permite conservar o compartir la meta seleccionada.

### Mejora pendiente

Comprobar si realmente son necesarias ambas fuentes.

Si la URL puede ser la única fuente de verdad, podría eliminarse parte del estado interno.

Si el estado interno tiene una razón de UX o de sincronización, mantener ambos puede ser correcto.

No debe modificarse sin comprobar el flujo exacto de navegación.

---

# `GroupGoalSelector`

Responsabilidad:

* mostrar las metas disponibles;
* permitir seleccionar una.

Recibe los datos desde la página y comunica la selección mediante callback.

No gestiona server state ni URL directamente.

### Valoración

Componente principalmente presentacional.

Correcto.

---

# `GroupGoalDetail`

Responsabilidad:

* mostrar el detalle de una meta;
* coordinar visualmente progreso, miembros, distribución e historial;
* mostrar acciones disponibles según permisos recibidos.

No realiza directamente llamadas HTTP ni mantiene los formularios.

### Valoración

Aunque es un componente relativamente grande, no se considera un "god component".

Su responsabilidad es principalmente compositiva.

Conviene mantenerlo así mientras no acumule estado o acceso directo a infraestructura.

---

# `MemberCard`

Representa un miembro individual.

Recibe:

* datos del miembro;
* información necesaria para presentación;
* callbacks para editar/eliminar.

Las acciones administrativas se muestran mediante el valor `isAdmin`.

No decide la autorización real.

### Valoración

Correcto.

La autorización definitiva corresponde al backend.

---

# `ContributionHistory`

Responsabilidad:

* consultar contribuciones paginadas;
* mostrarlas;
* controlar la paginación.

No realiza cálculos financieros complejos.

### Valoración

Correctamente delimitado.

El historial está desacoplado de la carga completa del detalle.

---

# `DistributionCard`

Responsabilidad:

* presentar el modo de distribución;
* mostrar su estado;
* mostrar información relacionada con la distribución;
* permitir cambiar el modo cuando el usuario tiene permisos.

No calcula por sí mismo toda la distribución.

### Punto a revisar

La validación de que los porcentajes personalizados suman aproximadamente 100 aparece en la UI.

Debe contrastarse con backend para determinar si es solo feedback preventivo o una regla de negocio duplicada.

---

# Dialogs

Los diálogos están separados por operación:

* crear meta;
* editar nombre;
* editar objetivo/modo;
* editar miembro;
* invitar miembro;
* añadir contribución.

Esto ayuda a mantener cada formulario acotado.

## Problema transversal

Todos utilizan estado y validación manual.

Eso provoca cierta repetición.

No obstante, no se debe introducir React Hook Form + Zod automáticamente solo por uniformidad.

La decisión debe basarse en:

* complejidad real;
* cantidad de validaciones;
* repetición;
* beneficio de centralización.

---

# `useGroupGoalQueries.ts`

Agrupa las queries relacionadas con:

* listado de metas;
* detalle;
* historial de contribuciones.

Esto es coherente.

## `useGroupGoals`

Obtiene la lista paginada.

## `useGroupGoalDetail`

Obtiene el detalle de la meta seleccionada y adapta los datos a la forma consumida por la UI.

## `useGroupGoalContributions`

Obtiene de forma independiente el historial paginado.

### Valoración

El agrupamiento es razonable.

Las transformaciones realizadas parecen estar relacionadas principalmente con adaptar la respuesta a la UI.

### Punto pendiente

El detalle parece incluir contribuciones completas aunque existe una query independiente y paginada para el historial.

Debe comprobarse si realmente se utilizan esas contribuciones incluidas en el detalle.

Si no se utilizan, puede existir sobreobtención de datos.

---

# `useGroupGoalMutations.ts`

Agrupa las operaciones mutables de la feature:

* crear meta;
* invitar miembro;
* actualizar meta;
* eliminar meta;
* actualizar miembro;
* eliminar miembro;
* añadir contribución.

Además gestiona:

* estado de pending;
* invalidación de queries;
* notificaciones;
* errores.

### Valoración

Es una pieza útil.

No es un simple wrapper de HTTP porque además coordina React Query y efectos posteriores.

No se considera necesario crear services separados para todas estas operaciones.

---

# Problema: edición de nombre mediante camino diferente

Existe una mutation general para actualizar una meta, pero la edición del nombre se realiza mediante una llamada HTTP directa desde `page.tsx`.

Esto crea dos caminos para modificar una misma entidad:

* mutation centralizada;
* llamada HTTP directa.

### Consecuencias

Puede provocar diferencias en:

* invalidación;
* notificaciones;
* manejo de errores;
* estados de pending.

### Mejora

Unificar el mecanismo de actualización si `updateGoal` cubre correctamente la operación.

Debe comprobarse antes el contrato exacto de la mutation existente.

---

# Invitaciones

El módulo tiene actualmente varios mecanismos relacionados con invitaciones.

## Crear invitación

Se realiza desde `useGroupGoalMutations`.

## Aceptar/rechazar

Existen:

* `invitations.service.ts`;
* `useInvitations.ts`;
* llamadas directas desde `JoinGroupGoalPage.tsx`.

### Problema

`JoinGroupGoalPage` no utiliza los mecanismos ya existentes.

Por tanto hay duplicación de caminos:

* service + hook;
* cliente HTTP directo.

Además, los flujos no tienen la misma estrategia de invalidación de cache.

### Mejora

Debe existir una única forma de ejecutar la aceptación/rechazo de invitaciones.

No es necesario decidir todavía si la solución definitiva debe ser:

* service;
* hook;
* llamada directa.

La decisión debe basarse en la convención final que adoptemos para las operaciones HTTP del frontend.

---

# `invitations.service.ts`

Actualmente es un wrapper fino de las llamadas HTTP de aceptación y rechazo.

No contiene transformación ni lógica compleja.

### Valoración

Su valor es bajo de forma aislada.

Puede mantenerse si forma parte de una convención coherente de acceso a API.

Si el resto de la aplicación no utiliza services equivalentes, será candidato a simplificación.

---

# `useInvitations.ts`

Proporciona hooks para aceptación/rechazo.

Aporta React Query e invalidación.

Es una abstracción con más valor que el service porque participa en server state y cache.

### Problema

El flujo principal mediante enlace no utiliza estos hooks.

Por tanto, existe duplicación funcional.

---

# `JoinGroupGoalPage`

Responsabilidades:

* leer el identificador desde URL;
* ejecutar aceptación/rechazo;
* mostrar resultado;
* notificar;
* navegar.

La mezcla es razonable para una página pequeña.

### Problema principal

No utiliza el flujo de invitaciones ya existente.

Además, debe comprobarse que la protección de autenticación esté garantizada por routing y no solo por comportamiento del endpoint.

---

# Permisos

El frontend utiliza información como:

* `isAdmin`;
* `isLastAdmin`;
* rol del miembro.

Estas condiciones sirven para decidir qué elementos mostrar o qué operaciones permitir visualmente.

### Regla importante

El frontend puede utilizar permisos para UX, pero el backend debe ser la autoridad final.

No debe confiarse en que ocultar el botón sea suficiente para impedir una operación.

### Punto a revisar

`isLastAdmin` parece representar una regla de integridad del grupo.

Debe contrastarse con backend.

Lo mismo ocurre con cualquier condición derivada del rol.

---

# `calculateContributions`

`utils.ts` contiene lógica que calcula la contribución esperada según el modo de distribución.

Interpreta:

* distribución equitativa;
* proporcional;
* personalizada.

Utiliza datos como:

* objetivo mensual;
* número de miembros;
* salario;
* porcentaje personalizado.

### Clasificación

Esto no es simplemente una transformación visual.

Es potencialmente **lógica financiera de dominio**.

### Mejora pendiente

Contrastar la implementación con el backend.

Si el backend ya calcula la contribución esperada y el porcentaje, el frontend debería evitar mantener una segunda implementación de esa regla.

Si el backend únicamente proporciona los datos base y el cálculo es una derivación puramente necesaria para la presentación, puede mantenerse.

No debe decidirse solo por el hecho de que el cálculo esté en `utils.ts`.

---

# Otras reglas derivadas

Deben revisarse junto con backend:

* suma de porcentajes personalizados;
* comprobación de último administrador;
* porcentaje total completado;
* tratamiento de distribución proporcional.

No todas tienen que eliminarse del frontend.

La pregunta es si constituyen reglas que backend debe garantizar o simples indicadores derivados para UX.

---

# Formularios y validación

Actualmente no se utiliza:

* React Hook Form;
* Zod.

Cada diálogo mantiene sus propios valores mediante estado local.

### Ventaja

Reduce dependencias reales del feature.

### Desventaja

Existe repetición de:

* estado;
* validación;
* reset;
* cierre;
* tratamiento de errores.

### Mejora

Revisar si existe suficiente repetición como para justificar una estrategia común.

No se considera necesario añadir una librería únicamente para conseguir uniformidad.

---

# React Query y cache

## Queries

* lista;
* detalle;
* contribuciones.

La estructura es razonable.

## Invalidación

Las mutations invalidan datos de la feature y, en determinadas operaciones, notificaciones.

### Problema pendiente

En algunas operaciones se realiza:

* invalidación;
* y además refetch explícito.

Debe comprobarse si el refetch aporta algo que la invalidación no cubre.

## Invitaciones

La mayor inconsistencia de cache está en `JoinGroupGoalPage`, que no sigue el mismo flujo que `useInvitations`.

### Mejora

Unificar primero el camino de invitaciones y después revisar las invalidaciones.

---

# Dependencias con otras features

## `auth`

Se consume `useAuth`.

Es una dependencia transversal y razonable.

---

## `categories`

`groupgoals` utiliza `ConfirmDeletionDialog` desde categories.

### Problema

Este componente parece ser genérico.

Si realmente no contiene ninguna lógica específica de categorías, su ubicación en categories es incorrecta.

### Mejora

Evaluar mover el componente a `shared/components` para que cualquier feature pueda utilizarlo sin depender de otra feature.

No moverlo si contiene propiedades o textos específicos de categories.

---

## Otras features

No se observan dependencias directas relevantes hacia:

* transactions;
* dashboard;
* investments;
* savingsgoals;
* profile;
* wiki;
* notifications.

---

# Dependencias con shared

Las dependencias hacia:

* API client;
* componentes UI;
* paginación;
* utilidades;
* errores;

son razonables.

`useServerPagination` también parece correctamente ubicado si realmente es genérico.

Debe revisarse su implementación junto con las otras features para confirmar que no contiene lógica específica de groupgoals.

---

# `constants.ts`

Contiene configuración visual para miembros.

No contiene reglas de negocio.

### Valoración

Correcto.

No hay necesidad de moverlo.

---

# `utils.ts`

Contiene `calculateContributions`.

A diferencia de `constants.ts`, aquí existe lógica financiera real.

Debe conservarse dentro de la feature mientras siga siendo específica de groupgoals.

El problema a resolver no es su ubicación, sino si el cálculo debería existir también en frontend.

---

# Tipos

Los tipos del módulo representan:

* roles;
* modos de distribución;
* estados de invitación;
* miembros;
* contribuciones;
* detalle;
* elementos de listado;
* miembros con datos derivados.

La existencia de `ContributionMember` como tipo derivado es razonable porque representa el dato que la UI necesita después de realizar cálculos.

No es necesario eliminarlo solo porque combine datos del backend con datos calculados.

### Puntos a revisar

* `InvitationStatus` parece no tener uso visible.
* El uso de strings en algunos lugares puede ser menos estricto de lo necesario.
* `GroupGoalDetail` puede contener información más amplia de la necesaria si realmente incluye contribuciones completas que no se utilizan.

---

# Comparación con backend

## Debe ser responsabilidad del backend

* ownership;
* roles efectivos;
* permisos;
* último administrador;
* validez de invitaciones;
* aceptación/rechazo;
* límites de contribución;
* integridad de distribución;
* consistencia de datos;
* estado de la meta.

## Validación frontend razonable

* campos obligatorios;
* números positivos;
* formatos;
* feedback inmediato;
* deshabilitar botones mientras existe una operación.

## Transformación de presentación razonable

* formatear fechas;
* formatear cantidades;
* asignar colores;
* preparar datos para visualización.

## Punto crítico de contraste

`calculateContributions` y las comprobaciones relacionadas con distribución deben compararse con el backend.

---

# Bundle y rendimiento

El módulo no utiliza:

* React Hook Form;
* Zod.

Esto evita esas dependencias dentro del chunk de groupgoals.

Las dependencias relevantes son principalmente:

* Radix a través de componentes compartidos;
* Lucide;
* React Query;
* componentes de UI.

No se observa un problema claro de bundle específico del módulo.

### Coste potencial de runtime

* múltiples queries al abrir una meta;
* detalle potencialmente sobrecargado;
* invalidaciones/refetch redundantes;
* cálculo de contribuciones sobre miembros.

Ninguno justifica cambios sin medición o comprobación adicional.

---

# Problemas detectados

## ALTO

### 1. Flujo de invitaciones duplicado

Existen varios caminos para aceptar/rechazar invitaciones.

Debe existir un único mecanismo.

**Archivos:** `JoinGroupGoalPage.tsx`, `useInvitations.ts`, `invitations.service.ts`.

**Consecuencia:** duplicación y comportamiento de cache diferente.

---

### 2. Edición de nombre por llamada HTTP directa

Existe una mutation de actualización general, pero el nombre utiliza otro camino.

**Archivos:** `page.tsx`, `useGroupGoalMutations.ts`.

**Consecuencia:** comportamiento inconsistente entre operaciones equivalentes.

---

### 3. Lógica de distribución en frontend

`calculateContributions` interpreta modos de distribución y calcula contribuciones esperadas.

**Archivo:** `utils.ts`.

**Consecuencia:** posible duplicación de lógica financiera con backend.

**Pendiente:** comparar con la implementación del backend.

---

### 4. Reglas de permisos/integridad calculadas en frontend

El frontend calcula condiciones como último administrador y validez de distribución.

**Archivos:** `page.tsx`, `EditMemberDialog.tsx`, `DistributionCard.tsx`.

**Consecuencia:** posible duplicación de reglas que backend debe garantizar.

**Pendiente:** contrastar reglas exactas del backend.

---

## MEDIO

### 5. `ConfirmDeletionDialog` ubicado en categories

`groupgoals` depende de un componente aparentemente genérico de categories.

**Consecuencia:** acoplamiento entre features.

**Pendiente:** comprobar si el diálogo es completamente genérico.

---

### 6. Invalidación + refetch

Algunas mutations hacen ambas cosas.

**Consecuencia:** posible petición redundante.

**Pendiente:** revisar necesidad real de cada refetch.

---

### 7. Selección duplicada entre URL y estado

La meta seleccionada existe tanto en navegación como en estado interno.

**Consecuencia:** complejidad adicional y potencial doble fuente de verdad.

**Pendiente:** comprobar si el estado interno es realmente necesario.

---

### 8. Posible sobreobtención en detalle

El detalle puede incluir contribuciones completas aunque existe un historial paginado independiente.

**Consecuencia:** payload potencialmente mayor.

**Pendiente:** buscar consumidores reales de esas contribuciones.

---

### 9. Validaciones manuales repetidas

Los diálogos implementan validaciones independientes.

**Consecuencia:** pueden evolucionar de forma inconsistente.

**Pendiente:** determinar si merece la pena una estrategia común.

---

### 10. `JoinGroupGoalPage` depende de comportamiento externo para autenticación

No queda claro en el archivo si la ruta está protegida por router.

**Consecuencia:** riesgo potencial de comportamiento inesperado.

**Pendiente:** comprobar configuración de `App.tsx` y route guard.

---

## BAJO

### 11. `InvitationStatus` sin uso visible

Debe comprobarse si tiene consumidores externos.

### 12. Naming de algunos hooks/types

Existen pequeñas inconsistencias de nomenclatura.

### 13. Strings menos estrictos para roles/modos

Se puede mejorar tipado, pero no es una prioridad arquitectónica.

---

# Elementos que están bien y conservaría

* `groupgoals` como una única feature.
* Separación entre queries y mutations.
* Uso de React Query para server state.
* Query independiente y paginada para historial.
* `GroupGoalSelector` como componente presentacional.
* `MemberCard` como componente presentacional.
* `ContributionHistory` bien delimitado.
* `GroupGoalDetail` como compositor de la vista de detalle.
* Separación de dialogs por operación.
* `constants.ts` limitado a presentación.
* `calculateContributions` dentro de la feature mientras siga siendo específico de groupgoals.
* Uso de componentes compartidos.
* `useServerPagination`.
* No introducir services adicionales para todas las operaciones.
* No añadir React Hook Form/Zod simplemente por homogeneidad.
* Mantener las restricciones visuales de permisos aunque backend siga siendo la autoridad.
* No mover cálculos triviales al backend únicamente porque puedan expresarse como operaciones matemáticas.

---

# Pendiente de comprobación

1. Contrato backend de distribución.
2. Reglas backend de último administrador.
3. Reglas backend sobre porcentajes personalizados.
4. Contrato de invitaciones.
5. Consumidores reales de `useInvitations`.
6. Consumidores de `invitations.service`.
7. Implementación de `useServerPagination`.
8. Todas las query keys invalidadas por mutations.
9. Consumidores de `GroupGoalDetail.contributions`.
10. Configuración de rutas de `JoinGroupGoalPage`.
11. Naturaleza realmente genérica de `ConfirmDeletionDialog`.
12. Todos los consumidores de `InvitationStatus`.

---

## Investments

### Responsabilidad

El feature `investments` gestiona la cartera de inversiones del usuario.

Permite:

* consultar activos;
* buscar activos;
* filtrar por tipo;
* crear inversiones;
* actualizar el precio actual;
* eliminar inversiones;
* consultar métricas de cartera;
* visualizar distribución;
* visualizar evolución;
* mostrar beneficio, pérdida y rentabilidad.

La feature está razonablemente cohesionada.

No se observa necesidad de dividirla en subfeatures.

---

# Estructura

```text id="4rt6m1"
features/investments/
├── constants.ts
├── components/
│   ├── AssetList.tsx
│   ├── AssetListSkeleton.tsx
│   ├── AssetRow.tsx
│   ├── AssetSkeletonRow.tsx
│   ├── DistributionChart.tsx
│   ├── EvolutionChart.tsx
│   ├── InvestmentMetrics.tsx
│   ├── NewInvestmentDialog.tsx
│   └── UpdatePriceDialog.tsx
├── hooks/
│   ├── useInvestmentMutations.ts
│   └── useInvestmentQueries.ts
├── pages/
│   └── page.tsx
└── types/
    └── index.ts
```

No existe capa `services`.

Para esta feature no se considera necesario añadirla salvo que aparezca una necesidad concreta.

---

# Responsabilidades reales

## Gestión de activos

* listado;
* búsqueda;
* filtrado;
* paginación;
* creación;
* actualización de precio;
* eliminación.

## Información agregada

* valor de cartera;
* rentabilidad;
* beneficios/pérdidas;
* distribución;
* evolución.

## Presentación

* métricas;
* filas;
* gráficos;
* colores;
* iconos;
* estados de carga;
* confirmaciones.

---

# Fuente de verdad financiera

La característica más positiva del módulo es que el frontend **no reproduce los principales cálculos financieros**.

Los valores relevantes son proporcionados por backend:

* valor actual;
* capital invertido;
* beneficio/pérdida;
* ROI;
* métricas agregadas;
* distribución;
* evolución.

El frontend se limita principalmente a presentar esos datos y adaptarlos a la UI.

Esto debe conservarse.

---

# Flujo general de datos

## Carga inicial

La página consume `useInvestmentQueries`, que agrupa:

* listado;
* métricas;
* evolución;
* distribución.

Estas consultas se ejecutan independientemente y pueden realizarse en paralelo cuando no dependen entre sí.

Flujo conceptual:

```text id="feq44n"
page
→ useInvestmentQueries
→ React Query
→ apiClient
→ backend
→ componentes
```

---

# `page.tsx`

Actúa como orquestador de la página.

## Estado

### UI state

* periodo;
* término de búsqueda;
* filtro por tipo;
* inversión seleccionada para eliminar.

### Server state

Se consume a través de:

* `useInvestmentQueries`;
* `useInvestmentMutations`.

### Form state

No existe en la página.

Los formularios viven en sus diálogos.

### Derived state

No contiene cálculos financieros importantes.

---

## Responsabilidad

La página:

* configura el periodo;
* mantiene búsqueda y filtro;
* controla la inversión seleccionada para eliminación;
* compone la lista;
* compone métricas y gráficos;
* monta los diálogos.

### Valoración

`page.tsx` es razonablemente ligero.

No se considera necesario extraer un controlador adicional.

---

# `useInvestmentQueries.ts`

Agrupa las consultas de la feature.

## Lista de inversiones

Obtiene activos paginados con:

* búsqueda;
* filtro por tipo.

## Métricas

Obtiene métricas agregadas en función del periodo.

## Evolución

Obtiene datos históricos para el gráfico.

## Distribución

Obtiene datos de distribución de cartera.

### Valoración

El hook cumple correctamente una función de agrupación.

No se observa que haya adquirido responsabilidades de negocio.

---

# `useInvestmentMutations.ts`

Centraliza:

* creación;
* actualización de precio;
* eliminación.

Además gestiona:

* peticiones HTTP;
* estado pending;
* invalidación de queries;
* notificaciones;
* extracción de errores.

## Patrón común

Las tres mutations siguen una estructura coherente.

Esto es positivo y conviene conservarlo.

### No se necesita service adicional

No existe una cadena innecesaria entre:

```text id="yq5wvc"
componente
→ hook
→ service
→ api
```

Las operaciones se resuelven mediante:

```text id="r0m1kb"
componente
→ hook
→ apiClient
```

Para esta feature es suficiente.

---

# React Query y cache

## Query keys

Existen claves diferenciadas para:

* inversiones;
* métricas;
* evolución;
* distribución.

Las consultas dependientes del periodo incorporan el periodo como parte de su identidad.

## Invalidaciones

Crear, actualizar precio y eliminar invalidan las cuatro áreas:

* lista;
* métricas;
* evolución;
* distribución.

Esto es coherente porque cualquier cambio sobre la cartera puede afectar a todas ellas.

### Valoración

La amplitud de la invalidación parece razonable.

No se observa una necesidad evidente de introducir actualizaciones manuales de cache.

---

# Creación

`NewInvestmentDialog` gestiona:

* nombre;
* ticker;
* cantidad;
* precio;
* tipo;
* fecha.

La validación actual es manual.

Antes de enviar se realizan pequeñas transformaciones de presentación/entrada:

* normalización del nombre;
* normalización del ticker;
* conversión numérica;
* tratamiento del ticker vacío.

El hook se encarga de la mutación.

### Valoración

La estructura es simple y adecuada.

No se considera necesario introducir React Hook Form ni Zod únicamente por homogeneidad.

---

# Actualización de precio

`UpdatePriceDialog` permite modificar el precio actual de un activo.

La operación envía:

* identificador del activo;
* nuevo precio.

El frontend no recalcula:

* valor actual;
* beneficio;
* ROI;
* distribución;
* evolución.

Después de la mutation se invalidan las consultas correspondientes.

### Valoración

Esta separación es correcta.

---

# Confirmación ante grandes cambios de precio

El diálogo calcula la desviación relativa entre el precio actual y el nuevo precio y solicita confirmación adicional cuando la desviación supera el umbral establecido.

Esta lógica se considera principalmente una **protección de UX**, no una regla de cálculo de cartera.

La validación definitiva del precio sigue correspondiendo al backend.

---

# Eliminación

El flujo es:

* seleccionar inversión;
* abrir confirmación;
* ejecutar `deleteInvestment`;
* invalidar consultas.

No se observa una llamada HTTP directa desde el componente.

### Mejora pendiente

El diálogo de confirmación utilizado por investments procede actualmente de `features/categories`.

Esto crea acoplamiento entre features.

Si el componente es completamente genérico, debe trasladarse a `shared`.

---

# `AssetList`

Responsabilidad:

* representar activos;
* gestionar búsqueda;
* gestionar filtro;
* mostrar paginación;
* propagar acciones.

No realiza cálculos financieros.

### Valoración

Correctamente delimitado.

### Mejora pendiente

Comprobar si su paginación debería utilizar el componente `Pagination` ya existente en `shared`, siempre que dicho componente cubra las necesidades de esta vista.

La discrepancia puede ser simplemente una decisión de UI específica y no debe cambiarse automáticamente.

---

# `AssetRow`

Representa un activo individual.

Realiza pequeñas derivaciones de presentación:

* tendencia positiva/negativa;
* visualización del ROI;
* abreviación de ticker;
* presentación de valores.

No calcula las métricas financieras.

### Valoración

Correcto.

Está memoizado, lo que es razonable en una lista.

### Mejora

Hay dos comportamientos potencialmente engañosos si backend permite `null`:

* un ROI inexistente puede terminar representándose como cero;
* un beneficio inexistente puede terminar clasificándose visualmente como positivo.

Debe comprobarse el contrato real del backend antes de cambiar esto.

---

# `DistributionChart`

Recibe la distribución preparada por backend.

Se limita a:

* adaptar los datos al gráfico;
* mostrar tooltip;
* presentar el valor total.

No calcula porcentajes.

### Valoración

Correctamente orientado a presentación.

### Mejora menor

Tiene una función local para formato compacto de moneda.

Debe comprobarse si `shared/utils` ya ofrece una función equivalente y localizada antes de mantener dos mecanismos distintos.

---

# `EvolutionChart`

Recibe datos históricos del backend.

Su responsabilidad es adaptar:

* labels;
* valores;

al componente gráfico compartido.

No calcula rentabilidad, crecimiento ni variaciones financieras.

### Valoración

Correcto.

---

# `InvestmentMetrics`

Muestra métricas agregadas recibidas del backend.

No recalcula:

* valor de cartera;
* retorno;
* dividendos;
* tendencias.

### Valoración

Muy buena separación de responsabilidades.

El componente es esencialmente presentacional.

---

# `AssetListSkeleton` y `AssetSkeletonRow`

Responsabilidad exclusiva de loading.

No contienen lógica.

### Valoración

Correctos.

---

# `constants.ts`

Contiene configuración específica de presentación y selección:

* colores por tipo;
* tipos de inversión;
* iconos;
* opciones de formulario.

## `TYPE_COLORS`

Configuración visual.

## `INVESTMENT_TYPES`

Configuración de UI para los tipos disponibles.

### Mejora importante

Existe una posible inconsistencia entre `INVESTMENT_TYPES` y `InvestmentAssetType`:

La constante incluye `FUND`, mientras el tipo TypeScript visible no parece incluirlo.

Debe comprobarse el enum o conjunto de tipos real del backend.

### Valoración

Las constantes deben permanecer en `investments`.

No hay necesidad de moverlas a `shared` mientras sean propias de esta feature.

---

# Cálculos financieros

La feature mantiene una frontera bastante sana.

## Valores que vienen del backend

* valor actual;
* capital invertido;
* beneficio/pérdida;
* ROI;
* métricas agregadas;
* distribución;
* evolución.

## Transformaciones legítimas

* adaptación a gráficos;
* formato monetario;
* tendencia visual;
* truncado de ticker;
* etiquetas;
* paginación.

## Lógica financiera propia

No se observa una implementación relevante de cálculos financieros de fondo.

### Valoración

Esto debe considerarse una de las principales decisiones correctas del módulo.

---

# Tipos

## `InvestmentAssetType`

Representa el conjunto de tipos permitidos.

Debe alinearse con:

* backend;
* constantes;
* filtros;
* formulario de creación.

## `InvestmentResponse`

Representa el activo recibido del backend.

Que contenga campos no utilizados por una pantalla no implica por sí mismo un problema.

No debe crearse automáticamente otro tipo de UI solo para eliminar campos.

Debe existir una separación adicional únicamente si:

* hay contratos diferentes;
* existen transformaciones reales;
* o distintos consumidores requieren modelos realmente diferentes.

## Request types

Hay tipos de request definidos para:

* creación;
* actualización de precio.

Pero no están siendo utilizados directamente por las mutations.

Esto es una inconsistencia de tipado.

### Mejora

El contrato TypeScript utilizado por las mutations debería coincidir con el payload real.

Especialmente importante en:

* creación;
* actualización de precio.

---

# Formularios y validación

Actualmente se utiliza estado local y validación manual.

No se usa:

* React Hook Form;
* Zod.

Esto no es necesariamente un problema.

Para formularios pequeños, el enfoque actual puede ser más ligero.

### Mejora pendiente

Revisar únicamente:

* duplicación de reglas;
* consistencia;
* contrato de backend.

No introducir librerías nuevas solo para homogeneizar.

---

# Estado y fuentes de verdad

## Server state

* lista de activos;
* métricas;
* evolución;
* distribución;
* estado pending de mutations.

## UI state

* periodo;
* búsqueda;
* filtro;
* activo seleccionado para eliminar;
* estado de diálogos.

## Form state

* creación;
* actualización de precio.

## Derived state

* tendencia visual;
* preparación de datos para charts;
* formato;
* desviación de precio para confirmación.

No se observa una copia problemática del server state a `useState`.

### Valoración

Fuente de verdad razonablemente clara.

---

# Dependencias con otras features

## Categories

Investments reutiliza el `ConfirmDeletionDialog` situado dentro de categories.

### Problema

No debería existir dependencia entre dos features para un componente genérico.

### Pendiente

Comprobar si el diálogo es completamente genérico.

Si lo es, su ubicación debería pasar a `shared`.

---

## Dashboard

No existe dependencia de código directa.

Las claves de React Query son diferentes.

### Pendiente

Comprobar si dashboard también muestra datos de cartera.

Si los muestra, habrá que decidir si determinadas mutations de investments deben invalidar queries del dashboard.

---

# Dependencias con shared

## Necesarias

* cliente HTTP;
* React Query helpers;
* componentes de UI;
* gráficos;
* notificaciones;
* formato;
* paginación.

En general, son reutilizaciones apropiadas.

### Punto a comprobar

Comparar `AssetList` con el componente `Pagination` de `shared` antes de mantener dos soluciones distintas para el mismo concepto.

---

# Integración con dashboard

No parece existir una reutilización directa de queries.

Investments mantiene sus propias claves:

* investments;
* investmentMetrics;
* investmentEvolution;
* investmentDistribution.

### Punto pendiente

Comprobar si alguna métrica global del dashboard depende de inversiones.

Si existe, habrá que asegurarse de que las mutations de investments dejan la información global correctamente actualizada.

No hay evidencia suficiente para afirmar que exista actualmente un problema.

---

# Bundle y rendimiento

## Framer Motion

Se utiliza en la lista.

No constituye por sí mismo un problema.

Debe valorarse según el peso real del chunk y la utilidad de las animaciones.

## Charts

La visualización depende de componentes compartidos.

Esto permite centralizar la librería y la configuración.

## Radix

Se utiliza a través de componentes compartidos.

## Lucide

Se usan iconos específicos del módulo.

## React Hook Form / Zod

No se utilizan aquí.

Eso reduce dependencias del módulo.

---

# Rendimiento de runtime

Aspectos positivos:

* AssetRow memoizado;
* lista paginada;
* gráficos alimentados por datos ya preparados;
* queries independientes.

Puntos a vigilar:

* una instancia de `UpdatePriceDialog` por fila;
* animación de múltiples filas;
* tamaño de payload de activos.

Ninguno justifica cambios sin datos reales de uso.

---

# Problemas detectados

## ALTO

### 1. Desalineación entre tipos de inversión y UI

`INVESTMENT_TYPES` incluye `FUND`, mientras `InvestmentAssetType` visible no lo contempla.

**Consecuencia:** posible inconsistencia entre formulario, filtros, tipos y backend.

**Pendiente:** comprobar enum real del backend.

---

### 2. Request types no utilizados por las mutations

Los tipos definidos para creación y actualización de precio no representan el contrato efectivo utilizado por los hooks.

**Consecuencia:** menor seguridad de tipos y posibilidad de que código y contrato diverjan silenciosamente.

**Pendiente:** comparar con los endpoints reales.

---

### 3. Dependencia hacia `ConfirmDeletionDialog` de categories

Investments depende de un componente interno de otra feature.

**Consecuencia:** acoplamiento estructural.

**Pendiente:** comprobar si el diálogo es realmente genérico y trasladarlo a shared si corresponde.

---

## MEDIO

### 4. Posible doble notificación de errores

Las mutations gestionan errores y algunos diálogos también pueden mostrar feedback de error.

**Consecuencia:** posibilidad de doble toast.

**Pendiente:** comprobar el flujo real de `mutate`/`mutateAsync` y establecer una estrategia única.

---

### 5. `AssetRow` puede representar datos nulos como datos válidos

Existe riesgo de que valores ausentes como ROI o beneficio nulo terminen representándose como cero/positivo.

**Consecuencia:** posible información visual incorrecta.

**Pendiente:** contrato real del backend y semántica deseada en UI.

---

### 6. Formato monetario local duplicado

`DistributionChart` tiene una implementación propia mientras existe infraestructura compartida de formato.

**Consecuencia:** riesgo de diferencias de locale o formato.

**Pendiente:** comprobar la capacidad de `shared/utils`.

---

### 7. Locale hardcoded

`AssetRow` utiliza un locale concreto para determinados formatos.

**Consecuencia:** puede no respetar el idioma seleccionado por el usuario.

**Pendiente:** revisar la utilidad compartida de internacionalización.

---

### 8. Posible búsqueda sin debounce

El término de búsqueda puede actualizar la query con cada pulsación.

**Consecuencia:** potencialmente más peticiones al backend.

**Pendiente:** revisar `useServerPagination` y el comportamiento real de búsqueda.

---

### 9. Precisión del precio

`UpdatePriceDialog` parece asumir una precisión de dos decimales.

**Consecuencia:** puede ser insuficiente para determinados tipos de activos.

**Pendiente:** contrato de precisión del backend y necesidades por tipo.

---

### 10. Posible falta de invalidación de datos globales

Las mutations invalidan las cuatro queries propias de investments, pero no claves externas.

**Consecuencia:** solo sería un problema si dashboard u otra feature muestran datos afectados por inversiones.

**Pendiente:** revisar consumidores globales.

---

## BAJO

### 11. Request types sin uso visible

Además del problema de contrato, son posibles tipos muertos si no existe intención de utilizarlos.

### 12. `AssetList` usa paginación distinta a `shared`

Puede ser una elección legítima o una inconsistencia de UI.

Debe comprobarse antes de cambiarla.

### 13. Namespaces de traducción cruzados

Investments utiliza algunas claves de `dashboard` y `transactions`.

No es necesariamente incorrecto, pero puede ser una señal de que determinados textos deberían pertenecer a un namespace común.

---

# Elementos que están bien

* La feature está cohesionada.
* No existe una capa `services` artificial.
* Las queries están agrupadas de forma razonable.
* Las mutations están centralizadas.
* No hay llamadas HTTP dispersas por los componentes.
* React Query se utiliza como fuente del server state.
* Las invalidaciones de la propia feature son coherentes.
* El backend parece ser la fuente de verdad de los cálculos financieros.
* Los gráficos son principalmente presentacionales.
* `InvestmentMetrics` no recalcula métricas financieras.
* `AssetRow` no reproduce cálculos complejos.
* La paginación evita renderizar una lista completa.
* El uso de memoización en filas es razonable.
* Los formularios son suficientemente pequeños como para no necesitar obligatoriamente React Hook Form/Zod.
* La confirmación adicional para grandes cambios de precio es una buena protección de UX.
* No hay duplicación evidente de estado remoto en `useState`.

---

# Pendiente de comprobación

1. Enum real de tipos de inversión del backend.
2. Contrato real de creación de inversiones.
3. Contrato real de actualización de precio.
4. Todos los consumidores de `ConfirmDeletionDialog`.
5. Implementación de `useServerPagination`.
6. Implementación de `shared/utils` de formato monetario/i18n.
7. Consumidores de las query keys relacionadas con dashboard.
8. Semántica real de valores `null` en ROI y beneficio.
9. Precisión decimal permitida según tipo de activo.
10. Comportamiento de búsqueda y posible debounce.

---

# Profile

## Valoración general

El módulo `profile` está razonablemente bien estructurado y no requiere una refactorización arquitectónica importante.

Aunque agrupa varias áreas —datos personales, contraseña, sesiones, eliminación de cuenta, notificaciones e integración con Telegram— todas están relacionadas con la gestión de la cuenta del usuario actual.

La estructura actual es sencilla:

**ProfilePage → Sections → Hooks → profileService → API**

Los componentes de sección mantienen su propio estado local y React Query gestiona el estado remoto. No se observa lógica financiera ni reglas de negocio complejas duplicadas en frontend.

La recomendación general es **mantener esta estructura y corregir únicamente problemas concretos y verificables**.

---

## Lo que está bien y debería mantenerse

### 1. Una sección por responsabilidad de UI

`PersonalInfoSection`, `ChangePasswordSection`, `SessionsSection` y `DeleteAccountSection` están razonablemente aislados.

No existe un único componente gigante que gestione todo profile.

### 2. Hooks de mutación separados

Cada operación sensible tiene su propia mutación:

* actualizar perfil;
* cambiar contraseña;
* cerrar sesión;
* cerrar otras sesiones;
* desactivar cuenta;
* desvincular Telegram.

La separación es clara y no necesita una capa adicional de servicios de aplicación.

### 3. React Query para server state

El perfil y las sesiones no se copian manualmente a `useState`.

La página consume los datos remotos y los componentes mantienen únicamente estado de UI/formulario.

### 4. `profileService` tiene sentido

En este módulo existen suficientes endpoints relacionados entre sí para justificar un service HTTP específico.

No es necesario eliminarlo para hacer el frontend más simple.

### 5. Los formularios son suficientemente pequeños

No parece necesario introducir React Hook Form únicamente para profile.

El estado local y la validación manual son aceptables mientras los formularios sigan siendo de este tamaño.

### 6. El backend mantiene las reglas sensibles

El frontend no intenta gestionar:

* autenticación real de la contraseña;
* autorización;
* revocación real de sesiones;
* eliminación real de cuenta;
* seguridad de Telegram.

Estas decisiones deben permanecer en backend.

---

# Problemas confirmados o muy probables

## 1. Invalidación de la query `user` después de cambiar la contraseña

`useChangePassword` invalida `user`, mientras que las queries visibles del módulo utilizan `profile` y `sessions`.

### Revisar

Buscar globalmente cualquier query que utilice realmente la clave `user`.

### Si no existe

Eliminar esa invalidación.

### Motivo

Evitar código muerto y operaciones de cache que aparentemente no afectan a ningún dato.

**Prioridad: alta.**

---

## 2. Limpieza duplicada al desactivar la cuenta

`useDeactivateAccount` realiza logout y posteriormente limpia nuevamente el `QueryClient`.

El `AuthContext` ya centraliza la limpieza asociada al logout.

### Mejora

Debe existir un único responsable de la limpieza completa de la sesión.

La operación de desactivar cuenta debería coordinar:

* llamada al backend;
* logout;
* navegación.

No debería conocer además detalles internos de limpieza que ya pertenecen a `AuthContext`.

**Prioridad: alta.**

---

## 3. `ConfirmDeletionDialog` está ubicado dentro de `categories`

El componente se utiliza desde profile y otros módulos.

### Revisar

Buscar todos sus consumidores.

### Mejora

Si sus props y comportamiento son completamente genéricos, moverlo a `shared`.

No crear una abstracción adicional.

**Prioridad: media.**

---

## 4. Mensajes de mutaciones hardcoded en español

Varias mutaciones generan directamente mensajes de éxito y error en español.

Esto contradice la existencia de internacionalización en la aplicación.

### Mejora

Unificar la estrategia de traducción de notificaciones en los hooks de mutación.

No es necesario introducir una nueva capa para ello.

**Prioridad: media.**

---

# Problemas que requieren comprobación antes de modificar

## 5. `AuthContext.user` frente a `profile`

Hay dos representaciones potenciales de información de usuario:

* `AuthContext.user`;
* query `profile`.

Esto solo constituye un problema real si ambas representan los mismos datos y se consumen simultáneamente como fuente de verdad.

### Revisar

Buscar todos los usos de `useAuth()` y determinar:

* qué propiedades de `user` se consumen;
* si username/email se muestran desde AuthContext;
* si alguna operación refresca AuthContext después de actualizar profile.

### No hacer todavía

No introducir un mecanismo de sincronización global hasta comprobar que realmente existe desincronización.

La solución debe ser la más simple posible una vez conocido el uso real.

**Prioridad: alta, pero pendiente de verificación.**

---

## 6. Validación de username y email

PersonalInfoSection solamente realiza validación mínima antes de enviar.

No debe asumirse automáticamente que esto sea incorrecto.

### Revisar en backend

* formato de email;
* longitud/formato de username;
* campos obligatorios;
* unicidad;
* posibilidad de actualizar solo uno de los dos campos.

La validación frontend debe utilizarse principalmente para mejorar UX y anticipar errores previsibles.

**Prioridad: media, pendiente de contrato backend.**

---

## 7. Validación de `currentPassword`

ChangePasswordSection no parece impedir enviar una contraseña actual vacía.

Es razonable añadir esta validación como mejora de UX, pero no constituye una regla de seguridad.

El backend debe seguir siendo responsable de comprobar la contraseña.

**Prioridad: baja.**

---

## 8. Invalidación de sesiones después de cambiar contraseña

Actualmente no se invalidan `sessions`.

No debe modificarse automáticamente.

### Revisar

Comportamiento real del backend al cambiar la contraseña:

* si revoca sesiones;
* si mantiene sesiones;
* si modifica la sesión actual;
* si obliga a reautenticarse.

La estrategia del frontend debe reflejar ese comportamiento.

**Prioridad: media, pendiente de backend.**

---

## 9. `NotificationsTable` muestra únicamente invitaciones pendientes

El componente filtra específicamente invitaciones de metas grupales que requieren acción.

Esto no es necesariamente un error.

### Revisar

Determinar la responsabilidad funcional de la sección dentro de profile:

* panel de acciones pendientes;
* resumen de notificaciones;
* listado completo de notificaciones.

Solo debe cambiarse si el producto requiere otra semántica.

**Prioridad: funcional, no arquitectónica.**

---

## 10. Estado de error de sesiones

`useSessions` expone error, pero la sección aparentemente solo representa loading/lista.

### Revisar

La estrategia global de la aplicación para errores de queries.

Si otras pantallas ya tienen un patrón común para estados de error, profile debería seguirlo.

No crear un sistema específico para esta sección.

**Prioridad: baja.**

---

## 11. Formato de fechas

SessionsSection utiliza el formato local del navegador.

### Revisar

Si existe una utilidad compartida de fechas y cómo se comportan los demás módulos con i18n.

Si ya existe una convención global, utilizarla.

No crear otra utilidad únicamente para profile.

**Prioridad: baja.**

---

## 12. Impacto real en bundle

El módulo importa código de:

* auth;
* notifications;
* groupgoals;
* categories.

Existe potencial de acoplamiento de bundle, pero el audit no demuestra por sí solo que exista un problema de tamaño real.

### Revisar

Analizar el build real y los chunks generados antes de mover funcionalidades únicamente por motivos teóricos de bundle.

**Prioridad: baja/pending build analysis.**

---

# Código potencialmente no utilizado

## Preferencias

Existen:

* `getPreferences`;
* `updatePreferences`;
* `UserPreferences`.

Actualmente no se observa UI que los utilice.

### Acción

Buscar usos globales antes de eliminar nada.

Si no existen consumidores y la funcionalidad ya no forma parte del producto, eliminarla.

Si es funcionalidad futura, decidir si merece la pena mantenerla según el roadmap.

---

## Tipos de Telegram

Revisar globalmente:

* `TelegramConnectionStatus`;
* `TelegramStatusResponse`.

Si no se utilizan, pueden ser restos de una implementación anterior.

---

## Tipos de moneda

Revisar globalmente:

* `CurrencyCode`;
* `CurrencyOption`.

No deben mantenerse únicamente porque existe `UserPreferences` si ninguna parte activa de la aplicación los utiliza.

---

## Campos no usados de `ProfileResponse`

`active` y `anonymized` no parecen necesarios para la UI actual.

Esto no significa que deban eliminarse del tipo.

El tipo puede representar correctamente la respuesta completa del backend aunque la pantalla no utilice todos sus campos.

**No reducir el contrato únicamente para adaptarlo a los consumidores actuales.**

---

# Naming

## `SettingsSection`

El componente es un contenedor visual, no un gestor de configuración.

El nombre puede resultar confuso, pero es un problema menor.

### Posibles nombres conceptuales

Un nombre relacionado con `Section`, `SettingsCard` o `ProfileSection` podría describir mejor su función.

No es una prioridad y no merece un refactor si no existe confusión real en el código.

---

# Telegram

`TelegramSection` está actualmente comentado en `ProfilePage`.

Por tanto, hay una funcionalidad existente pero no expuesta desde esta página.

### Revisar

Determinar si Telegram:

* está temporalmente desactivado;
* sigue formando parte del producto;
* se eliminó de la UI pero no del código.

Si está fuera del producto, eliminar también el código asociado.

Si volverá a activarse, puede mantenerse tal y como está.

No hacer una refactorización alrededor de una funcionalidad cuyo estado de producto aún no está claro.

---

# Dependencias entre features

## Auth

La dependencia está parcialmente justificada:

* sesión actual;
* logout;
* validación de contraseña.

No intentar eliminarla artificialmente.

La única dependencia más discutible es `passwordSchema`, porque profile consume una pieza interna de auth.

Antes de moverla a shared hay que comprobar si realmente representa una regla común de toda la aplicación o si únicamente se necesita para autenticación/cambio de contraseña.

---

## Notifications

`profile` consume una parte de las notificaciones.

Esto es razonable si profile funciona como dashboard de cuenta.

No es necesario duplicar llamadas HTTP ni crear adaptadores únicamente para evitar el import.

---

## Groupgoals

Las acciones de aceptar/rechazar invitaciones pertenecen conceptualmente a groupgoals.

Utilizar sus hooks es mejor que duplicar la lógica.

El acoplamiento existe, pero no es suficiente por sí solo para crear un sistema de eventos o una capa intermedia.

---

## Categories

La dependencia a `ConfirmDeletionDialog` es la menos justificada porque el componente es genérico.

Es el candidato más claro para pasar a `shared`, siempre que una búsqueda global confirme que su comportamiento es realmente genérico.

---

# Decisión arquitectónica

## Mantener

* `profileService`;
* hooks independientes por operación;
* componentes de sección;
* React Query;
* estado local de formularios;
* NotificationsTable como consumidor de otras features;
* uso de AuthContext para información estrictamente relacionada con autenticación/sesión.

## Revisar

* query key `user`;
* limpieza duplicada de QueryClient;
* fuente de verdad de username/email;
* ubicación de ConfirmDeletionDialog;
* estrategia de traducción de notificaciones;
* código de preferencias no utilizado;
* tipos aparentemente huérfanos;
* comportamiento real del backend al cambiar contraseña.

## No hacer

* No dividir profile en muchas subfeatures únicamente porque tenga varias secciones.
* No introducir una capa de servicios adicional.
* No crear un event bus para comunicar profile con notifications/groupgoals.
* No introducir React Hook Form únicamente por uniformidad.
* No mover modelos a `shared` sin comprobar reutilización real.
* No eliminar campos de respuestas solo porque una pantalla concreta no los utilice.
* No optimizar bundle sin medir primero.

---

# Estado final del audit

### Mantener sin cambios

La estructura general del módulo, los hooks, el uso de React Query, el estado local de formularios y `profileService`.

### Corregir probablemente

1. Invalidación `user` de `useChangePassword`, si la búsqueda global confirma que no existe esa query.
2. `queryClient.clear()` redundante después de `logout`.
3. Ubicación de `ConfirmDeletionDialog`, si realmente es genérico.
4. Mensajes de mutaciones sin traducción.

### Investigar antes de tocar

1. Sincronización `AuthContext.user` / `profile`.
2. Comportamiento backend del cambio de contraseña y las sesiones.
3. Reglas de validación de username/email.
4. Uso real de preferencias y tipos aparentemente huérfanos.
5. Responsabilidad funcional exacta de `NotificationsTable`.
6. Impacto real de las dependencias cruzadas en bundle.

### Conclusión

Profile es un buen ejemplo de una feature que **puede ser relativamente grande sin necesitar una arquitectura complicada**.

El trabajo principal aquí no sería “refactorizar profile”, sino **limpiar unos cuantos restos, eliminar duplicaciones y comprobar contratos globales**. El módulo debería seguir siendo una página compuesta por secciones independientes, en lugar de convertirlo en una jerarquía de capas adicionales.

---

# Savings Goals

## Valoración general

`savingsgoals` es una feature cohesionada y relativamente sencilla.

Su responsabilidad principal es gestionar metas de ahorro individuales:

* listado;
* búsqueda;
* paginación;
* creación;
* edición;
* eliminación;
* contribuciones.

La estructura general sigue correctamente el patrón:

**page → componentes/hooks → API**

React Query gestiona server state, el estado local se utiliza para UI y formularios, y no existen stores globales ni capas innecesarias.

No se observa lógica financiera compleja duplicada en frontend.

### Conclusión general

No necesita una refactorización arquitectónica.

El trabajo recomendable consiste en:

* corregir algunos detalles concretos;
* eliminar duplicaciones pequeñas;
* comprobar contratos con backend;
* revisar código posiblemente obsoleto.

La filosofía debe seguir siendo **mantener el módulo sencillo**, no añadir abstracciones.

---

# Lo que está bien y debería mantenerse

## 1. Cohesión del módulo

Todas las operaciones giran alrededor de `SavingsGoal`.

No hay una mezcla evidente con funcionalidades de otras features.

---

## 2. Página relativamente ligera

`SavingsGoalsPage` actúa principalmente como compositor.

Mantiene:

* búsqueda;
* meta seleccionada;
* apertura/cierre de diálogos;
* coordinación de algunas mutaciones.

No contiene reglas financieras importantes.

No hace falta dividirla artificialmente.

---

## 3. React Query para server state

La lista de metas vive en React Query.

No se copia la respuesta completa a `useState`.

Esto debe mantenerse.

---

## 4. Hooks de mutations separados

Existe una mutation específica para cada operación:

* crear;
* actualizar;
* eliminar;
* contribuir.

Es una separación clara y suficientemente sencilla.

No hace falta un `SavingsGoalManager` ni una abstracción equivalente.

---

## 5. Formularios manuales razonables

Los formularios son pequeños y no requieren necesariamente React Hook Form.

Mantener estado local es perfectamente válido mientras el número de campos y reglas siga siendo reducido.

---

## 6. Cálculos de presentación simples

El porcentaje de progreso calculado en `SavingsGoalCard` es una derivación visual sencilla.

No constituye por sí mismo lógica de negocio financiera que deba trasladarse al backend.

---

## 7. Invalidación sencilla

Las mutations invalidan el listado de `savings-goals`.

Es un mecanismo simple y comprensible.

No introducir actualizaciones optimistas ni sincronizaciones complejas salvo que aparezca una necesidad real.

---

## 8. Confirmación de borrado

La eliminación de una meta requiere confirmación y respeta el estado pending.

Es un patrón correcto.

---

## 9. Formato de moneda compartido

El uso de `formatCurrency` desde shared evita duplicar la lógica de formato monetario.

---

## 10. Pocas dependencias pesadas

La feature no depende directamente de:

* gráficos;
* Framer Motion;
* React Hook Form;
* sistemas globales de estado.

Esto encaja bien con el objetivo de mantener el frontend ligero.

---

## 11. Enlaces externos tratados correctamente

El enlace externo se abre con una configuración segura de nueva pestaña.

No se observa una problemática especial de seguridad en esta parte.

---

# Problemas confirmados o con alta probabilidad

## 1. ContributionModal no refleja claramente el estado pending

La operación de añadir contribución modifica un dato financiero y el modal aparentemente no recibe `isPending`.

### Problema

Durante una petición lenta el usuario podría pulsar varias veces y generar varias contribuciones.

### Prioridad

**Alta.**

No porque el componente sea arquitectónicamente malo, sino porque es una operación donde un doble envío puede tener consecuencias reales.

### Solución conceptual

El estado pending de `useAddContribution` debe llegar hasta el botón de confirmación del modal y bloquear nuevas confirmaciones mientras la operación está en curso.

La responsabilidad sigue siendo sencilla: página/hook coordinan la mutación y el modal representa su estado.

---

## 2. `SavingsGoalEditDialog` puede conservar datos editados anteriormente

El estado del formulario permanece dentro del diálogo.

### Problema

Si el componente permanece montado y se vuelve a abrir, podría conservar datos locales anteriores en determinadas secuencias.

### Prioridad

**Alta/Media.**

### Revisar

Comprobar exactamente cuándo se monta/desmonta el componente y qué ocurre cuando:

* se cancela una edición;
* se reabre la misma meta;
* cambia la meta seleccionada.

### Solución conceptual

El formulario debe sincronizarse claramente con la meta que está siendo editada y resetearse al comenzar una nueva edición.

No hace falta introducir una librería de formularios para resolverlo.

---

# Problemas de complejidad o duplicación

## 3. Doble notificación de errores en create/update

Las mutations y los diálogos parecen gestionar el mismo error.

### Problema

Puede producirse:

* un toast genérico desde el hook;
* otro toast con el mensaje del backend desde el diálogo.

### Consecuencia

Feedback duplicado y responsabilidad ambigua.

### Prioridad

**Media.**

### Solución conceptual

Elegir una sola capa responsable de mostrar el error al usuario.

La mutation puede encargarse del estado y el error, mientras la UI decide cómo mostrarlo, o puede mantenerse una estrategia centralizada de notificación, pero no ambas simultáneamente.

---

## 4. Validación y saneado de `link` duplicados

Los diálogos validan el enlace y las mutations también lo sanean.

### Problema

La misma intención funcional está repartida en varios sitios.

### Prioridad

**Media.**

### Solución conceptual

Separar claramente:

* validación UX;
* normalización del payload.

Y evitar repetir la misma lógica en create y edit.

Una pequeña utilidad propia de la feature podría ser suficiente. No hace falta moverla a `shared` salvo que exista uso real fuera de savingsgoals.

---

## 5. Lista de prioridades duplicada

Creación y edición parecen mantener la misma lista de prioridades.

### Problema

Existe duplicación pequeña.

### Prioridad

**Baja.**

### Solución

Mantener una única constante dentro de `features/savingsgoals/constants`.

Este es exactamente el tipo de limpieza pequeña que merece la pena hacer.

---

# Contratos que requieren comprobación con backend

## 6. `status` enviado durante update

`UpdateSavingsGoalDTO` incluye `status`, aunque la UI no parece permitir modificarlo.

### Esto puede significar dos cosas

Puede ser correcto si:

* el backend exige el estado actual en el PUT;
* el endpoint utiliza un DTO completo;
* el estado forma parte del contrato de actualización.

También puede ser innecesario si:

* el estado no es editable;
* las transiciones las controla exclusivamente backend;
* el endpoint realmente admite actualización parcial.

### No modificar todavía

Hay que comparar este punto con el backend antes de tocarlo.

### Prioridad

**Alta como punto de investigación, no como bug confirmado.**

---

## 7. Estado de las metas

Existen estados como:

* ACTIVE;
* PAUSED;
* COMPLETED;
* CANCELLED.

La UI no parece gestionar todas sus transiciones.

Esto no es automáticamente un problema.

Puede ser perfectamente válido que:

* backend gestione determinados estados;
* una meta llegue con un estado que solo se muestra;
* algunas operaciones existan únicamente en backend.

Debe comprobarse el contrato real.

---

## 8. Progreso y `targetAmount`

El frontend calcula:

* currentAmount;
* dividido entre targetAmount;
* limitado visualmente a 100%.

Eso está bien como presentación.

### Solo hay que comprobar

Que backend garantice siempre `targetAmount > 0`.

Si esa garantía existe, no hay problema.

Si no existe, habría que contemplarlo.

No hace falta duplicar reglas financieras complejas en frontend.

---

# Service HTTP

## 9. `savingsGoalsService`

El service tiene sentido, pero presenta una pequeña inconsistencia.

### Actualmente

Las mutations utilizan el service.

El listado paginado utiliza `apiClient` directamente.

Además, aparecen métodos como:

* `getAll`;
* `getById`;

sin consumidor visible.

### No considero esto un problema arquitectónico grave

El service sigue siendo razonable.

### Lo que hay que decidir

Comprobar globalmente si `getAll` y `getById` tienen consumidores.

Si no:

* pueden eliminarse;
* o puede revisarse si el service debe representar también correctamente el listado paginado.

No conviene mantener métodos únicamente porque “quizá se usen algún día”.

### Importante

Tampoco hace falta forzar que absolutamente todas las peticiones pasen por un service si el endpoint paginado necesita una forma específica y el proyecto ya acepta ese patrón.

La consistencia es deseable, pero no a costa de añadir complejidad artificial.

---

# ConfirmDeletionDialog

## 10. Dependencia con `categories`

`savingsgoals` importa `ConfirmDeletionDialog` desde `categories`.

Esto no es un problema funcional, pero sí una dependencia estructural innecesaria si el componente es verdaderamente genérico.

### Revisar

Buscar todos sus consumidores.

Si aparece en:

* categories;
* groupgoals;
* investments;
* savingsgoals;
* u otras features;

entonces la ubicación natural es `shared`.

Si solo se utiliza en unos pocos contextos muy específicos y el componente realmente depende de concepts de categories, entonces no debería moverse.

### Prioridad

**Media/Baja.**

---

# Dashboard y cache cross-feature

## 11. Posible invalidación insuficiente

Las mutations de savingsgoals invalidan el listado de savings goals.

Eso es correcto para la propia feature.

La cuestión pendiente es si otras partes de la aplicación consumen datos agregados de savings goals.

### Revisar

Comprobar si dashboard tiene queries que dependan de:

* ahorro acumulado;
* progreso de metas;
* número de metas;
* cualquier otra métrica derivada de savings goals.

Si existe esa dependencia, esas queries deben formar parte de la estrategia de invalidación.

Si no existe, no añadir ninguna invalidación.

No conviene invalidar dashboard “por si acaso”.

---

# Formularios

## 12. No hace falta React Hook Form

La ausencia de React Hook Form no es un problema.

Los formularios tienen pocos campos y la lógica es sencilla.

### Mantener

* estado local;
* validación explícita;
* pending state;
* reset.

### No hacer

Introducir React Hook Form únicamente para igualar otros módulos.

---

# Estado

## 13. `selectedGoal` y `deletingGoal` no son una duplicación problemática

Aunque contienen referencias a metas que también existen en React Query, su función es diferente.

Representan:

* qué meta se está editando;
* qué meta se está contribuyendo;
* qué meta está pendiente de eliminación.

Son estado de UI.

No deben confundirse con una segunda cache de server state.

### Mantener

Esta estrategia mientras el estado se limite a coordinar diálogos.

---

# Gestión de errores

## 14. Unificar el patrón

El módulo parece mezclar:

* notificaciones desde hooks;
* manejo de error desde componentes.

El problema principal no es qué patrón se elija, sino que una misma operación tenga dos responsables.

### Objetivo

Cada mutation debería tener una estrategia clara:

**API error → mutation → único mecanismo de feedback**

No hace falta construir un sistema nuevo de errores para conseguirlo.

---

# Código potencialmente muerto

## Revisar globalmente

### `savingsGoalsService.getAll`

Probablemente obsoleto porque el listado actual es paginado.

### `savingsGoalsService.getById`

No tiene consumidor visible.

### Estados de SavingsGoal

Algunos existen en los tipos pero no tienen interacción visible.

Esto no implica eliminarlos.

### Tipos y propiedades

No deben eliminarse simplemente porque una tarjeta no los utilice.

El contrato puede representar correctamente la respuesta completa del backend.

---

# Decisiones que no deben tomarse todavía

## No eliminar `status`

Primero comparar con backend.

## No eliminar `getById`

Primero búsqueda global.

## No añadir invalidaciones del dashboard

Primero comprobar consumidores reales.

## No mover todo a shared

Solo `ConfirmDeletionDialog` es un candidato claro, y únicamente si se confirma que es genérico.

## No mover el cálculo de progreso al backend

No hay una necesidad clara: como cálculo puramente visual, puede permanecer en frontend.

---

# Priorización final

## Corregir

### 1. Protección contra doble envío de contribuciones

Es el punto funcional más importante del módulo.

### 2. Sincronización/reset del formulario de edición

Evitar reutilización accidental de valores locales antiguos.

### 3. Eliminar doble notificación de errores

Una sola fuente de feedback.

### 4. Reducir duplicación de validación/saneado de enlaces

Mantener responsabilidades claras.

### 5. Centralizar la lista de prioridades

Pequeña limpieza.

---

## Investigar

### 1. Contrato de `status` en update

Determinar si realmente debe viajar en el PUT.

### 2. Reglas backend de targetAmount

Confirmar que nunca puede ser cero.

### 3. Comportamiento de `useServerPagination`

Especialmente:

* cambio de búsqueda;
* reset de página;
* número de elementos solicitados;
* cache por página.

### 4. Consumidores de `getAll` y `getById`

Buscar globalmente antes de eliminarlos.

### 5. Dependencias de dashboard

Confirmar si savings goals afecta a alguna query agregada.

### 6. Uso global de ConfirmDeletionDialog

Determinar si debe pasar a shared.

### 7. Validación backend de links

Confirmar qué parte de validación/normalización necesita realmente frontend.

---

# No hacer

* No aplicar DDD al frontend.
* No introducir una nueva capa de dominio.
* No crear repositories.
* No introducir stores globales.
* No crear event buses.
* No introducir React Hook Form solo por uniformidad.
* No duplicar cálculos financieros para “asegurar consistencia”.
* No mover lógica a backend simplemente porque existe una función frontend.
* No invalidar queries de otras features sin comprobar que realmente dependan de savings goals.
* No convertir componentes pequeños en abstracciones genéricas innecesarias.

---

# Conclusión

`savingsgoals` es una de las features que puede mantenerse prácticamente con su estructura actual.

La arquitectura:

**SavingsGoalsPage → componentes → hooks → API**

es adecuada para el objetivo del proyecto.

No hay señales de una sobrearquitectura ni de una acumulación de lógica de negocio en frontend.

El trabajo real aquí consiste en **pulir**:

* protección de operaciones financieras;
* ciclo de vida del formulario de edición;
* duplicación de errores;
* validación de enlaces;
* pequeñas inconsistencias del service.

El punto que todavía no permite cerrar completamente la auditoría es la comparación con backend, especialmente alrededor de `status`, contribuciones y reglas de las metas.

Por tanto, el estado del módulo queda:

**Arquitectura: mantener.**

**Lógica: mayoritariamente correcta.**

**Refactor grande: no.**

**Limpieza puntual: sí.**

**Comparación con backend: necesaria para cerrar los puntos pendientes.**

---

# Transactions

## Valoración general

`transactions` es una de las features más grandes del frontend, pero su tamaño está justificado por la funcionalidad que agrupa.

Gestiona dos conceptos estrechamente relacionados:

* transacciones puntuales;
* transacciones recurrentes o planificadas.

Además incluye:

* listado;
* búsqueda;
* filtros;
* paginación;
* métricas;
* creación;
* edición;
* eliminación;
* selección de categorías;
* información de recurrencia.

La estructura general sigue siendo razonable:

**TransactionsPage → componentes → hooks/API → backend**

No necesita una reestructuración arquitectónica grande.

Sin embargo, sí existe un problema estructural importante: **no todas las mutations utilizan el mismo camino**. Algunas pasan por `useTransactionMutations` y otras llaman directamente a `apiClient` desde componentes.

Ese diseño explica varias inconsistencias del módulo y es el principal punto a corregir.

---

# Lo que está bien y debería mantenerse

## 1. TransactionsPage es un compositor

La página principal mantiene muy poca lógica:

* periodo;
* layout;
* composición de secciones.

No contiene lógica financiera ni controla directamente las mutations.

Debe mantenerse así.

---

## 2. Puntuales y recurrentes están separados

Utilizar componentes y endpoints distintos para:

* transactions;
* planned-transactions;

es razonable y refleja una diferencia funcional real.

No hay necesidad de fusionarlos artificialmente.

---

## 3. Server state correctamente delegado a React Query

Las listas y métricas son server state.

No existe una copia masiva innecesaria de transacciones en `useState`.

Esto debe mantenerse.

---

## 4. Métricas financieras vienen del backend

Ingresos, gastos, balance y demás métricas se consultan al backend.

El frontend no intenta reproducir el cálculo financiero completo.

Esta decisión es correcta.

---

## 5. Categorías mediante `categoryId`

La transacción se relaciona con la categoría mediante identificador estable.

Esto es especialmente positivo porque evita el problema que vimos en `categories` de depender del nombre de la categoría para relacionar datos.

---

## 6. Formularios relativamente sencillos

Aunque hay varios formularios, siguen siendo suficientemente simples como para que el estado local sea razonable.

No hace falta introducir React Hook Form solo por consistencia.

---

## 7. Confirmación de operaciones destructivas

Eliminar transacciones puntuales o recurrentes requiere confirmación.

Debe mantenerse.

---

## 8. Filtros y paginación server-side

Search, type y period se envían al backend.

Esto evita descargar grandes cantidades de transacciones para filtrarlas en el navegador.

---

# Problema estructural principal

## 1. Mutations duplicadas: hooks centralizados frente a `apiClient` directo

Actualmente existe `useTransactionMutations`, pero varias operaciones no lo utilizan.

### Operaciones con hook

* createTransaction;
* createRecurring.

### Operaciones con llamada directa

* updateTransaction;
* deleteTransaction;
* updateRecurring;
* deleteRecurring.

Esto provoca diferencias en:

* invalidaciones;
* notificaciones;
* manejo de errores;
* estados pending;
* comportamiento posterior a la operación.

### Consecuencia

Una misma feature tiene dos formas distintas de modificar datos.

No es necesario crear una capa nueva para solucionarlo.

### Solución conceptual

Elegir un único camino para las mutations de transactions.

La opción natural, dado que ya existe `useTransactionMutations`, es que las operaciones pasen por esos hooks.

### Prioridad

**Alta.**

Este es el principal refactor que merece la pena hacer en el módulo.

---

# Invalidaciones

## 2. Invalidaciones inconsistentes

El hook centraliza una invalidación amplia de información relacionada con transacciones:

* transactions;
* transactionMetrics;
* dashboardMetrics;
* historyChart;
* categoryStats;
* budgets;
* recentTransactions;
* categoryExpenses;
* plannedTransactions.

En cambio, las operaciones directas invalidan subconjuntos distintos.

### Consecuencia

Una edición o borrado puede dejar datos stale en otras partes de la aplicación.

### Especialmente relevante

Esto es importante porque ya sabemos que dashboard consume información derivada de transactions.

### Solución conceptual

Centralizar la política de invalidación de las mutations.

No significa invalidar absolutamente todo siempre; significa que las operaciones equivalentes deben tener una estrategia coherente basada en consumidores reales.

### Prioridad

**Alta.**

---

# Recurrencia

## 3. `calculateNextExecution` es un punto de investigación importante

La feature calcula en frontend la próxima ejecución de una transacción recurrente.

Esta función depende de:

* fecha inicial;
* frecuencia;
* intervalo;
* fecha final;
* fecha actual.

Esto va más allá de un simple formateo.

### La cuestión correcta

Determinar si `nextExecution` es un dato que conceptualmente pertenece al estado de la recurrencia.

Si backend ya calcula o puede calcular la próxima ejecución, el backend debería ser la fuente canónica.

### Riesgos de mantener cálculos paralelos

* diferencias con backend;
* casos de fin de mes;
* años bisiestos;
* intervalos;
* timezone;
* comportamiento distinto entre frontend y backend.

### No modificar todavía

Primero comprobar cómo representa backend la próxima ejecución de una recurrencia.

### Prioridad

**Alta como investigación.**

---

# Signo de los importes

## 4. Inconsistencia en recurrencias

El audit detecta una diferencia importante:

* creación recurrente → importe con signo según income/expense;
* edición recurrente → importe positivo.

Esto no demuestra todavía que exista un bug, pero es suficientemente sospechoso como para investigarlo.

### Hay que determinar

Cuál es realmente el contrato del backend:

* `amount` siempre positivo + `type`;
* `amount` firmado;
* combinación de ambos;
* normalización backend.

### Objetivo

Que creación y edición utilicen exactamente el mismo contrato.

### Prioridad

**Alta como investigación.**

---

# Fechas

## 5. Gestión temporal potencialmente delicada

La creación de una transacción puntual utiliza la fecha actual del navegador.

Además:

* frontend trabaja con `Date`;
* existe formateo local;
* existe cálculo local de recurrencias.

Esto puede ser correcto, pero abre la posibilidad de inconsistencias entre:

* fecha local;
* UTC;
* `datetime`;
* `date-only`.

### Revisar con backend

Especialmente:

* tipo real de `date`;
* serialización;
* timezone;
* interpretación del día de la transacción.

### No introducir una solución todavía

Primero conocer el contrato backend.

### Prioridad

**Media/Alta como investigación.**

---

# Categorías

## 6. CategorySelect puede estar limitado por paginación

`CategorySelect` utiliza `useCategories`.

El punto que hay que verificar es si ese hook devuelve:

* todas las categorías disponibles;
* o únicamente una página.

Si devuelve una única página, un usuario con muchas categorías podría no poder seleccionar todas.

### Importante

Esto no está demostrado únicamente por el uso de `useServerPagination`.

Hay que revisar la implementación real de `useCategories`.

### Prioridad

**Media.**

---

# Moneda

## 7. Formato de moneda hardcoded

Las filas parecen mostrar importes con una combinación específica de:

* locale español;
* euro.

Esto contrasta con la existencia de utilidades compartidas para formato monetario.

Además, la aplicación tiene internacionalización.

### Mejora

Utilizar el formato monetario compartido, evitando duplicar:

* locale;
* símbolo;
* decimales.

### Prioridad

**Media.**

---

# Errores

## 8. Gestión inconsistente de errores

El módulo mezcla varios patrones:

* toast desde hooks;
* errores inline desde diálogos;
* `console.error` desde operaciones de borrado;
* ausencia de error visible en las listas.

### Problema principal

No existe una estrategia uniforme.

### Consecuencia

Algunas operaciones tienen buen feedback y otras pueden fallar sin una explicación clara al usuario.

### Mejora

Establecer un patrón coherente para mutations y operaciones de lista.

La solución no necesita una infraestructura nueva.

### Prioridad

**Media.**

---

# 9. Doble feedback de error

En creación de transacciones y recurrentes:

* la mutation puede mostrar un toast;
* el diálogo captura también el error.

Esto puede producir dos mensajes para una misma operación.

### Mejora

Una única capa debe ser responsable del feedback al usuario.

### Prioridad

**Media.**

---

# Búsqueda

## 10. Debounce pendiente de comprobar

La búsqueda parece ejecutarse conforme cambia el texto.

No se observa debounce específico dentro del módulo.

### No asumir que existe un problema

Hay que comprobar:

* `useServerPagination`;
* comportamiento del input;
* frecuencia real de las peticiones.

Si cada carácter provoca una petición y se considera problemático, introducir debounce sería una mejora razonable.

### Prioridad

**Baja/Media, según implementación compartida.**

---

# Estados de error de listas

## 11. Falta de error state visible

Las listas parecen diferenciar correctamente loading y empty, pero no presentan claramente error.

Esto puede generar una UI ambigua:

> “No hay transacciones”

cuando en realidad:

> “No se pudo cargar la lista”.

### Mejora

Distinguir claramente:

* loading;
* error;
* vacío real;
* búsqueda sin resultados.

No hace falta un sistema específico para transactions si ya existe un patrón compartido.

### Prioridad

**Media.**

---

# Mutations aparentemente no utilizadas

## 12. Revisar mutations huérfanas

Dentro de `useTransactionMutations`, aparentemente:

* updateTransaction;
* deleteTransaction;
* updateRecurring;
* deleteRecurring;

no tienen consumidores visibles.

Esto probablemente es consecuencia directa de la lógica duplicada actual.

### Acción

Buscar globalmente.

Después decidir:

* utilizar esas mutations;
* o eliminarlas.

No mantener ambas implementaciones.

### Prioridad

**Alta.**

---

# Componentes de edición

## 13. La edición de recurrentes merece revisión funcional

`EditRecurringTransactionDialog` no permite editar exactamente los mismos datos que la creación.

Puede conservar elementos de la entidad que no aparecen como campos editables.

Esto no es automáticamente incorrecto.

### Revisar

Qué campos permite modificar realmente backend y qué intención funcional tiene la edición de recurrentes.

### Prioridad

**Media.**

---

# Skeletons

## 14. Posible duplicación menor

Existen estructuras de skeleton similares entre:

* transacciones puntuales;
* recurrentes.

No es un problema importante.

No merece una abstracción hasta que exista duplicación real que dificulte mantenimiento.

### Prioridad

**Baja.**

---

# ConfirmDeletionDialog

## 15. Dependencia con `categories`

Transactions usa el `ConfirmDeletionDialog` de categories.

Ya hemos visto el mismo patrón en otros módulos.

A estas alturas, si la búsqueda global confirma que se utiliza en:

* categories;
* groupgoals;
* investments;
* savingsgoals;
* transactions;

hay suficientes evidencias para considerarlo realmente un componente compartido.

### Prioridad

**Media/Baja.**

---

# Lógica financiera

## 16. Lo que sí debe permanecer en frontend

Es correcto que frontend realice:

* mostrar signo visual según type;
* aplicar `Math.abs` para presentación;
* formatear importes;
* calcular un porcentaje puramente visual cuando corresponda.

No hay que mover automáticamente todo cálculo al backend.

---

## 17. Lo que debe seguir dependiendo del backend

El backend debe ser la autoridad sobre:

* importe real;
* signo definitivo si forma parte del modelo;
* balance;
* métricas;
* permisos;
* propiedad;
* validez de categorías;
* reglas de recurrencia;
* generación de futuras transacciones;
* estados;
* consistencia financiera.

---

# React Query

## 18. La política de cache necesita centralización

La estrategia general de React Query es buena.

El problema es que las operaciones directas hacen que cada componente tenga que conocer qué queries debe invalidar.

Eso es precisamente lo que debería evitarse.

### Objetivo

Que la UI diga conceptualmente:

> “actualizar transacción”

y la mutation conozca qué información derivada debe invalidarse.

Esto simplifica los componentes sin necesidad de una arquitectura adicional.

---

# Estado y fuentes de verdad

## 19. El estado local actual es razonable

No considero problemático que existan estados como:

* transacción seleccionada;
* transacción a eliminar;
* filtros;
* apertura de diálogos.

Son estados de UI.

No deben eliminarse en favor de un store global.

---

# Bundle y rendimiento

## 20. No hay una señal de problema grave

Aunque transactions utiliza varias librerías y algunos componentes compartidos, no hay evidencia suficiente para afirmar que el módulo tenga un problema de bundle grave.

El principal punto estructural que puede influir en code splitting es la dependencia de `ConfirmDeletionDialog` desde categories.

Debe medirse antes de optimizar.

---

# Priorización final

## Corregir

### 1. Unificar las mutations

Eliminar la convivencia de:

* `useTransactionMutations`;
* `apiClient` directo desde componentes.

### 2. Unificar invalidaciones

Las operaciones equivalentes deben tener un comportamiento consistente.

### 3. Eliminar mutations duplicadas o empezar a utilizarlas

Después de comprobar usos globales.

### 4. Unificar feedback de errores

Evitar toast + error duplicado y `console.error` sin feedback.

### 5. Usar el formato monetario compartido

Eliminar locale/moneda hardcoded si la utilidad compartida cubre el caso.

---

## Investigar

### 1. `calculateNextExecution`

Comprobar si backend debe proporcionar la próxima ejecución.

### 2. Contrato de `amount`

Especialmente la diferencia entre create/edit de recurrentes.

### 3. Contrato de fechas

Especialmente timezone y fecha de transacción.

### 4. `CategorySelect`

Comprobar si puede seleccionar todas las categorías o solo una página.

### 5. `useServerPagination`

Comprobar reset de página y posible debounce.

### 6. Dependencias del dashboard

Confirmar qué queries dependen realmente de transacciones.

### 7. `ConfirmDeletionDialog`

Buscar todos sus consumidores y moverlo a shared si corresponde.

---

## No hacer

* No introducir DDD frontend.
* No crear repositories.
* No crear managers.
* No introducir stores globales para filtros.
* No crear event buses.
* No introducir React Hook Form automáticamente.
* No mover toda la lógica de presentación al backend.
* No mover el cálculo de porcentajes visuales al backend por sistema.
* No invalidar todas las queries de la aplicación indiscriminadamente.
* No fusionar transactions y planned-transactions si backend los trata como conceptos distintos.
* No refactorizar `TransactionsPage` únicamente para reducir líneas.

---

# Estado del módulo

### Arquitectura

**Correcta en términos generales.**

### Complejidad

**Justificada por el tamaño funcional.**

### Principal problema

**Mutations implementadas por dos caminos distintos.**

### Riesgo financiero

**Especialmente concentrado en el contrato de `amount` y fechas recurrentes.**

### Riesgo temporal

**`calculateNextExecution` necesita contraste con backend.**

### Refactor grande

**No necesario.**

### Limpieza/refactor puntual

**Sí, claramente recomendable.**

---

# Conclusión

`transactions` no necesita una arquitectura nueva. La estructura general es buena y sigue el objetivo del proyecto.

Pero, a diferencia de `savingsgoals`, aquí sí hay una inconsistencia que merece una corrección directa:

**no tiene sentido que existan mutations centralizadas y que después los componentes las salten llamando directamente a `apiClient`.**

Corregir eso probablemente resolverá varias de las observaciones secundarias de una sola vez:

* invalidaciones;
* pending;
* notificaciones;
* manejo de errores;
* código duplicado.

Después hay que cerrar con backend los dos puntos delicados:

**recurrencia/next execution** y **semántica del amount**.

Una vez resueltos esos puntos, `transactions` puede seguir siendo una feature grande, pero perfectamente manejable sin añadir más arquitectura.

---

# src/lib

## Responsabilidad

`src/lib` contiene infraestructura transversal de bajo nivel utilizada por toda la aplicación.

Actualmente los archivos analizados son:

* `i18n.ts`;
* `cn.ts`.

No contiene lógica de negocio ni estado de features.

La estructura es pequeña y apropiada.

---

# i18n.ts

## Responsabilidad

Configuración global de internacionalización.

Incluye:

* inicialización de i18next;
* integración con React;
* detección del idioma;
* carga diferida de bundles;
* idiomas soportados;
* namespaces;
* idioma fallback;
* sincronización de `<html lang>`.

## Evaluación

La ubicación en `src/lib` es correcta.

No debe convertirse en un servicio de negocio ni trasladarse a una feature concreta.

---

## Decisiones correctas

### Carga dinámica por idioma

Cada idioma se carga mediante import dinámico.

Esto evita incluir todos los idiomas en el bundle inicial.

### Namespaces por feature

Las traducciones están separadas conceptualmente por áreas:

* auth;
* dashboard;
* transactions;
* categories;
* investments;
* groupGoals;
* savingsGoals;
* notifications;
* etc.

Esto encaja con la organización del frontend.

### Fallback

Existe `en` como fallback global.

### Idiomas soportados

Existe una lista explícita de idiomas permitidos.

### Sincronización de `document.lang`

El idioma activo de i18next se refleja en el atributo `lang` del elemento HTML.

### Debug

El modo debug está limitado a desarrollo.

### Escape

La configuración de interpolación es adecuada para React.

---

# Punto a revisar

## Loader de idiomas

El loader obtiene directamente el cargador asociado a `language`.

La configuración actual presupone que i18next solo solicitará valores presentes en `localeLoaders`.

La lista de idiomas soportados hace que normalmente se cumpla.

### Prioridad

Baja/Media.

### Acción

No introducir una nueva abstracción.

Únicamente valorar una protección defensiva si se quiere hacer el loader más robusto.

---

# Namespaces

La lista central de namespaces es apropiada.

### Revisar

Comprobar periódicamente:

* namespaces registrados sin traducciones;
* traducciones existentes que ya no están registradas.

Esto es limpieza de código, no un problema arquitectónico.

---

# Carga por namespace

Aunque el loader recibe `language` y `namespace`, técnicamente importa el bundle completo del idioma y posteriormente extrae el namespace solicitado.

Esto es aceptable para el tamaño actual de la aplicación.

### No hacer

No dividir cada idioma en archivos por namespace únicamente para aumentar la granularidad del lazy loading.

La complejidad adicional no está justificada sin un problema real de bundle.

---

# Relación con shared

La existencia de utilidades de formato i18n en `shared` no entra en conflicto con `src/lib/i18n.ts`.

La separación conceptual es correcta:

**src/lib/i18n.ts**
→ infraestructura/configuración.

**shared/utils**
→ funciones que consumen esa infraestructura.

No mover por uniformidad.

---

# cn.ts

## Responsabilidad

Combinar clases CSS mediante `clsx` y resolver conflictos Tailwind mediante `tailwind-merge`.

## Evaluación

Es una utilidad pequeña, transversal y sin estado.

No contiene complejidad innecesaria.

### Mantener

La implementación y la ubicación pueden permanecer.

### No hacer

No convertirla en hook, service o abstracción adicional.

---

# Estado general de src/lib

## Mantener

* `i18n.ts`;
* `cn.ts`;
* carga dinámica por idioma;
* namespaces separados;
* fallback;
* sincronización de `document.lang`.

## Revisar

* robustez del acceso `localeLoaders[language]`;
* namespaces realmente usados;
* posibles traducciones huérfanas.

## Corregir

No hay actualmente ninguna corrección urgente derivada de los archivos analizados.

## No hacer

* No crear una capa adicional de i18n.
* No mover la configuración global a una feature.
* No dividir los bundles de idioma sin necesidad.
* No crear abstracciones alrededor de `cn`.
* No introducir lógica de negocio en `src/lib`.

## Conclusión

`src/lib` está muy bien reducido a infraestructura transversal.

La implementación actual es simple, coherente con el resto del frontend y no muestra señales de sobrearquitectura.

El único punto técnico que merece vigilancia es la robustez del loader de idiomas, pero no constituye un problema estructural del proyecto.

---

# Shared

## Valoración general

La capa `shared` contiene principalmente infraestructura y reutilización transversal:

* cliente HTTP;
* navegación;
* hooks reutilizables;
* paginación;
* temas;
* idiomas;
* constantes.

La estructura general es adecuada.

No se observa lógica de negocio financiera importante, pero sí hay algunos componentes compartidos cuya responsabilidad o ubicación debe vigilarse porque pueden propagar decisiones incorrectas a muchas features.

---

# shared/api/axiosClient.ts

## Responsabilidad

Centralizar la configuración HTTP de Axios y comportamientos transversales:

* base URL;
* JSON;
* timeout;
* cookies de sesión;
* manejo global de 401;
* notificaciones globales de errores.

La responsabilidad es apropiada para `shared/api`.

---

## Decisiones correctas

### Cliente HTTP único

Evita configuraciones Axios diferentes en cada feature.

### `withCredentials`

Coherente con la autenticación mediante sesión/cookies.

### Manejo global de 401

El cliente no depende directamente de AuthContext.

Utiliza el evento global `auth:logout`, dejando que auth gestione el estado de autenticación.

### Opciones `skipGlobalErrorNotify` y `skipAuthErrorHandler`

Permiten excepciones controladas sin crear clientes Axios alternativos.

---

## Problema importante: doble notificación

El interceptor notifica globalmente errores HTTP por defecto.

Varias mutations de features también notifican sus propios errores.

Esto puede provocar:

**interceptor → toast**

y simultáneamente:

**mutation/componente → toast**

### Consecuencia

Es probablemente la causa de varias dobles notificaciones observadas en:

* profile;
* savingsgoals;
* transactions.

### Prioridad

Alta.

### Acción conceptual

Definir una única estrategia para cada tipo de error.

Cuando una feature necesita presentar el error de forma específica, debe poder evitar el feedback global mediante `skipGlobalErrorNotify`.

No eliminar automáticamente el interceptor global.

---

## Interceptor de request vacío

Existe un interceptor de request que únicamente devuelve la configuración recibida.

Actualmente no aporta comportamiento.

### Acción

Eliminarlo.

### Prioridad

Baja.

---

## `sessionExpiredNotified`

La protección contra múltiples 401 es razonable.

Sin embargo, una respuesta exitosa vuelve a poner el flag a `false`.

### Estado

Revisión menor.

### Acción

No rediseñar salvo que aparezcan problemas reales con múltiples peticiones concurrentes.

---

## `isAuthEndpoint`

Utiliza comparación mediante `includes`.

Actualmente funciona para los endpoints definidos.

### Estado

Mejora menor de robustez.

No requiere prioridad.

---

# shared/config/navigation.ts

## Responsabilidad

Centralizar el menú principal y su configuración:

* icono;
* label;
* path;
* sección;
* orden;
* enabled.

La idea de registry es válida.

---

## Problema: rutas duplicadas

Los paths del navigation registry también aparecen en las constantes de rutas de `app/router`.

### Consecuencia

Puede existir una misma ruta definida en dos lugares.

### Acción conceptual

Buscar una ubicación transversal adecuada para que exista una única fuente de verdad de las rutas, sin hacer que `shared` dependa de `app`.

### Prioridad

Media.

---

## `enabled`

Actualmente todas las entradas visibles tienen `enabled: true`.

### Revisar

Comprobar si existe algún consumidor o mecanismo que pueda cambiar realmente este valor.

Si no existe, es metadata innecesaria.

---

## `order`

El orden coincide actualmente con el orden del array.

### Revisar

Si nunca se modifica dinámicamente, podría eliminarse.

No hacerlo antes de comprobar consumidores.

---

## `getDisabledNavItems`

Buscar uso global.

Si no tiene consumidores, eliminarlo.

---

# shared/constants/CATEGORY_COLORS

## Responsabilidad

Mapa de nombres de categorías a clases visuales.

## Problema potencial importante

Las categorías son definidas por usuario y pueden cambiar de nombre.

Por tanto, depender de nombres concretos como `Alimentación`, `Hogar` o `Transporte` no es una estrategia estable.

El modelo real de categories ya proporciona un color por categoría.

### Acción

Buscar consumidores globalmente.

### Si no se usa

Eliminar.

### Si se usa

Revisar inmediatamente qué comportamiento resuelve y sustituirlo por el color real de la categoría cuando corresponda.

### Prioridad

Media/Alta según uso.

---

# shared/hooks/useCategories

## Responsabilidad

Obtener categorías paginadas y reutilizables.

La abstracción es razonable.

---

## Problema potencial: listados frente a selectores

Utiliza:

* serverSize 50;
* displaySize 10.

Esto funciona para una lista paginada.

Puede no ser suficiente para selectores que necesitan conocer todas las categorías.

### Revisar

Comprobar consumidores como `CategorySelect`.

No crear automáticamente otro hook hasta confirmar el problema real.

---

## Normalización del search

La petición utiliza `search.trim()` pero la query key utiliza el valor sin normalizar.

Esto puede generar entradas de cache diferentes para búsquedas equivalentes.

### Prioridad

Baja.

---

# shared/hooks/usePlannedTransactions

## Responsabilidad

Consulta paginada de planned-transactions.

La implementación es coherente con useCategories.

### Revisar

Comprobar todos los consumidores.

Si únicamente transactions lo utiliza, considerar moverlo a esa feature.

Si lo consumen varias features, mantenerlo en shared.

---

## Normalización del search

Misma observación que en `useCategories`.

---

# shared/hooks/useServerPagination

## Responsabilidad

Proporcionar paginación visual más pequeña sobre respuestas server-side mayores.

La idea es válida y debe mantenerse.

---

## Problema: `initialPage`

La API permite una página inicial configurable, pero el effect de reset establece la página a cero al iniciar.

### Consecuencia

`initialPage` distinto de cero no se respeta.

### Acción

O soportarlo correctamente o eliminar la opción.

### Prioridad

Media.

---

## Problema potencial: placeholderData

Al cambiar de server page, `placeholderData(previousData)` puede conservar temporalmente la respuesta anterior mientras `displayPage` ya apunta al nuevo chunk.

### Consecuencia

Podrían aparecer momentáneamente elementos de la página anterior mientras se carga la nueva.

El prefetch reduce la frecuencia del caso, pero no lo elimina completamente.

### Prioridad

Media.

---

## Prefetch

El prefetch del siguiente chunk está bien planteado.

Debe mantenerse.

---

## Dependencia del array `queryKey`

Los consumidores construyen arrays nuevos y el effect de prefetch depende del array.

Esto puede provocar reevaluaciones innecesarias.

No parece generar peticiones duplicadas mientras React Query tenga datos frescos.

### Prioridad

Baja.

---

## `queryKey.join(",")`

Funciona como una detección sencilla de cambio de filtros.

No merece sustituirse por una infraestructura más sofisticada.

---

# shared/hooks/useCountUp

## Responsabilidad

Animar visualmente un valor numérico.

Es pura lógica de presentación.

### Evaluación

Correcto y bien aislado.

### Mantener

Sin cambios importantes.

### Mejora menor

Podría aceptar únicamente valores numéricos finitos en lugar de limitarse a evitar `NaN`.

Prioridad baja.

---

# shared/hooks/useAvailableLanguages

## Responsabilidad

Obtener los idiomas soportados directamente desde la configuración de i18next.

Es una buena fuente única de verdad.

### Evaluación

Correcto.

No duplicar la lista de idiomas en este hook.

---

# shared/hooks/useTheme

## Responsabilidad

Gestionar:

* tema actual;
* persistencia en localStorage;
* aplicación de variables CSS;
* selección por nombre;
* detección inicial del modo oscuro del sistema.

La responsabilidad es apropiada para shared.

---

## Punto a comprobar

El hook contiene estado local.

Si múltiples componentes utilizan `useTheme()` simultáneamente, cada uno tendrá su propio estado.

### Acción

Buscar consumidores.

Si existe un único consumidor raíz, no hay problema.

Si existen varios consumidores que necesitan reaccionar a cambios globales, habrá que valorar una fuente única de estado.

### No hacer

No introducir ThemeContext sin comprobar primero que realmente existan varios consumidores.

---

## Persistencia

La persistencia en localStorage es sencilla y adecuada.

---

## Aplicación de variables CSS

Se realiza en un effect al cambiar el tema.

Correcto.

---

# Código transversal aparentemente innecesario

## Revisar globalmente

* `getDisabledNavItems`;
* metadata `enabled`;
* metadata `order`;
* posibles consumidores de `CATEGORY_COLORS`;
* posibles consumidores únicos de `usePlannedTransactions`;
* posibles consumidores múltiples de `useTheme`.

---

# Decisiones que deben mantenerse

* Un único cliente Axios.
* Manejo transversal de autenticación 401.
* Utilidad `cn`.
* Hook `useCountUp`.
* Fuente única de idiomas mediante i18next.
* Hooks compartidos cuando realmente existe reutilización.
* `useServerPagination` como abstracción común.

---

# Decisiones que no deben hacerse

* No crear otro Axios por feature.
* No crear un service HTTP genérico encima de Axios.
* No crear un store global para categorías.
* No sustituir `useServerPagination` por una arquitectura más compleja.
* No mover todos los hooks a shared simplemente por ser hooks.
* No mantener constantes específicas de categorías en shared si no tienen uso real.
* No crear un ThemeContext sin verificar primero los consumidores.
* No crear un sistema de navegación más sofisticado.

---

# Prioridad de trabajo

## Alta

1. Resolver la doble notificación global/local de errores.
2. Revisar `CATEGORY_COLORS` y eliminarlo si está obsoleto.

## Media

3. Corregir/decidir el comportamiento real de `initialPage`.
4. Revisar `placeholderData` en `useServerPagination`.
5. Eliminar duplicación de rutas.
6. Revisar si `useCategories` sirve correctamente tanto para listas como para selectores.

## Baja

7. Eliminar interceptor request vacío.
8. Revisar metadata de navigation posiblemente innecesaria.
9. Revisar consumidores de `useTheme`.
10. Normalizar search antes de construir query keys.
11. Revisar pequeñas optimizaciones del prefetch.

---

# Conclusión

`shared` está globalmente bien planteado.

No hay una sobrearquitectura importante.

El principal riesgo no está en la existencia de los hooks compartidos, sino en **compartir comportamientos demasiado específicos**:

* `CATEGORY_COLORS` puede codificar una visión antigua de las categorías;
* `useCategories` mezcla potencialmente necesidades de listado y selector;
* `useTheme` necesita comprobar si realmente actúa como estado global;
* `navigation` duplica parte de las rutas.

Y el hallazgo más importante de todo el bloque es `axiosClient`: **el manejo global de errores está compitiendo con el manejo de errores de las features**.

Resolver eso debería simplificar indirectamente varios módulos que ya hemos auditado.

---

# Shared

## Valoración general

`src/shared` contiene infraestructura transversal y reutilización real entre features.

La estructura actual es adecuada para el objetivo del proyecto, pero aquí hay que ser algo más cuidadoso que en las features porque cualquier decisión de `shared` se propaga a toda la aplicación.

No se observa una sobrearquitectura importante.

Las piezas principales analizadas son:

* `api`;
* `config`;
* `constants`;
* `hooks`;
* `themes`;
* `types`;
* `utils`.

La regla general para esta carpeta debe ser:

> Solo debe vivir aquí aquello que sea realmente transversal.

---

# 1. shared/api/axiosClient.ts

## Responsabilidad

Es el cliente HTTP global de la aplicación.

Centraliza:

* base URL;
* headers;
* timeout;
* cookies;
* interceptor de autenticación;
* manejo global de errores;
* notificación de errores;
* excepciones mediante `skipGlobalErrorNotify`;
* excepciones mediante `skipAuthErrorHandler`.

La ubicación y responsabilidad son correctas.

---

## Lo que está bien

### Cliente único

Toda la aplicación puede utilizar una configuración HTTP común.

### `withCredentials`

Coherente con la autenticación por cookie.

### 401 global

El cliente no conoce directamente `AuthContext`.

Emite `auth:logout` y auth decide cómo reaccionar.

Esta separación es buena.

### Flags de comportamiento

`skipGlobalErrorNotify` y `skipAuthErrorHandler` proporcionan una forma sencilla de permitir excepciones puntuales.

---

# 2. Problema importante: notificaciones globales frente a notificaciones de features

El interceptor muestra un `notify.error` para errores que no sean 401, salvo que se indique `skipGlobalErrorNotify`.

Ya se ha observado en varias features que los hooks/componentes también muestran sus propios errores.

Esto crea potencialmente:

**API → interceptor → notificación**

y simultáneamente:

**API → mutation/componente → notificación**

## Consecuencia

Duplicación de feedback.

Este patrón explica probablemente varios de los dobles errores detectados en:

* profile;
* savingsgoals;
* transactions.

## Decisión recomendada

Debe existir una política única.

Por ejemplo:

* errores genéricos/globales → interceptor;
* errores que la UI quiere presentar específicamente → feature con `skipGlobalErrorNotify`.

No es necesario eliminar el interceptor.

### Prioridad

**Alta.**

---

# 3. Interceptor de request vacío

Existe un interceptor de request que únicamente devuelve la configuración recibida.

Actualmente no aporta ninguna funcionalidad.

## Acción

Eliminarlo.

No necesita sustituto.

### Prioridad

**Baja.**

---

# 4. `sessionExpiredNotified`

La variable evita múltiples eventos de logout simultáneos.

La intención es correcta.

Sin embargo, cualquier respuesta exitosa vuelve a ponerla a `false`.

En escenarios de muchas peticiones concurrentes podría rearmarse mientras todavía existen peticiones antiguas fallando.

No hay evidencia suficiente para considerarlo bug funcional.

### Acción

Mantener salvo que aparezca un caso real de concurrencia problemática.

### Prioridad

Baja.

---

# 5. `isAuthEndpoint`

Actualmente comprueba endpoints mediante `includes`.

Funciona con las rutas actuales, pero es una comprobación permisiva.

No es una prioridad.

---

# 6. shared/config/navigation.ts

## Responsabilidad

Centralizar la configuración del menú:

* id;
* icono;
* label;
* path;
* enabled;
* section;
* order.

La idea de un navigation registry es válida.

---

# 7. Duplicación de rutas

Los paths de navegación aparecen también en el router.

Por ejemplo:

* dashboard;
* categories;
* transactions;
* investments;
* savings goals;
* group goals.

## Problema

Existe más de una fuente para la URL de una misma pantalla.

### Consecuencia

Cambiar una ruta obliga potencialmente a editar varios archivos.

## Solución conceptual

Tener las rutas en una ubicación verdaderamente transversal y hacer que:

* router;
* navigation;

consuman esa única fuente.

No hacer que `shared` dependa de `app`.

### Prioridad

**Media.**

---

# 8. Metadata `enabled` y `order`

Actualmente:

* las entradas visibles tienen `enabled: true`;
* `order` coincide con el orden del array.

Esto puede ser completamente válido si está preparado para configuración futura, pero también puede ser metadata que actualmente no aporta nada.

## Revisar

Buscar cómo se consumen.

Si no existe actualmente:

* desactivación dinámica;
* reordenación;
* configuración externa;

entonces parte de este modelo puede simplificarse.

No eliminarlo sin buscar consumidores.

---

# 9. `getDisabledNavItems`

Buscar uso global.

Si no tiene consumidores, es código innecesario.

---

# 10. shared/constants/CATEGORY_COLORS

## Problema potencial importante

El objeto asocia colores a nombres concretos:

* Alimentación;
* Hogar;
* Transporte;
* Suministros;
* Ocio.

Esto entra en conflicto conceptual con el modelo actual de categorías, donde:

* el usuario crea categorías;
* el nombre es editable;
* el color pertenece a la categoría.

Por tanto, el nombre no es una identidad estable.

## Acción

Buscar todos sus usos.

### Si no se usa

Eliminar.

### Si se usa

Revisar la funcionalidad que está resolviendo y sustituirla por la información real de la categoría cuando corresponda.

### Prioridad

**Media/Alta según consumidores.**

---

# 11. shared/hooks/useCategories

## Responsabilidad

Proporciona categorías con:

* búsqueda;
* paginación;
* server-side filtering.

Es una abstracción razonable.

---

## Problema potencial

La misma implementación puede estar sirviendo tanto para:

* listado paginado de categorías;
* selector de categorías.

El selector quizá necesite todas las categorías mientras que el listado necesita solo una página.

## Revisar

Especialmente el uso desde `CategorySelect`.

No asumir que existe un bug hasta comprobar cómo funciona `displayItems` y cuántas categorías puede devolver el backend.

---

## Normalización de búsqueda

Se utiliza `search.trim()` al construir la petición, pero la query key se basa en el valor sin normalizar.

Por tanto:

* `"foo"`;
* `" foo "`:

pueden generar keys diferentes para una petición equivalente.

### Prioridad

Baja.

---

# 12. shared/hooks/usePlannedTransactions

## Responsabilidad

Consulta planned-transactions con búsqueda, tipo y paginación.

La implementación es coherente.

## Ubicación

Hay que comprobar si realmente es shared.

### Mantener en shared si

Lo consumen varias features.

### Mover a transactions si

Solo existe un consumidor real y no hay intención de reutilización.

La regla aquí no debe ser “todos los hooks comunes van a shared”, sino reutilización demostrada.

---

# 13. shared/hooks/useServerPagination

## Responsabilidad

Gestionar una paginación visual menor sobre chunks server-side mayores.

La idea es buena y debe mantenerse.

---

## Funcionamiento

Conceptualmente:

**servidor: 50 elementos**

→

**frontend: 10 elementos por página visual**

→

**prefetch del siguiente chunk**

Es una optimización razonable.

---

# 14. Incoherencia de `initialPage`

El hook expone una opción `initialPage`, pero el effect de cambio de query key establece inmediatamente la página a cero.

Por tanto, una página inicial distinta de cero no parece quedar realmente soportada.

## Acción

Determinar si:

* `initialPage` se necesita;
* o debe desaparecer.

No mantener una opción pública que no cumple el contrato esperado.

### Prioridad

Media.

---

# 15. `placeholderData` y cambio de chunk

El hook conserva temporalmente los datos anteriores mientras carga otro server page.

En determinadas transiciones, `displayPage` puede haber cambiado mientras `data` todavía pertenece al chunk anterior.

El prefetch reduce el problema, pero no lo elimina en redes lentas.

## Acción

Verificar visualmente el comportamiento al cruzar el límite de un chunk.

No asumir que hay un bug sin reproducirlo.

### Prioridad

Media.

---

# 16. Prefetch del siguiente chunk

El prefetch está bien diseñado.

Solo carga el siguiente bloque cuando el usuario llega al final del bloque actual.

### Mantener

No sustituirlo por una estrategia más compleja sin necesidad.

---

# 17. shared/hooks/useCountUp

Responsabilidad exclusivamente visual.

La implementación mantiene:

* valor actual;
* valor inicial;
* target;
* requestAnimationFrame;
* delay;
* easing.

Está bien aislado.

## Mantener

No requiere refactor.

## Mejora menor

Podría tratar valores no finitos de forma más estricta.

No es importante.

---

# 18. shared/hooks/useAvailableLanguages

La fuente de idiomas es:

`i18n.options.supportedLngs`

Esto es correcto porque evita duplicar el catálogo de idiomas.

## Mantener

No crear una segunda lista global de idiomas.

---

# 19. shared/hooks/useTheme

## Responsabilidad

Gestionar:

* tema actual;
* persistencia;
* detección inicial;
* variables CSS.

Es una responsabilidad legítimamente compartida.

---

## Punto importante

El hook mantiene estado React local.

Por tanto, dos consumidores independientes tendrían dos estados React distintos, aunque ambos modifiquen las mismas variables CSS y el mismo localStorage.

## Revisar

Buscar todos los consumidores.

### Si hay un único consumidor

La implementación actual es suficiente.

### Si hay varios consumidores que deben mantenerse sincronizados

Entonces sí existe un problema de fuente de verdad y habría que valorar una solución global.

## No hacer todavía

No crear un `ThemeContext` sin comprobar el uso real.

---

# 20. shared/themes/palettes

## Responsabilidad

Definir los temas disponibles y sus colores.

Esto está correctamente separado de `useTheme`.

El hook gestiona comportamiento.

El archivo de themes define datos.

La separación es buena.

---

# 21. ThemeColors

La interfaz contiene todos los tokens necesarios para traducir un tema a variables CSS.

Esto permite que `useTheme` aplique los colores de forma homogénea.

### Mantener

No simplificar la interfaz eliminando tokens individualmente hasta comprobar qué consume el CSS.

---

# 22. Número de temas

Actualmente existen:

* Millete;
* Dark Millete;
* Rosé Millete;
* Ember Millete.

El array `THEMES` actúa como catálogo único para `useTheme`.

Correcto.

No hace falta una estructura más compleja.

---

# 23. shared/types/ApiError

## Responsabilidad

Representar mínimamente la estructura de error que puede devolver Axios/backend.

Es pequeño y razonable.

## Revisar

Comprobar que todos los errores realmente compatibles con esta estructura la utilicen.

No crear un sistema de errores global más elaborado sin necesidad.

---

# 24. shared/types/i18n.d.ts

## Responsabilidad

Extender los tipos de i18next para:

* namespaces;
* resources;
* traducciones;
* react-i18next.

La idea es buena y proporciona tipado estático de los recursos.

---

## Problema potencial importante

En el código proporcionado se declara:

`wiki: typeof enWiki`

pero no aparece el import de `enWiki`.

Si el archivo es exactamente como el proporcionado, esto puede provocar un error de compilación TypeScript.

### Acción

Comprobar el archivo real.

Si falta el import, corregirlo.

### Prioridad

**Alta si el archivo es exactamente así.**

---

## Segunda comprobación

`i18n.ts` registra `wiki` como namespace, mientras que el fragmento de importaciones de `i18n.d.ts` incluye varios namespaces pero no muestra `enWiki`.

Debe existir coherencia entre:

* namespaces registrados en i18n;
* resources tipados;
* bundles reales.

No debe haber tres catálogos diferentes manteniendo información parcialmente duplicada.

---

# 25. Notificaciones

## notify

El wrapper sobre Sonner está correctamente encapsulado.

La feature puede usar:

* success;
* error;
* info;
* warning.

Y la configuración visual queda en un único sitio.

Esto es una buena abstracción.

---

# 26. ToastContent

Responsabilidad correcta:

* render visual del toast;
* icono;
* descripción;
* cierre;
* traducción de aria-label.

No tiene lógica de negocio.

---

# 27. Posible sobreabstracción en notifications

`notify` sí aporta valor porque:

* unifica apariencia;
* evita repetir `toast.custom`;
* centraliza duración;
* centraliza estilos.

No lo eliminaría.

Tampoco crearía una segunda abstracción de notificaciones por feature.

---

# 28. shared/utils/formatDate

Responsabilidad:

* convertir una fecha a representación localizada.

Es correcta para shared.

---

## Punto a revisar

Utiliza `i18n.language` directamente y `toLocaleDateString`.

Esto está bien para fechas de presentación, siempre que el contrato de fechas del backend esté claro.

La lógica temporal de negocio no debería acabar aquí.

---

# 29. shared/utils/i18nFormat / formatCurrency

## Responsabilidad

Centralizar:

* locale;
* currency;
* formato de números;
* formato monetario.

Esto es muy útil y debe mantenerse.

Evita el problema que vimos en transactions de formatos hardcoded.

---

# 30. Problema pendiente de preferencias de moneda

`getCurrency()` intenta obtener:

`userPreferences`

desde `sessionCache`.

Pero ya está documentado que actualmente nadie escribe ese valor.

Por tanto, en la situación actual:

**getCurrency() → EUR**

prácticamente siempre.

## Consecuencia

Existe una implementación parcial de preferencias de moneda en shared aunque la UI de preferencias todavía no esté conectada.

### Acción

Elegir una de estas dos direcciones cuando se cierre profile/preferences:

* conectar correctamente las preferencias;
* eliminar esta dependencia hasta que exista una fuente real.

No mantener una falsa fuente de verdad.

### Prioridad

Media.

---

# 31. `COMMON_CURRENCIES`

Solo se precargan:

* EUR;
* USD;
* GBP;
* CHF;
* CAD;
* AUD;
* JPY.

Esto es suficiente si esas son las únicas monedas soportadas.

## Riesgo

Si `getCurrency()` recibe una moneda válida que no esté en esa lista, `formatCurrency` cae silenciosamente a EUR.

### Acción

Comparar con las monedas realmente permitidas por backend/producto.

### Prioridad

Media.

No asumir que hay un problema hasta conocer el contrato.

---

# 32. `formatNumber`

Tiene bastante lógica defensiva:

* límites de fraction digits;
* coherencia min/max;
* formatter cacheado;
* fallback.

Es más compleja que `formatDate`, pero la complejidad está relacionada con una utilidad transversal reutilizada.

No la simplificaría sin comprobar consumidores.

---

# 33. shared/utils/languages

## Responsabilidad

Transformar un código de idioma en información visual:

* código;
* nombre nativo;
* nombre inglés;
* bandera.

La responsabilidad es legítima.

---

## LANGUAGE_MAP

Es una fuente declarativa para los siete idiomas soportados.

Correcto.

---

## `getFlagFromCode`

El cálculo de bandera es genérico para códigos ISO de dos letras.

Está bien como fallback.

No debe interpretarse como una validación de que el código pertenece a un idioma soportado.

---

## `getLanguageFromCode`

Permite códigos no conocidos y genera información genérica.

Esto es útil para robustez.

---

## `getSupportedLanguages`

Parece no estar exportada.

### Revisar

Si no tiene consumidores dentro del archivo o globalmente, puede eliminarse.

Actualmente `useAvailableLanguages` obtiene los idiomas directamente desde i18next, por lo que podría ser una función heredada de un diseño anterior.

### Prioridad

Baja.

---

# 34. shared/utils/sessionCache

## Responsabilidad

Dar una pequeña API para `sessionStorage` con prefijo.

El contrato documenta correctamente que:

* no es almacenamiento seguro;
* cualquier JavaScript de la página puede leerlo;
* no deben almacenarse secretos.

Esta documentación es buena.

---

# 35. Punto sensible: `sessionId`

El helper almacena:

* user;
* sessionId.

El `user` puede ser razonable como caché de presentación.

`sessionId` merece más cuidado.

Aunque no sea el token real de autenticación y la sesión real esté protegida mediante cookie httpOnly, el valor puede seguir siendo información sensible dependiendo de cómo lo utilice backend.

Además, todo XSS puede leer `sessionStorage`.

## Acción

Comprobar exactamente qué representa `sessionId`:

* simple identificador de sesión para UI;
* o valor que por sí solo pueda utilizarse para operaciones sensibles.

Si solo sirve para comparar la sesión actual visualmente, probablemente sea tolerable.

Si tiene capacidad operativa, no debería almacenarse así.

### Prioridad

**Alta como revisión de seguridad.**

No afirmar que existe vulnerabilidad sin conocer el contrato backend.

---

# 36. Redundancia de `setItem`/`setUser`

`setUser` repite directamente la escritura en sessionStorage mientras que existe `setItem`.

No es un problema funcional.

Puede ser intencional para dejar explícito el contrato de user.

### Prioridad

Baja.

---

# 37. `sessionCache.clear`

Solo elimina:

* user;
* sessionId.

Eso es correcto si esos son los únicos datos que realmente gestiona el helper.

No debería transformarse en un “clear de todo sessionStorage”, porque la aplicación no es propietaria necesariamente de todas sus claves.

---

# 38. Relaciones con módulos ya auditados

## Transactions

`useServerPagination`, `usePlannedTransactions`, `useCategories`, `formatCurrency` y `ConfirmDeletionDialog` afectan directamente a los problemas detectados allí.

## Savings Goals

`useServerPagination`, formato monetario y `ConfirmDeletionDialog`.

## Profile

`sessionCache`, `formatCurrency` y posibles preferencias.

## Dashboard

`formatCurrency`, `useCountUp`, categorías y cache.

Esto demuestra por qué los cambios de shared deben hacerse con más cuidado.

---

# 39. Prioridad global

## Alta

### 1. Resolver estrategia global/local de errores

Evitar dobles notificaciones provocadas por `axiosClient` y las features.

### 2. Comprobar `sessionId` en `sessionCache`

Determinar si es solo un identificador visual o un dato sensible/operativo.

### 3. Comprobar `enWiki` en `i18n.d.ts`

Si falta realmente el import, corregir.

### 4. Revisar `CATEGORY_COLORS`

Especialmente si sigue siendo utilizado.

---

## Media

### 5. Unificar fuente de verdad para rutas

Evitar duplicación entre navegación y router.

### 6. Revisar `initialPage` de useServerPagination

### 7. Revisar transición entre chunks con placeholderData

### 8. Resolver la falsa fuente de preferencias de moneda

### 9. Comprobar CategorySelect frente a paginación de categorías

### 10. Comprobar monedas realmente soportadas

---

## Baja

### 11. Eliminar interceptor request vacío

### 12. Revisar `enabled`/`order` del navigation registry

### 13. Revisar `getDisabledNavItems`

### 14. Revisar `getSupportedLanguages`

### 15. Pequeñas mejoras de robustez

---

# 40. Mantener

* `apiClient` único.
* Manejo global de 401 mediante evento.
* `skipGlobalErrorNotify` y `skipAuthErrorHandler`.
* `cn`.
* `notify`.
* `ToastContent`.
* `useCountUp`.
* `useAvailableLanguages`.
* `useServerPagination`.
* `useTheme`, mientras tenga una única fuente de uso coherente.
* `formatDate`.
* `formatCurrency`.
* `formatNumber`.
* catálogo de temas.
* tipado de i18next.
* sessionCache como wrapper mínimo, con el contrato de seguridad actual.

---

# 41. No hacer

* No crear una arquitectura nueva para `shared`.
* No crear un service genérico para Axios.
* No crear un sistema global de estado únicamente para resolver preferencias.
* No convertir `useServerPagination` en una abstracción más compleja.
* No mover todos los hooks a `shared` por ser “reutilizables potencialmente”.
* No meter lógica específica de una feature dentro de `shared`.
* No mantener constantes de negocio antiguas en shared por compatibilidad imaginaria.
* No crear un segundo sistema de traducción.
* No eliminar contratos completos solo porque una pantalla no utiliza todos sus campos.

---

# Conclusión

`shared` está **bien planteado en líneas generales**, pero aquí sí conviene hacer una limpieza selectiva porque sus decisiones se propagan a todas las features.

El hallazgo más importante es `axiosClient`: el sistema de errores global está compitiendo con el sistema de errores de las features y eso ya se manifiesta en varios módulos.

El segundo punto que merece atención especial es `sessionCache`, concretamente `sessionId`: hay que confirmar que sea un identificador puramente visual y no un dato que permita realizar operaciones sobre la sesión.

Después están varias limpiezas razonables:

* eliminar `CATEGORY_COLORS` si ya no tiene uso;
* resolver la duplicación de rutas;
* corregir el contrato de `initialPage`;
* revisar `placeholderData`;
* conectar o eliminar la falsa lectura de preferencias de moneda;
* comprobar la consistencia del tipado de i18next.

La filosofía general debe ser:

**shared debe contener soluciones realmente transversales, no acumular restos de funcionalidades antiguas ni conocimiento específico de las features.**
