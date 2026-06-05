# Family — Documentación técnica (Frontend)

## Estructura de archivos

- **components/FamilyDetail.tsx** — Vista de detalle de una familia
- **components/FamilySelector.tsx** — Selector de familias con lista y creación
- **components/ProgressBar.tsx** — Barra de progreso multicolor por miembro
- **components/MemberCard.tsx** — Tarjeta individual de miembro con acciones
- **components/DistributionCard.tsx** — Selector de modo de distribución
- **components/ContributionHistory.tsx** — Historial de aportaciones
- **components/dialogs/AddContributionDialog.tsx** — Diálogo para añadir aportación
- **components/dialogs/ChangeGoalDialog.tsx** — Diálogo para cambiar objetivo mensual
- **components/dialogs/CreateFamilyDialog.tsx** — Diálogo para crear nueva familia
- **components/dialogs/EditMemberDialog.tsx** — Diálogo para editar miembro
- **components/dialogs/InviteMemberDialog.tsx** — Diálogo para invitar por email
- **constants.ts** — Colores de miembros
- **hooks/useFamilyQueries.ts** — Hook centralizado de queries
- **hooks/useFamilyMutations.ts** — Hook centralizado de mutaciones
- **hooks/index.ts** — Re-exportación de hooks
- **pages/page.tsx** — Página principal de familias
- **pages/JoinFamilyPage.tsx** — Página de aceptación de invitación
- **types/index.ts** — Tipos y contratos de datos
- **utils.ts** — Cálculos de contribuciones y formato de fechas

---

## pages/page.tsx

Página principal de familias. Gestiona dos vistas: selector de familias y detalle de familia.

### Estado local

- `selectedFamilyId`: ID de la familia seleccionada. Si es null, muestra el selector.
- `isCreateOpen`, `isInviteOpen`, `isGoalOpen`, `isAddContributionOpen`: controlan los 4 diálogos.
- `editingMember`: miembro que se está editando.
- `deletingMemberId`: ID del miembro a eliminar (controla ConfirmDeletionDialog).
- `customPercentages`: objeto `Record<string, number>` que mapea ID de miembro a porcentaje personalizado.

### Hooks utilizados

- `useFamilyQueries(selectedFamilyId)`: obtiene familias y detalle. Devuelve `families` (ordenadas: admin primero, luego alfabéticamente), `isLoading` y `selectedFamily`.
- `useFamilyMutations(selectedFamilyId)`: devuelve 7 funciones de mutación y 7 estados de carga.

### Cálculos

- `totalCustomPercentage`: suma de todos los porcentajes personalizados (useMemo).
- `contributions`: calculados con `calculateContributions` de utils.ts. Mapea cada miembro con `expectedContribution`, `contributed` y `percentage`.
- `totalContributed`: suma de todas las contribuciones realizadas.
- `percentageCompleted`: porcentaje del objetivo mensual alcanzado (`totalContributed / monthlyGoal * 100`).

### Funciones handler

- `handleCreateFamily(name, monthlyGoal)`: llama a `mutations.handleCreateFamily` y cierra el diálogo.
- `handleInviteMember(email)`: llama a `mutations.handleInviteMember` y cierra el diálogo.
- `handleUpdateGoal(newGoal)`: llama a `mutations.handleUpdateGoal` y cierra el diálogo.
- `handleEditMember(member)`: llama a `mutations.handleEditMember` y limpia `editingMember`.
- `handleDeleteMember()`: llama a `mutations.handleDeleteMember` con `deletingMemberId` y limpia el estado.
- `handleCustomPercentageChange`: actualiza `customPercentages` con clamp 0-100.

### Layout

- Sidebar izquierdo permanente.
- Vista selector: FamilySelector.
- Vista detalle: FamilyDetail (recibe familia, contribuciones, porcentajes y callbacks).
- 5 diálogos modales + ConfirmDeletionDialog para eliminar miembro.

---

## pages/JoinFamilyPage.tsx

Página independiente para aceptar o rechazar invitaciones a una familia.

### Estados

Máquina de estados con 4 valores: `"ready" | "accepting" | "success" | "error"`.

### Flujo

1. Verifica que `useAuth().isLoading` no esté en curso. Si está cargando, muestra Loader2 animado a pantalla completa.
2. Lee el token de la URL (`?token=`).
3. Si no hay token: muestra tarjeta de error con XCircle, mensaje "Token inválido" y botón para volver al dashboard.
4. Estado `ready`: muestra icono Users, título "Invitación recibida", descripción y botones Aceptar/Rechazar.
5. Al aceptar: `POST /families/invitations/:token/accept`.
6. Estado `accepting`: Loader2 animado con texto "Procesando invitación...".
7. Estado `success`: CheckCircle verde, mensaje de bienvenida y botón para ir a `/family`. Notificación toast de éxito.
8. Estado `error`: XCircle destructivo, mensaje del backend o genérico, botón para volver al dashboard. Notificación toast de error.
9. Al rechazar: navega a `/dashboard`.

---

## hooks/useFamilyQueries.ts

Hook que centraliza las queries de Family. Recibe `selectedFamilyId`.

### Queries

- **families:** `GET /families`. Query key: `['families']`. Sin retry ni staleTime explícitos (usa defaults).
- **family detail:** `GET /families/:id`. Query key: `['family', selectedFamilyId]`. Solo se ejecuta si `selectedFamilyId` existe (`enabled: !!selectedFamilyId`).

### Tipos internos

Define interfaces `RawFamilyResponse`, `RawFamilyMember` y `RawFamilyContribution` para tipar la respuesta del backend antes del mapeo.

### Mapeo

`selectedFamily` se construye con `useMemo` a partir de `rawFamily`:
- Mapea `RawFamilyMember` a `FamilyMember`: name con fallback "Miembro", role normalizado a "ADMIN" | "MEMBER", salary con fallback 0.
- Mapea `RawFamilyContribution` a `FamilyContribution`: memberName con fallback de `name` o "Miembro", date con fallback de `contributionDate`.

### Ordenación

`families` se ordena con `useMemo`: primero las que el usuario administra, luego alfabéticamente por nombre.

### Retorno

`{ families, isLoading, selectedFamily }`.

---

## hooks/useFamilyMutations.ts

Hook que centraliza todas las mutaciones de Family. Recibe `selectedFamilyId`.

### invalidateAll

Función que invalida `['families']` y, si hay `selectedFamilyId`, también `['family', selectedFamilyId]`.

### Mutaciones

- **createFamily:** `POST /families` con `{ name, monthlyTarget, distributionMode: "EQUITATIVE" }`. Invalida solo `['families']` en onSuccess. Notificaciones toast.
- **inviteMember:** `POST /families/:id/invitations` con `{ email }`. Invalida ambas queries. Notificaciones toast.
- **changeMode:** `PUT /families/:id` con `{ distributionMode }`. Invalida ambas queries. En onError también invalida para restaurar UI. Notificaciones toast.
- **updateGoal:** `PUT /families/:id` con `{ monthlyTarget }`. Invalida ambas queries. Notificaciones toast.
- **editMember:** `PUT /families/:id/members/:memberId` con `{ role, salary, customPercentage }`. Invalida ambas queries. Notificaciones toast.
- **deleteMember:** `DELETE /families/:id/members/:memberId`. Invalida ambas queries. Notificaciones toast.
- **addContribution:** `POST /families/:id/contributions` con `{ amount }`. Invalida ambas queries. Notificaciones toast.

### Retorno

7 funciones `handle*` (mutateAsync), 7 estados de carga (`isCreating`, `isInviting`, `isChangingMode`, `isUpdatingGoal`, `isEditingMember`, `isDeletingMember`, `isAddingContribution`).

---

## utils.ts

Funciones de utilidad:

- **formatDate:** formatea una fecha ISO a formato español (día 2 dígitos + mes abreviado + año) con `toLocaleDateString("es-ES")`.
- **calculateContributions:** calcula las contribuciones esperadas y realizadas de cada miembro según el modo de distribución:
  - **CUSTOM:** `expected = (customPercentage / 100) * monthlyGoal`. Si `totalCustomPercentage` es 0, expected es 0.
  - **EQUITATIVE:** `expected = monthlyGoal / members.length`.
  - **PROPORTIONAL:** `expected = (salary / totalSalary) * monthlyGoal`. Si `totalSalary` es 0, expected es 0.
  - Construye `contributedMap` desde `selectedFamily.contributions` agrupando por `memberId`.
  - Devuelve array de `ContributionMember` con expected, contributed y percentage (0 si expected es 0).

---

## constants.ts

- **MEMBER_COLORS:** array de 6 colores de fondo Tailwind para identificar visualmente a cada miembro (primary, emerald, amber, rose, purple, cyan).

---

## types/index.ts

Define todos los contratos de datos:

- **FamilyRole:** "ADMIN" | "MEMBER".
- **DistributionMode:** "EQUAL" | "PROPORTIONAL" | "CUSTOM" (nota: el backend usa "EQUITATIVE", el tipo dice "EQUAL").
- **InvitationStatus:** "PENDING" | "ACCEPTED" | "REJECTED".
- **FamilyResponse:** id, name, distributionMode, createdAt.
- **CreateFamilyRequest:** name, distributionMode.
- **InviteMemberRequest:** email, role.
- **InvitationResponse:** id, familyId, inviterId, inviteeEmail, status, createdAt.
- **FamilyMember:** id, name, role, salary, customPercentage (opcional), userId (opcional).
- **FamilyContribution:** id, memberId, memberName, amount, date, contributionDate (opcional), name (opcional).
- **FamilyUnitData:** id, name, monthlyGoal, distributionMode, members, contributions, isAdmin.
- **FamilyListItem:** id, name, monthlyGoal, memberCount, isAdmin.
- **ContributionMember:** extiende FamilyMember con expectedContribution, contributed, percentage.

---

## components/FamilySelector.tsx

Vista de selección de familia.

### Props

- **families:** FamilyListItem[]
- **isLoading:** boolean
- **onSelect:** callback con familyId
- **onCreateClick:** callback al hacer clic en crear

### Estados

- **Carga:** 5 skeletons de tarjetas con animación pulse, icono Users, título y descripción simulados.
- **Vacío (sin familias):** mensaje "No hay familias" y botón "Crear primera familia".
- **Con datos:** lista de tarjetas con nombre, badge de admin (Crown + "Admin"), número de miembros, objetivo mensual y flecha ArrowRight animada al hover. Botón "Crear nueva familia" siempre visible abajo.

---

## components/FamilyDetail.tsx

Vista de detalle de una familia. Recibe todos los datos por props.

### Props

- **family:** FamilyUnitData
- **contributions:** ContributionMember[]
- **totalContributed:** number
- **percentageCompleted:** number
- **customPercentages:** Record<string, number>
- **onCustomPercentageChange:** callback
- **totalCustomPercentage:** number
- **onBack:** callback para volver al selector
- **onInviteClick:** callback para abrir diálogo de invitar
- **onGoalClick:** callback para abrir diálogo de cambiar objetivo
- **onEditMember:** callback con ContributionMember
- **onDeleteMember:** callback con memberId
- **onModeChange:** callback con modo
- **onAddContribution:** callback para abrir diálogo de aportación

### Secciones

- **Header:** botón volver, nombre de la familia, número de miembros. Si es admin: botones "Invitar miembro" (UserPlus) y "Cambiar objetivo" (Target).
- **Progreso (Goal Progress + Distribution):** grid 9+3 columnas.
  - Tarjeta de progreso: total recolectado, porcentaje, ProgressBar multicolor y desglose por miembro con indicador de color, nombre, contribución y porcentaje.
  - DistributionCard: selector de modo de distribución.
- **Miembros:** grid responsive (1/2/3 columnas) de MemberCard.
- **Historial:** ContributionHistory con botón para añadir aportación.

### Validación de porcentajes

`isPercentageInvalid` es true si el modo es CUSTOM y la suma de porcentajes no es exactamente 100% (tolerancia 0.01).

---

## components/ProgressBar.tsx

Barra de progreso multicolor. Cada miembro aporta un segmento proporcional a su contribución respecto al objetivo total.

### Props

- **contributions:** ContributionMember[]
- **monthlyGoal:** number

### Funcionamiento

Calcula `memberPct = (contributed / monthlyGoal) * 100` para cada miembro. Renderiza segmentos con `MEMBER_COLORS[index % MEMBER_COLORS.length]` y ancho proporcional. Tooltip con nombre y cantidad. Altura fija de 12px (`h-3`).

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
- **Detalles:** salario, contribución esperada, contribución realizada (formateados con separadores españoles).
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
- Descripción del modo seleccionado desde i18n (`family.modes.{mode}Desc`).
- En modo CUSTOM: banner de validación. Si porcentajes suman 100% (tolerancia 0.01): banner verde con CheckCircle2 y texto "Porcentajes correctos". Si no: banner ámbar con AlertCircle y texto "Los porcentajes deben sumar 100%".

---

## components/ContributionHistory.tsx

Historial de aportaciones.

### Props

- **contributions:** FamilyContribution[]
- **onAddClick:** callback

### Estructura

- Cabecera con título y botón "Añadir aportación" (Plus).
- Si no hay aportaciones: mensaje "No hay aportaciones registradas" centrado.
- Lista de aportaciones: nombre del miembro, fecha formateada con `formatDate` (con fallback a `contributionDate`), importe en verde con signo "+".

---

## Diálogos (dialogs/)

### CreateFamilyDialog.tsx

**Props:** open, onOpenChange, onCreate.

**Campos:** nombre de la familia (texto con foco automático y placeholder) y objetivo mensual (numérico).

**Validación:** botón deshabilitado si nombre vacío u objetivo ≤ 0.

**Flujo:** llama a `onCreate(name.trim(), monthlyGoal)`, resetea campos y cierra.

---

### InviteMemberDialog.tsx

**Props:** open, onOpenChange, onInvite.

**Campos:** email con validación regex (`/^[^\s@]+@[^\s@]+\.[^\s@]+$/`).

**Validación:** muestra error "El formato del email no es válido" si el regex no coincide. El error se limpia al modificar el input. Botón deshabilitado si email vacío.

**Flujo:** al invitar, llama a `onInvite(email.trim())`, resetea email y error, y cierra. Al cerrar el diálogo se resetean los campos.

---

### ChangeGoalDialog.tsx

**Props:** open, onOpenChange, currentGoal, onSave.

**Campos:** nuevo objetivo mensual (numérico, inicializado con `currentGoal`).

**Validación:** botón deshabilitado si objetivo ≤ 0.

**Key dinámica:** `key={currentGoal}` para reiniciar el valor al cambiar de familia.

---

### EditMemberDialog.tsx

**Props:** member (FamilyMember | null), open, onOpenChange, onSave.

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
| GET | /families | Listar familias del usuario |
| POST | /families | Crear nueva familia |
| GET | /families/:id | Obtener detalle de familia |
| PUT | /families/:id | Actualizar familia (modo, objetivo) |
| POST | /families/:id/invitations | Invitar miembro |
| POST | /families/invitations/:token/accept | Aceptar invitación |
| PUT | /families/:id/members/:memberId | Editar miembro |
| DELETE | /families/:id/members/:memberId | Eliminar miembro |
| POST | /families/:id/contributions | Añadir aportación |

---

## Mejoras implementadas (v0.0.1)

- **Invalidación completa de queries en todas las mutaciones:** `invalidateAll` invalida `['families']` y `['family', selectedFamilyId]`. La lista del selector se mantiene actualizada tras cualquier cambio.
- **Diálogo de confirmación al eliminar miembro:** modal ConfirmDeletionDialog reutilizado desde Categories, con soporte i18n y botones Cancelar/Eliminar. Gestionado desde page.tsx con estado `deletingMemberId`.
- **Validación de email en InviteMemberDialog:** regex que verifica formato `xxx@xxx.xxx`. Mensaje de error con i18n. Campos se resetean al cerrar el diálogo.
- **ChangeGoalDialog con botón deshabilitado:** el botón de guardar se deshabilita cuando el objetivo es menor o igual a cero. Key dinámica para reiniciar valor.
- **EditMemberDialog con clamp en customPercentage:** el porcentaje personalizado se limita a rango 0-100 mediante `Math.max(0, Math.min(100, ...))`. Campo salary con `min="0"`. Key dinámica para reiniciar al cambiar de miembro.
- **Clases de Tailwind estáticas en FamilyDetail:** los círculos de color de la leyenda usan `MEMBER_COLORS[index % MEMBER_COLORS.length]` de constants.ts en lugar de clases dinámicas con template strings.
- **useFamilyQueries con mapeo tipado:** interfaces RawFamilyResponse, RawFamilyMember y RawFamilyContribution para tipar la respuesta del backend. Mapeo defensivo con fallbacks para name, memberName, role, salary y date.
- **FamilySelector skeleton mejorado:** 5 items de carga con estructura visual completa (icono, título, descripción).
- **DistributionCard con indicador de carga:** muestra Loader2 animado durante el cambio de modo. Select deshabilitado mientras se procesa.
- **AddContributionDialog con protección de cierre:** el diálogo no se cierra mientras `isSaving` es true, evitando envíos duplicados.
- **JoinFamilyPage con estados de autenticación:** verifica `authLoading` antes de procesar la invitación, mostrando loader a pantalla completa.
- **Notificaciones toast en todas las mutaciones:** éxito y error notificados con `notify.success` y `notify.error` desde useFamilyMutations.
- **JoinFamilyPage con notificaciones:** éxito y error notificados al aceptar invitación.
- **Cálculo de contribuciones robusto:** `calculateContributions` maneja casos límite (totalSalary = 0, totalCustomPercentage = 0, sin contribuciones) devolviendo expected = 0 y percentage = 0.