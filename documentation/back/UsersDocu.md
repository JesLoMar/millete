# Users — Documentación técnica (Backend)

## Estructura de archivos

- application/services/UserService.java — Servicio central que implementa todos los casos de uso
- application/services/AccountLockService.java — Servicio de control y gestión del bloqueo de sesiones por fuerza bruta (v0.1.0)
- application/services/SessionPersistenceService.java — Servicio de persistencia de intentos fallidos con transaccionalidad independiente (v0.1.0)
- domain/model/User.java — Entidad de dominio rica para la gestión del usuario
- domain/model/UserSession.java — Entidad de dominio que encapsula el estado y las reglas de negocio de la sesión (v0.1.0)
- domain/exception/AccountLockedException.java — Excepción de negocio lanzada al intentar autenticarse en una sesión bloqueada (v0.1.0)
- domain/ports/in/RegisterUserUseCase.java — Interfaz de registro
- domain/ports/in/LoginUserUseCase.java — Interfaz de login
- domain/ports/in/GetUserDataUseCase.java — Interfaz de consulta por ID
- domain/ports/out/UserRepository.java — Interfaz del repositorio de usuarios
- domain/ports/out/UserSessionRepository.java — Interfaz del repositorio de sesiones de usuario (v0.1.0)
- domain/ports/out/PasswordHasherPort.java — Interfaz de hashing de contraseñas
- domain/ports/out/TokenProvider.java — Interfaz de generación de JWT
- infrastructure/in/controller/AuthController.java — Controlador REST con endpoints
- infrastructure/in/controller/dto/LoginRequestDTO.java — DTO de petición de login
- infrastructure/in/controller/dto/RegisterUserRequestDTO.java — DTO de petición de registro
- infrastructure/in/controller/dto/TokenResponseDTO.java — DTO de respuesta con JWT
- infrastructure/in/controller/dto/TopNavUserResponseDTO.java — DTO para barra de navegación
- infrastructure/in/controller/dto/UserResponseDTO.java — DTO de respuesta del usuario
- infrastructure/out/persistence/postgresql/adapters/UserPostgresAdapter.java — Adaptador del repositorio de usuarios
- infrastructure/out/persistence/postgresql/adapters/UserSessionPostgresAdapter.java — Adaptador del repositorio de sesiones (v0.1.0)
- infrastructure/out/persistence/postgresql/entity/UserEntity.java — Entidad JPA de usuarios
- infrastructure/out/persistence/postgresql/entity/UserSessionEntity.java — Entidad JPA de sesiones (v0.1.0)
- infrastructure/out/persistence/postgresql/mappers/UserEntityMapper.java — Mapper MapStruct de usuarios
- infrastructure/out/persistence/postgresql/mappers/UserSessionEntityMapper.java — Mapper MapStruct de sesiones (v0.1.0)
- infrastructure/out/persistence/postgresql/repository/JpaUserRepository.java — Repositorio Spring Data de usuarios
- infrastructure/out/persistence/postgresql/repository/JpaUserSessionRepository.java — Repositorio Spring Data de sesiones (v0.1.0)
- infrastructure/out/security/BCryptPasswordHasherAdapter.java — Hashing con BCrypt
- infrastructure/out/security/JwtTokenAdapter.java — Generación y validación de JWT

---

## Arquitectura hexagonal

El módulo sigue el patrón de Puertos y Adaptadores:

- Puertos de entrada (in): interfaces que definen qué puede hacer el sistema. RegisterUserUseCase, LoginUserUseCase y GetUserDataUseCase.
- Núcleo de dominio: UserService implementa todos los casos de uso. User.java y UserSession.java contienen la lógica pura de negocio.
- Puertos de salida (out): interfaces que definen qué necesita el sistema del exterior. UserRepository, UserSessionRepository, PasswordHasherPort y TokenProvider.
- Adaptadores: implementan los puertos de salida. UserPostgresAdapter y UserSessionPostgresAdapter para la capa de persistencia, BCryptPasswordHasherAdapter para la seguridad criptográfica, JwtTokenAdapter para tokens.

El controlador AuthController habla de manera exclusiva con los puertos de entrada. No conoce detalles de la base de datos ni de las tecnologías de seguridad subyacentes.

---

## AuthController.java

Controlador REST mapeado a /api/v1/auth.

### POST /register

1. Recibe RegisterUserRequestDTO (username opcional, email opcional, password).
2. Traduce a RegisterUserCommand.
3. Ejecuta registerUserUseCase.register().
4. Mapea el User resultante a UserResponseDTO.
5. Responde 201 Created.

### POST /login

1. Recibe LoginRequestDTO (identifier, password).
2. Traduce a LoginUserCommand.
3. Ejecuta loginUserUseCase.login().
4. Envuelve el JWT en TokenResponseDTO.
5. Responde 200 OK.

### GET /me/topnav

Este endpoint está protegido con @PreAuthorize("isAuthenticated()") y validación defensiva adicional:

1. Verifica que el objeto Authentication no sea nulo y que esté autenticado. Si no, responde 401 Unauthorized.
2. Extrae el userId del token JWT mediante authentication.getName().
3. Busca el usuario con getUserDataUseCase.getUserById().
4. Devuelve TopNavUserResponseDTO con username y email.

La doble protección (anotación + validación manual) garantiza que nunca se intente acceder a datos de usuario sin un contexto de seguridad válido, incluso si la configuración de rutas públicas cambiase.

---

## DTOs

### LoginRequestDTO

Campos validados: identifier con @NotBlank, password con @NotBlank. Es un DTO de tipo Record inmutable.

### RegisterUserRequestDTO

Campos: username opcional, email opcional con @Email, password con @NotBlank y @Size(min=8). Es un DTO de tipo Record inmutable.

### TokenResponseDTO

Campo: token (String) con el JWT generado.

### UserResponseDTO

Campos: id, username, email, createdAt, modifiedAt, active, anonymized.

### TopNavUserResponseDTO

Campos: username, email. Datos mínimos para mostrar en la barra superior.

---

## UserService.java

Servicio central que implementa RegisterUserUseCase, LoginUserUseCase y GetUserDataUseCase.

### Registro

1. Valida que venga al menos un identificador (username o email). Si no, lanza excepción.
2. Si se envió email, verifica que no esté ya registrado con findByEmail.
3. Si se envió username, verifica que no esté ya en uso con findByUsername.
4. Hashea la contraseña con PasswordHasherPort.hashPassword.
5. Crea un nuevo User con UUID aleatorio y fechas actuales.
6. Guarda mediante UserRepository.save y devuelve el User creado.

### Login

1. Busca al usuario por identifier con findByIdentifier. Si no existe, lanza "Credenciales inválidas" (mensaje genérico por seguridad).
2. Llama a accountLockService.checkLockStatus pasándole el ID del usuario para verificar si la sesión del canal WEB está bloqueada.
3. Compara la contraseña con PasswordHasherPort.matches.
4. Si no coincide, invoca a accountLockService.handleFailedLogin para registrar el fallo (incrementando reintentos o bloqueando) y luego lanza "Credenciales inválidas".
5. Si coincide, invoca a accountLockService.handleSuccessfulLogin para limpiar cualquier intento fallido histórico, genera un JWT con TokenProvider.generateToken y lo devuelve.

### Recuperar usuario por ID

Busca por UUID con findById. Si no existe, lanza excepción.

---

## User.java (Modelo de dominio)

Entidad rica con lógica de negocio propia. Usa las anotaciones de Lombok.

### Campos

- id (UUID, inmutable)
- username (String, opcional)
- email (String, opcional)
- password (String, hasheada)
- createdAt (LocalDateTime, inmutable)
- modifiedAt (LocalDateTime)
- active (boolean)
- anonymized (boolean)

### Validaciones en el constructor

- Al menos username o email debe estar presente y no vacío.
- Password obligatoria y no vacía.

### Métodos de dominio

- anonymize(): reemplaza username, email y password por valores anónimos. Marca como inactivo y anonimizado.
- updatePassword(): actualiza la contraseña hasheada y modifiedAt.
- deactivate(): desactiva la cuenta y actualiza modifiedAt.
- hasValidIdentity(): devuelve true si tiene al menos un identificador válido.
- getPrimaryIdentifier(): devuelve email si existe, si no devuelve username.

---

## UserSession.java (Modelo de dominio)

Entidad rica encargada de modelar el ciclo de vida de los inicios de sesión por canal, protegiendo al sistema contra ataques dirigidos de fuerza bruta. Usa las anotaciones @Getter y @Setter de Lombok.

### Campos

- id (UUID, inmutable)
- userId (UUID, relacional)
- channel (String, discriminador entre WEB y TELEGRAM)
- telegramChatId (Long, opcional para el bot)
- loginAttempts (int, contador consecutivo de fallos)
- blockedUntil (LocalDateTime, marca temporal hasta cuándo dura la penalización)
- lastAttemptAt (LocalDateTime)
- createdAt (LocalDateTime)
- modifiedAt (LocalDateTime)

### Métodos de dominio

- isBlocked(): Evalúa si la sesión web o de telegram está bloqueada. Si la penalización de tiempo ha expirado, realiza un desbloqueo perezoso (lazy-unlock) limpiando el contador y la fecha de expiración automáticamente.
- registerFailedAttempt(maxAttempts, lockDurationMinutes): Incrementa en uno el contador secuencial de fallos y actualiza las marcas de tiempo. Si se alcanza el umbral de intentos máximos, calcula el valor futuro para el campo blockedUntil.
- resetAttempts(): Restablece los intentos fallidos a cero y limpia el bloqueo tras un acceso exitoso.

---

## Puertos de entrada

### RegisterUserUseCase

Define register(RegisterUserCommand) que devuelve User. RegisterUserCommand contiene: username, email, rawPassword.

### LoginUserUseCase

Define login(LoginUserCommand) que devuelve String (JWT). LoginUserCommand contiene: identifier, rawPassword.

### GetUserDataUseCase

Define getUserById(UUID) que devuelve User.

---

## Puertos de salida

### UserRepository

Define: save, findByEmail, findByUsername, findByIdentifier (busca por email o username), findById.

### UserSessionRepository

Define: findByUserIdAndChannel (recupera la sesión de un usuario según el entorno de acceso), save (persiste el estado mutado de la sesión).

### PasswordHasherPort

Define: hashPassword (encode con BCrypt), matches (compara contraseña en texto plano con hash).

### TokenProvider

Define: generateToken (crea JWT con claims del usuario), extractUserId (extrae el subject del token), isTokenValid (verifica firma y expiración).

---

## Adaptador de persistencia

### UserEntity

Entidad JPA mapeada a la tabla users. Columnas: id (UUID, PK), username (unique), email (unique), password, created_at, modified_at, active, anonymized. Usa Lombok para getters, setters y constructores.

### UserSessionEntity

Entidad JPA mapeada a la tabla user_sessions de la versión v0.1.0 del esquema. Columnas: id (UUID, PK), user_id, channel, telegram_chat_id, login_attempts, blocked_until, last_attempt_at, created_at, modified_at. Incluye una restricción única estructurada para que exista una única sesión por usuario y canal de comunicación simultáneo.

### JpaUserRepository

Interfaz que extiende JpaRepository. Spring Data genera la implementación automáticamente. Métodos personalizados: findByEmail, findByUsername, findByUsernameOrEmail.

### JpaUserSessionRepository

Interfaz que extiende JpaRepository dedicada a las sesiones. Método personalizado: findByUserIdAndChannel.

### UserEntityMapper

Interfaz MapStruct anotada con @Mapper(componentModel = "spring"). Traduce de forma bidireccional los objetos entre el modelo User y UserEntity.

### UserSessionEntityMapper

Interfaz MapStruct anotada con @Mapper(componentModel = "spring"). Mapea de forma transparente las propiedades de UserSession hacia su entidad JPA homónima y viceversa.

### UserPostgresAdapter

Implementa UserRepository. Traduce entre dominio y entidad usando UserEntityMapper. Delega las operaciones de base de datos en JpaUserRepository.

### UserSessionPostgresAdapter

Implementa UserSessionRepository. Se encarga de aislar la persistencia de las sesiones. Mapea la información con UserSessionEntityMapper y utiliza JpaUserSessionRepository para ejecutar las consultas SQL en PostgreSQL.

---

## Adaptadores de seguridad

### BCryptPasswordHasherAdapter

Implementa PasswordHasherPort. Usa PasswordEncoder de Spring Security inyectado en el constructor. hashPassword llama a encode, matches llama a matches.

### JwtTokenAdapter

Implementa TokenProvider. Recibe jwt.secret y jwt.expiration desde application.yml. Realiza la firma y validación basándose en las especificaciones estables de la librería io.jsonwebtoken.

---

## AccountLockService (v0.1.0)

Servicio ubicado en la capa de aplicación encargado de orquestar la política de seguridad web y bot. Estructura el límite de reintentos máximos en 5 y la penalización de tiempo en 15 minutos.

A partir de la versión v0.1.0, la persistencia del intento fallido se ha delegado en un servicio independiente (SessionPersistenceService) para asegurar que la actualización de login_attempts y blocked_until se comitee en una transacción separada antes de lanzar AccountLockedException. Esto resuelve un problema de rollback que impedía que el bloqueo llegara a la base de datos.

### Métodos principales

- checkLockStatus(UUID userId): Carga la sesión WEB del usuario si existe. Evalúa si se encuentra bloqueada ejecutando el método de dominio isBlocked(). Si persiste el bloqueo, calcula dinámicamente los minutos redondeados que le quedan al usuario y lanza AccountLockedException. Si el tiempo de bloqueo expiró, el propio isBlocked() realiza un desbloqueo perezoso y se guarda el nuevo estado mediante SessionPersistenceService.
- handleFailedLogin(UUID userId): Delega en SessionPersistenceService.persistFailedAttempt() la creación o actualización de la sesión y el registro del fallo, usando una transacción con propagación REQUIRES_NEW para garantizar que los cambios se persistan incluso si a continuación se lanza AccountLockedException. Después evalúa si la sesión quedó bloqueada y, en ese caso, lanza la excepción con los minutos restantes.
- handleSuccessfulLogin(UUID userId): Busca la sesión activa del canal WEB tras una verificación exitosa de contraseña y, si existen intentos fallidos previos o un bloqueo, invoca resetAttempts() y guarda el estado limpio.

### SessionPersistenceService (v0.1.0)

Servicio de apoyo con responsabilidad exclusiva sobre la persistencia de intentos fallidos. Sus métodos están anotados con @Transactional(propagation = Propagation.REQUIRES_NEW) para ejecutarse en una transacción independiente que se comitea antes de retornar el control a AccountLockService.

- persistFailedAttempt(UUID userId): Recupera la sesión existente para el canal WEB o crea una nueva, aplica registerFailedAttempt y guarda inmediatamente. Devuelve la sesión actualizada.
- saveSession(UserSession session): Guarda de forma genérica cualquier sesión, usado principalmente tras los desbloqueos perezosos en checkLockStatus.

### AccountLockedException

Excepción de negocio que hereda de RuntimeException. Aloja los campos lockTime y remainingMinutes. El mensaje autogenerado expone dinámicamente la penalización exacta en minutos. Es capturada en la infraestructura compartida por el GlobalExceptionHandler, el cual responde al cliente web con un código de estado HTTP 423 Locked y la estructura ErrorResponseDTO unificada.

---

## Protección contra fuerza bruta (doble capa)

El sistema implementa dos capas complementarias de protección:

**Capa 1 - Rate Limiting por IP (v0.0.3, implementado):**
LoginRateLimitFilter intercepta las llamadas antes de tocar la base de datos. Limita a 5 intentos por minuto por dirección IP mediante un mapa concurrente en memoria. Responde de forma inmediata con un HTTP 429 Too Many Requests.

**Capa 2 - Bloqueo de Cuenta por Sesión y Canal (v0.1.0, implementado):**
AccountLockService gestiona la persistencia de accesos erróneos por usuario en la tabla user_sessions. Si se alcanzan 5 fallos consecutivos para la cuenta, bloquea el canal afectado durante 15 minutos respondiendo con un HTTP 423 Locked de forma aislada (un bloqueo en WEB no impide el uso legítimo de TELEGRAM). El campo blocked_until se persiste correctamente incluso cuando se lanza una excepción de bloqueo, gracias al servicio SessionPersistenceService con transacción independiente.

---

## Conexión con el frontend

El módulo auth del frontend se comunica con estos endpoints:

- POST /api/v1/auth/register -> Petición con RegisterUserRequestDTO -> 201 Created con UserResponseDTO
- POST /api/v1/auth/login -> Petición con LoginRequestDTO -> 200 OK con TokenResponseDTO. Si la cuenta está penalizada por intentos fallidos, el servidor retorna un error de estado 423 Locked detallando el tiempo de espera restante.
- GET /api/v1/auth/me/topnav -> Cabecera Authorization: Bearer token -> 200 OK con TopNavUserResponseDTO

---

## Flujo completo de registro

1. Frontend envía RegisterUserRequestDTO con username, email y password.
2. AuthController traduce a RegisterUserCommand.
3. UserService valida que haya al menos un identificador.
4. UserService verifica que email y username no estén en uso.
5. BCryptPasswordHasherAdapter hashea la contraseña.
6. UserService crea el modelo User con UUID aleatorio.
7. UserPostgresAdapter traduce a UserEntity y guarda en PostgreSQL.
8. AuthController mapea el User a UserResponseDTO y responde 201.

---

## Flujo completo de login

1. Frontend envía LoginRequestDTO con identifier y password.
2. Si la dirección IP superó los 5 intentos por minuto en el servidor, LoginRateLimitFilter intercepta la petición y responde un código 429 sin realizar accesos a la base de datos.
3. El controlador AuthController delega la petición transformándola en un LoginUserCommand hacia el UserService.
4. UserService recupera el modelo de dominio User buscando por su identificador único mediante el adaptador de persistencia. Si no existe, se lanza "Credenciales inválidas".
5. UserService ejecuta accountLockService.checkLockStatus(user.getId()). Si la sesión web está bloqueada (incluso si el tiempo expiró, se evalúa en ese momento), se lanza AccountLockedException interrumpiendo el flujo de inmediato y el GlobalExceptionHandler responde un código 423 Locked. Si el bloqueo expiró, el propio checkLockStatus realiza el desbloqueo perezoso y guarda el estado limpio.
6. Si la sesión está disponible, BCryptPasswordHasherAdapter realiza el emparejamiento criptográfico de la contraseña enviada contra el hash persistido.
7. Si la contraseña es incorrecta, se invoca accountLockService.handleFailedLogin(user.getId()). Internamente, este método llama a SessionPersistenceService.persistFailedAttempt(userId) que, en una transacción separada, incrementa el contador de fallos y, si se alcanza el límite, establece blocked_until. Luego, handleFailedLogin evalúa si la sesión quedó bloqueada; en caso afirmativo, lanza AccountLockedException (con el tiempo restante), y en caso negativo, simplemente retorna. A continuación, UserService lanza "Credenciales inválidas" (o la excepción de bloqueo se propaga directamente, evitando el mensaje genérico).
8. Si la contraseña coincide, se ejecuta accountLockService.handleSuccessfulLogin(user.getId()) para restablecer el estado de seguridad a limpio (resetea intentos y bloqueo).
9. JwtTokenAdapter genera y firma digitalmente un nuevo token con los datos básicos del usuario, retornando un código HTTP 200 OK hacia la interfaz de React.