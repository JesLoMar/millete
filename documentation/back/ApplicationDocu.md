# Configuración de la aplicación — application.yml y application-prod.yml

La aplicación utiliza dos archivos de configuración principales: uno para desarrollo local con valores por defecto y otro para producción con todas las credenciales externalizadas mediante variables de entorno.

---

## application.yml

Configuración base para desarrollo local. Contiene valores por defecto que permiten ejecutar la aplicación sin necesidad de configurar variables de entorno adicionales.

### spring.application.name

Nombre de la aplicación: MyBudgetApp. Identifica la aplicación en logs, métricas y contexto de Spring.

### spring.profiles.active

Perfil activo por defecto: dev. La aplicación arranca en modo desarrollo sin necesidad de especificar un perfil explícitamente.

### spring.jpa

Configuración de Hibernate y JPA.

- database-platform: org.hibernate.dialect.PostgreSQLDialect, dialecto específico para PostgreSQL.
- open-in-view: false, evita errores de sesión en consola y mejora el rendimiento al no mantener la sesión de Hibernate abierta durante el renderizado de vistas.
- hibernate.ddl-auto: validate, Hibernate no modifica el esquema, solo valida que las entidades coincidan con las tablas existentes. Flyway se encarga de las migraciones.
- show-sql: true, muestra las consultas SQL generadas en los logs para facilitar la depuración.

### spring.flyway

Configuración de Flyway para migraciones automáticas de base de datos.

- enabled: true, activa el sistema de migraciones.
- baseline-on-migrate: true, permite ejecutar migraciones en bases de datos que ya contienen tablas, creando la tabla de historial de Flyway en la primera ejecución.

### spring.datasource

Conexión a la base de datos PostgreSQL. Utiliza placeholders con valores por defecto para desarrollo local.

- url: jdbc:postgresql://localhost:5432/mybudgetapp_dev, base de datos local de desarrollo.
- username: developer, usuario local de desarrollo.
- password: devpassword123, contraseña local de desarrollo, no apta para producción.
- driver-class-name: org.postgresql.Driver.

Las variables de entorno DATABASE_URL, DATABASE_USER y DATABASE_PASSWORD pueden sobrescribir estos valores si están definidas.

### spring.mail

Configuración del servicio de envío de correos electrónicos mediante un proveedor SMTP externo. Utiliza placeholders con valores por defecto para desarrollo.

- host: smtp.example-mail.com.
- port: 587, puerto SMTP con STARTTLS.
- username: devuser@example-mail.com, credencial de autenticación SMTP de desarrollo.
- password: dev-api-key-example-12345, clave API de desarrollo para envío de correos.
- from: dev-app@example.com, dirección remitente de los correos en desarrollo.
- properties.mail.smtp.auth: true, requiere autenticación SMTP.
- properties.mail.smtp.starttls.enable: true, activa cifrado TLS para la conexión.

El adaptador de correo usa esta configuración para enviar correos de invitación con enlaces de aceptación. Las variables de entorno MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD y MAIL_FROM pueden sobrescribir estos valores.

### app.version

Versión actual de la aplicación: 0.0.1. Se inyecta en el servicio de exportación para incluirla en los metadatos de los archivos generados.

### app.frontend.url

URL del frontend: http://localhost:3000 por defecto. Usada para construir enlaces en los correos electrónicos y para configuración de CORS. La variable de entorno FRONTEND_URL puede sobrescribir este valor.

### jwt.secret

Clave secreta HMAC de 256 bits para firmar y verificar tokens JWT. El valor por defecto es solo para desarrollo local. Se inyecta en el adaptador JWT mediante la anotación Value. La variable de entorno JWT_SECRET puede sobrescribir este valor.

### jwt.expiration

Tiempo de expiración de los tokens JWT en milisegundos. Valor por defecto: 86400000 ms (24 horas). Transcurrido este tiempo, el token deja de ser válido y el usuario debe volver a iniciar sesión. La variable de entorno JWT_EXPIRATION puede sobrescribir este valor.

### logging.level

Niveles de log para desarrollo.

- root: INFO, muestra información general de la aplicación.
- com.mybudgetapp: DEBUG, muestra detalles de beans, consultas SQL y otra información útil para depuración.

---

## application-prod.yml

Configuración para entornos de producción. Se activa con el perfil prod mediante la variable de entorno SPRING_PROFILES_ACTIVE=prod. Todas las credenciales sensibles deben proporcionarse mediante variables de entorno.

### jasypt.encryptor.password

Clave maestra para desencriptación Jasypt. Es opcional: si no se define, los valores de las variables de entorno se toman literalmente. Si se define, Jasypt descifra automáticamente cualquier valor que comience por ENC. Se proporciona mediante la variable de entorno JASYPT_ENCRYPTOR_PASSWORD.

### spring.jpa

Misma configuración de JPA que en desarrollo, excepto show-sql que se establece en false para no exponer consultas SQL en los logs de producción por seguridad y rendimiento.

### spring.flyway

Misma configuración de Flyway que en desarrollo: enabled en true y baseline-on-migrate en true.

### spring.datasource

Conexión a la base de datos PostgreSQL en producción. Todos los valores son obligatorios y deben proporcionarse mediante variables de entorno sin valores por defecto.

- url: proporcionado por DATABASE_URL.
- username: proporcionado por DATABASE_USER.
- password: proporcionado por DATABASE_PASSWORD. Puede estar en texto plano o encriptado con Jasypt usando el formato ENC.

### spring.mail

Configuración de correo electrónico en producción. Todos los valores son obligatorios y deben proporcionarse mediante variables de entorno.

- host: proporcionado por MAIL_HOST.
- port: proporcionado por MAIL_PORT.
- username: proporcionado por MAIL_USERNAME.
- password: proporcionado por MAIL_PASSWORD. Puede estar en texto plano o encriptado con Jasypt.
- from: proporcionado por MAIL_FROM.
- properties.mail.smtp.auth: true.
- properties.mail.smtp.starttls.enable: true.

### app.frontend.url

URL del frontend en producción. Proporcionado por la variable de entorno FRONTEND_URL. No tiene valor por defecto para evitar configuraciones incorrectas.

### jwt.secret

Clave secreta JWT para producción. Obligatoria, sin valor por defecto. Proporcionada por la variable de entorno JWT_SECRET. Puede estar en texto plano o encriptada con Jasypt.

### jwt.expiration

Tiempo de expiración de los tokens JWT. Valor por defecto: 86400000 ms (24 horas) si no se define JWT_EXPIRATION.

### logging.level

Niveles de log para producción, más restrictivos que en desarrollo.

- root: WARN, solo muestra advertencias y errores.
- com.mybudgetapp: INFO, muestra inicio de aplicación y endpoints accedidos sin detalles excesivos.

---

## Variables de entorno requeridas en producción

La aplicación incluye un archivo .env.template como plantilla para configurar las variables de entorno necesarias en producción. Las variables requeridas son:

- SPRING_PROFILES_ACTIVE: debe ser prod.
- DATABASE_URL: URL completa de conexión JDBC a PostgreSQL.
- DATABASE_USER: usuario de la base de datos.
- DATABASE_PASSWORD: contraseña de la base de datos.
- MAIL_HOST: servidor SMTP.
- MAIL_PORT: puerto SMTP.
- MAIL_USERNAME: usuario de autenticación SMTP.
- MAIL_PASSWORD: contraseña o API key del servicio de correo.
- MAIL_FROM: dirección remitente de los correos.
- FRONTEND_URL: URL del frontend desplegado.
- JWT_SECRET: clave secreta para firmar tokens JWT, mínimo 256 bits.
- JWT_EXPIRATION: opcional, tiempo de expiración de tokens en milisegundos, por defecto 86400000 (24 horas).
- JASYPT_ENCRYPTOR_PASSWORD: opcional, clave maestra para desencriptar valores protegidos con Jasypt.

---

## Jasypt — Encriptación opcional de propiedades

La aplicación soporta Jasypt para encriptar valores sensibles en las variables de entorno. Su uso es completamente opcional.

Si no se define JASYPT_ENCRYPTOR_PASSWORD, todas las variables de entorno se interpretan como texto plano. Si se define, cualquier variable que comience por ENC se descifra automáticamente usando la clave maestra proporcionada.

Para generar valores encriptados se puede usar el plugin Maven incluido en el proyecto:

mvn jasypt:encrypt-value -Djasypt.encryptor.password="claveMaestra" -Djasypt.plugin.value="valorAEncriptar"

El resultado se usa en la variable de entorno correspondiente con el formato ENC(resultado).

---

## Perfiles

La aplicación utiliza dos perfiles de configuración:

- dev: perfil por defecto, activo al arrancar sin especificar SPRING_PROFILES_ACTIVE. Usa los valores por defecto definidos en application.yml para desarrollo local.
- prod: perfil de producción, activado con SPRING_PROFILES_ACTIVE=prod. Requiere que todas las credenciales se proporcionen mediante variables de entorno.

---

## Valores sensibles

Los siguientes valores nunca deben exponerse en repositorios públicos ni documentación:

- spring.mail.password
- jwt.secret
- spring.datasource.password
- JASYPT_ENCRYPTOR_PASSWORD

Se recomienda usar siempre variables de entorno en producción y, opcionalmente, encriptar los valores con Jasypt para mayor seguridad.