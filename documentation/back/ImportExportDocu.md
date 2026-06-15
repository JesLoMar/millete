# Data Export/Import — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/DataExportService.java** — Servicio de exportación de datos (JSON, CSV, PDF)
- **application/services/DataImportService.java** — Servicio de importación con validación y migración
- **domain/migration/DataMigration.java** — Interfaz de migración entre versiones
- **domain/migration/MigrationChain.java** — Cadena de migraciones versionadas
- **domain/model/ExportVersion.java** — Versionado semántico del formato
- **domain/model/UserDataSnapshot.java** — Contenedor de datos exportados
- **domain/ports/out/FileExportPort.java** — Puerto de salida para generación de archivos
- **infrastructure/in/controller/DataExportController.java** — Endpoints de exportación
- **infrastructure/in/controller/DataImportController.java** — Endpoint de importación
- **infrastructure/out/persistence/postgresql/adapters/CsvFileExportAdapter.java** — Adaptador de exportación CSV
- **infrastructure/out/persistence/postgresql/adapters/PdfFileExportAdapter.java** — Adaptador de exportación PDF

---

## DataExportController.java

Controlador REST mapeado a /api/v1/data.

### GET /export

Exporta todos los datos del usuario autenticado en un archivo JSON.

1. Extrae el userId del token JWT.
2. Llama a DataExportService.exportAllUserData().
3. Devuelve el UserDataSnapshot como archivo descargable con cabeceras Content-Disposition.
4. Incluye cabeceras X-Export-Version y X-Export-Date.

### GET /export/csv

Exporta todos los datos del usuario autenticado en un archivo CSV.

1. Extrae el userId del token JWT.
2. Llama a DataExportService.exportUserDataAsCsv().
3. Devuelve el archivo CSV como descargable con Content-Type text/csv.
4. El nombre del archivo es familybudget_export.csv.
5. Incluye transacciones e inversiones en formato tabular con cabeceras: Tipo, ID, Descripcion, Monto, Fecha, Categoria.

### GET /export/pdf

Exporta todos los datos del usuario autenticado en un archivo PDF.

1. Extrae el userId del token JWT.
2. Llama a DataExportService.exportUserDataAsPdf().
3. Devuelve el archivo PDF como descargable con Content-Type application/pdf.
4. El nombre del archivo es familybudget_export.pdf.
5. El documento incluye título con fecha de exportación, versión del formato, y secciones separadas para transacciones e inversiones.
6. Si el contenido excede una página, se genera una nueva automáticamente.

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
6. Responde 400 Bad Request si hay errores de formato, versión o base de datos.

---

## DataExportService.java

Servicio que genera un snapshot completo de los datos del usuario en múltiples formatos.

### exportAllUserData

1. Crea un SnapshotMetadata con la versión actual del formato, fecha de exportación y versión de la app.
2. Recopila datos de los repositorios: categorías, transacciones, transacciones programadas e inversiones.
3. Devuelve un UserDataSnapshot con metadata y datos.

### exportUserDataAsCsv

1. Llama a exportAllUserData para obtener el snapshot.
2. Delega la generación del archivo CSV en el puerto FileExportPort.
3. Devuelve el array de bytes del archivo CSV.

### exportUserDataAsPdf

1. Llama a exportAllUserData para obtener el snapshot.
2. Delega la generación del archivo PDF en el puerto FileExportPort.
3. Devuelve el array de bytes del archivo PDF.

---

## DataImportService.java

Servicio que importa datos con validación de compatibilidad de versión y migración automática.

### importUserData

1. Deserializa el archivo JSON a UserDataSnapshot.
2. Valida la compatibilidad de versión: mismo MAJOR que la versión actual.
3. Si la versión es anterior, aplica las migraciones necesarias mediante MigrationChain.
4. Importa cada entidad asignando el userId del usuario autenticado.
5. Devuelve un resumen con el número de registros importados y la versión.

### validateAndMigrate

- Compara la versión del archivo con ExportVersion.CURRENT.
- Si el MAJOR es distinto: error de incompatibilidad.
- Si la versión es anterior: aplica MigrationChain.migrateToLatest().

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

- metadata: SnapshotMetadata con version, exportDate y appVersion.
- categories, transactions, plannedTransactions, investments: listas de datos.

---

## FileExportPort.java

Interfaz que define el puerto de salida para la generación de archivos de exportación. Permite desacoplar la lógica de negocio de la implementación concreta del formato de archivo.

### Métodos

- generateCsv: recibe un UserDataSnapshot y devuelve un array de bytes con el contenido CSV.
- generatePdf: recibe un UserDataSnapshot y devuelve un array de bytes con el contenido PDF.

---

## CsvFileExportAdapter.java

Implementación del puerto FileExportPort para generar archivos CSV. Utiliza Apache Commons CSV.

### generateCsv

1. Crea un CSVPrinter con cabeceras: Tipo, ID, Descripcion, Monto, Fecha, Categoria.
2. Itera sobre las transacciones del snapshot y escribe cada una como fila con tipo "TRANSACCION".
3. Itera sobre las inversiones del snapshot y escribe cada una como fila con tipo "INVERSION".
4. Retorna el archivo CSV como array de bytes.

---

## PdfFileExportAdapter.java

Implementación del puerto FileExportPort para generar archivos PDF. Utiliza Apache PDFBox.

### generatePdf

1. Crea un documento PDF tamaño A4.
2. Escribe el título "Exportacion de datos" con la fecha de exportación en negrita.
3. Escribe la versión del formato.
4. Dibuja una línea separadora.
5. Añade sección "Transacciones" con los campos: descripción, tipo, monto y fecha.
6. Añade sección "Inversiones" con los campos: nombre del activo, tipo, cantidad y fecha de compra.
7. Si el contenido excede el espacio disponible en la página, crea una nueva página automáticamente.
8. Retorna el archivo PDF como array de bytes.

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
| GET | /api/v1/data/export | Exportar todos los datos (JSON) |
| GET | /api/v1/data/export/csv | Exportar todos los datos (CSV) |
| GET | /api/v1/data/export/pdf | Exportar todos los datos (PDF) |
| POST | /api/v1/data/import | Importar datos desde archivo JSON |

---

## Dependencias externas

| Librería | Versión | Uso |
|----------|---------|-----|
| Apache Commons CSV | 1.12.0 | Generación de archivos CSV |
| Apache PDFBox | 3.0.4 | Generación de archivos PDF |

---

## Seguridad

- Los archivos de exportación no contienen información del propietario, son portables entre cuentas.
- Cualquier usuario autenticado puede importar cualquier archivo compatible.
- El userId se asigna automáticamente con el del usuario autenticado durante la importación.
- La importación es transaccional: o se importa todo o nada.
- Los archivos de versiones incompatibles se rechazan automáticamente.
- Los endpoints de exportación requieren autenticación JWT válida.