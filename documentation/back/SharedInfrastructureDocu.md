# Shared — Infraestructura compartida (Backend)

## Estructura de archivos

- **config/SecurityConfig.java** — Configuración de seguridad, CORS y beans
- **config/filter/JwtAuthenticationFilter.java** — Filtro de autenticación JWT por petición
- **config/scheduler/TransactionScheduler.java** — Tarea programada diaria para transacciones recurrentes
- **in/controller/advice/GlobalExceptionHandler.java** — Manejador global de excepciones
- **in/controller/dto/ErrorResponseDTO.java** — DTO estándar de respuesta de error

---

## SecurityConfig.java

Configuración central de seguridad con Spring Security. Anotada con @Configuration.

### SecurityFilterChain

Define las reglas de seguridad de la aplicación:

- **CORS:** usa configuración personalizada que permite peticiones desde el frontend.
- **CSRF:** deshabilitado por ser una API REST sin estado.
- **Sesiones:** STATELESS, sin almacenamiento en memoria del servidor.
- **Rutas públicas:** /api/v1/auth/** no requieren autenticación.
- **Rutas protegidas:** todas las demás requieren token JWT válido.
- **Filtro JWT:** se ejecuta antes que el filtro de autenticación por defecto de Spring.

### CORS

Permite peticiones desde http://localhost:5173. Métodos HTTP permitidos: GET, POST, PUT, PATCH, DELETE, OPTIONS. Cabeceras permitidas: Authorization y Content-Type. Se aplica a todas las rutas.

### PasswordEncoder

Bean de BCryptPasswordEncoder para codificar y verificar contraseñas de forma segura.

---

## JwtAuthenticationFilter.java

Filtro que se ejecuta en cada petición HTTP. Extiende OncePerRequestFilter de Spring, lo que garantiza que se ejecuta una sola vez por petición.

### Flujo de autenticación

1. Extrae el token JWT del header Authorization.
2. Si el token existe, valida su firma y expiración mediante TokenProvider.
3. Si es válido, extrae el userId del token.
4. Crea un UsernamePasswordAuthenticationToken con el userId y lo establece en el SecurityContextHolder.
5. Spring Security reconoce al usuario como autenticado para el resto de la petición.
6. Si el token no existe o es inválido, la petición continúa sin autenticación.
7. Si la ruta requiere autenticación y no la tiene, Spring devuelve 401.

### Extracción del token

Busca el header Authorization, verifica el prefijo "Bearer " y extrae el resto de la cadena como token JWT.

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

---

## ErrorResponseDTO.java

Estructura estándar de respuesta de error para toda la API.

### Campos

- **timestamp:** momento exacto en que ocurrió el error.
- **status:** código de estado HTTP (400, 404, etc.).
- **error:** descripción del código (ej: "Bad Request", "Not Found").
- **message:** mensaje descriptivo del error.
- **path:** ruta de la API donde ocurrió el error.

---

## Flujo completo de una petición

1. El frontend envía la petición con header Authorization: Bearer <token>.
2. SecurityConfig aplica las reglas CORS y verifica que la ruta requiera autenticación.
3. JwtAuthenticationFilter extrae el token, lo valida y establece el usuario en el contexto de seguridad.
4. El controlador recibe la petición con el userId disponible mediante authentication.getName().
5. Si ocurre una excepción en cualquier capa, GlobalExceptionHandler la captura y devuelve un ErrorResponseDTO.
6. Si la ruta lo requiere, TransactionScheduler ejecuta las tareas programadas en segundo plano.