# Informe de Auditoría de Seguridad — Millete v0.1.0

> **Tipo:** Revisión de seguridad estática pasiva (sin ejecución de código ni escaneos activos).  
> **Alcance:** Código fuente completo de `/backend`, `/frontend` e infraestructura (Docker Compose, Nginx, scripts).  
> **Arquitectura analizada:** Spring Boot 4.0.6 (Java 25) con DDD hexagonal; React 19.2.5 SPA; Docker Compose + Nginx 1.27.  
> **Idioma del informe:** Español.  
> **Fecha:** 2026-06-24

---

## 1. Executive Summary

La plataforma **Millete** presenta una arquitectura backend relativamente ordenada desde el punto de vista de la separación de responsabilidades (DDD hexagonal) y un frontend moderno con validaciones de formulario robustas. Sin embargo, la revisión ha identificado **múltiples vulnerabilidades reales**, especialmente en tres áreas críticas:

1. **Autorización a nivel de objeto (BOLA/IDOR)** en el módulo de metas compartidas (`groupgoals`) y en ciertos flujos transversales (importación, categorías), que permiten a un usuario autenticado manipular o leer recursos de otros usuarios.
2. **Gestión insegura de secretos y sesiones:** claves JWT y contraseñas de base de datos hardcodeadas en el repositorio, token JWT almacenado en `localStorage` tras una ofuscación XOR reversible, y tráfico HTTP sin cifrar en la configuración por defecto.
3. **Endurecimiento deficiente de infraestructura:** contenedores ejecutándose como `root`, puerto de PostgreSQL expuesto al host, ausencia de cabeceras de seguridad modernas (CSP, HSTS), y falta de bloqueos distribuidos en tareas programadas.

### Distribución de hallazgos

| Severidad | Cantidad |
|-----------|----------|
| Crítica   | 0 |
| Alta      | 10 |
| Media     | 14 |
| Baja      | 13 |
| **Total** | **37** |

> Nota: No se han asignado severidades "Críticas" únicamente porque ningún hallazgo por sí solo permite la ejecución remota de código (RCE) sin otra condición previa; no obstante, varios hallazgos "Alta" (IDOR + JWT forjable + DB expuesta) pueden encadenarse para comprometer cuentas y datos financieros.

---

## 2. Consolidated Vulnerability Log

| ID | Título | Severidad | Componente |
|----|--------|-----------|------------|
| SEC-01 | Clave JWT hardcodeada en `application.yml` | Alta | Backend / Config |
| SEC-02 | Contraseña PostgreSQL hardcodeada en `application.yml` | Alta | Backend / Config |
| SEC-03 | Puerto de PostgreSQL expuesto al host | Alta | Infraestructura / Docker |
| SEC-04 | Contenedor backend ejecutándose como `root` | Alta | Infraestructura / Docker |
| SEC-05 | Aportaciones a metas grupales sin verificar membresía (IDOR) | Alta | Backend / `groupgoals` |
| SEC-06 | Cálculo de aportaciones accesible sin autorización (IDOR) | Alta | Backend / `groupgoals` |
| SEC-07 | Actualización/borrado de miembros entre metas distintas (IDOR) | Alta | Backend / `groupgoals` |
| SEC-08 | Ofuscación XOR reversible del JWT en `localStorage` | Alta | Frontend / `secureStorage.ts` |
| SEC-09 | Datos de sesión sensibles persistentes en `localStorage` | Alta | Frontend / `AuthContext.tsx`, `secureStorage.ts` |
| SEC-10 | Tráfico HTTP sin cifrar / ausencia de TLS | Alta | Infraestructura / Nginx |
| SEC-11 | Endpoint de estado de Telegram revela vinculación arbitraria | Media | Backend / `users` |
| SEC-12 | Importación de datos permite apropiación de IDs ajenos | Media | Backend / `dataexport` |
| SEC-13 | Transacciones aceptan categorías de otros usuarios | Media | Backend / `transactions`, `categories` |
| SEC-14 | Filtro de rate-limit de login: race condition, spoofing y OOM | Media | Backend / `shared` |
| SEC-15 | Scheduler de transacciones recurrentes sin bloqueo distribuido | Media | Backend / `shared`, `plannedtransactions` |
| SEC-16 | Deserialización JSON insegura en importación de datos | Media | Backend / `dataexport` |
| SEC-17 | `GlobalExceptionHandler` filtra detalles internos | Media | Backend / `shared` |
| SEC-18 | Enumeración de usuarios en registro y login | Media | Backend / `users` |
| SEC-19 | Secretos inyectados como variables de entorno planas | Media | Infraestructura / Docker Compose |
| SEC-20 | Ausencia de cabeceras de seguridad modernas en Nginx | Media | Infraestructura / Nginx |
| SEC-21 | `/nginx_status` accesible desde rango privado demasiado amplio | Media | Infraestructura / Nginx |
| SEC-22 | Contenedor `db-backup` como `root` con escritura en host | Media | Infraestructura / Docker Compose |
| SEC-23 | `escapeValue: false` en `i18next` con interpolación de datos de usuario | Media | Frontend / `i18n.ts` |
| SEC-24 | Construcción de URLs de exportación sin codificación | Media | Frontend / `useExport.ts` |
| SEC-25 | `CategoryService.findById` sin filtro de propiedad (latente) | Baja | Backend / `categories` |
| SEC-26 | Falta de manejador global de excepciones | Baja | Backend / `shared` |
| SEC-27 | `@PreAuthorize` no habilitado (`@EnableMethodSecurity`) | Baja | Backend / `shared` |
| SEC-28 | Interceptor de Axios adjunta token a endpoints públicos | Baja | Frontend / `axiosClient.ts` |
| SEC-29 | Fallback a `localhost:8080` en `axiosClient.ts` | Baja | Frontend / `axiosClient.ts` |
| SEC-30 | Evento global `auth:logout` manipulable | Baja | Frontend / `axiosClient.ts`, `AuthContext.tsx` |
| SEC-31 | Callback global `window.__sidebarOpen` permite inyección | Baja | Frontend / `Sidebar.tsx`, `TopNav.tsx` |
| SEC-32 | Mensajes de error del backend mostrados directamente al usuario | Baja | Frontend / múltiples |
| SEC-33 | Validación de importación solo por extensión/tipo MIME | Baja | Frontend / `ImportModal.tsx` |
| SEC-34 | Validación insuficiente en `restore.sh` | Baja | Infraestructura / scripts |
| SEC-35 | Falta de `.env.example` | Baja | Infraestructura / proyecto |
| SEC-36 | `vite.config.ts` carga variables de entorno sin prefijo | Baja | Frontend / `vite.config.ts` |
| SEC-37 | Recursos externos (Google Fonts) sin integridad | Baja | Frontend / `index.html` |

---

## 3. Detailed Findings

### SEC-01: Clave JWT hardcodeada en `application.yml`

- **Severidad:** Alta
- **Componente:** Backend — `backend/src/main/resources/application.yml` (líneas 37-39)

#### Descripción técnica
El archivo `application.yml` contiene una clave JWT codificada en Base64 (`U3VwZXJTZWNy5XRfSldUX0tleV9Gb3JfVGVzdGluZ187MDI2X01pbGxldGUK`) directamente en el repositorio. Cualquier persona con acceso al código fuente puede extraerla y firmar tokens válidos para cualquier usuario.

#### Escenario de ataque / PoC
```bash
# 1. Clonar el repositorio y leer la clave.
# 2. Generar un token JWT con userId arbitrario firmado con HS256.
jwt_tool -S hs256 -p "<secret-base64-decodificado>" -t <token-válido-cualquiera>
# 3. Usar el token en Authorization: Bearer <token-forjado>.
```
El atacante accede como otro usuario sin conocer su contraseña.

#### Remediación
- Eliminar el valor real del repositorio.
- Externalizar obligatoriamente mediante variable de entorno.
- Generar al menos 256 bits aleatorios: `openssl rand -base64 64`.

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:43200000}
```
En `docker-compose.yml` o `.env`:
```bash
JWT_SECRET=$(openssl rand -base64 64)
```

---

### SEC-02: Contraseña PostgreSQL hardcodeada en `application.yml`

- **Severidad:** Alta
- **Componente:** Backend — `backend/src/main/resources/application.yml` (líneas 29-31 aprox.)

#### Descripción técnica
La contraseña de la base de datos (`654321`) está almacenada en texto plano en el repositorio. Si el despliegue no sobreescribe explícitamente `DATABASE_PASSWORD`, el backend se conecta con credenciales públicas.

#### Escenario de ataque / PoC
Si PostgreSQL es accesible (véase SEC-03), un atacante que conozca la contraseña por el código fuente puede conectarse directamente:
```bash
psql -h <ip-host> -U postgres -d millete_db
```

#### Remediación
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/millete_db}
    username: ${DATABASE_USER:postgres}
    password: ${DATABASE_PASSWORD:}
```
Usar Docker Secrets o un gestor de secretos en producción.

---

### SEC-03: Puerto de PostgreSQL expuesto al host

- **Severidad:** Alta
- **Componente:** Infraestructura — `docker-compose.yml` (líneas 19-20)

#### Descripción técnica
El servicio `postgres` publica el puerto 5432 en la interfaz del host (`"${DB_PORT:-5432}:5432"`). El backend es el único consumidor necesario de la base de datos.

#### Escenario de ataque / PoC
Si el host tiene una IP pública o está en una red comprometida:
```bash
nmap -p 5432 <ip-host>
psql -h <ip-host> -U postgres -d millete_db
```
Si además `.env` no existe, `POSTGRES_PASSWORD` puede quedar vacío y PostgreSQL aceptar autenticación `trust`.

#### Remediación
Eliminar el mapeo de puerto o restringirlo a loopback:
```yaml
postgres:
  # ports:
  #   - "127.0.0.1:5432:5432"
```
Si realmente se necesita acceso local, usar `127.0.0.1:5432:5432` y firewallar el puerto.

---

### SEC-04: Contenedor backend ejecutándose como `root`

- **Severidad:** Alta
- **Componente:** Infraestructura — `backend/Dockerfile` (líneas 19-26)

#### Descripción técnica
La imagen de runtime no define un usuario no privilegiado (`USER`). El proceso Java corre como `root` (uid 0) dentro del contenedor.

#### Escenario de ataque / PoC
Si se explota una vulnerabilidad en Spring Boot o en una dependencia, el atacante dispone de `root` en el contenedor, facilitando la escalada al host mediante monturas, socket de Docker o exploits de kernel.

#### Remediación
```dockerfile
FROM eclipse-temurin:25-jre-jammy
RUN groupadd -r millete -g 1001 && \
    useradd -r -g millete -u 1001 millete
WORKDIR /app
COPY --from=builder --chown=millete:millete /app/target/*.jar app.jar
USER millete
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD exec 3<>/dev/tcp/127.0.0.1/8080 && exec 3<&-
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### SEC-05: Aportaciones a metas grupales sin verificar membresía (IDOR)

- **Severidad:** Alta
- **Componente:** Backend — `groupgoals/application/services/GroupGoalService.java` (líneas 231-244); controlador (líneas 138-146)

#### Descripción técnica
El método `addContribution(goalId, userId, request)` nunca verifica que `userId` sea miembro activo de `goalId`. Inserta directamente una fila `GoalContribution`.

#### Escenario de ataque / PoC
```bash
POST /api/v1/goals/<arbitrary-goal-id>/contributions
{
  "amount": 9999.99,
  "date": "2026-06-24"
}
```
Cualquier usuario autenticado puede contaminar el total y la distribución de una meta a la que no pertenece.

#### Remediación
```java
GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
        .filter(GoalMember::isActive)
        .orElseThrow(() -> new RuntimeException("No eres miembro activo de esta meta"));
// Ahora sí guardar la contribución
```

---

### SEC-06: Cálculo de aportaciones accesible sin autorización (IDOR)

- **Severidad:** Alta
- **Componente:** Backend — `groupgoals/application/services/GroupGoalService.java` (líneas 72-79); controlador (líneas 132-136)

#### Descripción técnica
`GET /api/v1/goals/{goalId}/contributions` invoca `calculateContributions(goalId)` sin comprobar siquiera que el usuario autenticado esté relacionado con la meta.

#### Escenario de ataque / PoC
El atacante enumera `goalId` (UUIDs predecibles de alta entropía, pero los IDs podrían filtrarse por otros medios) y obtiene la lista de miembros, nombres, salarios, porcentajes personalizados y cálculos de aportación de cualquier meta.

#### Remediación
Pasar el `userId` del llamante al servicio y verificar membresía activa antes de calcular:
```java
public GoalContributionSummaryDTO calculateContributions(UUID goalId, UUID callerId) {
    goalMemberRepository.findByGoalIdAndUserId(goalId, callerId)
        .filter(GoalMember::isActive)
        .orElseThrow(() -> new RuntimeException("Meta no encontrada o sin acceso"));
    // ... resto del cálculo
}
```

---

### SEC-07: Actualización/borrado de miembros entre metas distintas (IDOR)

- **Severidad:** Alta
- **Componente:** Backend — `groupgoals/application/services/GroupGoalService.java` (líneas 151-168 y 216-229)

#### Descripción técnica
Tras verificar que el solicitante es administrador de `goalId`, el código carga el miembro objetivo solo por `memberId`, sin comprobar que pertenezca a `goalId`.

#### Escenario de ataque / PoC
Administrador de la Meta A realiza:
```bash
PUT /api/v1/goals/{goalA}/members/{memberOfGoalB}
DELETE /api/v1/goals/{goalA}/members/{memberOfGoalB}
```
Y modifica o elimina miembros de la Meta B.

#### Remediación
Cargar el miembro siempre dentro del ámbito de la meta:
```java
GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, targetUserId)
        .orElseThrow(() -> new RuntimeException("Miembro no encontrado en esta meta"));
```

---

### SEC-08: Ofuscación XOR reversible del JWT en `localStorage`

- **Severidad:** Alta
- **Componente:** Frontend — `frontend/src/shared/utils/secureStorage.ts` (líneas 1-38, 102-135)

#### Descripción técnica
La función `deriveFingerprint()` crea una clave a partir de datos públicos y predecibles (`navigator.userAgent`, dimensiones de pantalla, idioma, `hardwareConcurrency`, etc.). El hash es de 32 bits y se convierte a base36, con un espacio de claves muy pequeño. El "cifrado" es XOR cíclico contra esa clave, envuelto en Base64.

#### Escenario de ataque / PoC
Un atacante con acceso al almacenamiento (XSS, extensión de navegador comprometida o malware local) puede recuperar el token en segundos:
```js
const enc = localStorage.getItem('ms_token');
const seed = [
  navigator.userAgent, screen.colorDepth, screen.width,
  screen.height, navigator.language, navigator.hardwareConcurrency
].join('|');
let h = 0;
for (const c of seed) { h = ((h << 5) - h) + c.charCodeAt(0); h &= h; }
const key = Math.abs(h).toString(36);
const xor = (s, k) => s.split('').map((ch, i) =>
  String.fromCharCode(ch.charCodeAt(0) ^ k.charCodeAt(i % k.length))
).join('');
const jwt = xor(atob(enc), key).split('::')[0]; // token usable
```

#### Remediación
- **Opción recomendada:** no almacenar el token en el cliente. El backend debe emitir el JWT en una cookie `HttpOnly; Secure; SameSite=Strict` y el frontend debe confiar en ella, eliminando `secureStorage.setToken/getToken`.
- Si se mantiene el modelo SPA puro, mover el token a `sessionStorage` reduce el tiempo de exposición, aunque sigue siendo legible ante XSS. Eliminar la ofuscación XOR (no aporta seguridad real) para evitar una falsa sensación de protección.

---

### SEC-09: Datos de sesión sensibles persistentes en `localStorage`

- **Severidad:** Alta
- **Componente:** Frontend — `frontend/src/shared/utils/secureStorage.ts` (líneas 77-170) y `frontend/src/features/auth/context/AuthContext.tsx` (líneas 48-106)

#### Descripción técnica
Se almacenan en `localStorage` (prefijo `ms_`):
- `ms_token` → JWT de acceso.
- `ms_user` → objeto con `name` y `email`.
- `ms_sessionId` → identificador de sesión.
- `ms_userPreferences` → preferencias (moneda, idioma, tema).
Además, `theme-name` se guarda en texto plano (`useTheme.ts`).

#### Escenario de ataque
Cualquier script que se ejecute en el origen (XSS, extensión, dependencia comprometida) puede leer o exfiltrar estos valores. `localStorage` no expira ni se invalida al cerrar la pestaña.

#### Remediación
- Migrar la sesión a cookies `HttpOnly; Secure; SameSite=Strict` gestionadas por el backend.
- Limitar `localStorage` a datos puramente de UI no sensibles.
- Si se conserva `sessionStorage` para el token, implementar limpieza explícita al recibir 401/403 y al cerrar sesión.

---

### SEC-10: Tráfico HTTP sin cifrar / ausencia de TLS

- **Severidad:** Alta
- **Componente:** Infraestructura — `frontend/nginx.conf` (líneas 84-87)

#### Descripción técnica
El servidor único escucha solo en el puerto 80. No existe `listen 443 ssl`, ni certificados ni redirección a HTTPS.

#### Escenario de ataque / PoC
Un atacante en la misma red (Wi-Fi pública, red corporativa, ISP malicioso) captura el tráfico:
```bash
tshark -i eth0 -Y 'http.request || http.response' -T fields -e http.host -e http.request.uri
```
Las peticiones de login y el JWT viajan en texto claro.

#### Remediación
Terminar TLS en un balanceador/reverse proxy externo (recomendado) o configurar Nginx directamente:
```nginx
server {
    listen 80;
    return 301 https://$host$request_uri;
}
server {
    listen 443 ssl http2;
    ssl_certificate     /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    # ...
}
```

---

### SEC-11: Endpoint de estado de Telegram revela vinculación arbitraria

- **Severidad:** Media
- **Componente:** Backend — `users/infrastructure/in/controller/AuthController.java` (líneas 123-136)

#### Descripción técnica
`GET /api/v1/auth/telegram/status?chatId=...` está disponible para cualquier usuario autenticado y devuelve si el `chatId` está vinculado y, en caso afirmativo, el `userId` asociado, sin comprobar propiedad.

#### Escenario de ataque / PoC
El atacante itera una lista de `chatId` de Telegram y cosecha qué cuentas están vinculadas a usuarios de Millete y sus UUIDs.

#### Remediación
Restringir el endpoint al usuario autenticado:
```java
UUID currentUserId = ((JwtUser) authentication.getPrincipal()).getId();
User user = userRepository.findById(currentUserId).orElseThrow(...);
boolean linked = user.getTelegramChatId() != null && user.getTelegramChatId().equals(chatId);
```

---

### SEC-12: Importación de datos permite apropiación de IDs ajenos

- **Severidad:** Media
- **Componente:** Backend — `dataexport/application/services/DataImportService.java` (líneas 104-179)

#### Descripción técnica
Las entidades importadas conservan los UUID del JSON y se guardan con el `userId` del importador. JPA `save()` con un ID existente realiza un `merge`, sobrescribiendo la fila. Si el atacante conoce UUIDs de registros de otra persona, puede reasignarlos.

#### Escenario de ataque / PoC
1. El atacante obtiene (por otro IDOR o exportación filtrada) UUIDs de transacciones/inversiones/categorías de la víctima.
2. Envía un JSON de importación con esos UUIDs.
3. Tras `POST /api/v1/data/import`, los registros de la víctima pasan a pertenecer al atacante.

#### Remediación
Descartar los IDs importados y generar siempre nuevos UUIDs para el usuario importador:
```java
Category safeCat = new Category(
        UUID.randomUUID(), loggedInUserId, cat.getName(), ...
);
```
Igual para transacciones, transacciones planificadas e inversiones.

---

### SEC-13: Transacciones aceptan categorías de otros usuarios

- **Severidad:** Media
- **Componente:** Backend — `transactions/application/services/TransactionService.java` (líneas 27-30, 119-127); `plannedtransactions/application/services/PlannedTransactionService.java` (líneas 47-51, 127-130); `dashboard/application/services/DashboardService.java` (líneas 104, 171)

#### Descripción técnica
El registro y la actualización solo comprueban `categoryRepository.findById(...)` para verificar existencia, no propiedad. El `categoryId` ajeno se almacena y más tarde el dashboard lo resuelve por `findById(...)`, filtrando el nombre de la categoría de otro usuario.

#### Escenario de ataque / PoC
```bash
POST /api/v1/transactions
{
  "categoryId": "<uuid-categoría-víctima>",
  ...
}
```
El dashboard del atacante muestra el nombre privado de la categoría de la víctima.

#### Remediación
Usar búsquedas con propiedad en todas partes:
```java
categoryRepository.findByIdAndUserId(command.categoryId(), command.userId())
        .orElseThrow(() -> new RuntimeException("La categoría no existe o no pertenece al usuario"));
```

---

### SEC-14: Filtro de rate-limit de login: race condition, spoofing y OOM

- **Severidad:** Media
- **Componente:** Backend — `shared/infrastructure/config/filter/LoginRateLimitFilter.java` (líneas 20-117)

#### Descripción técnica
- `AttemptWindow.attempts++` no es atómico a pesar de ser mutado por múltiples hilos.
- `attemptsPerIp` no tiene límite; un atacante puede crear una entrada por IP spoofeada hasta agotar la memoria de la JVM.
- La IP del cliente se extrae de `X-Forwarded-For` sin validar un proxy de confianza, por lo que el rate-limit se evita trivialmente.
- El hilo de limpieza corre para siempre, pero solo cada minuto.

#### Escenario de ataque / PoC
```bash
for i in $(seq 1 1000); do
  curl -H "X-Forwarded-For: 1.2.3.$i" -X POST https://<host>/api/v1/auth/login -d '{...}'
done
```
Ninguna IP supera el límite de 5 intentos.

#### Remediación
Usar una caché con TTL y contadores atómicos, por ejemplo Caffeine:
```java
private final LoadingCache<String, AtomicInteger> attempts = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(k -> new AtomicInteger(0));
```
Y leer la IP de una fuente de confianza, o usar `request.getRemoteAddr()` cuando no haya proxies.

---

### SEC-15: Scheduler de transacciones recurrentes sin bloqueo distribuido

- **Severidad:** Media
- **Componente:** Backend — `shared/infrastructure/config/scheduler/TransactionScheduler.java` (líneas 16-20); `plannedtransactions/application/services/PlannedTransactionService.java` (líneas 80-112)

#### Descripción técnica
El cron se ejecuta en cada instancia del backend y selecciona todas las transacciones planificadas activas sin bloqueo a nivel de fila o distribuido. En un despliegue multi-instancia, la misma plantilla puede procesarse varias veces, generando transacciones duplicadas.

#### Escenario de ataque / PoC
Dos contenedores arrancan a las 00:01. Ambos ejecutan `processScheduledTasks()` y generan el mismo conjunto de transacciones recurrentes.

#### Remediación
Usar un bloqueo distribuido (ShedLock / advisory lock de PostgreSQL) y/o seleccionar plantillas `FOR UPDATE SKIP LOCKED`:
```sql
SELECT ... FROM planned_transactions WHERE active = true FOR UPDATE SKIP LOCKED
```

---

### SEC-16: Deserialización JSON insegura en importación de datos

- **Severidad:** Media
- **Componente:** Backend — `dataexport/application/services/DataImportService.java` (líneas 52-79)

#### Descripción técnica
`ObjectMapper.readValue(inputStream, UserDataSnapshot.class)` lee JSON arbitrario sin límites de tamaño, profundidad ni lista blanca de clases. Combinado con `@JsonIgnoreProperties(ignoreUnknown = true)`, es frágil: si se introducen tipos polimórficos en el futuro, podría habilitar cadenas de gadgets de deserialización.

#### Escenario de ataque / PoC
Subir un JSON muy grande o profundamente anidado → agotamiento de memoria o CPU. Futuros cambios podrían habilitar RCE a través de Jackson gadgets.

#### Remediación
- Configurar límites de multipart en Spring.
- Limitar profundidad de anidación de Jackson:
```java
objectMapper.getFactory()
    .setStreamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(100).build());
```
- Mantener un DTO estricto de importación separado de los modelos de dominio.
- Deshabilitar `defaultTyping` y no usar `@JsonTypeInfo` en el DTO de importación.

---

### SEC-17: `GlobalExceptionHandler` filtra detalles internos

- **Severidad:** Media
- **Componente:** Backend — `shared/infrastructure/in/controller/advice/GlobalExceptionHandler.java` (líneas 20-68)

#### Descripción técnica
Los manejadores de `RuntimeException` e `IllegalArgumentException` devuelven `ex.getMessage()` directamente. Errores de JPA/validación/transacciones pueden exponer nombres de tablas, columnas o estado interno. `MethodArgumentNotValidException` devuelve `validationErrors.toString()`, filtrando nombres de campos internos.

#### Escenario de ataque / PoC
Forzar una violación de restricción o un error de persistencia; el cuerpo de respuesta contiene el mensaje de error crudo de la base de datos.

#### Remediación
Devolver mensajes genéricos para tipos de excepción amplios; registrar el stack trace solo en servidor:
```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ErrorResponseDTO> handleRuntime(RuntimeException ex, HttpServletRequest req) {
    log.error("Unhandled runtime error", ex);
    return buildError(req, HttpStatus.BAD_REQUEST, "Ha ocurrido un error inesperado");
}
```

---

### SEC-18: Enumeración de usuarios en registro y login

- **Severidad:** Media
- **Componente:** Backend — `users/application/services/UserService.java` (líneas 43-48 y 68-81)

#### Descripción técnica
- El registro devuelve mensajes distintos para email existente vs. username existente, revelando qué identificadores están registrados.
- En login, si el identificador no existe se devuelve 400; si existe y la cuenta está bloqueada, se devuelve 423. La diferencia de estado revela cuentas válidas.

#### Escenario de ataque / PoC
```bash
POST /api/v1/auth/register {"email":"victim@x.com"}
# Respuesta: "El email … ya está registrado"
```
```bash
POST /api/v1/auth/login {"identifier":"victim"}
# 423 => cuenta válida y bloqueada; 400 => no existe
```

#### Remediación
- Registro: devolver un único mensaje genérico (`El usuario o el email ya están registrados`).
- Login: realizar una comparación de hash de contraseña simulada para usuarios inexistentes y devolver la misma secuencia de respuesta (por ejemplo, siempre ejecutar la lógica de `accountLockService` con un ID sintético determinista).

---

### SEC-19: Secretos inyectados como variables de entorno planas

- **Severidad:** Media
- **Componente:** Infraestructura — `docker-compose.yml` (líneas 50-58, 111-115)

#### Descripción técnica
`DATABASE_PASSWORD`, `JASYPT_ENCRYPTOR_PASSWORD` y `JWT_SECRET` se pasan como variables de entorno normales. Cualquier usuario con acceso al daemon de Docker puede verlos:
```bash
docker inspect millete-backend -f '{{ json .Config.Env }}'
```

#### Escenario de ataque / PoC
Un atacante que comprometa una cuenta con permisos `docker` en el host obtiene todas las credenciales de la base de datos y la clave de firma JWT.

#### Remediación
Usar Docker Secrets (disponible en Docker Swarm o como monturas de archivos en Compose moderno):
```yaml
secrets:
  jwt_secret:
    file: ./secrets/jwt_secret.txt
  db_password:
    file: ./secrets/db_password.txt
backend:
  secrets:
    - jwt_secret
    - db_password
  environment:
    JWT_SECRET_FILE: /run/secrets/jwt_secret
    SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password
```
El backend debe leer el archivo correspondiente en lugar de la variable directa.

---

### SEC-20: Ausencia de cabeceras de seguridad modernas en Nginx

- **Severidad:** Media
- **Componente:** Infraestructura — `frontend/nginx.conf` (líneas 150-153 y bloque de assets)

#### Descripción técnica
Solo se configuran `X-Frame-Options`, `X-Content-Type-Options` y `X-XSS-Protection`. Faltan `Strict-Transport-Security` (HSTS), `Content-Security-Policy`, `Referrer-Policy` y `Permissions-Policy`. Además, las cabeceras actuales están en `location /` y no se aplican a la ubicación de assets ni al proxy `/api/`.

#### Escenario de ataque / PoC
```bash
curl -I http://<host>
# No aparece Content-Security-Policy ni Referrer-Policy
```
Un futuro vector XSS podría ejecutar scripts inline o conectar con orígenes arbitrarios sin restricción.

#### Remediación
Mover las cabeceras al bloque `server` para que sean globales:
```nginx
server {
    listen 80;
    # ...
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' /api/; frame-ancestors 'self'; base-uri 'self'; form-action 'self';" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Permissions-Policy "geolocation=(), microphone=(), camera=(), payment=()" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    # ...
}
```
Ajustar la CSP según las fuentes reales utilizadas (Google Fonts, etc.).

---

### SEC-21: `/nginx_status` accesible desde rango privado demasiado amplio

- **Severidad:** Media
- **Componente:** Infraestructura — `frontend/nginx.conf` (líneas 102-108)

#### Descripción técnica
La ubicación `/nginx_status` permite acceso a `127.0.0.1` y a todo el rango `172.0.0.0/8`, no solo a la subnet de Docker definida (`172.20.0.0/16`).

#### Escenario de ataque / PoC
Desde otro contenedor con IP `172.x.x.x` en un entorno compartido:
```bash
curl http://<frontend-ip>/nginx_status
```

#### Remediación
Restringir a loopback y a la subnet exacta del proyecto:
```nginx
location /nginx_status {
    stub_status;
    access_log off;
    allow 127.0.0.1;
    allow 172.20.0.0/16;
    deny all;
}
```

---

### SEC-22: Contenedor `db-backup` como `root` con escritura en host

- **Severidad:** Media
- **Componente:** Infraestructura — `docker-compose.yml` (líneas 105-131), `scripts/backup.sh`, `scripts/restore.sh`

#### Descripción técnica
La imagen `postgres:16-alpine` ejecuta el script de backup como `root`. El volumen `./backups:/backups` permite escribir en el host. Además, `restore.sh` puede eliminar y recrear la base de datos.

#### Escenario de ataque / PoC
Si se compromete el contenedor de backup:
```bash
docker exec -it millete-db-backup id
# uid=0(root)
ls -la /backups   # archivos propiedad de root en el host
```
El atacante puede modificar backups, borrar la base de datos o elevar privilegios.

#### Remediación
Ejecutar como usuario no root, asignar UID/GID del host y montar solo lo necesario:
```yaml
db-backup:
  image: postgres:16-alpine
  user: "1000:1000"
  volumes:
    - ./backups:/backups
    - ./scripts/backup.sh:/backup.sh:ro
```

---

### SEC-23: `escapeValue: false` en `i18next` con interpolación de datos de usuario

- **Severidad:** Media
- **Componente:** Frontend — `frontend/src/lib/i18n.ts` (líneas 160-163)

#### Descripción técnica
Se desactiva el escapeo automático de valores interpolados. Aunque React escapa el resultado final cuando se usa como texto JSX, la combinación con `dangerouslySetInnerHTML`, atributos como `href` o futuros componentes que interpreten HTML podría derivar en XSS. Se interpolan datos de usuario (nombres de miembros, descripciones de transacciones, nombres de periodos) en múltiples componentes.

#### Escenario de ataque / PoC
Un nombre de usuario o descripción como `<img src=x onerror=alert(1)>` llega al backend, se devuelve y se interpola. Hoy React lo escapa, pero si alguien refactoriza a `dangerouslySetInnerHTML` o a un atributo URL, se ejecuta el script.

#### Remediación
- Restaurar el escapeo por defecto eliminando `escapeValue: false` (o cambiándolo a `true`).
- Si se necesita HTML en traducciones, usar `<Trans components={...} />` con componentes explícitos y sanitizar con DOMPurify.
- No pasar datos de usuario directamente a claves de traducción; preferir placeholders.

---

### SEC-24: Construcción de URLs de exportación sin codificación

- **Severidad:** Media
- **Componente:** Frontend — `frontend/src/features/dashboard/hooks/useExport.ts` (líneas 24-31)

#### Descripción técnica
Los valores `configValue` se insertan directamente en la ruta y en la query string sin `encodeURIComponent`:
```ts
response = (await apiClient.get(`/data/export/csv/${configValue}`, ...)).data
response = (await apiClient.get(`/data/export/pdf?period=${configValue}`, ...)).data
```
Aunque hoy provienen de constantes, `performExport` acepta cualquier cadena.

#### Escenario de ataque / PoC
Si un atacante puede llamar a `performExport` con un valor manipulado (por ejemplo mediante XSS), puede generar peticiones inesperadas (path traversal, parámetros extra).

#### Remediación
```ts
case "csv":
  response = (await apiClient.get(`/data/export/csv/${encodeURIComponent(configValue)}`, ...)).data;
  break;
case "pdf":
  response = (await apiClient.get(`/data/export/pdf?${new URLSearchParams({ period: configValue })}`, ...)).data;
  break;
```

---

### SEC-25: `CategoryService.findById` sin filtro de propiedad (latente)

- **Severidad:** Baja
- **Componente:** Backend — `categories/application/services/CategoryService.java` (líneas 59-63)

#### Descripción técnica
Existe un método público `findById(UUID id)` que carga cualquier categoría sin filtrar por `userId`. No es usado actualmente por `CategoryController`, pero está expuesto a otros servicios y futuros controladores.

#### Escenario de ataque / PoC
Cualquier endpoint futuro o llamada interna que use este método será vulnerable a IDOR.

#### Remediación
Hacer el método package-private o eliminarlo; forzar `findByIdAndUserId`:
```java
Optional<Category> findByIdAndUserId(UUID id, UUID userId);
```

---

### SEC-26: Falta de manejador global de excepciones

- **Severidad:** Baja
- **Componente:** Backend — `shared/infrastructure/in/controller/advice/GlobalExceptionHandler.java`

#### Descripción técnica
No existe `@ExceptionHandler(Exception.class)`. Si una excepción no controlada escapa, el manejo de errores por defecto de Spring puede incluir stack trace si no está explícitamente deshabilitado.

#### Remediación
Añadir un manejador catch-all y deshabilitar stack traces:
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponseDTO> handleAll(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);
    return buildError(req, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
}
```
```yaml
server:
  error:
    include-stacktrace: never
    include-message: never
```

---

### SEC-27: `@PreAuthorize` no habilitado (`@EnableMethodSecurity`)

- **Severidad:** Baja
- **Componente:** Backend — `shared/infrastructure/config/SecurityConfig.java`

#### Descripción técnica
La clase tiene `@EnableWebSecurity` pero no `@EnableMethodSecurity`. Las anotaciones de seguridad a nivel de método (`@PreAuthorize`, `@PostAuthorize`) serían ignoradas. Actualmente no crea brechas porque `authorizeHttpRequests(...).anyRequest().authenticated()` protege todos los endpoints, pero anotaciones futuras basadas en roles fallarían silenciosamente.

#### Remediación
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { ... }
```

---

### SEC-28: Interceptor de Axios adjunta token a endpoints públicos

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/src/shared/api/axiosClient.ts` (líneas 22-32)

#### Descripción técnica
El interceptor añade `Authorization: Bearer <token>` siempre que existe token, incluso en endpoints públicos como `/auth/login` y `/auth/register`.

#### Escenario de ataque / PoC
Fuga menor del token en logs de red o proxies; también podría provocar comportamientos inesperados en el backend si un endpoint público procesa la cabecera.

#### Remediación
Saltar la autorización en endpoints públicos:
```ts
apiClient.interceptors.request.use((config) => {
  const isPublic = config.url?.startsWith('/auth');
  const token = secureStorage.getToken();
  if (token && config.headers && !isPublic) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

### SEC-29: Fallback a `localhost:8080` en `axiosClient.ts`

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/src/shared/api/axiosClient.ts` (línea 12)

#### Descripción técnica
Si `VITE_API_URL` no se define en el entorno de build, el cliente intenta llamar a `http://localhost:8080/api/v1`. El `Dockerfile` fija `/api/v1`, pero el fallback del código fuente es el entorno de desarrollo.

#### Escenario de ataque / PoC
En una compilación mal configurada, el frontend podría fallar o realizar peticiones mixtas/inseguras; no hay fuga directa, pero aumenta la superficie de error.

#### Remediación
No proporcionar un fallback sensible; hacer fallar el build si falta la variable:
```ts
const API_URL = import.meta.env.VITE_API_URL;
if (!API_URL) {
  throw new Error('VITE_API_URL debe estar definido');
}
```

---

### SEC-30: Evento global `auth:logout` manipulable

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/src/shared/api/axiosClient.ts` (línea 41); `frontend/src/features/auth/context/AuthContext.tsx` (línea 83)

#### Descripción técnica
Ante un 401 se lanza `window.dispatchEvent(new CustomEvent('auth:logout'))`. `AuthContext` escucha ese evento en `window` y cierra sesión. Cualquier script en la misma página puede lanzarlo.

#### Escenario de ataque / PoC
Una extensión maliciosa o un XSS puede forzar el cierre de sesión repetidamente (DoS) o manipular el flujo de logout.

#### Remediación
Gestionar el logout directamente desde el contexto de autenticación (por ejemplo, exponiendo una función interna) en lugar de depender de un evento global.

---

### SEC-31: Callback global `window.__sidebarOpen` permite inyección

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/src/shared/components/Sidebar.tsx` (líneas 44-49); `frontend/src/shared/components/TopNav.tsx` (línea 66)

#### Descripción técnica
`Sidebar` expone `window.__sidebarOpen = () => setIsMobileOpen(true)`, y `TopNav` lo invoca. Un script externo puede sobrescribir esta propiedad antes de que el componente se monte.

#### Escenario de ataque / PoC
Si un atacante puede ejecutar JS en el origen, reemplaza `__sidebarOpen` por código arbitrario que se ejecutará al pulsar el menú hamburguesa.

#### Remediación
Usar un contexto de React, un hook compartido o un evento interno con verificación de origen; evitar variables globales en `window`.

---

### SEC-32: Mensajes de error del backend mostrados directamente al usuario

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/src/shared/api/axiosClient.ts` (líneas 39, 48); `frontend/src/features/auth/hooks/useAuthMutations.ts` (líneas 56-59); `frontend/src/features/dashboard/pages/page.tsx` (líneas 55-64)

#### Descripción técnica
El frontend muestra `error.response.data.message` en notificaciones y consola. Si el backend devolviera detalles internos (stack traces, nombres de tablas, etc.), se filtrarían al cliente.

#### Escenario de ataque / PoC
Un error controlado o forzado podría revelar información sensible del backend o de la estructura de la API.

#### Remediación
- En el frontend, mostrar mensajes genéricos mapeados por código de estado:
```ts
const messages: Record<number, string> = {
  400: t('api:errors.badRequest'),
  401: t('api:errors.unauthorized'),
  403: t('api:errors.forbidden'),
  500: t('api:errors.serverError'),
};
notify.error(messages[status] ?? t('api:errors.default'));
```
- Garantizar en el backend que los mensajes de error no incluyan información interna.

---

### SEC-33: Validación de importación solo por extensión/tipo MIME

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/src/features/dashboard/components/ImportModal.tsx` (líneas 30-34)

#### Descripción técnica
Solo se comprueba `file.type !== "application/json" && !file.name.endsWith(".json")`. No se valida el contenido JSON antes de enviarlo.

#### Escenario de ataque / PoC
Un archivo con extensión `.json` pero contenido malicioso puede subirse al backend. Si el parser del servidor es permisivo, podría derivar en problemas de integridad o, en casos extremos, en vulnerabilidades de deserialización.

#### Remediación
Añadir validación client-side del contenido (schema Zod):
```ts
const validateAndSetFile = useCallback(async (file: File) => {
  if (file.type !== "application/json" && !file.name.endsWith(".json")) {
    setError(t('dashboard:importModal.jsonOnly'));
    return;
  }
  try {
    const text = await file.text();
    const parsed = JSON.parse(text);
    importSchema.parse(parsed); // esquema Zod esperado
    setFileName(file.name);
    setSelectedFile(file);
  } catch {
    setError(t('dashboard:importModal.invalidContent'));
  }
}, [t]);
```

---

### SEC-34: Validación insuficiente en `restore.sh`

- **Severidad:** Baja
- **Componente:** Infraestructura — `scripts/restore.sh` (líneas 22-30)

#### Descripción técnica
La selección del backup se lee con `read SELECTION` y se usa directamente en `sed -n "${SELECTION}p"` sin validar que sea un número entero. Una entrada vacía hace que `sed -n "p"` imprima todas las líneas, rompiendo la variable `BACKUP_FILE`.

#### Escenario de ataque / PoC
El usuario introduce caracteres no numéricos:
```bash
Enter backup number to restore: ;q
BACKUP_FILE=$(... | sed -n ";qp")
```
Puede generar un `BACKUP_FILE` vacío o con múltiples rutas, causando fallo o comportamiento inesperado.

#### Remediación
Validar y citar correctamente:
```sh
case "$SELECTION" in
    ''|*[!0-9]*) echo "Invalid selection"; exit 1 ;;
esac
BACKUP_FILE=$(ls -1t "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | sed -n "${SELECTION}p")
```

---

### SEC-35: Falta de `.env.example`

- **Severidad:** Baja
- **Componente:** Infraestructura — raíz del proyecto (`.gitignore`)

#### Descripción técnica
`.env` está ignorado, pero no existe `.env.example`. Un nuevo despliegue puede arrancar con variables vacías y caer en los secretos por defecto de `application.yml` o en PostgreSQL sin contraseña.

#### Remediación
Crear `.env.example` con valores de ejemplo y documentación:
```bash
DATABASE_NAME=millete_db
DATABASE_USER=millete_user
DATABASE_PASSWORD=CHANGE_ME_32CHARS
JWT_SECRET=CHANGE_ME_64BASE64
JASYPT_ENCRYPTOR_PASSWORD=CHANGE_ME
```

---

### SEC-36: `vite.config.ts` carga variables de entorno sin prefijo

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/vite.config.ts` (línea 8)

#### Descripción técnica
Se invoca `loadEnv(mode, process.cwd(), '')`, es decir, sin prefijo. Aunque solo se usa `__APP_VERSION__`, cualquier secreto presente en el entorno de build quedaría disponible en `process.env` y podría filtrarse accidentalmente si se añade un `define` nuevo.

#### Escenario de ataque / PoC
Un `define` mal configurado en el futuro podría empaquetar `JWT_SECRET`, `DATABASE_PASSWORD`, etc., en el bundle del cliente.

#### Remediación
Usar el prefijo `VITE_`:
```ts
loadEnv(mode, process.cwd(), 'VITE_');
```
Y validar que no se expongan secretos en el bundle.

---

### SEC-37: Recursos externos (Google Fonts) sin integridad

- **Severidad:** Baja
- **Componente:** Frontend — `frontend/index.html` (líneas 10-12)

#### Descripción técnica
Se cargan estilos desde `https://fonts.googleapis.com` y `https://fonts.gstatic.com` sin Subresource Integrity y sin CSP que limite estilos/orígenes.

#### Escenario de ataque / PoC
Si la CDN de Google Fonts se viera comprometida, se podría inyectar CSS (y en escenarios antiguos, contenido activo) en la aplicación.

#### Remediación
- Autocontener las fuentes en `public/fonts` y servirlas desde el mismo origen.
- O restringir CSP: `style-src 'self' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com;`.

---

## 4. Recomendaciones prioritarias

1. **Corregir los IDOR del módulo `groupgoals`** y añadir verificación de membresía en todos los endpoints que operan sobre recursos compartidos.
2. **Eliminar secretos hardcodeados** de `application.yml` y exigir `JWT_SECRET` y `DATABASE_PASSWORD` mediante variables/secretos gestionados.
3. **No publicar PostgreSQL** en el host; comunicar backend↔postgres únicamente por la red interna de Docker.
4. **Ejecutar backend y db-backup como usuarios no root** y aplicar límites de recursos y opciones de seguridad (`no-new-privileges`, `read_only`, tmpfs).
5. **Migrar la sesión a cookies `HttpOnly; Secure; SameSite=Strict`** gestionadas por el backend y eliminar el almacenamiento del JWT en `localStorage`.
6. **Forzar TLS** en el punto de terminación expuesto a Internet y añadir HSTS.
7. **Implementar CSP** con orígenes restringidos y mover las cabeceras de seguridad al bloque `server` de Nginx.
8. **Añadir bloqueos distribuidos** al scheduler de transacciones recurrentes y endurecer el rate-limit de login.
9. **Restaurar `escapeValue: true`** en i18next y sanitizar cualquier renderizado HTML futuro.
10. **Sanejar IDs en la importación de datos** generando siempre nuevos UUIDs y validando propiedad de categorías en transacciones.

---

## 5. Observaciones positivas

- El backend utiliza correctamente consultas JPQL con parámetros nombrados; no se detectaron inyecciones SQL ni nativas inseguras.
- La generación de PDF/CSV es actualmente segura (`th:text` escapa HTML; CSV usa escaping RFC-compliant).
- El frontend no emplea `dangerouslySetInnerHTML`, `eval`, `new Function`, `document.write`, `postMessage` ni renderizado de Markdown/HTML desde datos de usuario en el código analizado.
- React Hook Form + Zod se usan de forma adecuada para validar formularios.
- El frontend se ejecuta rootless en su `Dockerfile` y se usan builds multi-etapa en ambos servicios.
- `manage.sh clean-all` requiere confirmación explícita (`DELETE`).
- `server_tokens off` evita la divulgación de la versión de Nginx.

---

*Fin del informe.*

Soluciones implementadas: SEC-03 -> SEC-04 -> SEC-05 -> SEC-06 -> SEC-07 -> SEC-08 -> SEC-09 -> SEC-11 -> SEC-13 -> SEC-14 -> SEC-15 -> SEC-16 -> SEC-18 -> SEC-21 -> SEC-22 -> SEC-24 -> SEC-25 -> SEC-27 -> SEC-28 -> SEC-30 -> SEC-31 -> SEC-33 -> SEC-34 -> SEC-35 -> SEC-36 -> SEC-37