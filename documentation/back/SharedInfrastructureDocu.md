# Shared — Infraestructura compartida (Backend)

## Estructura de archivos

- config/SecurityConfig.java — Configuración de seguridad, CORS, filtros y beans
- config/filter/JwtAuthenticationFilter.java — Filtro de autenticación JWT por petición
- config/filter/LoginRateLimitFilter.java — Filtro de rate limiting por IP para protección contra fuerza bruta
- config/scheduler/TransactionScheduler.java — Tarea programada diaria para transacciones recurrentes
- in/controller/advice/GlobalExceptionHandler.java — Manejador global de excepciones
- in/controller/dto/ErrorResponseDTO.java — DTO estándar de respuesta de error

---

## SecurityConfig.java

Configuración central de seguridad con Spring Security. Anotada con @Configuration y @EnableWebSecurity.

### SecurityFilterChain

Define las reglas de seguridad de la aplicación:

- CORS: usa configuración personalizada que permite peticiones desde el frontend. Los orígenes permitidos se configuran mediante la variable de entorno CORS_ALLOWED_ORIGINS (por defecto http://localhost:5173 en desarrollo).
- CSRF: deshabilitado por ser una API REST sin estado.
- Sesiones: STATELESS, sin almacenamiento en memoria del servidor.
- Rutas públicas: solo POST /api/v1/auth/register y POST /api/v1/auth/login no requieren autenticación. El resto de rutas bajo /api/v1/auth (como /me/topnav) están protegidas y requieren token JWT válido.
- Rutas protegidas: todas las demás requieren autenticación.
- Filtro LoginRateLimitFilter: se ejecuta antes que UsernamePasswordAuthenticationFilter para aplicar rate limiting por IP en el endpoint de login.
- Filtro JWT: se ejecuta antes que UsernamePasswordAuthenticationFilter para validar el token en cada petición.

### CORS

Permite peticiones desde los orígenes configurados en CORS_ALLOWED_ORIGINS. Métodos HTTP permitidos: GET, POST, PUT, PATCH, DELETE, OPTIONS. Cabeceras permitidas: Authorization, Content-Type, X-Requested-With, Accept, Origin. Expone la cabecera Authorization. Permite credenciales. Se aplica a todas las rutas.

### PasswordEncoder

Bean de BCryptPasswordEncoder para codificar y verificar contraseñas de forma segura.

### LoginRateLimitFilter

Bean que crea una instancia del filtro de rate limiting para inyectarlo en la cadena de seguridad.

---

## JwtAuthenticationFilter.java

Filtro que se ejecuta en cada petición HTTP. Extiende OncePerRequestFilter de Spring, lo que garantiza que se ejecuta una sola vez por petición. Anotado con @Component.

### Flujo de autenticación

1. Extrae el token JWT del header Authorization.
2. Si el token existe y tiene texto, valida su firma y expiración mediante TokenProvider.
3. Si es válido, extrae el userId del token.
4. Si el userId no es nulo y no hay autenticación previa en el contexto, crea un UsernamePasswordAuthenticationToken con el userId y una lista vacía de authorities, y lo establece en el SecurityContextHolder.
5. Spring Security reconoce al usuario como autenticado para el resto de la petición.
6. Si el token no existe o es inválido, la petición continúa sin autenticación.
7. Si la ruta requiere autenticación y no la tiene, Spring devuelve 401.
8. Si ocurre alguna excepción durante el proceso, se registra en el log y la petición continúa sin autenticación.

### Extracción del token

Busca el header Authorization, verifica que tenga texto y que empiece por "Bearer " (7 caracteres). Si cumple, extrae el resto de la cadena como token JWT. Si no, devuelve null.

---

## LoginRateLimitFilter.java

Filtro de protección contra ataques de fuerza bruta en el endpoint de login. Extiende OncePerRequestFilter. No tiene dependencias externas: usa ConcurrentHashMap con limpieza periódica en un hilo daemon para controlar intentos por IP en memoria.

### Configuración

- Límite: 5 intentos por minuto por dirección IP.
- Ventana de tiempo: 1 minuto (60 segundos).
- Ruta protegida: solo aplica a POST /api/v1/auth/login.
- Limpieza: un hilo daemon elimina las entradas expiradas del mapa cada minuto.

### Flujo

1. Verifica si la petición es POST a /api/v1/auth/login. Si no lo es, deja pasar la petición sin intervenir.
2. Obtiene la IP del cliente. Primero busca el header X-Forwarded-For (para proxies y balanceadores de carga). Si no existe, usa request.getRemoteAddr().
3. Busca o crea una ventana de intentos para esa IP en el ConcurrentHashMap. Si la ventana no existe o ya expiró, crea una nueva con 1 intento. Si existe y no ha expirado, incrementa el contador.
4. Si la ventana tiene más de 5 intentos y no ha expirado, responde inmediatamente con 429 Too Many Requests y un mensaje JSON indicando los segundos restantes para reintentar. No se llega a tocar la base de datos ni el controlador.
5. Si no se ha superado el límite, deja pasar la petición al siguiente filtro.

### Respuesta 429

Devuelve un JSON con status 429, error "Too Many Requests" y un mensaje indicando cuántos segundos debe esperar el cliente antes de reintentar.

### Clase interna AttemptWindow

Almacena el número de intentos y el timestamp de inicio de la ventana. Métodos: increment (suma 1 al contador), isExpired (true si pasó más de 1 minuto desde windowStart), isBlocked (true si no ha expirado y los intentos superan 5), secondsUntilReset (segundos que faltan para que expire la ventana).

### Hilo de limpieza

Un hilo daemon llamado "rate-limit-cleanup" se ejecuta cada minuto y elimina del mapa todas las entradas cuya ventana haya expirado. Esto evita que el mapa crezca indefinidamente con IPs que ya no hacen peticiones.

---

## TransactionScheduler.java

Tarea programada que automatiza la creación de transacciones recurrentes. Anotada con @Component.

### Programación

Ejecuta el método runDailyTransactions cada día a las 00:01 AM mediante la expresión cron "0 1 0 * * ?".

### Funcionamiento

Llama a ProcessPlannedTransactionsUseCase.processScheduledTasks(), que verifica qué transacciones recurrentes deben ejecutarse ese día según su frecuencia, intervalo y rango de fechas, y las crea como transacciones reales.

---

## GlobalExceptionHandler.java

Manejador global de excepciones anotado con @RestControllerAdvice. Centraliza todas las respuestas de error de la API en un formato consistente.

### Excepciones manejadas

**RuntimeException:**
- Por defecto devuelve 400 Bad Request.
- Si el mensaje contiene "no encontrada" o "no existe", devuelve 404 Not Found.
- Útil para errores de negocio lanzados desde los servicios.

**IllegalArgumentException:**
- Errores de validación del modelo de dominio.
- Devuelve siempre 400 Bad Request con el mensaje de la excepción.

**MethodArgumentNotValidException:**
- Errores de validación de DTOs anotados con @Valid.
- Recorre todos los campos que fallaron y extrae sus mensajes.
- Devuelve 400 Bad Request con el detalle de cada campo inválido.

### Respuesta

Todas las excepciones devuelven un ErrorResponseDTO con timestamp, código HTTP, descripción, mensaje y ruta donde ocurrió el error.

### AccountLockedException (preparado para v0.1.0)

El manejador está preparado para capturar AccountLockedException y devolver 423 Locked cuando se implemente el bloqueo de cuenta por intentos fallidos. Actualmente la excepción no está creada, pero el GlobalExceptionHandler ya contempla su manejo futuro.

---

## ErrorResponseDTO.java

Estructura estándar de respuesta de error para toda la API.

### Campos

- timestamp: momento exacto en que ocurrió el error.
- status: código de estado HTTP (400, 404, 423, 429, etc.).
- error: descripción del código (ej: "Bad Request", "Not Found").
- message: mensaje descriptivo del error.
- path: ruta de la API donde ocurrió el error.

---

## Flujo completo de una petición

1. El frontend envía la petición con header Authorization: Bearer (token).
2. SecurityConfig aplica las reglas CORS y verifica que la ruta requiera autenticación.
3. Si la ruta es POST /api/v1/auth/login, LoginRateLimitFilter verifica que la IP no haya superado el límite de 5 intentos por minuto. Si lo superó, responde 429 inmediatamente sin consultar la base de datos.
4. JwtAuthenticationFilter extrae el token, lo valida y establece el usuario en el contexto de seguridad.
5. El controlador recibe la petición con el userId disponible mediante authentication.getName().
6. Si ocurre una excepción en cualquier capa, GlobalExceptionHandler la captura y devuelve un ErrorResponseDTO.
7. Si la ruta lo requiere, TransactionScheduler ejecuta las tareas programadas en segundo plano.

---

## Protección contra fuerza bruta (doble capa)

El sistema implementa dos capas de protección contra ataques de fuerza bruta en el login:

**Capa 1 - Rate Limiting por IP (v0.0.3, implementado):**
LoginRateLimitFilter limita a 5 intentos por minuto por IP. No requiere base de datos. Responde 429 Too Many Requests. Implementado con ConcurrentHashMap en memoria.

**Capa 2 - Bloqueo de Cuenta (planificado para v0.1.0):**
AccountLockService bloqueará la cuenta tras 5 intentos fallidos consecutivos durante 15 minutos. Requiere migración de base de datos (columnas failed_login_attempts, account_non_locked, lock_time en la tabla users). El GlobalExceptionHandler ya está preparado para manejar AccountLockedException con código 423 Locked.

Ambas capas pueden coexistir sin conflicto. Si la migración de la capa 2 no se ha ejecutado, la capa 1 sigue protegiendo.