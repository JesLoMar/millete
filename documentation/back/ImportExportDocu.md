# Data Export/Import — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/DataExportService.java** — Servicio de exportación de datos
- **application/services/DataImportService.java** — Servicio de importación con validación
- **domain/exception/OwnershipException.java** — Excepción de validación de propiedad
- **domain/migration/DataMigration.java** — Interfaz de migración entre versiones
- **domain/migration/MigrationChain.java** — Cadena de migraciones versionadas
- **domain/model/ExportVersion.java** — Versionado semántico del formato
- **domain/model/UserDataSnapshot.java** — Contenedor de datos exportados
- **infrastructure/in/controller/DataExportController.java** — Endpoint de exportación
- **infrastructure/in/controller/DataImportController.java** — Endpoint de importación

---

## DataExportController.java

Controlador REST mapeado a /api/v1/data.

### GET /export

Exporta todos los datos del usuario autenticado en un archivo JSON.

1. Extrae el userId del token JWT.
2. Llama a DataExportService.exportAllUserData().
3. Devuelve el UserDataSnapshot como archivo descargable con cabeceras Content-Disposition.
4. Incluye cabeceras X-Export-Version y X-Export-Date.

---

## DataImportController.java

Controlador REST mapeado a /api/v1/data.

### POST /import

Importa datos desde un archivo JSON previamente exportado.

1. Recibe un archivo MultipartFile.
2. Valida que el archivo no esté vacío.
3. Valida que tenga extensión .json.
4. Llama a DataImportService.importUserData().
5. Responde 200 con resumen de la importación si todo es correcto.
6. Responde 403 Forbidden si el archivo no pertenece al usuario (OwnershipException).
7. Responde 400 Bad Request si hay errores de formato o versión.

---

## DataExportService.java

Servicio que genera un snapshot completo de los datos del usuario.

### exportAllUserData

1. Crea un SnapshotMetadata con la versión actual del formato, fecha de exportación, userId y versión de la app.
2. Recopila datos de los repositorios: categorías, transacciones, transacciones programadas e inversiones.
3. Devuelve un UserDataSnapshot con metadata y datos.

---

## DataImportService.java

Servicio que importa datos con validación de propiedad, compatibilidad de versión y migración automática.

### importUserData

1. Deserializa el archivo JSON a UserDataSnapshot.
2. Valida la propiedad: el userId del archivo debe coincidir con el usuario autenticado.
3. Valida la compatibilidad de versión: mismo MAJOR que la versión actual.
4. Si la versión es anterior, aplica las migraciones necesarias mediante MigrationChain.
5. Importa cada entidad sobrescribiendo el userId por seguridad.
6. Devuelve un resumen con el número de registros importados y la versión.

### validateOwnership

- Si el archivo no tiene userId: lanza OwnershipException con código ARCHIVO_SIN_PROPIETARIO.
- Si el userId no coincide con el usuario autenticado: lanza OwnershipException con código PROPIETARIO_NO_COINCIDE.

### validateAndMigrate

- Compara la versión del archivo con ExportVersion.CURRENT.
- Si el MAJOR es distinto: error de incompatibilidad.
- Si la versión es anterior: aplica MigrationChain.migrateToLatest().

---

## OwnershipException.java

Excepción personalizada con código de error para identificar el tipo de problema de propiedad. Extiende RuntimeException.

### Códigos de error

- ARCHIVO_SIN_PROPIETARIO: el archivo no contiene userId.
- PROPIETARIO_NO_COINCIDE: el userId del archivo no es el del usuario autenticado.

---

## ExportVersion.java

Versionado semántico (MAJOR.MINOR.PATCH) para el formato de exportación.

### CURRENT

Versión actual del formato: 0.0.1.

### Reglas de compatibilidad

- Mismo MAJOR: compatible, puede necesitar migración.
- Distinto MAJOR: incompatible, no se puede importar.

### Métodos

- fromString: parsea una cadena "X.Y.Z".
- isCompatibleWith: true si comparten MAJOR.
- needsMigration: true si esta versión es anterior a la target.
- compareTo: comparación numérica por MAJOR, luego MINOR, luego PATCH.

---

## UserDataSnapshot.java

Contenedor de todos los datos exportados. Anotado con @JsonIgnoreProperties(ignoreUnknown = true) para permitir lectura de versiones anteriores.

### Estructura

- metadata: SnapshotMetadata con version, exportDate, userId, appVersion.
- categories, transactions, plannedTransactions, investments: listas de datos.

---

## DataMigration.java

Interfaz que define una transformación entre dos versiones del formato.

- fromVersion: versión de origen.
- toVersion: versión de destino.
- description: texto descriptivo del cambio.
- migrate: ejecuta la transformación.

---

## MigrationChain.java

Cadena de migraciones que transforma datos desde cualquier versión anterior hasta la versión actual.

### Funcionamiento

1. Registra todas las migraciones conocidas en orden cronológico.
2. Al recibir un snapshot, itera sobre las migraciones aplicando solo las necesarias.
3. Valida que la cadena no tenga huecos al iniciar la aplicación.
4. Si no hay migraciones registradas (primera versión), simplemente retorna el snapshot.

### Migraciones actuales

Ninguna. La versión 0.0.1 es la primera, por lo que el registro de migraciones está vacío. Las migraciones se añadirán cuando el esquema de exportación evolucione.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/data/export | Exportar todos los datos |
| POST | /api/v1/data/import | Importar datos desde archivo |

---

## Seguridad

- Solo el propietario original de los datos puede importarlos.
- El userId se sobrescribe con el del usuario autenticado durante la importación.
- La importación es transaccional: o se importa todo o nada.
- Los archivos de versiones incompatibles se rechazan automáticamente.