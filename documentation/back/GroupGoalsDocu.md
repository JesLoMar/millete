# Group Goals — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/GroupGoalService.java** — Servicio central con toda la lógica de metas compartidas y validaciones de consistencia
- **domain/model/GoalUnit.java** — Entidad de dominio con cálculo de contribuciones
- **domain/model/GoalMember.java** — Miembro de meta con rol y salario
- **domain/model/GoalInvitation.java** — Invitación con lógica de aceptación y rechazo
- **domain/model/GoalContribution.java** — Aportación individual
- **domain/model/DistributionMode.java** — Enum: EQUITATIVE, PROPORTIONAL, CUSTOM
- **domain/model/GoalRole.java** — Enum: ADMIN, MEMBER
- **domain/model/InvitationStatus.java** — Enum: PENDING, ACCEPTED, REJECTED, EXPIRED
- **domain/ports/in/CreateGoalUnitUseCase.java** — Interfaz de creación de meta
- **domain/ports/in/AcceptInvitationUseCase.java** — Interfaz de aceptación de invitación
- **domain/ports/in/CalculateContributionsUseCase.java** — Interfaz de cálculo de contribuciones
- **domain/ports/in/DeleteGoalUnitUseCase.java** — Interfaz de eliminación de meta
- **domain/ports/out/GoalUnitRepository.java** — Puerto de salida de meta compartida
- **domain/ports/out/GoalMemberRepository.java** — Puerto de salida de miembros
- **domain/ports/out/GoalInvitationRepository.java** — Puerto de salida de invitaciones
- **domain/ports/out/GoalContributionRepository.java** — Puerto de salida de aportaciones
- **infrastructure/in/controller/GroupGoalController.java** — Controlador REST con 13 endpoints
- **infrastructure/in/controller/dto/** — 11 DTOs
- **infrastructure/out/persistence/postgresql/** — Adaptadores, entidades JPA, mappers MapStruct y repositorios

---

## GroupGoalController.java

Controlador REST mapeado a /api/v1/goals.

### GET /
Lista las metas compartidas del usuario autenticado. Devuelve lista de GoalListItemResponseDTO ordenadas: primero las que administra, luego alfabéticamente.

### GET /{goalId}
Obtiene el detalle de una meta. Verifica que el usuario sea miembro activo. Devuelve GoalDetailResponseDTO con miembros, aportaciones y flag isAdmin.

### POST /
Crea una nueva meta compartida. El usuario creador se convierte automáticamente en ADMIN. Responde 201 con GoalResponseDTO.

### PUT /{goalId}
Actualiza la meta (objetivo mensual o modo de distribución). Solo el ADMIN puede hacerlo. Al cambiar a modo CUSTOM, asigna automáticamente porcentajes equitativos a los miembros que no tengan customPercentage definido.

### DELETE /{goalId}
Elimina lógicamente la meta y todos sus miembros activos (soft delete). Solo ADMIN.

### POST /{goalId}/invitations
Invita a un usuario por username o email. Verifica que el identificador corresponda a un usuario registrado, que no sea ya miembro activo y que no tenga una invitación pendiente. La invitación expira en 7 días. No se envía correo electrónico.

### PUT /{goalId}/members/{memberId}
Actualiza el rol, salario o porcentaje personalizado de un miembro. Solo ADMIN.

### DELETE /{goalId}/members/{memberId}
Elimina lógicamente a un miembro (soft delete). Solo ADMIN.

### GET /{goalId}/contributions
Calcula las contribuciones esperadas de cada miembro según el modo de distribución.

### POST /{goalId}/contributions
Añade una aportación al historial de la meta.

### GET /invitations/pending
Lista las invitaciones pendientes del usuario autenticado. Incluye el nombre de la meta y el nombre del usuario que invitó.

### POST /invitations/{invitationId}/accept
Acepta una invitación por ID. Verifica que la invitación sea para el usuario, esté PENDING y no haya expirado. Crea el miembro con rol MEMBER y marca la invitación como ACCEPTED.

### POST /invitations/{invitationId}/reject
Rechaza una invitación por ID. Verifica que la invitación sea para el usuario y esté PENDING. Marca la invitación como REJECTED.

---

## GroupGoalService.java

Servicio central que implementa los 4 casos de uso y contiene métodos adicionales para el controlador. Utiliza @Slf4j de Lombok para logging estructurado. Todos los mensajes de excepción y logs están en inglés.

Inyecta `CreateNotificationUseCase` para crear notificaciones internas cuando se envía una invitación a un Group Goal.

### Crear meta (createGoalUnit)
1. Crea la GoalUnit con UUID aleatorio, nombre, objetivo y modo de distribución.
2. Guarda en base de datos.
3. Crea un GoalMember como ADMIN para el usuario creador.
4. Registra el evento mediante log.info.
5. Devuelve la meta compartida.

### Invitar miembro (inviteMember)
1. Verifica que el solicitante sea ADMIN de la meta.
2. Busca al usuario invitado por username o email mediante UserRepository.findByIdentifier().
3. Verifica que no sea ya miembro activo.
4. Verifica que no tenga invitación PENDING previa.
5. Crea GoalInvitation con token único, expiración de 7 días y referencia al usuario invitado.
6. Crea una notificación interna (`NotificationType.GOAL_INVITATION`) para el usuario invitado mediante `CreateNotificationUseCase.create()`. La notificación incluye metadata con goalId, invitationId, goalName e inviterName.
7. Guarda y registra el evento mediante log.info.

### Aceptar invitación (acceptInvitation)
1. Busca la invitación por ID.
2. Verifica que la invitación sea para el usuario autenticado.
3. Verifica que sea aceptable (PENDING, no expirada, activa) mediante invitation.isAcceptable().
4. Verifica que el usuario no sea ya miembro activo.
5. Crea GoalMember con rol MEMBER.
6. Marca la invitación como ACCEPTED.
7. Registra el evento mediante log.info.

### Rechazar invitación (rejectInvitation)
1. Busca la invitación por ID.
2. Verifica que la invitación sea para el usuario autenticado.
3. Verifica que esté en estado PENDING.
4. Marca la invitación como REJECTED.
5. Registra el evento mediante log.info.

### Obtener invitaciones pendientes (getPendingInvitations)
1. Busca todas las invitaciones con estado PENDING para el usuario.
2. Devuelve la lista de GoalInvitation.

### Calcular contribuciones (calculateContributions)
1. Busca la meta compartida y sus miembros.
2. Delega en goalUnit.calculateContributions().

### Actualizar miembro (updateMember)
1. Verifica que el solicitante sea ADMIN de la meta.
2. Busca el miembro a actualizar.
3. Aplica los campos enviados en el request: role, salary, customPercentage.
4. Guarda y registra el evento mediante log.info.

### Actualizar meta (updateGoal)
1. Verifica que el solicitante sea ADMIN.
2. Si se cambia el modo de distribución a CUSTOM desde otro modo, asigna automáticamente porcentajes equitativos a los miembros activos que no tengan customPercentage definido. El último miembro recibe el porcentaje restante para completar 100%.
3. Aplica los cambios y registra el cambio de modo mediante log.info.

### Eliminar miembro (deleteMember)
1. Verifica que el solicitante sea ADMIN.
2. Soft delete del miembro (active = false).
3. Registra el evento mediante log.info.

### Añadir aportación (addContribution)
1. Crea la aportación con UUID aleatorio y fecha actual.
2. Guarda y registra mediante log.info.

### Eliminar meta (deleteGoalUnit)
1. Verifica que el solicitante sea ADMIN.
2. Verifica que la meta esté activa.
3. Soft delete de la meta y de todos sus miembros activos.
4. Registra el evento mediante log.info con el número de miembros desactivados.

### Métodos adicionales

- getGoalsByUserId: obtiene las metas donde el usuario es miembro activo.
- getGoalDetail: obtiene el detalle con miembros y aportaciones. Resuelve nombres desde UserRepository.

---

## GoalUnit.java (Modelo de dominio)

Entidad rica con lógica de negocio para calcular contribuciones.

### Método calculateContributions

Según el modo de distribución:

- EQUITATIVE: divide el objetivo mensual entre el número de miembros a partes iguales.
- PROPORTIONAL: distribuye el objetivo según el porcentaje del salario de cada miembro respecto al total de salarios. Si totalSalary es 0, lanza IllegalStateException.
- CUSTOM: distribuye según los porcentajes personalizados asignados a cada miembro.

Devuelve un mapa de userId → cantidad a aportar.

---

## GoalInvitation.java (Modelo de dominio)

Contiene la lógica de validación de invitaciones.

### Constructor
Inicializa el UUID, estado PENDING, expiración a 7 días y fechas de creación/modificación.

### isAcceptable()
Devuelve true si el estado es PENDING, la fecha de expiración no ha pasado y está activa.

### markAsAccepted()
Cambia el estado a ACCEPTED y actualiza modifiedAt.

### markAsRejected()
Cambia el estado a REJECTED y actualiza modifiedAt.

---

## GoalMember.java (Modelo de dominio)

### isAdmin()
Devuelve true si el rol es ADMIN.

---

## Puertos de entrada

- CreateGoalUnitUseCase: createGoalUnit(adminUserId, name, monthlyTarget, distributionMode)
- AcceptInvitationUseCase: acceptInvitation(userId, invitationId)
- CalculateContributionsUseCase: calculateContributions(goalId)
- DeleteGoalUnitUseCase: deleteGoalUnit(goalId, userId)

---

## Puertos de salida

- GoalUnitRepository: save, findById, deleteById
- GoalMemberRepository: save, findById, findByGoalIdAndUserId, findByGoalId, deleteByGoalIdAndUserId, findByUserId
- GoalInvitationRepository: save, findByToken, findById, findByGoalIdAndEmailAndStatus, findByInvitedUserIdAndStatus, findByGoalIdAndInvitedUserIdAndStatus
- GoalContributionRepository: save, findByGoalId

---

## Sistema de invitaciones internas

Las invitaciones son internas y no dependen de correo electrónico. Un ADMIN invita a otro usuario por su username o email. La invitación se vincula al userId del invitado mediante el campo invitedUserId.

El usuario invitado puede ver sus invitaciones pendientes en el endpoint GET /invitations/pending. Desde ahí puede aceptar o rechazar cada invitación.

Ventajas del sistema:
- No requiere que el usuario tenga email configurado.
- Las invitaciones son inmediatas y no dependen de servicios externos.
- El usuario tiene control total sobre qué invitaciones aceptar o rechazar.
- La expiración de 7 días evita invitaciones huérfanas indefinidamente.

---

## Adaptadores de persistencia

Siguen el patrón de puertos y adaptadores: implementan la interfaz del puerto de salida, usan un mapper MapStruct con componentModel = "spring" y delegan en un JpaRepository.

- GoalPostgresAdapter → JpaGoalUnitRepository
- GoalMemberPostgresAdapter → JpaGoalMemberRepository
- GoalInvitationPostgresAdapter → JpaGoalInvitationRepository
- GoalContributionPostgresAdapter → JpaGoalContributionRepository

Las aportaciones se ordenan por fecha descendente. Los miembros y aportaciones filtran por active = true.

---

## Entidades JPA

- GoalUnitEntity: tabla goal_units. Campos: id, name, monthly_target, distribution_mode, created_at, modified_at, active.
- GoalMemberEntity: tabla goal_members. Campos: id, goal_id, user_id, role, salary, custom_percentage, joined_at, created_at, modified_at, active. Restricción única en (goal_id, user_id).
- GoalInvitationEntity: tabla goal_invitations. Campos: id, goal_id, email, token, inviter_user_id, invited_user_id, status, expires_at, created_at, modified_at, active.
- GoalContributionEntity: tabla goal_contributions. Campos: id, goal_id, user_id, amount, date, created_at, modified_at, active.

---

## DTOs

- CreateGoalRequestDTO: name, monthlyTarget, distributionMode
- GoalResponseDTO: id, name, monthlyTarget, distributionMode
- UpdateGoalRequestDTO: monthlyTarget, distributionMode
- InviteMemberRequestDTO: identifier (username o email)
- InvitationResponseDTO: id, goalId, goalName, inviterUserId, inviterName, invitedUserId, status, createdAt
- UpdateMemberRequestDTO: role, salary, customPercentage
- GoalListItemResponseDTO: record con id, name, monthlyTarget, activeMembers, isAdmin
- GoalDetailResponseDTO: record con id, name, monthlyTarget, distributionMode, isAdmin, members, contributions
- GoalMemberDTO: record con id, userId, memberName, role, salary, customPercentage
- GoalContributionDTO: record con id, userId, userName, amount, date
- AddContributionRequestDTO: amount

---

## Migraciones Flyway

- V1: Crea las tablas originales con nombres family_*.
- V2: Añade tablas de sesiones, preferencias, metas de ahorro y campos premium/Telegram. Añade columnas inviter_user_id e invited_user_id a family_invitations, hace opcionales email y token, añade estado REJECTED y EXPIRED. Renombra todas las tablas y columnas de family_* a goal_*. Actualiza constraints, foreign keys e índices. Elimina la foreign key obsoleta de categories a family_units. Añade soporte multi-sesión (columna active en user_sessions, elimina uq_user_channel). Añade tabla notifications con índices para notificaciones activas, no leídas y ordenadas por fecha.

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/goals | Listar metas compartidas |
| GET | /api/v1/goals/{id} | Detalle de meta |
| POST | /api/v1/goals | Crear meta |
| PUT | /api/v1/goals/{id} | Actualizar meta |
| DELETE | /api/v1/goals/{id} | Eliminar meta |
| POST | /api/v1/goals/{id}/invitations | Invitar miembro |
| GET | /api/v1/goals/invitations/pending | Ver invitaciones pendientes |
| POST | /api/v1/goals/invitations/{id}/accept | Aceptar invitación |
| POST | /api/v1/goals/invitations/{id}/reject | Rechazar invitación |
| PUT | /api/v1/goals/{id}/members/{mid} | Editar miembro |
| DELETE | /api/v1/goals/{id}/members/{mid} | Eliminar miembro |
| GET | /api/v1/goals/{id}/contributions | Ver contribuciones |
| POST | /api/v1/goals/{id}/contributions | Añadir aportación |

---

## Seguridad

- Todos los endpoints requieren autenticación JWT válida.
- Las operaciones de escritura verifican que el usuario sea ADMIN de la meta.
- Las invitaciones solo pueden ser aceptadas o rechazadas por el usuario invitado.
- No se puede invitar a un usuario que ya sea miembro activo.
- No se pueden crear invitaciones duplicadas en estado PENDING.
- Las metas eliminadas no pueden ser modificadas.