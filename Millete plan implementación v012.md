# Plan de Correcciones y Mejoras — Millete v0.2.0

> **Objetivo:** Guiar paso a paso la corrección de incidencias de seguridad, UX, i18n y lógica de negocio detectadas en la auditoría.  
> **Regla de oro:** No se modifica código en este archivo; aquí solo vive el plan, los archivos afectados y las instrucciones precisas de cambio.  
> **Idioma de trabajo:** Español (código y comentarios del proyecto son mayoritariamente inglés/español).

---

## Índice de tareas

1. [T1 — Proteger último administrador en metas grupales](#t1) OK
2. [T2 — Arreglar mensaje verde de porcentajes en metas grupales](#t2) OK
3. [T3 — Corregir clave de traducción `savingsGoals.addFunds`](#t3) OK
4. [T4 — Notificaciones: navegar a la sección concreta](#t4) OK
5. [T5 — Notificaciones: ocultar badge al abrir el diálogo](#t5) OK
6. [T6 — Paleta de colores oficial en modo oscuro](#t6)
7. [T7 — Menú de 3 puntos en inversiones (móvil)](#t7)
8. [T8 — Flujo de confirmación de contraseña en información personal](#t8)
9. [T9 — Añadir/activar guardián de presupuesto en el frontend](#t9)
10. [T10 — Mitigar vulnerabilidad de signo en transacciones](#t10) OK
11. [T11 — Mitigar inyección de fórmulas en exportación CSV](#t11) OK
12. [T12 — Migrar JWT de `sessionStorage` a cookies seguras](#t12) OK

---

## Fases recomendadas de ejecución

| Fase | Tareas | Justificación |
|------|--------|---------------|
| **Fase 1 — Seguridad crítica** | T10, T11, T12 | Son las únicas con impacto de seguridad real; deben ir primero. | Hecho
| **Fase 2 — Integridad de negocio** | T1, T2, T8, T9 | Correcciones de reglas de negocio y UX sensible. |
| **Fase 3 — UX / i18n / estilo** | T3, T4, T5, T6, T7 | Mejoras visuales y de navegación; menor riesgo. |

> **Nota sobre decisiones pendientes:** En varias tareas se señalan decisiones de diseño que afectan al resultado final (paleta oscura, comportamiento del guardián, etc.). Antes de ejecutar cada tarea, revisa la sección **“Decisiones que necesito que tomes”** al final de este plan. Si no hay instrucción explícita tuya, se aplicará la opción **recomendada** marcada con ⭐.

---

<a name="t1"></a>
## T1 — Proteger último administrador en metas grupales

### Problema
En `GroupGoalService.updateMember()` cualquier administrador puede cambiar su propio rol a `MEMBER` (o el de otro admin) incluso si es el **último administrador activo** de la meta grupal. Esto deja la meta sin administradores y bloquea la gestión futura.

### Archivos afectados
- **Backend:**
  - `backend/src/main/java/com/puntomartinez/millete/groupgoals/application/services/GroupGoalService.java` (método `updateMember`)
  - `backend/src/test/java/com/puntomartinez/millete/groupgoals/application/services/GroupGoalServiceTest.java`
- **Frontend:**
  - `frontend/src/features/groupgoals/components/dialogs/EditMemberDialog.tsx`
  - `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/groupGoals.json`

### Solución teórica
Antes de permitir un cambio de rol `ADMIN → MEMBER`, contar cuántos administradores activos quedarían. Si el miembro a editar es el último admin (`countAdmins == 1` y su rol actual es `ADMIN` y el nuevo rol es `MEMBER`), rechazar la operación.

### Instrucciones de cambio concretas

#### Backend
1. En `GroupGoalService.updateMember()`:
   - Después de cargar `member`, verificar si `request.getRole() != null` y el nuevo rol es `MEMBER`.
   - Si `member.getRole() == GoalRole.ADMIN`:
     - Contar admins activos en el goal: `long activeAdmins = goalMemberRepository.findByGoalId(goalId).stream().filter(m -> m.isActive() && m.getRole() == GoalRole.ADMIN).count()`.
     - Si `activeAdmins <= 1`, lanzar `InvalidInputException("No puedes dejar la meta grupal sin administradores.")`.
2. Añadir test unitario:
   - Caso positivo: un admin puede bajar a otro admin si hay más de uno.
   - Caso negativo: el último admin no puede bajarse a sí mismo.

#### Frontend
1. En `EditMemberDialog.tsx`:
   - Si `member.role === 'ADMIN'` y no se conoce que haya más admins, deshabilitar la opción `MEMBER` en el `<Select>` o mostrar un hint.
2. Añadir clave de traducción `groupGoals:cannotRemoveLastAdmin` en los 7 idiomas.

---

<a name="t2"></a>
## T2 — Arreglar mensaje verde/amarillo de porcentajes en metas grupales

### Problema
La clave `groupGoals:customPercentageHint` contiene la interpolación `{{total}}%`, pero `DistributionCard` la invoca sin pasar el parámetro `total`. Esto hace que el mensaje se vea estático (muestra el literal `{{total}}` o, según la config de i18next, vacío) y no reaccione a los cambios reales de porcentaje.

### Archivos afectados
- `frontend/src/features/groupgoals/components/DistributionCard.tsx`
- `frontend/src/features/groupgoals/components/GroupGoalDetail.tsx`
- `frontend/src/features/groupgoals/components/dialogs/EditMemberDialog.tsx`
- `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/groupGoals.json`

### Solución teórica
Pasar `totalCustomPercentage` como prop a `DistributionCard` e interpolarlo en la traducción. Revisar también el hint del diálogo de edición.

### Instrucciones de cambio concretas
1. En `DistributionCardProps`, añadir `totalCustomPercentage: number`.
2. En `GroupGoalDetail.tsx`, pasar `totalCustomPercentage={totalCustomPercentage}` al `<DistributionCard />`.
3. En `DistributionCard.tsx`:
   - Reemplazar `t('groupGoals:customPercentageHint')` por `t('groupGoals:customPercentageHint', { total: totalCustomPercentage.toFixed(2) })`.
4. En `EditMemberDialog.tsx`, revisar si el hint debe mostrar el total; de momento dejarlo como hint genérico o pasarle el total actual si está disponible.
5. Actualizar todas las traducciones:
   - `customPercentageHint`: mantener `{{total}}` y asegurar que todas las lenguas lo incluyen.
   - Opcionalmente añadir `customPercentageRemaining`: "Faltan {{remaining}}% para llegar a 100%." (aunque no es estrictamente necesario).

---

<a name="t3"></a>
## T3 — Corregir clave de traducción `savingsGoals.addFunds`

### Problema
En `ContributionModal.tsx` se usa `t("savingsGoals.addFunds")`. Con `defaultNS: "common"`, i18next interpreta la notación punto como clave anidada **dentro del namespace por defecto**, no como namespace `savingsGoals` + clave `addFunds`. El namespace correcto es `savingsGoals` y la sintaxis correcta es `savingsGoals:addFunds`.

> Nota: el usuario escribió `savingGoals.addFunds` (singular). El namespace real del proyecto es `savingsGoals` (plural), por lo que la corrección es de sintaxis (`:` vs `.`), no de nombre.

### Archivos afectados
- `frontend/src/features/savingsgoals/components/ContributionModal.tsx`
- `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/savingsGoals.json` (verificar que `addFunds` exista e incluya `{{name}}`)

### Solución teórica
Cambiar la llamada a `t` para usar el separador de namespace `:`.

### Instrucciones de cambio concretas
1. En `ContributionModal.tsx` línea 32:
   ```tsx
   // Antes
   {t("savingsGoals.addFunds", { name: goal?.name })}
   // Después
   {t("savingsGoals:addFunds", { name: goal?.name })}
   ```
2. Verificar que en los 7 archivos `savingsGoals.json` exista:
   ```json
   "addFunds": "$t(common:actions.add) fondos a {{name}}"
   ```
   (o su traducción equivalente).
3. Revisar con `pnpm run type-check` que no haya otros usos incorrectos de notación punto con namespaces no comunes.

---

<a name="t4"></a>
## T4 — Notificaciones: navegar a la sección concreta

### Problema
Actualmente `NotificationBellItem` redirige siempre a `/profile` para invitaciones a metas grupales y a `/notifications` para el resto. No aprovecha el `metadata` de la notificación para llevar al usuario directamente a la meta grupal concreta (`/group-goals?goalId=xxx`).

### Archivos afectados
- `frontend/src/features/notifications/components/NotificationBellItem.tsx`
- `frontend/src/features/notifications/components/NotificationItem.tsx`
- `frontend/src/features/notifications/types/index.ts`
- `frontend/src/features/groupgoals/pages/page.tsx`

### Solución teórica
Usar el campo `metadata` (que ya contiene `goalId` e `invitationId`) para construir una URL profunda. Añadir soporte en la página de metas grupales para leer `goalId` de query params y seleccionar la meta automáticamente.

### Instrucciones de cambio concretas
1. En `NotificationBellItem.tsx`:
   - Para `GOAL_INVITATION`, si `notification.metadata.goalId` existe, navegar a `/group-goals?goalId=${notification.metadata.goalId}`.
   - Si no existe metadata, mantener fallback a `/profile`.
2. En `NotificationItem.tsx` (página completa de notificaciones):
   - Aplicar la misma lógica al hacer click en el item o en el botón "View".
3. En `GroupGoalsPage` (`frontend/src/features/groupgoals/pages/page.tsx`):
   - Leer `searchParams` con `useSearchParams`.
   - En un `useEffect` al montar, si existe `goalId`, llamar `setSelectedGoalId(goalId)`.
4. (Opcional) Marcar la notificación como leída al navegar.

---

<a name="t5"></a>
## T5 — Notificaciones: ocultar badge al abrir el diálogo

### Problema
El badge rojo de notificaciones no leídas en `NotificationBell` sigue visible mientras el usuario tiene abierto el diálogo de notificaciones. Se pide que desaparezca automáticamente al abrir el desplegable, pero manteniendo el borrado manual de la lista.

### Archivos afectados
- `frontend/src/features/notifications/components/NotificationBell.tsx`
- `frontend/src/features/notifications/hooks/useNotifications.ts`

### Solución teórica
Al abrir el diálogo (`open === true`), invalidar la query de unread-count o forzar su recálculo a 0 de forma local. No marcar todas como leídas automáticamente (eso rompería el requisito de borrado manual).

### Instrucciones de cambio concretas
1. En `NotificationBell.tsx`:
   - Detectar cuando `open` pasa a `true`.
   - Opción A (recomendada): usar `queryClient.setQueryData(['notifications', 'unread-count'], 0)` para ocultar el badge localmente.
   - Opción B: invalidar la query para que el backend devuelva 0 (solo si el backend expone un endpoint para marcar todas como leídas, que no existe actualmente).
2. Asegurar que al cerrar y volver a abrir no haya flicker: la query se invalida/oculta solo al abrir.
3. Mantener `NotificationBellList` como está: el usuario sigue viendo la lista y puede descartar items manualmente.

---

<a name="t6"></a>
## T6 — Paleta de colores oficial en modo oscuro (petición de Lili)

### Problema
El proyecto solo tiene tema claro (`MILLETE_THEME`). No existe definición `.dark` en CSS ni una paleta oscura oficial. El hook `useTheme` tiene `setTheme` como no-op.

### Archivos afectados
- `frontend/src/shared/themes/palettes.ts`
- `frontend/src/shared/hooks/useTheme.ts`
- `frontend/src/index.css`
- `frontend/src/features/profile/types/index.ts`
- `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/userProfile.json`
- (Opcional) `frontend/src/features/profile/pages/page.tsx` — añadir selector de tema

### Solución teórica
Crear un segundo tema oscuro (`MILLETE_DARK_THEME`) con los mismos tokens semánticos pero valores adaptados a fondos oscuros. Activarlo vía clase `.dark` en `<html>` y leer/guardar preferencia en el backend mediante `/profile/preferences`.

### Decisiones que necesito que tomes
| Opción | Descripción |
|--------|-------------|
| ⭐ **A** | Usar variantes oscuras de la paleta actual (crumb/beige invertidos a carbón/verde bill-ink). |
| B | Usar una paleta totalmente diferente (azul/gris estilo slate). |
| C | Implementar solo `light`/`dark` automático por `prefers-color-scheme`, sin selector manual. |

### Instrucciones de cambio concretas (Opción A recomendada)
1. En `palettes.ts`:
   - Crear `MILLETE_DARK_THEME: Theme` con colores oscuros coherentes (ej. background #1A1612, card #242019, foreground #F5E6D3, primary #6EE7B7 o similar).
2. En `index.css`:
   - Añadir bloque `.dark { ... }` con las mismas variables CSS pero valores oscuros.
3. En `useTheme.ts`:
   - Permitir cambiar entre `MILLETE_THEME` y `MILLETE_DARK_THEME`.
   - Al cambiar, alternar clase `dark` en `document.documentElement`.
4. En `profile/types/index.ts` ya existe `theme: 'light' | 'dark' | 'system'`. Usar ese campo para persistir.
5. En `profile/pages/page.tsx`:
   - Añadir sección de preferencias de tema (selector light/dark/system) si no existe.
   - Conectar con `profileService.updatePreferences`.
6. En `App.tsx` o `main.tsx`:
   - Al iniciar, leer preferencia del backend o del sistema y aplicar tema antes del primer render para evitar flash.

---

<a name="t7"></a>
## T7 — Menú de 3 puntos en inversiones (móvil)

### Problema
En `AssetRow.tsx` el menú de 3 puntos (`DropdownMenu`) en móvil queda oculto o no es intuitivo. Actualmente el layout móvil apila el botón de "Actualizar precio" a ancho completo debajo de la fila, y el menú de 3 puntos está arriba a la derecha junto al nombre, lo que lo hace fácil de pasar por alto.

### Archivos afectados
- `frontend/src/features/investments/components/AssetRow.tsx`
- `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/investments.json`

### Solución teórica
Hacer el menú de 3 puntos más visible en móvil: aumentar tamaño del área táctil, añadir borde/background sutil, o mostrar las acciones directamente como botones en lugar de esconderlas tras el menú. También evitar que el dropdown quede cortado por el scroll.

### Instrucciones de cambio concretas
1. En `AssetRow.tsx` (bloque `sm:hidden`):
   - Cambiar el botón de 3 puntos a un estilo más prominente: `className="size-9 shrink-0 border border-border/60 bg-card/80"`.
   - Añadir `sideOffset={4}` y `align="end"` al `DropdownMenuContent`.
   - Considerar añadir el botón de "Actualizar precio" **dentro del menú de 3 puntos** en móvil para liberar espacio y unificar acciones.
2. Si se opta por mostrar acciones directas:
   - En móvil, reemplazar el menú por una fila de iconos: Editar (si aplica), Actualizar precio, Eliminar.
3. Añadir/verificar claves de traducción:
   - `investments:updatePrice`, `investments:delete`, `investments:assetOptions`.
4. Probar en viewport móvil (< 640 px) que el dropdown no queda cortado por `overflow-x-auto` de `AssetList`.

---

<a name="t8"></a>
## T8 — Flujo de confirmación de contraseña en información personal

### Problema
Actualmente, al editar la información personal (`PersonalInfoSection`) se exige la contraseña actual. Si el usuario la introduce mal, el backend devuelve `401 Unauthorized`. El interceptor global de Axios detecta el 401, limpia `sessionStorage` y redirige a `/login` de forma agresiva. Además, no queda claro por qué se pide contraseña en la sección superior (información personal) en lugar de en el cambio de contraseña.

### Archivos afectados
- **Backend:**
  - `backend/src/main/java/com/puntomartinez/millete/users/application/services/ProfileService.java`
  - `backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/in/controller/advice/GlobalExceptionHandler.java`
- **Frontend:**
  - `frontend/src/features/profile/components/PersonalInfoSection.tsx`
  - `frontend/src/shared/api/axiosClient.ts`
  - `frontend/src/features/profile/hooks/useUpdateProfile.ts`
  - `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/userProfile.json`

### Solución teórica
Cambiar el código HTTP de "contraseña incorrecta" en actualización de perfil de `401` a `400 Bad Request` o `403 Forbidden` para que no dispare el logout global. Añadir texto explicativo en el formulario indicando que la contraseña se pide como verificación de seguridad. Opcionalmente, quitar el requisito de contraseña para cambios no sensibles (solo email/username).

### Decisiones que necesito que tomes
| Opción | Descripción |
|--------|-------------|
| ⭐ **A** | Mantener la contraseña como verificación, pero devolver `400` en vez de `401` y mostrar mensaje amigable sin logout. |
| B | Eliminar el requisito de contraseña para cambiar username/email; solo pedirla para cambio de contraseña y borrado de cuenta. |

### Instrucciones de cambio concretas (Opción A recomendada)
1. Backend:
   - En `ProfileService.updateProfile()`, cambiar el lanzamiento de `AuthenticationFailedException` por `InvalidInputException("Contraseña incorrecta")`.
   - Verificar que `GlobalExceptionHandler` mapee `InvalidInputException` a `400 Bad Request`.
2. Frontend:
   - En `PersonalInfoSection.tsx`:
     - Añadir un texto de ayuda debajo del campo de contraseña: `t('personalInfo.currentPasswordHelp')`.
     - Añadir la traducción en los 7 idiomas: "Introduce tu contraseña actual para confirmar los cambios de seguridad."
   - En `useUpdateProfile.ts`, mantener el `onError` para mostrar el mensaje del backend.
   - Revisar `axiosClient.ts`: asegurar que solo los 401 reales (token inválido, sesión expirada) disparen logout, no los 400 de validación de contraseña (ya debería ser así, pero verificar).

---

<a name="t9"></a>
## T9 — Añadir/activar guardián de presupuesto en el frontend

### Problema
El backend ya calcula `limitExceeded` en `TransactionService.register()` cuando los gastos superan el 70 % de los ingresos del mes. Sin embargo, el frontend actualmente **ignora** el flag `alertLimitExceeded` de la respuesta. El README incluso lo marca como `[Not Implemented]`.

### Archivos afectados
- **Backend:**
  - `backend/src/main/java/com/puntomartinez/millete/transactions/application/services/TransactionService.java` (ya implementado, revisar)
  - `backend/src/main/java/com/puntomartinez/millete/transactions/infrastructure/in/controller/dto/TransactionResponseDTO.java` (ya incluye `alertLimitExceeded`)
- **Frontend:**
  - `frontend/src/features/transactions/hooks/useTransactionMutation.ts`
  - `frontend/src/features/transactions/components/dialogs/NewTransactionDialog.tsx`
  - `frontend/src/features/transactions/components/dialogs/EditTransactionDialog.tsx`
  - `frontend/src/features/transactions/index.ts` (`TransactionResponse`)
  - `frontend/src/assets/locales/{es,en,de,fr,it,pt,ja}/transactions.json`

### Solución teórica
Capturar `alertLimitExceeded` en la respuesta del backend y mostrar un aviso al usuario tras crear/editar una transacción.

### Decisiones que necesito que tomes
| Opción | Descripción |
|--------|-------------|
| ⭐ **A** | Mostrar un `notify.warning()` de 6 segundos cuando `alertLimitExceeded === true` tras crear/editar transacción. |
| B | Añadir un banner persistente en el Dashboard que muestre el estado del guardián. |
| C | Ambos: toast inmediato + indicador sutil en Dashboard. |

### Instrucciones de cambio concretas (Opción A recomendada)
1. En `frontend/src/features/transactions/index.ts`:
   - Añadir `alertLimitExceeded?: boolean` a `TransactionResponse`.
2. En `useTransactionMutation.ts`:
   - En `onSuccess` de `createTransaction` y `updateTransaction`, verificar `data.data.alertLimitExceeded`.
   - Si es `true`, llamar `notify.warning(t('transactions:alerts.budgetGuardian'), { duration: 6000 })`.
3. En `NewTransactionDialog.tsx` y `EditTransactionDialog.tsx`:
   - Asegurar que el `onSuccess` recibe la respuesta (actualmente usan `mutateAsync` sin leer la respuesta; cambiar para leer el resultado).
4. Añadir clave de traducción `budgetGuardian` en todos los idiomas:
   - Ej. ES: "Has superado el 70 % de tus ingresos en gastos este mes."
5. Backend — pequeña mejora:
   - Revisar `TransactionService.register()`: el cálculo de `totalExpense` suma `command.amount()` sin `.abs()`. Si el frontend envía gastos como positivos, esto está bien; si envía negativos, el cálculo falla. Dado que T10 fuerza valores positivos, asegurar que la lógica sigue siendo coherente.

---

<a name="t10"></a>
## T10 — Mitigar vulnerabilidad de signo en transacciones

### Problema
El backend acepta y persiste valores negativos en `amount` para transacciones de tipo `INCOME` (y positivos para `EXPENSE`). Aunque el frontend usa `Math.abs()` y la lectura en dashboard/métricas también aplica `.abs()`, la base de datos almacena datos "sucios". Además, el guardián de presupuesto se rompe si llega un gasto negativo.

### Archivos afectados
- **Backend:**
  - `backend/src/main/java/com/puntomartinez/millete/transactions/infrastructure/in/controller/dto/RegisterTransactionRequestDTO.java`
  - `backend/src/main/java/com/puntomartinez/millete/transactions/infrastructure/in/controller/dto/UpdateTransactionRequestDTO.java`
  - `backend/src/main/java/com/puntomartinez/millete/transactions/domain/model/Transaction.java`
  - `backend/src/test/java/com/puntomartinez/millete/transactions/application/services/TransactionServiceTest.java`
  - `backend/src/test/java/com/puntomartinez/millete/transactions/domain/model/TransactionTest.java` (crear si no existe)
- **Frontend (deuda técnica posterior):**
  - `frontend/src/features/transactions/components/dialogs/NewTransactionDialog.tsx`
  - `frontend/src/features/transactions/components/dialogs/EditTransactionDialog.tsx`

### Solución teórica
Defensa en profundidad:
1. Validación en DTO con `@Positive`.
2. Guardia en el modelo de dominio rechazando `amount <= 0`.
3. En el frontend, dejar de enviar cantidades negativas (quitar los `-Math.abs()` para EXPENSE) porque el backend asume valores absolutos.

### Instrucciones de cambio concretas
1. Backend:
   - En `RegisterTransactionRequestDTO` y `UpdateTransactionRequestDTO`, añadir:
     ```java
     @NotNull(message = "La cantidad es obligatoria")
     @Positive(message = "La cantidad debe ser mayor que cero")
     BigDecimal amount;
     ```
     Requiere import `jakarta.validation.constraints.Positive`.
   - En `Transaction.java` constructor y `updateDetails()`:
     ```java
     if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
         throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
     }
     ```
     (Antes solo rechazaba `== 0`; ahora también `< 0`.)
   - Actualizar tests existentes que creen transacciones con cantidad negativa (no debería haber, pero verificar).
   - Añadir test: `shouldRejectNegativeAmount` en dominio y servicio.
2. Frontend:
   - En `NewTransactionDialog.tsx` y `EditTransactionDialog.tsx`, cambiar:
     ```ts
     // Antes
     amount: form.type === "EXPENSE" ? -Math.abs(Number(form.amount)) : Math.abs(Number(form.amount))
     // Después
     amount: Math.abs(Number(form.amount))
     ```
     El signo ya no es necesario porque el backend lo maneja mediante el campo `type` y su propia lógica.
3. Verificar que `TransactionMetricsService`, `DashboardService` y `DataExportService` funcionan correctamente con cantidades siempre positivas. **No eliminar todavía los `.abs()` de lectura** (son la red de seguridad); marcar como deuda técnica para una fase posterior.

---

<a name="t11"></a>
## T11 — Mitigar inyección de fórmulas en exportación CSV

### Problema
La exportación CSV escribe campos de texto (descripciones, nombres, etc.) sin sanitizar. Si un atacante introduce texto que empiece por `=`, `+`, `-` o `@`, Excel/Google Sheets pueden interpretarlo como fórmula y ejecutar código (CSV Injection / Formula Injection).

### Archivos afectados
- `backend/src/main/java/com/puntomartinez/millete/dataexport/infrastructure/out/fileexport/ZipFileExportAdapter.java`
- `backend/src/test/java/com/puntomartinez/millete/dataexport/infrastructure/out/fileexport/ZipFileExportAdapterTest.java` (crear si no existe)

### Solución teórica
Sanitizar cada celda de texto antes de escribirla: si el valor comienza por uno de los caracteres de fórmula (`=`, `+`, `-`, `@`), anteponer una comilla simple (`'`) para forzar el tratamiento como texto. Crear un helper reutilizable.

### Instrucciones de cambio concretas
1. En `ZipFileExportAdapter.java`:
   - Crear método privado:
     ```java
     private String sanitizeCsvField(String value) {
         if (value == null || value.isBlank()) {
             return value;
         }
         char first = value.charAt(0);
         if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
             return "'" + value;
         }
         return value;
     }
     ```
   - Aplicar `sanitizeCsvField(...)` a todos los campos de texto en `generateZip` y `generateCsv`:
     - `row.description()`, `row.categoryName()`, `row.name()`, `row.assetName()`, `row.ticker()`, `row.type()`, `row.priority()`, `row.status()`, etc.
     - No aplicar a campos numéricos ni fechas.
2. Añadir tests unitarios:
   - Exportar una transacción con descripción `=CMD|' /C calc'!A0` y verificar que el CSV contiene `'=CMD...`.
   - Exportar descripción normal y verificar que no se añade comilla.

---

<a name="t12"></a>
## T12 — Migrar JWT de `sessionStorage` a cookies seguras

### Problema
El token JWT se almacena en `sessionStorage` (prefijo `ms_token`), lo que lo expone a ataques XSS: cualquier script inyectado puede leerlo. Además, el frontend lo envía manualmente en el header `Authorization`.

### Archivos afectados
- **Backend:**
  - `backend/src/main/java/com/puntomartinez/millete/users/infrastructure/in/controller/AuthController.java`
  - `backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/filter/JwtAuthenticationFilter.java`
  - `backend/src/main/java/com/puntomartinez/millete/shared/infrastructure/config/SecurityConfig.java`
  - `backend/src/main/resources/application.yml`
- **Frontend:**
  - `frontend/src/shared/api/axiosClient.ts`
  - `frontend/src/features/auth/context/AuthContext.tsx`
  - `frontend/src/features/auth/hooks/useAuthMutations.ts`
  - `frontend/src/shared/utils/secureStorage.ts`
  - `frontend/src/features/auth/services/auth.service.ts`

### Solución teórica
El backend inyecta el JWT en una cookie `HttpOnly`, `Secure` y `SameSite=Strict` tras el login. El frontend deja de almacenar/gestionar el token; el navegador lo envía automáticamente gracias a `withCredentials: true`. El filtro JWT lee la cookie en lugar del header `Authorization`.

### Decisiones que necesito que tomes
| Opción | Descripción |
|--------|-------------|
| ⭐ **A** | Migración completa: cookie `HttpOnly` + `Secure` + `SameSite=Strict`; se elimina `Authorization` header del frontend. |
| B | Doble soporte: aceptar token por cookie **y** por header durante un periodo de transición. |

### Instrucciones de cambio concretas (Opción A recomendada)
1. Backend:
   - En `application.yml`, añadir/verificar:
     ```yaml
     jwt:
       cookie-name: ms_token
       cookie-secure: true        # false en local/dev si no hay HTTPS
       cookie-http-only: true
       cookie-same-site: Strict
       cookie-path: /
       cookie-max-age: 43200      # 12 horas en segundos
     ```
   - En `AuthController.login()`:
     - No devolver `TokenResponseDTO` con el token en JSON.
     - Usar `ResponseCookie.from(cookieName, jwt).httpOnly(...).secure(...).sameSite(...).path("/").maxAge(...).build()`.
     - Añadir al response con `response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())`.
     - Devolver `200 OK` con un body mínimo (opcional: `{ "status": "authenticated" }`).
   - En `JwtAuthenticationFilter.extractJwtFromRequest()`:
     - Primero intentar leer la cookie `ms_token`.
     - Fallback opcional: header `Authorization` (solo si se quiere compatibilidad; si no, eliminar).
   - En `AuthController.logout()`:
     - Invalidar la cookie enviando una cookie vacía con `maxAge(0)`.
   - En `SecurityConfig.corsConfigurationSource()`:
     - Añadir `"Cookie"` a `AllowedHeaders` si es necesario.
     - Mantener `allowCredentials(true)`.
2. Frontend:
   - En `axiosClient.ts`:
     - Añadir `withCredentials: true` a la configuración base.
     - Eliminar el interceptor de request que añade `Authorization`.
   - En `AuthContext.tsx`:
     - Eliminar `secureStorage.setToken()`, `getToken()`, `getSessionId()`.
     - Mantener `user` en `sessionStorage` o moverlo también a cookie/memoria (opcional).
     - El `sessionId` ya no es necesario en el frontend; el backend lo extrae del JWT en la cookie.
   - En `useAuthMutations.ts`:
     - No recibir `token` de la respuesta; asumir que la cookie se ha establecido.
     - Llamar a `/auth/me/topnav` para obtener datos de usuario tras login.
   - En `secureStorage.ts`:
     - Eliminar métodos relacionados con token y sessionId, o dejarlos como no-op.
3. Consideraciones de despliegue:
   - En local sin HTTPS, `Secure=true` hará que el navegador no envíe la cookie. Usar `Secure` solo cuando `SPRING_PROFILES_ACTIVE=prod` o similar.
   - Actualizar `.env.example` si se añaden nuevas variables.

---

## Decisiones que necesito que tomes

Antes de ejecutar el plan, confirma o modifica las siguientes decisiones. Si no me das instrucciones, aplicaré las opciones marcadas con ⭐.

| ID | Decisión | Opción recomendada | Alternativas |
|----|----------|-------------------|--------------|
| T6 | Paleta oscura | ⭐ Variantes oscuras de la paleta actual | Paleta slate/azul, o solo automático por sistema |
| T8 | Contraseña en info personal | ⭐ Mantenerla, pero sin logout agresivo (400 en vez de 401) | Eliminar requisito de contraseña |
| T9 | Aviso del guardián | ⭐ Toast warning de 6 segundos | Banner en Dashboard, o ambos |
| T12 | JWT cookies | ⭐ Migración completa a cookie HttpOnly | Doble soporte header + cookie |

---

## Checklist global de verificación

- [ ] Todos los tests de backend pasan (`./mvnw test`).
- [ ] El frontend compila sin errores (`pnpm run type-check`).
- [ ] El linter pasa sin warnings (`pnpm run lint`).
- [ ] Se han actualizado las traducciones en los 7 idiomas para cada clave nueva o modificada.
- [ ] Se han añadido tests unitarios para T1, T10 y T11.
- [ ] Se ha probado el flujo de login/logout tras T12.
- [ ] Se ha probado la exportación CSV con campos maliciosos tras T11.

---

*Plan generado el 2026-07-01. No contiene cambios de código, solo instrucciones de implementación.*
