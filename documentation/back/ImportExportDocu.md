# Importación y exportación de datos

Las rutas están en [`DataExportController.java`](../../backend/src/main/java/com/puntomartinez/millete/dataexport/infrastructure/in/controller/DataExportController.java) y [`DataImportController.java`](../../backend/src/main/java/com/puntomartinez/millete/dataexport/infrastructure/in/controller/DataImportController.java). El usuario se deriva de la sesión autenticada.

## Exportación

Base `/api/v1/data`:

| Método y ruta | Formato |
|---|---|
| `GET /export` | Snapshot JSON descargable `millete_export.json`; cabeceras `X-Export-Version` y `X-Export-Date` |
| `GET /export/zip` | ZIP con exportación tabular |
| `GET /export/csv/{entityType}` | CSV del tipo admitido por el servicio |
| `GET /export/pdf?period=1m` | Informe PDF; los códigos válidos se definen en `PeriodType` |

La exportación JSON es la representación de `UserDataSnapshot`; no es el inventario histórico antiguo de entidades CRUD.

## Formato versionado e importación

La versión vigente del formato es **0.2.0**, definida en [`ExportVersion.java`](../../backend/src/main/java/com/puntomartinez/millete/dataexport/domain/model/ExportVersion.java). `MigrationChain` contiene las migraciones registradas y valida su continuidad respecto a esa versión. El snapshot representa categorías, transacciones, plantillas recurrentes, inversiones mediante `InvestmentLedgerSnapshot`, metas personales y preferencias; no incluye metas grupales. Véanse [`UserDataSnapshot.java`](../../backend/src/main/java/com/puntomartinez/millete/dataexport/domain/model/UserDataSnapshot.java) y los adaptadores de `dataexport/infrastructure/out`.

`POST /import` consume multipart con campo `file`. Los archivos vacíos y extensiones no JSON se rechazan con `400`; la capa web y el servicio limitan el tamaño a 50 MB. JSON mal formado, estructura inválida y versión incompatible se informan como error de importación. Los archivos de otra versión mayor se rechazan; versiones anteriores compatibles pasan por la cadena registrada.

La importación asigna al usuario autenticado los datos personales, mapea IDs de categorías del archivo a IDs de destino y verifica las transacciones al final. Categorías activas con nombre coincidente (lowercase) se actualizan; las nuevas se crean con IDs nuevos. Se omiten transacciones inactivas y tipos `TRANSFER_IN`/`TRANSFER_OUT`, derivados de actividades de inversión. Los registros de inversiones del formato legacy se omiten y cuentan en `omittedLegacyInvestmentCount`; no se crean holdings a partir de ellos.

`importUserData` está anotado `@Transactional`; procesa categorías, transacciones, plantillas, preferencias, inversiones y metas de ahorro, y verifica transacciones al final. Runtime exceptions se relanzan o envuelven como `InvalidInputException`; el controlador responde `400` con `ERROR_IMPORTACION`. La respuesta exitosa contiene `success`, `message`, `importedCount`, `omittedLegacyInvestmentCount` y `warnings`. Esta descripción no supone que los adapters reemplacen el conjunto importado ni promete equivalencia para formatos antiguos.

El snapshot contiene `metadata` obligatorio (`version`, `exportDate`, `appVersion`), `categories`, `transactions`, `plannedTransactions`, `investments` (ledger), `savingsGoals` y `userPreferences`; se ignoran propiedades JSON desconocidas. No incluye credenciales, sesiones ni metas grupales. El propietario siempre es el usuario autenticado. IDs personales se remapean según el adapter; categorías se mapean de IDs de origen a destino.

La restauración usa `InvestmentLedgerSnapshot`: tracking, activos propios, precios manuales, holdings, actividades y auditorías. Activos compartidos se resuelven por `stableCatalogId`; no se incluyen como catálogo y deben existir en destino. El restore crea recursos con IDs nuevos, valida replay combinado y reconstruye proyecciones; tracking existente debe coincidir con el snapshot. No borra previamente todos los datos de inversiones. Si hay registros legacy, el adapter omite la importación de inversiones de ese snapshot.

La cadena registrada migra v0.0.1→v0.1.0 añadiendo preferencias y v0.1.0→v0.2.0 normalizando importes planificados negativos a su valor absoluto y actualizando metadata; los demás campos del snapshot se preservan en esas migraciones. La cadena valida continuidad al construirse; la compatibilidad de versión se comprueba antes de importar.

CSV admite los tipos enumerados por el servicio de exportación y ZIP empaqueta salidas tabulares; no son respaldos JSON restaurables. PDF acepta los períodos definidos por `PeriodType`. La importación se ejecuta en transacción para evitar aplicar parcialmente el conjunto de datos.

Referencias: [`DataImportService.java`](../../backend/src/main/java/com/puntomartinez/millete/dataexport/application/services/DataImportService.java), [`MigrationChain.java`](../../backend/src/main/java/com/puntomartinez/millete/dataexport/domain/migration/MigrationChain.java), [`InvestmentLedgerSnapshotService.java`](../../backend/src/main/java/com/puntomartinez/millete/investments/application/services/InvestmentLedgerSnapshotService.java).
