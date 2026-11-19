# Usuarios, autenticación y perfil

El módulo `users` gestiona registro, inicio de sesión, sesiones web, perfil y preferencias. Los controladores son [`AuthController.java`](../../backend/src/main/java/com/puntomartinez/millete/users/infrastructure/in/controller/AuthController.java) y [`ProfileController.java`](../../backend/src/main/java/com/puntomartinez/millete/users/infrastructure/in/controller/ProfileController.java).

## Autenticación

| Método y ruta | Función |
|---|---|
| `POST /api/v1/auth/register` | Registro |
| `POST /api/v1/auth/login` | Login; emite cookie de autenticación y devuelve estado, username, email y sessionId |
| `POST /api/v1/auth/logout` | Revoca la sesión actual |
| `GET /api/v1/auth/me/topnav` | Datos mínimos de navegación |

El JWT se transporta en cookie configurada por `CookieAuthFactory`; la respuesta de login no contiene un token Bearer. Las rutas privadas requieren JWT válido y sesión activa. El logout identifica la sesión actual, la desactiva y expira la cookie con la misma configuración. El filtro común consulta la actividad de la sesión en cada petición autenticada.

`POST /register` recibe `username`, `email` y `password`. El DTO normaliza username/email vacíos o nulos a `null`; valida username con máximo 50, email con validador de formato y máximo 100, y password no blanco de 8–100 caracteres. El servicio requiere al menos username o email, comprueba unicidad de los valores presentes y admite registrar solo uno; no existe mínimo de longitud para username en este contrato. `POST /login` recibe `identifier` y `password`. El login responde `username`, `email` y `sessionId` además de estado; no devuelve un token para almacenamiento en el cliente. Las contraseñas se procesan mediante BCrypt. El endpoint `/api/v1/auth/telegram/status` no está expuesto por el controlador actual.

Los valores predeterminados de `CookieAuthFactory` son nombre `__Host-ms_token`, `Secure=true`, `HttpOnly=true`, `SameSite=Strict`, `Path=/` y edad máxima 86400 segundos; pueden configurarse mediante `jwt.cookie-*`. Para el prefijo `__Host-`, el constructor exige Secure y `Path=/`. `JwtAuthenticationFilter` comprueba firma/expiración y actividad de sesión en cada petición autenticada. No se deriva de la cookie una garantía general de ausencia de riesgo CSRF.

El registro también admite `timezone` como campo opcional. La aplicación web envía la zona IANA del navegador. La cuenta se crea con `localCurrency: "EUR"` y esa zona horaria, o `"UTC"` si no se recibe una zona admitida. Las preferencias quedan guardadas antes del primer inicio de sesión.

## Perfil

`ProfileController` expone `GET/PUT /api/v1/profile`, `PUT /password`, `GET/PUT /preferences`, `GET /sessions`, `DELETE /sessions/{sessionId}`, `DELETE /sessions` y `POST /deactivate`. Perfil modifica username/email según DTO; cambio de contraseña exige contraseña actual y nueva. Las preferencias contienen locale y zona horaria y se validan contra valores admitidos por los DTO/servicio. La identidad se extrae del principal autenticado. La consulta devuelve sesiones activas; la revocación individual y masiva responde `204`, y la desactivación responde `200` y expira la cookie. Las sesiones revocadas dejan de ser válidas al pasar por el filtro JWT.

`SecurityConfig` permite todas las solicitudes `OPTIONS` y únicamente `POST /api/v1/auth/register` y `POST /api/v1/auth/login` sin autenticación; las demás requieren autenticación. La configuración deshabilita CSRF explícitamente (`csrf.disable()`). CORS toma orígenes de `CORS_ALLOWED_ORIGINS` (predeterminado `http://localhost:5173`), permite credenciales y define métodos y cabeceras en esa configuración. SameSite Strict es configurable; no prueba por sí solo ausencia de todo riesgo CSRF.

## Preferencias y fechas locales

Las preferencias se persisten separadamente. [`UserLocalDateService.java`](../../backend/src/main/java/com/puntomartinez/millete/users/application/services/UserLocalDateService.java) convierte instantes a fechas locales según las preferencias del usuario para lógica que depende del día civil. El resto de los módulos recibe el reloj mediante [`TimeProvider`](../../backend/src/main/java/com/puntomartinez/millete/shared/domain/ports/out/TimeProvider.java).

Referencias: [`AuthenticationService.java`](../../backend/src/main/java/com/puntomartinez/millete/users/application/services/AuthenticationService.java), [`SessionPersistenceService.java`](../../backend/src/main/java/com/puntomartinez/millete/users/application/services/SessionPersistenceService.java), [`UserSession.java`](../../backend/src/main/java/com/puntomartinez/millete/users/domain/model/UserSession.java).
