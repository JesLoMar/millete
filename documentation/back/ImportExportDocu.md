# Data Export/Import — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/DataExportService.java** — Servicio de exportación de datos (JSON, ZIP, CSV individual, PDF)
- **application/services/DataImportService.java** — Servicio de importación con validación y migración
- **domain/migration/DataMigration.java** — Interfaz de migración entre versiones
- **domain/migration/MigrationChain.java** — Cadena de migraciones versionadas
- **domain/model/ExportData.java** — DTO para exportación tabular (CSV/ZIP)
- **domain/model/ExportVersion.java** — Versionado semántico del formato
- **domain/model/PdfExportData.java** — DTO para exportación PDF con métricas
- **domain/model/PeriodType.java** — Enum de periodos disponibles para PDF
- **domain/model/UserDataSnapshot.java** — Contenedor de datos exportados (JSON)
- **domain/ports/out/FileExportPort.java** — Puerto de salida para generación de archivos
- **infrastructure/in/controller/DataExportController.java** — Endpoints de exportación
- **infrastructure/in/controller/DataImportController.java** — Endpoint de importación
- **infrastructure/out/fileexport/ZipFileExportAdapter.java** — Adaptador de exportación ZIP y CSV
- **infrastructure/out/fileexport/HtmlPdfFileExportAdapter.java** — Adaptador de exportación PDF con Thymeleaf + Flying Saucer
- **resources/templates/export-pdf.html** — Plantilla Thymeleaf para el PDF

---

## DataExportController.java

Controlador REST mapeado a /api/v1/data.

### GET /export

Exporta todos los datos del usuario autenticado en un archivo JSON (snapshot completo para backup o migración entre cuentas).

1. Extrae el userId del token JWT.
2. Llama a DataExportService.exportAllUserData().
3. Devuelve el UserDataSnapshot como archivo descargable con cabeceras Content-Disposition.
4. Incluye cabeceras X-Export-Version y X-Export-Date.

### GET /export/zip

Exporta todos los datos del usuario autenticado en un archivo ZIP con un CSV por entidad.

1. Extrae el userId del token JWT.
2. Llama a DataExportService.exportUserDataAsZip().
3. Devuelve el archivo ZIP como descargable con Content-Type application/zip.
4. El nombre del archivo es familybudget_export.zip.
5. Contiene cinco archivos CSV: categories.csv, transactions.csv, planned_transactions.csv, investments.csv, savings_goals.csv.
6. Los campos exportados están optimizados para análisis en Excel o Google Sheets, excluyendo IDs, timestamps internos y metadatos.

### GET /export/csv/{entityType}

Exporta una entidad concreta en formato CSV individual.

1. Extrae el userId del token JWT.
2. El parámetro entityType puede ser: categories, transactions, planned_transactions, investments, savings_goals.
3. Llama a DataExportService.exportUserDataAsCsv(userId, entityType).
4. Devuelve el archivo CSV como descargable con Content-Type text/csv.
5. El nombre del archivo es familybudget_{entityType}.csv.

### GET /export/pdf

Exporta un informe financiero en PDF con resumen de métricas y listados de datos.

1. Extrae el userId del token JWT.
2. Acepta el parámetro period con valores: 1m, 3m, 6m, 1y (por defecto 1m).
3. Llama a DataExportService.exportUserDataAsPdf(userId, period).
4. Devuelve el archivo PDF como descargable con Content-Type application/pdf.
5. El nombre del archivo es millete_financial_data_{period}.pdf.
6. La primera página contiene cabecera con título y periodo, 8 tarjetas de métricas (Balance, Income, Expenses, Transactions, Investments Value, Active Investments, Savings Goals, Total Saved), tabla de inversiones activas y tabla de savings goals.
7. En páginas siguientes se lista la tabla de transacciones del periodo.
8. Si el contenido excede una página, se generan páginas adicionales automáticamente con el mismo estilo.
9. El diseño usa tema oscuro con la paleta de colores corporativa.

---

## DataImportController.java

Controlador REST mapeado a /api/v1/data.

### POST /import

Importa datos desde un archivo JSON previamente exportado con GET /export.

1. Recibe un archivo MultipartFile.
2. Valida que el archivo no esté vacío.
3. Valida que tenga extensión .json.
4. Llama a DataImportService.importUserData().
5. Responde 200 con resumen de la importación si todo es correcto.
6. Responde 400 Bad Request si hay errores de formato, versión o base de datos.

---

## DataExportService.java

Servicio que genera exportaciones de datos en múltiples formatos.

### exportAllUserData

1. Crea un SnapshotMetadata con la versión actual del formato, fecha de exportación y versión de la app.
2. Recopila datos de los repositorios: categorías, transacciones, transacciones programadas, inversiones y savings goals.
3. Devuelve un UserDataSnapshot con metadata y datos.

### buildExportData

1. Llama a exportAllUserData para obtener el snapshot completo.
2. Construye un mapa de categoryId -> categoryName para resolver nombres de categoría.
3. Filtra solo registros activos de cada entidad.
4. Convierte cada entidad del dominio a su correspondiente ExportRow, sustituyendo IDs de categoría por nombres.
5. Calcula el porcentaje de progreso para cada savings goal.
6. Devuelve un objeto ExportData con las cinco listas de filas listas para serializar.

### buildPdfExportData

1. Llama a exportAllUserData para obtener el snapshot completo.
2. Filtra transacciones por el rango de fechas del periodo seleccionado.
3. Calcula métricas: balance, ingresos totales, gastos totales, número de transacciones, categoría con mayor gasto y su porcentaje.
4. Calcula valor total de inversiones activas.
5. Calcula número de savings goals activos y total ahorrado.
6. Construye filas específicas para el PDF con tipos traducidos (Ingreso/Gasto).
7. Devuelve un objeto PdfExportData con resumen, transacciones, inversiones y savings goals.

### exportUserDataAsZip

1. Llama a buildExportData.
2. Delega la generación del ZIP en el puerto FileExportPort (ZipFileExportAdapter).
3. Devuelve el array de bytes del archivo ZIP.

### exportUserDataAsCsv

1. Llama a buildExportData.
2. Recibe un entityType para seleccionar qué entidad exportar.
3. Delega la generación del CSV individual en el puerto FileExportPort (ZipFileExportAdapter).
4. Devuelve el array de bytes del archivo CSV.

### exportUserDataAsPdf

1. Llama a buildPdfExportData con el periodo indicado.
2. Delega la generación del PDF en el puerto FileExportPort (HtmlPdfFileExportAdapter).
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

Versionado semántico (MAJOR.MINOR.PATCH) para el formato de exportación JSON.

### CURRENT

Versión actual del formato: 0.1.0.

### Reglas de compatibilidad

- Mismo MAJOR: compatible, puede necesitar migración.
- Distinto MAJOR: incompatible, no se puede importar.

### Métodos

- fromString: parsea una cadena "X.Y.Z".
- isCompatibleWith: true si comparten MAJOR.
- needsMigration: true si esta versión es anterior a la target.
- compareTo: comparación numérica por MAJOR, luego MINOR, luego PATCH.

---

## PeriodType.java

Enum que define los periodos disponibles para el informe PDF.

### Valores

- ONE_MONTH ("1m", 1 mes): "1 month"
- THREE_MONTHS ("3m", 3 meses): "3 months"
- SIX_MONTHS ("6m", 6 meses): "6 months"
- ONE_YEAR ("1y", 12 meses): "1 year"

### Métodos

- fromCode: convierte el código (1m, 3m, 6m, 1y) al enum correspondiente.
- getStartDate: devuelve la fecha de inicio restando los meses desde la fecha actual.
- getEndDate: devuelve la fecha actual.
- getDisplayName: devuelve el nombre legible en inglés.

---

## UserDataSnapshot.java

Contenedor de todos los datos exportados en formato JSON. Anotado con @JsonIgnoreProperties(ignoreUnknown = true) para permitir lectura de versiones anteriores.

### Estructura

- metadata: SnapshotMetadata con version, exportDate y appVersion.
- categories, transactions, plannedTransactions, investments, savingsGoals: listas de datos.

---

## ExportData.java

DTO específico para la exportación tabular (CSV y ZIP). Contiene listas de records con campos optimizados para análisis, sin IDs, timestamps internos ni metadatos de versión.

### Estructura

- categories: lista de CategoryExportRow (name, budgetLimit).
- transactions: lista de TransactionExportRow (categoryName, amount, date, type, description).
- plannedTransactions: lista de PlannedTransactionExportRow (categoryName, amount, type, description, frequencyType, frequencyInterval, startDate, endDate, lastExecutedDate).
- investments: lista de InvestmentExportRow (assetName, ticker, quantity, purchasePrice, currentPrice, type, purchaseDate).
- savingsGoals: lista de SavingsGoalExportRow (name, targetAmount, currentAmount, progress, deadline, priority, status, link).

---

## PdfExportData.java

DTO específico para la exportación PDF. Contiene un resumen ejecutivo con métricas y listas de filas formateadas.

### Estructura

- periodDisplayName, startDate, endDate: información del periodo.
- summary: objeto Summary con balance, totalIncome, totalExpenses, transactionCount, topCategoryName, topCategoryAmount, topCategoryPercentage, investmentsTotalValue, activeInvestmentsCount, activeSavingsGoalsCount, totalSavedAmount.
- transactions: lista de TransactionRow (date, categoryName, description, type, amount).
- investments: lista de InvestmentRow (assetName, ticker, type, quantity, purchasePrice, currentPrice, currentValue, profitLoss, returnPercentage).
- savingsGoals: lista de SavingsGoalRow (name, targetAmount, currentAmount, progress, deadline, priority, status, link).

---

## FileExportPort.java

Interfaz que define el puerto de salida para la generación de archivos de exportación.

### Métodos

- generateZip: recibe un ExportData y devuelve un array de bytes con un archivo ZIP conteniendo un CSV por entidad.
- generateCsv: recibe un ExportData y un entityType, devuelve un array de bytes con el CSV de la entidad solicitada.
- generatePdf: recibe un PdfExportData y devuelve un array de bytes con el contenido PDF.

---

## ZipFileExportAdapter.java

Implementación del puerto FileExportPort para generar archivos ZIP y CSV individuales. Utiliza Apache Commons CSV y java.util.zip.

### generateZip

1. Crea un ZipOutputStream.
2. Para cada entidad (categories, transactions, planned_transactions, investments, savings_goals), añade una entrada ZIP con su CSV correspondiente.
3. Cada CSV incluye cabeceras descriptivas y los campos acordados para análisis.
4. Retorna el archivo ZIP como array de bytes.

### generateCsv

1. Recibe un entityType que indica qué entidad exportar.
2. Genera un CSV con cabeceras y filas específicas para esa entidad.
3. Retorna el archivo CSV como array de bytes.

---

## HtmlPdfFileExportAdapter.java

Implementación del puerto FileExportPort para generar archivos PDF. Utiliza Thymeleaf como motor de plantillas y Flying Saucer para renderizar HTML+CSS a PDF.

### generatePdf

1. Recibe un PdfExportData con métricas y listados.
2. Procesa la plantilla Thymeleaf export-pdf.html con los datos.
3. Convierte el HTML resultante a PDF mediante ITextRenderer de Flying Saucer.
4. Retorna el archivo PDF como array de bytes.

### Plantilla export-pdf.html

- Tema oscuro con paleta de colores corporativa (#0A1020, #1E293B, #3B82F6).
- Cabecera con título "Millete - Financial Data" y periodo.
- Primera página: 8 tarjetas de métricas en dos filas de 4, tabla de inversiones activas, tabla de savings goals.
- Páginas siguientes: tabla de transacciones del periodo con columnas Date, Category, Description, Type, Amount.
- Filas alternas con color #293548 para mejorar legibilidad.
- Ingresos en verde (#22C55E), gastos en rojo (#EF4444).
- Fuente Helvetica/Arial estándar.
- Márgenes de 1.5cm, tamaño A4.

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

Ninguna. La versión 0.1.0 es la primera documentada, por lo que el registro de migraciones está vacío. Las migraciones se añadirán cuando el esquema de exportación evolucione.

---

## Campos exportados en CSV/ZIP

### categories.csv

| Campo | Descripción |
|-------|-------------|
| name | Nombre de la categoría |
| budget_limit | Límite de presupuesto |

### transactions.csv

| Campo | Descripción |
|-------|-------------|
| category_name | Nombre de la categoría |
| amount | Monto de la transacción |
| date | Fecha y hora |
| type | INCOME o EXPENSE |
| description | Descripción |

### planned_transactions.csv

| Campo | Descripción |
|-------|-------------|
| category_name | Nombre de la categoría |
| amount | Monto |
| type | INCOME o EXPENSE |
| description | Descripción |
| frequency_type | DAYS, WEEKS, MONTHS, YEARS |
| frequency_interval | Número de unidades de frecuencia |
| start_date | Fecha de inicio |
| end_date | Fecha de fin (vacía si no tiene) |
| last_executed_date | Última fecha de ejecución |

### investments.csv

| Campo | Descripción |
|-------|-------------|
| asset_name | Nombre del activo |
| ticker | Símbolo o ticker |
| quantity | Cantidad poseída |
| purchase_price | Precio de compra unitario |
| current_price | Precio actual |
| type | STOCK, CRYPTO, FUND, REAL_ESTATE, OTHER |
| purchase_date | Fecha de compra |

### savings_goals.csv

| Campo | Descripción |
|-------|-------------|
| name | Nombre del objetivo |
| target_amount | Monto objetivo |
| current_amount | Monto ahorrado |
| progress | Porcentaje de progreso |
| deadline | Fecha límite |
| priority | LOW, MEDIUM, HIGH |
| status | ACTIVE, PAUSED, COMPLETED, CANCELLED |
| link | Enlace asociado |

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/data/export | Exportar backup completo (JSON) |
| GET | /api/v1/data/export/zip | Exportar todos los datos en ZIP con CSVs |
| GET | /api/v1/data/export/csv/{entityType} | Exportar una entidad en CSV |
| GET | /api/v1/data/export/pdf?period=1m | Exportar informe financiero en PDF |
| POST | /api/v1/data/import | Importar datos desde archivo JSON |

Los valores válidos para {entityType} son: categories, transactions, planned_transactions, investments, savings_goals.

Los valores válidos para period son: 1m (1 mes), 3m (3 meses), 6m (6 meses), 1y (1 año). Por defecto 1m.

---

## Dependencias externas

| Librería | Versión | Uso |
|----------|---------|-----|
| Apache Commons CSV | 1.12.0 | Generación de archivos CSV |
| Apache PDFBox | 3.0.4 | Dependencia transitiva de Flying Saucer |
| Flying Saucer | 9.11.3 | Renderizado HTML+CSS a PDF |
| Thymeleaf | (Spring Boot) | Motor de plantillas para el PDF |

---

## Seguridad

- Los archivos de exportación no contienen información del propietario, son portables entre cuentas.
- Cualquier usuario autenticado puede importar cualquier archivo JSON compatible.
- El userId se asigna automáticamente con el del usuario autenticado durante la importación.
- La importación es transaccional: o se importa todo o nada.
- Los archivos de versiones incompatibles se rechazan automáticamente.
- Todos los endpoints de exportación requieren autenticación JWT válida.