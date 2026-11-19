# Autenticación

El cliente mantiene la identidad autenticada en memoria. La sesión la gestiona una cookie HTTP-only del servidor; no se guarda ningún JWT en almacenamiento web.

## Arranque y sesión

[`AuthContext.tsx`](../../frontend/src/features/auth/context/AuthContext.tsx) consulta `GET /auth/me/topnav` al iniciar. Una respuesta válida establece nombre, email e identificador de sesión; un 401 limpia el estado, mientras un fallo no-401 marca `isOffline` y permite reintentar con `retryAuth`. El contexto no confunde indisponibilidad de red con sesión inválida. `sessionCache` conserva solo datos de presentación no sensibles en `sessionStorage`; no es autenticación ni protección frente a XSS ([`sessionCache.ts`](../../frontend/src/shared/utils/sessionCache.ts)).

El cliente Axios usa `withCredentials: true`; no añade un encabezado Bearer ([`axiosClient.ts`](../../frontend/src/shared/api/axiosClient.ts)). Un 401 de rutas no-auth puede emitir `auth:logout`; los errores de autenticación y solicitudes que lo omiten siguen rutas específicas.

## Login y registro

La página [`page.tsx`](../../frontend/src/features/auth/pages/page.tsx) ofrece login y registro. Los esquemas de [`auth.schema.ts`](../../frontend/src/features/auth/schemas/auth.schema.ts) validan contraseña y campos; `useLoginMutation` envía credenciales y luego recupera el perfil. El destino posterior al login se resuelve mediante `handlePostAuthSuccess` y `sanitizeRedirect`; el registro usa su propia secuencia de servicio/mutación.

Los servicios usan `POST /auth/login`, `POST /auth/register` y `GET /auth/me/topnav` ([`auth.service.ts`](../../frontend/src/features/auth/services/auth.service.ts)). El frontend documenta solo los datos que consume, no requisitos internos del backend.

## Cierre

`logout` limpia usuario, sesión y caché de React Query, y solicita `POST /auth/logout`. Llamadas concurrentes esperan la misma operación de logout. No hay persistencia de credenciales local.

## Validación y secuencias

`auth.schema.ts`: contraseña requiere al menos ocho caracteres; login exige `identifier` no vacío; registro exige username o email y valida formato de email solo si se proporciona. `AuthForm` selecciona esquema y campos para login/registro; campos se deshabilitan durante la mutación.

| Acción | Flujo |
|---|---|
| Login | `POST /auth/login` con `{identifier,password}`; `handlePostAuthSuccess` actualiza contexto y resuelve ruta interna mediante destino previo o parámetro redirect sanitizado. Error muestra mensaje API/fallback. |
| Registro | `POST /auth/register` con username/email/password y la zona horaria del navegador. La cuenta se crea con EUR como moneda local. Después se inicia sesión con el nombre de usuario o el correo; si falla el inicio automático, se informa y se permite continuar manualmente. |
| Rehidratación | Al montar se consulta `GET /auth/me/topnav` con timeout de 6 s. Respuesta 401 se trata como sesión no autenticada; indisponibilidad marca modo offline sin borrar por ello la sesión. `retryAuth` repite la comprobación. |
| Logout | Limpieza inmediata local, incremento de auth epoch, vaciado de React Query y `POST /auth/logout` con timeout corto; fallo de red no impide limpiar UI. |

La respuesta de perfil consumida incluye `username`, `email` y `sessionId`; el usuario visible deriva nombre de username o parte local del email. `isAuthenticated` se deriva de que haya usuario en memoria.
