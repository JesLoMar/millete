# Mejoras pendientes para futuras versiones

## 1. Implementar lógica real de procesamiento de transacciones recurrentes

**Archivo:** PlannedTransactionService.shouldExecuteToday()

**Problema:** Actualmente solo compara startDate con la fecha actual. Si una transaccion recurrente tiene frequencyType = WEEKS y frequencyInterval = 2, deberia ejecutarse cada 2 semanas, pero solo se ejecuta el dia exacto de startDate.

**Impacto:** Las transacciones recurrentes con intervalo distinto de 1 no funcionan. La funcionalidad esta rota para la mayoria de casos de uso.

**Solucion propuesta:**
- Añadir un campo lastExecutedDate a PlannedTransaction y su entidad
- En processScheduledTasks(), calcular si hoy corresponde ejecucion segun frequencyType y frequencyInterval
- Respetar endDate si esta definida (no ejecutar despues)
- Añadir migracion Flyway para la nueva columna last_executed_date

**Complejidad:** Media. Requiere cambios en modelo, entidad, servicio, migracion y tests.

---

## 2. Validacion de consistencia entre modo de distribucion y datos de miembros

**Archivos:** FamilyService.updateMember(), FamilyUnit.calculateContributions()

**Problema:** No hay validacion que impida asignar customPercentage cuando el modo es EQUITATIVE o PROPORTIONAL, ni que impida tener salary = 0 en modo PROPORTIONAL. Los calculos pueden ser incorrectos.

**Impacto:** El administrador puede configurar datos inconsistentes sin saberlo, resultando en contribuciones mal calculadas.

**Solucion propuesta:**
- En updateMember(): si el modo es EQUITATIVE, ignorar customPercentage y salary. Si es PROPORTIONAL, ignorar customPercentage. Si es CUSTOM, ignorar salary
- En calculateContributions(): si modo PROPORTIONAL y totalSalary = 0, lanzar excepcion con mensaje claro
- Al cambiar el modo de distribucion a CUSTOM, requerir que todos los miembros tengan customPercentage asignado

**Complejidad:** Baja. Principalmente validaciones en servicio.

---

## 3. Doble logica de borrado: soft delete manual + ON DELETE SET NULL

**Archivos:** CategoryService.delete(), V1__initial_schema.sql

**Problema:** El servicio hace soft delete (active = false) y desvincula transacciones manualmente (categoryId = null). La migracion tiene ON DELETE SET NULL en la FK, pero como nunca se hace DELETE fisico, esa restriccion nunca se ejecuta. No rompe nada, pero es codigo muerto.

**Impacto:** Ninguno actualmente. La FK sirve como red de seguridad si en el futuro se hace borrado fisico.

**Solucion propuesta:**
- Mantener la restriccion ON DELETE SET NULL como red de seguridad
- Documentar claramente en el codigo que la desvinculacion manual es necesaria porque el borrado es logico

**Complejidad:** Ninguna. Solo documentar.

---

## 4. Endpoint savings-goals sin implementacion en frontend

**Archivos:** DashboardService.getSavingsGoals(), DashboardController.getSavingsGoals()

**Problema:** El backend genera metas de ahorro (Fondo de Emergencia, Vacaciones) calculadas automaticamente, pero no hay ningun componente en el frontend que consuma este endpoint.

**Impacto:** Codigo muerto en backend. Ocupa endpoints, servicio y DTOs sin uso real.

**Solucion propuesta:**
- Si se planea usar en el futuro: dejarlo documentado como "proximamente"
- Si no se planea usar: eliminar el endpoint, el metodo del servicio y los DTOs relacionados

**Complejidad:** Baja. Decision de producto.

---

## 5. Observaciones menores

### 5.1 CORS hardcodeado a localhost

**Archivo:** SecurityConfig.java

**Problema:** CORS solo permite http://localhost:5173. En produccion, la URL del frontend sera diferente.

**Solucion:** Mover la URL permitida a la propiedad app.frontend.url que ya existe en application.yml.

**Complejidad:** Baja.

---

### 5.2 JWT sin refresh token

**Archivos:** JwtTokenAdapter.java, AuthContext.tsx

**Problema:** El token expira a las 12 horas y el usuario debe volver a iniciar sesion. No hay mecanismo de refresh token para sesiones prolongadas.

**Solucion:**
- Backend: endpoint POST /auth/refresh que acepte un refresh token y devuelva un nuevo JWT
- Frontend: interceptor en apiClient que detecte 401, llame al refresh y reintente la peticion

**Complejidad:** Media. Requiere cambios en backend y frontend.

---

### 5.3 Inconsistencia MapStruct vs mapeo manual

**Archivos:** InvestmentEntityMapper.java (MapStruct), CategoryMapper.java (manual)

**Problema:** Algunos modulos usan MapStruct con anotaciones, otros usan mapeo manual con setters/getters. No afecta al funcionamiento, pero es inconsistente.

**Solucion:** Unificar a MapStruct en todos los modulos. Es mas limpio, menos codigo y menos propenso a errores.

**Complejidad:** Baja. Refactorizar los mappers manuales a interfaces MapStruct.

---