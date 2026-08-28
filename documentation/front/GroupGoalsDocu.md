# Group Goals — Documentación técnica (Frontend)

## Estructura de archivos

- **components/GroupGoalDetail.tsx** — Vista de detalle de una meta grupal
- **components/GroupGoalSelector.tsx** — Selector de metas con lista y creación
- **components/MemberCard.tsx** — Tarjeta individual de miembro con acciones
- **components/DistributionCard.tsx** — Selector de modo de distribución
- **components/ContributionHistory.tsx** — Historial de aportaciones
- **components/dialogs/AddContributionDialog.tsx** — Diálogo para añadir aportación
- **components/dialogs/CreateGroupGoalDialog.tsx** — Diálogo para crear nueva meta
- **components/dialogs/EditGoalNameDialog.tsx** — Diálogo para editar nombre de la meta
- **components/dialogs/EditMemberDialog.tsx** — Diálogo para editar miembro
- **components/dialogs/InviteMemberDialog.tsx** — Diálogo para invitar por nombre de usuario o email (acepta cualquier identificador no vacío, sin validación de formato)
- **components/dialogs/UpdateGoalDialog.tsx** — Diálogo para cambiar objetivo mensual
- **constants.ts** — Colores de miembros
- **hooks/useGroupGoalQueries.ts** — Hook centralizado de queries
- **hooks/useGroupGoalMutations.ts** — Hook centralizado de mutaciones
- **pages/page.tsx** — Página principal de metas grupales
- **pages/JoinGroupGoalPage.tsx** — Página de aceptación de invitación
- **types/index.ts** — Tipos y contratos de datos
- **utils.ts** — Cálculos de contribuciones y formato de fechas

---

## pages/page.tsx

Página principal de metas grupales. Gestiona dos vistas: selector de metas y detalle de meta grupal.

### Estado local

- `selectedGoalId`: ID de la meta seleccionada. Si es null, muestra el selector.
- `isCreateOpen`, `isInviteOpen`, `isUpdateGoalOpen`, `isEditNameOpen`, `isAddContributionOpen`: controlan los diálogos.
- `editingMember`: miembro que se está editando.
- `deletingMemberId`: ID del miembro a eliminar (controla ConfirmDeletionDialog).
- `customPercentages`: objeto `Record<string, number>` que mapea ID de miembro a porcentaje personalizado.

### Hooks utilizados

- `useGroupGoalQueries(selectedGoalId)`: obtiene metas y detalle. Devuelve `goals` (ordenadas: admin primero, luego alfabéticamente), `isLoading` y `selectedGoal`.
- `useGroupGoalMutations(selectedGoalId)`: devuelve funciones de mutación y estados de carga.

### Cálculos

- `totalCustomPercentage`: suma de todos los porcentajes personalizados (useMemo).
- `contributions`: calculados con `calculateContributions` de utils.ts. Mapea cada miembro con `expectedContribution`, `contributed` y `percentage`.
- `totalContributed`: suma de todas las contribuciones realizadas.
- `percentageCompleted`: porcentaje del objetivo mensual alcanzado (`totalContributed / monthlyTarget * 100`).

### Funciones handler

- `handleCreateGoal(name, monthlyTarget, distributionMode)`: llama a `mutations.handleCreateGoal` y cierra el diálogo.
- `handleInviteMember(identifier)`: llama a `mutations.handleInviteMember` y cierra el diálogo.
- `handleUpdateGoal(newGoal)`: llama a `mutations.handleUpdateGoal` y cierra el diálogo.
- `handleEditName(name)`: llama a `mutations.handleEditName` y cierra el diálogo.
- `handleEditMember(member)`: llama a `mutations.handleEditMember` y limpia `editingMember`.
- `handleDeleteMember()`: llama a `mutations.handleDeleteMember` con `deletingMemberId` y limpia el estado.
- `handleCustomPercentageChange`: actualiza `customPercentages` con clamp 0-100.

### Layout

- Sidebar izquierdo permanente.
- Vista selector: GroupGoalSelector.
- Vista detalle: GroupGoalDetail (recibe meta, contribuciones, porcentajes y callbacks).
- 6 diálogos modales + ConfirmDeletionDialog para eliminar miembro.

---

## pages/JoinGroupGoalPage.tsx

Página independiente para aceptar o rechazar invitaciones a una meta grupal.

### Estados

Máquina de estados con 4 valores: `"ready" | "accepting" | "success" | "error"`.

### Flujo

1. Verifica que `useAuth().isLoading` no esté en curso. Si está cargando, muestra Loader2 animado a pantalla completa.
2. Lee el token de la URL (`?token=`).
3. Si no hay token: muestra tarjeta de error con XCircle, mensaje "Token inválido" y botón para volver al dashboard.
4. Estado `ready`: muestra icono Users, título "Invitación recibida", descripción y botones Aceptar/Rechazar.
5. Al aceptar: `POST /group-goals/invitations/:token/accept`.
6. Estado `accepting`: Loader2 animado con texto "Procesando invitación...".
7. Estado `success`: CheckCircle verde, mensaje de bienvenida y botón para ir a `/group-goals`. Notificación toast de éxito.
8. Estado `error`: XCircle destructivo, mensaje del backend o genérico, botón para volver al dashboard. Notificación toast de error.
9. Al rechazar: navega a `/dashboard`.

---

## hooks/useGroupGoalQueries.ts

Hook que centraliza las queries de GroupGoal. Recibe `selectedGoalId`.

### Queries

- **goals:** `GET /group-goals?page=&size=`. Usa `useServerPagination` (server 12, display 4). Query key base: `['group-goals']`.
- **goal detail:** `GET /group-goals/:id`. Query key: `['group-goal', selectedGoalId]`. Solo se ejecuta si `selectedGoalId` existe (`enabled: !!selectedGoalId`).
- **contributions:** `GET /group-goals/:id/contributions?page=&size=`. Usa `useServerPagination` (server 60, display 20) dentro del detalle de meta.

### Tipos internos

Define interfaces `RawGoalResponse`, `RawGoalMember` y `RawGoalContribution` para tipar la respuesta del backend antes del mapeo.

### Mapeo

`selectedGoal` se construye con `useMemo` a partir de `rawGoal`:
- Mapea `RawGoalMember` a `GoalMember`: name con fallback "Miembro", role normalizado a "ADMIN" | "MEMBER", salary con fallback 0.
- Mapea `RawGoalContribution` a `GoalContribution`: name con fallback "Miembro", date formateada con `formatDate`.

### Ordenación

`goals` se ordena con `useMemo`: primero las que el usuario administra, luego alfabéticamente por nombre.

### Retorno

`{ goals, isLoading, selectedGoal, contributions, contributionsPagination }`.

---

## hooks/useGroupGoalMutations.ts

Hook que centraliza todas las mutaciones de GroupGoal. Recibe `selectedGoalId`.

### invalidateAll

Función que invalida `['group-goals']` y, si hay `selectedGoalId`, también `['group-goal', selectedGoalId]`.

### Mutaciones

- **createGoal:** `POST /group-goals` con `{ name, monthlyTarget, distributionMode }`. Invalida solo `['group-goals']` en onSuccess. Notificaciones toast.
- **inviteMember:** `POST /group-goals/:id/invitations` con `{ identifier }`. Invalida ambas queries. Notificaciones toast.
- **changeMode:** `PUT /group-goals/:id` con `{ distributionMode }`. Invalida ambas queries. En onError también invalida para restaurar UI. Notificaciones toast.
- **updateGoal:** `PUT /group-goals/:id` con `{ monthlyTarget }`. Invalida ambas queries. Notificaciones toast.
- **editName:** `PUT /group-goals/:id` con `{ name }`. Invalida ambas queries. Notificaciones toast.
- **editMember:** `PUT /group-goals/:id/members/:memberId` con `{ role, salary, customPercentage }`. Invalida ambas queries. Notificaciones toast.
- **deleteMember:** `DELETE /group-goals/:id/members/:memberId`. Invalida ambas queries. Notificaciones toast.
- **addContribution:** `POST /group-goals/:id/contributions` con `{ amount }`. Invalida ambas queries. Notificaciones toast.

### Retorno

Funciones `handle*` (mutateAsync) y estados de carga (`isCreating`, `isInviting`, `isChangingMode`, `isUpdatingGoal`, `isEditingName`, `isEditingMember`, `isDeletingMember`, `isAddingContribution`).

---

## utils.ts

Funciones de utilidad:

- **formatDate:** re-exportado desde `@/shared/utils/date`. Formatea una fecha ISO/LocalDateTime a formato legible (día 2 dígitos + mes abreviado + año) usando el locale activo.
- **calculateContributions:** calcula las contribuciones esperadas y realizadas de cada miembro según el modo de distribución:
  - **CUSTOM:** `expected = (customPercentage / 100) * monthlyTarget`. Si `totalCustomPercentage` es 0, expected es 0.
  - **EQUITATIVE:** `expected = monthlyTarget / members.length`.
  - **PROPORTIONAL:** `expected = (salary / totalSalary) * monthlyTarget`. Si `totalSalary` es 0, expected es 0.
  - Construye `contributedMap` desde `selectedGoal.contributions` agrupando por `userId`.
  - Devuelve array de `ContributionMember` con expected, contributed y percentage (0 si expected es 0).

---

## constants.ts

- **MEMBER_COLORS:** array de 6 colores de fondo Tailwind para identificar visualmente a cada miembro (primary, emerald, amber, rose, purple, cyan).

---

## types/index.ts

Define todos los contratos de datos:

- **GoalRole:** "ADMIN" | "MEMBER".
- **DistributionMode:** "EQUITATIVE" | "PROPORTIONAL" | "CUSTOM".
- **InvitationStatus:** "PENDING" | "ACCEPTED" | "REJECTED".
- **GoalResponse:** id, name, monthlyTarget, distributionMode, createdAt.
- **CreateGoalRequest:** name, monthlyTarget, distributionMode.
- **InviteMemberRequest:** identifier.
- **InvitationResponse:** id, goalId, inviterId, inviterName, invitedUserId, status, createdAt, goalName.
- **GoalMember:** id, userId, name, role, salary, customPercentage (opcional).
- **GoalContribution:** id, userId, name, amount, date.
- **GroupGoalDetail:** id, name, monthlyTarget, distributionMode, isAdmin, members, contributions.
- **GoalListItem:** id, name, monthlyTarget, memberCount, isAdmin.
- **ContributionMember:** extiende GoalMember con expectedContribution, contributed, percentage.

---

## components/GroupGoalSelector.tsx

Vista de selección de meta grupal.

### Props

- **goals:** GoalListItem[]
- **isLoading:** boolean
- **onSelect:** callback con goalId
- **onCreateClick:** callback al hacer clic en crear

### Estados

- **Carga:** skeletons de tarjetas con animación pulse, icono Users, título y descripción simulados.
- **Vacío (sin metas):** mensaje "No hay metas" y botón "Crear primera meta".
- **Con datos:** lista de tarjetas con nombre, badge de admin (Crown + "Admin"), número de miembros, objetivo mensual y flecha ArrowRight animada al hover. Botón "Crear nueva meta" siempre visible abajo.

---

## components/GroupGoalDetail.tsx

Vista de detalle de una meta grupal. Recibe todos los datos por props.

### Props

- **goal:** GroupGoalDetail
- **contributions:** ContributionMember[]
- **totalContributed:** number
- **percentageCompleted:** number
- **customPercentages:** Record<string, number>
- **onCustomPercentageChange:** callback
- **totalCustomPercentage:** number
- **onBack:** callback para volver al selector
- **onInviteClick:** callback para abrir diálogo de invitar
- **onGoalClick:** callback para abrir diálogo de cambiar objetivo
- **onEditNameClick:** callback para abrir diálogo de editar nombre
- **onEditMember:** callback con ContributionMember
- **onDeleteMember:** callback con memberId
- **onModeChange:** callback con modo
- **onAddContribution:** callback para abrir diálogo de aportación

### Secciones

- **Header:** botón volver, nombre de la meta, número de miembros. Si es admin: botones "Invitar miembro" (UserPlus), "Editar nombre" (Pencil) y "Cambiar objetivo" (Target).
- **Progreso:** tarjeta de progreso con total recolectado, porcentaje, barra de progreso y desglose por miembro.
- **DistributionCard:** selector de modo de distribución.
- **Miembros:** grid responsive de MemberCard.
- **Historial:** ContributionHistory con botón para añadir aportación.

### Validación de porcentajes

`isPercentageInvalid` es true si el modo es CUSTOM y la suma de porcentajes no es exactamente 100% (tolerancia 0.01).

---

## components/MemberCard.tsx

Tarjeta individual de miembro.

### Props

- **member:** ContributionMember
- **index:** number (para asignar color)
- **isAdmin:** boolean
- **isCustomMode:** boolean
- **customPercentage:** number
- **onCustomPercentageChange:** callback
- **onEdit:** callback
- **onDelete:** callback

### Estructura

- **Cabecera:** nombre + corona (Crown) si es admin. Menú desplegable (visible al hover solo si isAdmin) con opciones Editar (Edit2) y Eliminar (Trash2, destructivo) separadas por DropdownMenuSeparator.
- **Rol:** badge en uppercase con "Admin" o "Miembro".
- **Modo CUSTOM:** si es admin, input numérico para cambiar porcentaje (0-100, step 0.1). Si no es admin, texto con el porcentaje asignado.
- **Detalles:** salario, contribución esperada, contribución realizada.
- **Barra individual:** barra de progreso con `MEMBER_COLORS[index]` y ancho `Math.min(percentage, 100)%`.

---

## components/DistributionCard.tsx

Selector de modo de distribución.

### Props

- **distributionMode:** string
- **isAdmin:** boolean
- **isCustomMode:** boolean
- **isPercentageInvalid:** boolean
- **onModeChange:** callback
- **isChangingMode?:** boolean (default false)

### Estructura

- Si es admin: Select desplegable con 3 opciones (Equitativo, Proporcional, Personalizado). Muestra Loader2 animado durante el cambio.
- Si no es admin: texto con el modo actual.
- Descripción del modo seleccionado desde i18n (`groupgoals.modes.{mode}Desc`).
- En modo CUSTOM: banner de validación. Si porcentajes suman 100% (tolerancia 0.01): banner verde con CheckCircle2 y texto "Porcentajes correctos". Si no: banner ámbar con AlertCircle y texto "Los porcentajes deben sumar 100%".

---

## components/ContributionHistory.tsx

Historial de aportaciones paginado.

### Props

- **contributions:** GoalContribution[]
- **onAddClick:** callback
- **pagination:** controles de paginación (currentPage, totalDisplayPages, nextPage, prevPage)

### Datos

- Obtiene aportaciones paginadas desde `GET /group-goals/:id/contributions?page=&size=` (server 60, display 20).
- Fechas formateadas con `formatDate` de `@/shared/utils/date`.

### Estructura

- Cabecera con título y botón "Añadir aportación" (Plus).
- Si no hay aportaciones: mensaje "No hay aportaciones registradas" centrado.
- Lista de aportaciones: nombre del miembro, fecha formateada, importe en verde con signo "+".
- Controles de paginación al final si hay más de una página.

---

## Diálogos (dialogs/)

### CreateGroupGoalDialog.tsx

**Props:** open, onOpenChange, onCreate.

**Campos:** nombre de la meta (texto con foco automático y placeholder), objetivo mensual (numérico) y modo de distribución (Select con EQUITATIVE/PROPORTIONAL/CUSTOM).

**Validación:** botón deshabilitado si nombre vacío u objetivo ≤ 0.

**Flujo:** llama a `onCreate(name.trim(), monthlyGoal, distributionMode)`, resetea campos y cierra.

---

### InviteMemberDialog.tsx

**Props:** open, onOpenChange, onInvite.

**Campos:** identifier (nombre de usuario o email, cualquier texto no vacío). El input es de tipo `text` sin validación de formato (el backend valida que el identificador corresponda a un usuario registrado).

**Validación:** muestra error si identifier está vacío. Botón deshabilitado si identifier vacío.

**Flujo:** al invitar, llama a `onInvite(identifier.trim())`, resetea identifier y cierra. Al cerrar el diálogo se resetean los campos. Tras éxito, invalida las queries de `notifications` y `notifications/unread-count` para que la tabla del perfil se refresque.

---

### UpdateGoalDialog.tsx

**Props:** open, onOpenChange, currentGoal, onSave.

**Campos:** nuevo objetivo mensual (numérico, inicializado con `currentGoal`).

**Validación:** botón deshabilitado si objetivo ≤ 0.

**Key dinámica:** `key={currentGoal}` para reiniciar el valor al cambiar de meta.

---

### EditGoalNameDialog.tsx

**Props:** open, onOpenChange, currentName, onSave.

**Campos:** nuevo nombre (texto, inicializado con `currentName`).

**Validación:** botón deshabilitado si nombre vacío.

**Key dinámica:** `key={currentName}` para reiniciar el valor al cambiar de meta.

---

### EditMemberDialog.tsx

**Props:** member (GoalMember | null), open, onOpenChange, onSave.

**Campos:**
- Nombre: input deshabilitado con opacidad reducida.
- Rol: Select con ADMIN/MEMBER.
- Salario mensual: numérico con min 0.
- Porcentaje personalizado: numérico con min 0, max 100, step 0.1.

**Flujo:** al guardar, aplica clamp al porcentaje (`Math.max(0, Math.min(100, ...))`) y llama a `onSave` con los datos actualizados.

**Key dinámica:** `key={member?.id ?? "new"}` para reiniciar al cambiar de miembro.

---

### AddContributionDialog.tsx

**Props:** open, onOpenChange, onSave (async), isSaving.

**Campos:** importe en euros (numérico con min 0.01, step 0.01, foco automático).

**Validación:** botón deshabilitado si importe vacío, ≤ 0 o `isSaving`. El diálogo no se puede cerrar mientras `isSaving` es true.

**Flujo:** llama a `await onSave(Number(amount))`, resetea amount y cierra. Si hay error, lo captura y loguea.

---

## Conexión con el backend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /group-goals?page=&size= | Listar metas del usuario paginadas |
| POST | /group-goals | Crear nueva meta |
| GET | /group-goals/:id | Obtener detalle de meta |
| PUT | /group-goals/:id | Actualizar meta (nombre, modo, objetivo) |
| POST | /group-goals/:id/invitations | Invitar miembro |
| POST | /group-goals/invitations/:token/accept | Aceptar invitación |
| PUT | /group-goals/:id/members/:memberId | Editar miembro |
| DELETE | /group-goals/:id/members/:memberId | Eliminar miembro |
| GET | /group-goals/:id/contributions?page=&size= | Ver contribuciones paginadas |
| POST | /group-goals/:id/contributions | Añadir aportación |

---

## Notas de migración (v0.1.0)

- El feature fue renombrado de `family` a `groupgoals` para alinearse con el dominio del backend (`GroupGoal`).
- Los endpoints cambiaron de `/families` a `/group-goals`.
- Se añadió el diálogo `EditGoalNameDialog` para permitir renombrar la meta.
- `InviteMemberDialog` ahora usa `identifier` (puede ser username o email) en lugar de solo email. No valida formato en el frontend; el backend valida que el identificador corresponda a un usuario registrado.
- Todos los tipos utilizan `Goal*` y `GroupGoal*` en lugar de `Family*`.
- Se mantiene la invalidación completa de queries en todas las mutaciones.
- Notificaciones toast en todas las mutaciones y en `JoinGroupGoalPage`.

## Notas de versión v0.2.0

- `InviteMemberDialog` eliminó la validación regex de email. Ahora acepta cualquier identificador no vacío (nombre de usuario o email).
- La mutación `inviteMember` invalida además las queries `['notifications']` y `['notifications', 'unread-count']` para que la tabla de notificaciones del perfil se refresque automáticamente tras enviar una invitación.
- `EditGoalNameDialog` eliminó el `useEffect` de reset de estado; el padre fuerza el reset con `key={actions.editingGoal?.id}`.
- `calculateContributions` sigue manejando los tres modos de distribución y casos límite.
