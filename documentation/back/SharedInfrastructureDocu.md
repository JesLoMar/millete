# Infraestructura compartida

El paquete `shared` centraliza seguridad HTTP, excepciones comunes, paginación y acceso al tiempo. Configuración principal: [`SecurityConfig.java`](../../backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/SecurityConfig.java).

## Seguridad HTTP

La API es stateless. Solo `POST /api/v1/auth/register`, `POST /api/v1/auth/login` y las peticiones `OPTIONS` son públicas. El JWT se extrae de la cookie configurada (por defecto `__Host-ms_token`), no del encabezado Bearer. El filtro valida firma/expiración y comprueba en cada petición que el `sessionId` del token corresponda a una sesión activa; los tokens sin identidad de sesión o de sesión revocada no autentican. Véase [`JwtAuthenticationFilter.java`](../../backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/filter/JwtAuthenticationFilter.java).

### Login: IP y proxies de confianza

El límite del login admite 20 intentos por IP en una ventana de un minuto. Sin configuración, el servidor usa la IP de conexión e ignora `X-Forwarded-For`; las personas que comparten el proxy pueden compartir el límite hasta configurarlo.

Configure `MILLETE_TRUSTED_PROXIES` con una lista separada por comas de CIDR que correspondan únicamente a los proxies reales que conectan con el backend. No confíe en redes privadas completas ni en encabezados del proveedor sin validar primero el proxy de transporte. Con configuración válida, el backend examina `X-Forwarded-For` desde el salto más cercano y continúa hacia atrás solo mientras cada salto sea de confianza; entradas malformadas se ignoran. El mismo resultado validado se usa para limitar y para registrar la IP en el flujo de autenticación.

Ejemplos ilustrativos (las direcciones de documentación reservadas no son direcciones de despliegue; sustitúyalas por los rangos reales de su instalación):

```dotenv
# Caddy, si el backend solo recibe conexiones desde este proxy:
MILLETE_TRUSTED_PROXIES=192.0.2.10/32

# Cloudflare delante de Caddy: incluya los CIDR oficiales de Cloudflare
# y el CIDR real del proxy Caddy que conecta directamente al backend.
MILLETE_TRUSTED_PROXIES=198.51.100.0/24,192.0.2.10/32
```

Use los rangos IPv4/IPv6 publicados por [Cloudflare](https://developers.cloudflare.com/fundamentals/concepts/cloudflare-ip-addresses/) y la configuración de [encabezados de Caddy reverse_proxy](https://caddyserver.com/docs/caddyfile/directives/reverse_proxy#headers). La configuración del proxy y el aislamiento de red del backend son responsabilidad del operador. El actual `nginx $proxy_add_x_forwarded_for` puede conservarse: se procesa la cadena de derecha a izquierda; nginx también debe estar incluido en la lista de proxies confiables cuando conecta directamente al backend.

La cookie se configura mediante `jwt.cookie-name` (predeterminado `__Host-ms_token`), `jwt.cookie-secure` (`true`), `jwt.cookie-http-only` (`true`), `jwt.cookie-same-site` (`Strict`), `jwt.cookie-path` (`/`) y `jwt.cookie-max-age` (86400), y se crea con [`CookieAuthFactory.java`](../../backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/CookieAuthFactory.java). El factory rechaza `__Host-` con Secure desactivado o Path distinto de `/`. CORS lee `CORS_ALLOWED_ORIGINS` (predeterminado `http://localhost:5173`), permite credenciales y define métodos/cabeceras en `SecurityConfig`. La configuración deshabilita CSRF (`csrf.disable()`); SameSite Strict es configurable y no debe presentarse por sí sola como garantía general de ausencia de riesgo CSRF.

## Tiempo y tareas

Los servicios que dependen de hora inyectan `TimeProvider`; para fechas civiles del usuario existe `UserLocalDateService`. Esto permite aplicar la zona horaria configurada por el usuario donde corresponde y evitar que cada servicio consulte directamente el reloj del sistema.

[`TransactionScheduler.java`](../../backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/scheduler/TransactionScheduler.java) lanza el procesamiento recurrente diariamente a las 00:01. Su comportamiento de recuperación y control de fallos pertenece a `plannedtransactions`.

## Errores

Las excepciones de dominio compartidas están en [`shared/domain/exception`](../../backend/src/main/java/com/puntomartinez/millete/shared/domain/exception). No se debe asumir un único mapeo HTTP para excepciones no compartidas: controladores y advice pueden tener manejo específico. Los errores de validación web dependen del DTO y del manejador activo.

## Notificaciones

Notificaciones es un módulo separado, no infraestructura de transporte. Consulte [su guía](NotificationsDocu.md) para los endpoints vigentes.
