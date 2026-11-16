# Perfil y preferencias

[`ProfilePage`](../../frontend/src/features/profile/pages/page.tsx) muestra datos personales, moneda local, zona horaria, cambio de contraseña, notificaciones/invitaciones, sesiones y desactivación de cuenta. No contiene integración Telegram.

## Preferencias

`LocalCurrencySection.tsx` y `TimezoneSection.tsx` leen y actualizan preferencias mediante `profileService`; invalidan la query `preferences` después de guardar. La moneda local se inicializa desde `localCurrency` o `currencyFormat.currency`, con EUR como fallback. La zona horaria usa el valor guardado o la zona del navegador. Estas preferencias son funciones implementadas, no propuestas.

## Cuenta y sesiones

Las secciones `PersonalInfoSection`, `ChangePasswordSection`, `SessionsSection` y `DeleteAccountSection` usan hooks/servicios del módulo. Las rutas, métodos y payloads visibles están en [`profileService.ts`](../../frontend/src/features/profile/services/profileService.ts); tipos en [`types/index.ts`](../../frontend/src/features/profile/types/index.ts). La interfaz ofrece gestión de sesiones y operaciones de cuenta; no se afirma aquí cómo el servidor las persiste.

## Invitaciones

`NotificationsTable` se muestra en el perfil y permite responder a invitaciones de metas grupales. La página puede desplazarse a esta sección cuando recibe `?section=notifications`. Ver [GroupGoalsDocu.md](GroupGoalsDocu.md) para el flujo asociado.

## Solicitudes consumidas

| Servicio | Método y ruta | Uso de UI |
|---|---|---|
| `profileService` | GET/PUT `/profile` | Obtener `ProfileResponse`; actualización con campos opcionales de nombre/email y contraseña actual. |
| | PUT `/profile/password` | `currentPassword,newPassword`; la mutación notifica y cierra sesión en éxito. |
| | GET/PUT `/profile/preferences` | Preferencias, incluyendo `localCurrency`, formato de moneda y `timezone`; el cliente cachea preferencias de presentación en sessionCache y consulta de nuevo al invalidar `preferences`. |
| | GET `/profile/sessions` | Lista sesiones con identificador, canal, estado y fecha. |
| | DELETE `/profile/sessions/{id}` / DELETE `/profile/sessions` | Cierre de una sesión o de las demás; interfaz muestra confirmación y refresca sesiones. |
| | POST `/profile/deactivate` | Solicitud con contraseña; éxito desencadena logout en el cliente. |

Las secciones visibles son PersonalInfo, LocalCurrency, Timezone, ChangePassword, NotificationsTable, Sessions y DeleteAccount. Moneda usa valores del runtime `Intl.supportedValuesOf('currency')` con fallback en código; zona lista zonas disponibles de `Intl` e incluye la del navegador. Guardar cada preferencia actualiza la clave `preferences` y muestra resultado. La vista de perfil no documenta Telegram ni preferencias no montadas.

Los hooks principales son `useProfile`, `useUpdateProfile`, `useChangePassword`, `useSessions`, `useDeactivateAccount`; los componentes de preferencias usan queries y mutaciones React Query directamente. Notificaciones/invitaciones tienen su propio flujo documentado en `NotificationsDocu.md`.
