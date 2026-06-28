# Configuración de la aplicación — application.yml y application-prod.yml

La aplicación utiliza dos archivos de configuración principales: uno para desarrollo local con valores por defecto y otro para producción con todas las credenciales externalizadas mediante variables de entorno.

---

## application.yml

Configuración base para desarrollo local. Contiene valores por defecto que permiten ejecutar la aplicación sin necesidad de configurar variables de entorno adicionales.

### server.port

Puerto del servidor: `8080`.

### spring.application.name

Nombre de la aplicación: `Millete`. Identifica la aplicación en logs, métricas y contexto de Spring.

### spring.profiles.active

Perfil activo por defecto: `dev`. La aplicación arranca en modo desarrollo sin necesidad de especificar un perfil explícitamente.

### spring.servlet.multipart

Límites para subida de archivos:
- **max-file-size:** 10MB.
- **max-request-size:** 10MB.

### spring.jpa

Configuración de Hibernate y JPA.

- **database-platform:** `org.hibernate.dialect.PostgreSQLDialect`, dialecto específico para PostgreSQL.
- **open-in-view:** false, evita errores de sesión en consola y mejora el rendimiento al no mantener la sesión de Hibernate abierta durante el renderizado de vistas.
- **hibernate.ddl-auto:** `validate`, Hibernate no modifica el esquema, solo valida que las entidades coincidan con las tablas existentes. Flyway se encarga de las migraciones.
- **show-sql:** false, no muestra consultas SQL en desarrollo por defecto.
- **properties.hibernate.format_sql:** true, formatea las consultas SQL cuando están habilitadas.

### spring.flyway

Configuración de Flyway para migraciones automáticas de base de datos.

- **enabled:** true, activa el sistema de migraciones.
- **baseline-on-migrate:** true, permite ejecutar migraciones en bases de datos que ya contienen tablas, creando la tabla de historial de Flyway en la primera ejecución.

### spring.datasource

Conexión a la base de datos PostgreSQL. Utiliza placeholders con valores por defecto para desarrollo local.

- **url:** `jdbc:postgresql://localhost:5432/millete_db`, base de datos local de desarrollo.
- **username:** `postgres`, usuario local de desarrollo.
- **password:** `654321`, contraseña local de desarrollo, no apta para producción.
- **driver-class-name:** `org.postgresql.Driver`.

Las variables de entorno `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD` pueden sobrescribir estos valores si están definidas.

### app.version

Versión actual de la aplicación: `0.1.0`. Se inyecta en el servicio de exportación para incluirla en los metadatos de los archivos generados.

### app.frontend.url

URL del frontend: `http://localhost:3000` por defecto. Usada para construir enlaces en correos electrónicos y para configuración de CORS. La variable de entorno `FRONTEND_URL` puede sobrescribir este valor.

### jwt.secret

Clave secreta HMAC para firmar y verificar tokens JWT. El valor por defecto es solo para desarrollo local. Se inyecta en el adaptador JWT mediante la anotación `@Value`. La variable de entorno `JWT_SECRET` puede sobrescribir este valor.

### jwt.expiration

Tiempo de expiración de los tokens JWT en milisegundos. Valor por defecto: `43200000` ms (12 horas). Transcurrido este tiempo, el token deja de ser válido y el usuario debe volver a iniciar sesión. La variable de entorno `JWT_EXPIRATION` puede sobrescribir este valor.

### logging.level

Niveles de log para desarrollo.

- **root:** INFO, muestra información general de la aplicación.
- **com.puntomartinez:** DEBUG, muestra detalles útiles para depuración.

---

## application-prod.yml

Configuración para entornos de producción. Se activa con el perfil `prod` mediante la variable de entorno `SPRING_PROFILES_ACTIVE=prod`. Todas las credenciales sensibles deben proporcionarse mediante variables de entorno.

### jasypt.encryptor.password

Clave maestra para desencriptación Jasypt. Es opcional: si no se define, los valores de las variables de entorno se toman literalmente. Si se define, Jasypt descifra automáticamente cualquier valor que comience por `ENC(...)`. Se proporciona mediante la variable de entorno `JASYPT_ENCRYPTOR_PASSWORD`.

### spring.jpa

Misma configuración de JPA que en desarrollo, excepto `show-sql` que se establece en false para no exponer consultas SQL en los logs de producción por seguridad y rendimiento.

### spring.flyway

Misma configuración de Flyway que en desarrollo: enabled en true y baseline-on-migrate en true.

### spring.datasource

Conexión a la base de datos PostgreSQL en producción. Todos los valores son obligatorios y deben proporcionarse mediante variables de entorno sin valores por defecto.

- **url:** proporcionado por `DATABASE_URL`.
- **username:** proporcionado por `DATABASE_USER`.
- **password:** proporcionado por `DATABASE_PASSWORD`. Puede estar en texto plano o encriptado con Jasypt usando el formato `ENC(...)`.

### jwt.secret / jwt.expiration

Proporcionados por las variables de entorno `JWT_SECRET` y `JWT_EXPIRATION`. En producción es obligatorio generar un secreto fuerte (mínimo 256 bits).

### Variables de entorno principales

| Variable | Uso |
|----------|-----|
| `DATABASE_URL` | URL JDBC de PostgreSQL |
| `DATABASE_USER` | Usuario de PostgreSQL |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL (puede ser `ENC(...)`) |
| `JWT_SECRET` | Secreto para firmar JWT |
| `JWT_EXPIRATION` | Tiempo de expiración del JWT en ms |
| `JASYPT_ENCRYPTOR_PASSWORD` | Clave maestra Jasypt (opcional) |
| `FRONTEND_URL` | URL del frontend para CORS y enlaces |
| `SPRING_PROFILES_ACTIVE` | Perfil activo (`dev` o `prod`) |
