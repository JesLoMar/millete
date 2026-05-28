# Users — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/UserService.java** — Servicio central que implementa todos los casos de uso
- **domain/model/User.java** — Entidad de dominio con lógica de negocio
- **domain/ports/in/RegisterUserUseCase.java** — Interfaz de registro
- **domain/ports/in/LoginUserUseCase.java** — Interfaz de login
- **domain/ports/in/GetUserDataUseCase.java** — Interfaz de consulta por ID
- **domain/ports/out/UserRepository.java** — Interfaz del repositorio
- **domain/ports/out/PasswordHasherPort.java** — Interfaz de hashing de contraseñas
- **domain/ports/out/TokenProvider.java** — Interfaz de generación de JWT
- **infrastructure/in/controller/AuthController.java** — Controlador REST con endpoints
- **infrastructure/in/controller/dto/LoginRequestDTO.java** — DTO de petición de login
- **infrastructure/in/controller/dto/RegisterUserRequestDTO.java** — DTO de petición de registro
- **infrastructure/in/controller/dto/TokenResponseDTO.java** — DTO de respuesta con JWT
- **infrastructure/in/controller/dto/TopNavUserResponseDTO.java** — DTO para barra de navegación
- **infrastructure/in/controller/dto/UserResponseDTO.java** — DTO de respuesta del usuario
- **infrastructure/out/persistence/postgresql/adapters/UserPostgresAdapter.java** — Adaptador del repositorio
- **infrastructure/out/persistence/postgresql/entity/UserEntity.java** — Entidad JPA
- **infrastructure/out/persistence/postgresql/mappers/UserEntityMapper.java** — Mapper MapStruct
- **infrastructure/out/persistence/postgresql/repository/JpaUserRepository.java** — Repositorio Spring Data
- **infrastructure/out/security/BCryptPasswordHasherAdapter.java** — Hashing con BCrypt
- **infrastructure/out/security/JwtTokenAdapter.java** — Generación y validación de JWT

---

## Arquitectura hexagonal

El módulo sigue el patrón de Puertos y Adaptadores:

- **Puertos de entrada (in):** interfaces que definen qué puede hacer el sistema. RegisterUserUseCase, LoginUserUseCase y GetUserDataUseCase.
- **Núcleo de dominio:** UserService implementa todos los casos de uso. User.java contiene la lógica de negocio.
- **Puertos de salida (out):** interfaces que definen qué necesita el sistema del exterior. UserRepository, PasswordHasherPort y TokenProvider.
- **Adaptadores:** implementan los puertos de salida. UserPostgresAdapter para persistencia, BCryptPasswordHasherAdapter para seguridad, JwtTokenAdapter para tokens.

El controlador AuthController habla solo con puertos de entrada. No conoce detalles de base de datos ni de seguridad.

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

1. Extrae el userId del token JWT mediante authentication.getName().
2. Busca el usuario con getUserDataUseCase.getUserById().
3. Devuelve TopNavUserResponseDTO con username y email.

---

## DTOs

### LoginRequestDTO

Campos validados: identifier con @NotBlank, password con @NotBlank.

### RegisterUserRequestDTO

Campos: username opcional, email opcional con @Email, password con @NotBlank y @Size(min=8).

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
2. Compara la contraseña con PasswordHasherPort.matches.
3. Si no coincide, lanza "Credenciales inválidas".
4. Si coincide, genera un JWT con TokenProvider.generateToken y lo devuelve.

### Recuperar usuario por ID

Busca por UUID con findById. Si no existe, lanza excepción.

---

## User.java (Modelo de dominio)

Entidad rica con lógica de negocio propia. Usa @Getter de Lombok.

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

### PasswordHasherPort

Define: hashPassword (encode con BCrypt), matches (compara contraseña en texto plano con hash).

### TokenProvider

Define: generateToken (crea JWT con claims del usuario), extractUserId (extrae el subject del token), isTokenValid (verifica firma y expiración).

---

## Adaptador de persistencia

### UserEntity

Entidad JPA mapeada a la tabla users. Columnas: id (UUID, PK), username (unique), email (unique), password, created_at, modified_at, active, anonymized. Usa Lombok para getters, setters y constructores.

### JpaUserRepository

Interfaz que extiende JpaRepository. Spring Data genera la implementación automáticamente. Métodos personalizados: findByEmail, findByUsername, findByUsernameOrEmail.

### UserEntityMapper

Interfaz MapStruct anotada con @Mapper(componentModel = "spring"). Spring la inyecta como bean. Métodos: toEntity (User → UserEntity), toDomain (UserEntity → User).

### UserPostgresAdapter

Implementa UserRepository. Traduce entre dominio y entidad usando UserEntityMapper. Delega las operaciones de base de datos en JpaUserRepository. Flujo de save: dominio → toEntity → jpaUserRepository.save → toDomain → retorna dominio.

---

## Adaptadores de seguridad

### BCryptPasswordHasherAdapter

Implementa PasswordHasherPort. Usa PasswordEncoder de Spring Security inyectado en el constructor. hashPassword llama a encode, matches llama a matches.

### JwtTokenAdapter

Implementa TokenProvider. Recibe jwt.secret y jwt.expiration desde application.yml. generateToken crea un JWT con: subject = userId, claims (email, username), iat (fecha actual), exp (fecha de expiración), firma HMAC con clave secreta. extractUserId extrae el subject del token. isTokenValid verifica firma y expiración, devuelve false si el token es inválido o ha expirado.

---

## Conexión con el frontend

El módulo auth del frontend se comunica con estos endpoints:

- POST /api/v1/auth/register → RegisterUserRequest (username?, email?, password) → 201 Created
- POST /api/v1/auth/login → LoginRequest (identifier, password) → 200 con TokenResponse (token)
- GET /api/v1/auth/me/topnav → Authorization: Bearer token → 200 con TopNavUserResponse (username, email)

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
2. AuthController traduce a LoginUserCommand.
3. UserService busca al usuario por identifier mediante UserPostgresAdapter.
4. BCryptPasswordHasherAdapter compara la contraseña con el hash almacenado.
5. Si es correcto, JwtTokenAdapter genera un JWT con los datos del usuario.
6. AuthController envuelve el JWT en TokenResponseDTO y responde 200.
7. Frontend guarda el token en AuthContext y localStorage.