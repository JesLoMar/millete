# Profile — Documentación técnica (Frontend)

## Estructura de archivos

- **components/NotificationsTable.tsx** — Tabla de notificaciones/invitaciones pendientes en el perfil
- **components/ChangePasswordSection.tsx** — Sección de cambio de contraseña
- **components/DeleteAccountSection.tsx** — Sección de eliminación/desactivación de cuenta
- **components/PersonalInfoSection.tsx** — Sección de información personal (usuario/email)
- **components/SessionsSection.tsx** — Sección de gestión de sesiones activas
- **components/SettingsSection.tsx** — Wrapper visual reutilizable para cada sección
- **components/TelegramSection.tsx** — Sección de vinculación/desvinculación de Telegram
- **hooks/useChangePassword.ts** — Mutación de cambio de contraseña
- **hooks/useDeactivateAccount.ts** — Mutación de desactivación de cuenta
- **hooks/useNotifications.ts** — Query de notificaciones del usuario (reexportado desde features/notifications)
- **hooks/useAcceptInvitation.ts** / **useRejectInvitation.ts** — Mutaciones para responder invitaciones a Group Goals
- **hooks/useProfile.ts** — Query de perfil de usuario
- **hooks/useSessions.ts** — Queries y mutaciones de sesiones
- **hooks/useTelegramUnlink.ts** — Mutación para desvincular Telegram
- **hooks/useUpdateProfile.ts** — Mutación para actualizar perfil
- **pages/page.tsx** — Página principal de perfil
- **services/profileService.ts** — Servicio de API de perfil
- **services/invitations.service.ts** — Servicio de API para aceptar/rechazar invitaciones
- **types/index.ts** — Tipos y contratos de datos

---

## pages/page.tsx

Página de perfil de usuario. Renderiza un layout vertical con secciones independientes:

1. **PersonalInfoSection** — edición de usuario/email.
2. **ChangePasswordSection** — cambio de contraseña.
3. **TelegramSection** — vinculación con Telegram.
4. **NotificationsTable** — tabla de invitaciones/notificaciones pendientes.
5. **SessionsSection** — gestión de sesiones activas.
6. **DeleteAccountSection** — desactivación de cuenta.

> **Nota:** La sección de preferencias (tema/idioma/moneda/formato de fecha) no se renderiza porque el tema y el idioma se gestionan globalmente desde `ThemeSelector` y `LanguageSelector` en `shared/components/`. Las preferencias de moneda y formato de fecha aún no están implementadas.

---

## services/profileService.ts

Servicio de API para gestión de perfil. Base URL: `/profile`.

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /profile | Obtener perfil del usuario autenticado |
| PUT | /profile | Actualizar usuario/email |
| PUT | /profile/password | Cambiar contraseña |
| GET | /profile/preferences | Obtener preferencias (JSON string) |
| PUT | /profile/preferences | Guardar preferencias (JSON string) |
| DELETE | /profile/telegram | Desvincular Telegram |
| GET | /profile/sessions | Listar sesiones activas |
| DELETE | /profile/sessions/:id | Cerrar sesión específica |
| DELETE | /profile/sessions?currentSessionId= | Cerrar todas las demás sesiones |
| POST | /profile/deactivate | Desactivar cuenta |

---

## hooks/useProfile.ts

Query de perfil del usuario.

- **queryKey:** `['profile']`
- **queryFn:** `profileService.getProfile`

Retorna `{ profile, isLoading, error }`.

---

## hooks/useUpdateProfile.ts

Mutación para actualizar información personal.

- **mutationFn:** `profileService.updateProfile`
- **onSuccess:** invalida `['profile']` y `['user']`, muestra toast de éxito.
- **onError:** toast de error.

---

## hooks/useChangePassword.ts

Mutación para cambiar contraseña.

- **mutationFn:** `profileService.changePassword`
- **onSuccess:** toast de éxito y logout automático.
- **onError:** toast de error.

---

## hooks/useTelegramUnlink.ts

Mutación para desvincular Telegram.

- **mutationFn:** `profileService.unlinkTelegram`
- **onSuccess:** invalida `['profile']`, muestra toast de éxito.

---

## hooks/useSessions.ts

Centraliza queries y mutaciones de sesiones.

- **useSessionsQuery:** `GET /profile/sessions`, queryKey `['sessions']`.
- **useCloseSessionMutation:** `DELETE /profile/sessions/:id`, invalida `['sessions']`.
- **useCloseAllOtherSessionsMutation:** `DELETE /profile/sessions?currentSessionId=`, invalida `['sessions']`.

---

## hooks/useDeactivateAccount.ts

Mutación para desactivar cuenta.

- **mutationFn:** `profileService.deactivateAccount`
- **onSuccess:** logout automático y toast informativo.
- **onError:** toast de error.

---

## components/TelegramSection.tsx

Sección de vinculación con Telegram.

### Estados

- **Loading:** muestra `common:loading`.
- **Vinculado:** muestra el `telegramChatId` y botón "Desvincular".
- **No vinculado:** muestra instrucciones y botón para abrir `@Millete_bot` en Telegram.

### Flujo

1. Si no está vinculado, el usuario pulsa el botón y se abre Telegram en una nueva pestaña.
2. En Telegram, el usuario escribe `/start` y completa login.
3. El bot vincula automáticamente el `chatId` con la cuenta.
4. Al recargar la página de perfil, se muestra el estado vinculado.

### Desvinculación

Usa `ConfirmDeletionDialog` para confirmar antes de llamar a `useTelegramUnlink`.

---

## components/NotificationsTable.tsx

Tabla de notificaciones/invitaciones pendientes del usuario. Muestra solo notificaciones de tipo `GOAL_INVITATION` que requieren acción y no han sido actionadas.

### Props

Ninguna. Usa hooks internamente.

### Hooks utilizados

- `useNotifications()` — obtiene todas las notificaciones activas del usuario.
- `useAcceptInvitation()` — mutación para aceptar invitación a Group Goal.
- `useRejectInvitation()` — mutación para rechazar invitación a Group Goal.

### Estructura

- Renderiza dentro de un `SettingsSection` con título traducido desde `userProfile:notificationsTitle`.
- Usa el componente `Table` de `shared/components/core/table` con `TableHeader`, `TableBody`, `TableRow`, `TableCell`.
- Cada fila muestra el título de la notificación y dos botones de acción:
  - ✓ (aceptar): llama a `accept(n.metadata.invitationId)`.
  - ✕ (rechazar): llama a `reject(n.metadata.invitationId)`.
- Tras aceptar/rechazar, invalida las queries `['notifications']` y `['group-goals']`.
- Si no hay notificaciones pendientes, muestra un mensaje vacío.

### Conexión con Group Goals

Cuando un usuario es invitado a un Group Goal, el backend crea una notificación interna. Esta tabla permite al usuario ver y responder la invitación directamente desde el perfil, sin necesidad de ir a `/group-goals`.

---

## hooks/useNotifications, useAcceptInvitation, useRejectInvitation

- **useNotifications:** reexportado desde `@/features/notifications/hooks/useNotifications`. Query key `['notifications']`.
- **useAcceptInvitation:** `POST /goals/invitations/:id/accept`. Invalida `['notifications']` y `['group-goals']`.
- **useRejectInvitation:** `POST /goals/invitations/:id/reject`. Invalida `['notifications']` y `['group-goals']`.

---

### Estados

- Lista de sesiones con canal (`WEB` o `TELEGRAM`) y fecha de inicio.
- Marca la sesión actual.
- Permite cerrar una sesión específica o todas las demás.

### Confirmación

- Cerrar sesión individual: `ConfirmDeletionDialog`.
- Cerrar todas las demás: diálogo de confirmación propio.

---

## types/index.ts

- **ProfileResponse:** id, username, email, active, anonymized, telegramChatId.
- **UpdateProfileRequest:** newUsername?, newEmail?, currentPassword.
- **ChangePasswordRequest:** currentPassword, newPassword.
- **UserPreferences:** theme, language, dateFormat, currencyFormat.
- **SessionResponse:** id, channel ('WEB' | 'TELEGRAM'), active, createdAt.
- **DeactivateAccountRequest:** password.

---

## Notas de implementación (v0.1.0)

- Todos los mensajes de éxito/error de los hooks de perfil están hardcodeados en español. Pendiente de revisión completa de i18n.
- El botón de Telegram usa `asChild` en `Button` para envolver un enlace `<a>` a `https://t.me/Millete_bot`.
- La desactivación de cuenta requiere confirmación mediante checkbox y contraseña.

## Notas de versión v0.2.0

- Se añadió `NotificationsTable` entre `TelegramSection` y `SessionsSection` en la página de perfil.
- Se eliminó `usePreferences.ts` (archivo no referenciado desde ningún entry point).
- `EditCategoryDialog` eliminó el `useEffect` de pre-populado; ahora el estado se inicializa directamente y el padre fuerza el reset con `key={editingCategory?.id}`.
