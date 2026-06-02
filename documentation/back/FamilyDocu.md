# Family — Documentación técnica (Backend)

## Estructura de archivos

- **application/services/FamilyService.java** — Servicio central con toda la lógica de familia y validaciones de consistencia
- **domain/model/FamilyUnit.java** — Entidad de dominio con cálculo de contribuciones y validaciones
- **domain/model/FamilyMember.java** — Miembro de familia con rol y salario
- **domain/model/FamilyInvitation.java** — Invitación con lógica de aceptación
- **domain/model/FamilyContribution.java** — Aportación individual
- **domain/model/DistributionMode.java** — Enum: EQUITATIVE, PROPORTIONAL, CUSTOM
- **domain/model/FamilyRole.java** — Enum: ADMIN, MEMBER
- **domain/model/InvitationStatus.java** — Enum: PENDING, ACCEPTED, EXPIRED
- **domain/ports/in/CreateFamilyUnitUseCase.java** — Interfaz de creación de familia
- **domain/ports/in/InviteFamilyMemberUseCase.java** — Interfaz de invitación
- **domain/ports/in/AcceptInvitationUseCase.java** — Interfaz de aceptación
- **domain/ports/in/CalculateFamilyContributionsUseCase.java** — Interfaz de cálculo
- **domain/ports/out/FamilyUnitRepository.java** — Puerto de salida de unidad familiar
- **domain/ports/out/FamilyMemberRepository.java** — Puerto de salida de miembros
- **domain/ports/out/FamilyInvitationRepository.java** — Puerto de salida de invitaciones
- **domain/ports/out/FamilyContributionRepository.java** — Puerto de salida de aportaciones
- **domain/ports/out/EmailSenderPort.java** — Puerto de salida de envío de emails
- **infrastructure/in/controller/FamilyController.java** — Controlador REST con 9 endpoints
- **infrastructure/in/controller/dto/** — 11 DTOs
- **infrastructure/out/persistence/postgresql/** — Adaptadores, entidades, mappers y repositorios
- **infrastructure/out/email/adapters/EmailSenderAdapter.java** — Adaptador de email

---

## FamilyController.java

Controlador REST mapeado a /api/v1/families.

### GET /
Lista las familias del usuario autenticado. Devuelve lista de FamilyListItemResponseDTO ordenadas: primero las que administra, luego alfabéticamente.

### GET /{familyId}
Obtiene el detalle de una familia. Verifica que el usuario sea miembro activo. Devuelve FamilyDetailResponseDTO con miembros, aportaciones y flag isAdmin.

### POST /
Crea una nueva familia. El usuario creador se convierte automáticamente en ADMIN. Responde 201 con FamilyResponseDTO.

### PUT /{familyId}
Actualiza la familia (objetivo mensual o modo de distribución). Solo el ADMIN puede hacerlo. Al cambiar a modo CUSTOM, valida que todos los miembros activos tengan customPercentage asignado.

### POST /{familyId}/invitations
Invita a un usuario por email. Verifica que el email esté registrado, que no sea ya miembro y que no tenga invitación pendiente. Envía email con enlace. La invitación expira en 48 horas.

### PUT /{familyId}/members/{memberId}
Actualiza el rol, salario o porcentaje personalizado de un miembro. Solo ADMIN. Solo acepta los campos aplicables según el modo de distribución de la familia.

### DELETE /{familyId}/members/{memberId}
Elimina lógicamente a un miembro (soft delete). Solo ADMIN.

### GET /{familyId}/contributions
Calcula las contribuciones esperadas de cada miembro según el modo de distribución.

### POST /{familyId}/contributions
Añade una aportación al historial de la familia.

### POST /invitations/{token}/accept
Acepta una invitación. Verifica que el token sea válido, no haya expirado y el usuario no sea ya miembro. Crea el miembro con rol MEMBER y marca la invitación como ACCEPTED.

---

## FamilyService.java

Servicio central que implementa los 4 casos de uso y contiene métodos adicionales para el controlador. Utiliza @Slf4j de Lombok para logging estructurado con niveles de severidad. Todos los mensajes de excepción y logs están en inglés.

### Crear familia (createFamilyUnit)
1. Crea la FamilyUnit con UUID aleatorio, nombre, objetivo y modo de distribución.
2. Guarda en base de datos.
3. Crea un FamilyMember como ADMIN para el usuario creador.
4. Registra el evento mediante log.info.
5. Devuelve la unidad familiar.

### Invitar miembro (inviteMember)
1. Verifica que el solicitante sea ADMIN de la familia.
2. Verifica que el email invitado pertenezca a un usuario registrado.
3. Verifica que no sea ya miembro activo.
4. Verifica que no tenga invitación PENDING.
5. Crea FamilyInvitation con token único y expiración de 48 horas.
6. Guarda, envía email y registra el evento mediante log.info.

### Aceptar invitación (acceptInvitation)
1. Busca la invitación por token.
2. Verifica que sea aceptable (PENDING, no expirada, activa) mediante invitation.isAcceptable().
3. Verifica que el usuario no sea ya miembro.
4. Crea FamilyMember con rol MEMBER.
5. Marca la invitación como ACCEPTED.
6. Registra el evento mediante log.info.

### Calcular contribuciones (calculateContributions)
1. Busca la unidad familiar y sus miembros.
2. Delega en familyUnit.calculateContributions().

### Actualizar miembro (updateMember)
1. Verifica que el solicitante sea ADMIN de la familia.
2. Obtiene la familia para conocer el modo de distribución actual.
3. Busca el miembro a actualizar.
4. Aplica solo los campos relevantes según el modo de distribución:
   - EQUITATIVE: rechaza salary y customPercentage (lanza IllegalArgumentException).
   - PROPORTIONAL: rechaza customPercentage, acepta salary.
   - CUSTOM: rechaza salary, acepta customPercentage.
5. Guarda y registra el evento mediante log.info.

### Actualizar familia (updateFamily)
1. Verifica que el solicitante sea ADMIN.
2. Si se cambia el modo de distribución a CUSTOM, valida que todos los miembros activos tengan customPercentage asignado. Si falta algún miembro, lanza IllegalStateException con la lista de IDs.
3. Aplica los cambios y registra el cambio de modo mediante log.info.

### Eliminar miembro (deleteMember)
1. Verifica que el solicitante sea ADMIN.
2. Soft delete del miembro (active = false).
3. Registra el evento mediante log.info.

### Añadir aportación (addContribution)
1. Crea la aportación con UUID aleatorio y fecha actual.
2. Guarda y registra mediante log.info.

### Métodos adicionales

- getFamiliesByUserId: obtiene las familias donde el usuario es miembro activo.
- getFamilyDetail: obtiene el detalle con miembros y aportaciones. Resuelve nombres desde UserRepository.

---

## FamilyUnit.java (Modelo de dominio)

Entidad rica con lógica de negocio para calcular contribuciones.

### Método calculateContributions

Según el modo de distribución:

- EQUITATIVE: divide el objetivo mensual entre el número de miembros a partes iguales.
- PROPORTIONAL: distribuye el objetivo según el porcentaje del salario de cada miembro respecto al total de salarios. Si totalSalary es 0, lanza IllegalStateException con mensaje descriptivo.
- CUSTOM: distribuye según los porcentajes personalizados asignados a cada miembro.

Devuelve un mapa de userId → cantidad a aportar.

---

## FamilyInvitation.java (Modelo de dominio)

Contiene la lógica de validación de invitaciones.

### isAcceptable()
Devuelve true si el estado es PENDING, la fecha de expiración no ha pasado y está activa.

### markAsAccepted()
Cambia el estado a ACCEPTED y actualiza modifiedAt.

---

## FamilyMember.java (Modelo de dominio)

### isAdmin()
Devuelve true si el rol es ADMIN.

---

## Puertos de entrada

- CreateFamilyUnitUseCase: createFamilyUnit(adminUserId, name, monthlyTarget, distributionMode)
- InviteFamilyMemberUseCase: inviteMember(adminUserId, familyId, guestEmail)
- AcceptInvitationUseCase: acceptInvitation(userId, token)
- CalculateFamilyContributionsUseCase: calculateContributions(familyId)

---

## Puertos de salida

- FamilyUnitRepository: save, findById, deleteById
- FamilyMemberRepository: save, findById, findByFamilyIdAndUserId, findByFamilyId, deleteByFamilyIdAndUserId, findByUserId
- FamilyInvitationRepository: save, findByToken, findById, findByFamilyIdAndEmailAndStatus
- FamilyContributionRepository: save, findByFamilyId
- EmailSenderPort: sendInvitationEmail(toEmail, invitationToken)

---

## Adaptador de email

### EmailSenderAdapter

Implementa EmailSenderPort usando JavaMailSender de Spring. Envía un correo con el enlace de invitación que apunta a /join-family?token=. Usa la URL del frontend configurada en app.frontend.url.

---

## Adaptadores de persistencia

Siguen el mismo patrón que el resto de módulos: implementan la interfaz del puerto de salida, usan un mapper (MapStruct o manual) y delegan en un JpaRepository.

- FamilyPostgresAdapter → JpaFamilyUnitRepository
- FamilyMemberPostgresAdapter → JpaFamilyMemberRepository
- FamilyInvitationPostgresAdapter → JpaFamilyInvitationRepository
- FamilyContributionPostgresAdapter → JpaFamilyContributionRepository

Las aportaciones se ordenan por fecha descendente. Los miembros y aportaciones filtran por active = true.

---

## Entidades JPA

- FamilyUnitEntity: tabla family_units. Campos: id, name, monthly_target, distribution_mode, created_at, modified_at, active.
- FamilyMemberEntity: tabla family_members. Campos: id, family_id, user_id, role, salary, custom_percentage, joined_at, created_at, modified_at, active.
- FamilyInvitationEntity: tabla family_invitations. Campos: id, family_id, email, token (unique), status, expires_at, created_at, modified_at, active.
- FamilyContributionEntity: tabla family_contributions. Campos: id, family_id, user_id, amount, date, created_at, modified_at, active.

---

## DTOs

- CreateFamilyRequestDTO: name, monthlyTarget, distributionMode
- FamilyResponseDTO: id, name, monthlyTarget, distributionMode
- UpdateFamilyRequestDTO: monthlyTarget, distributionMode
- InviteMemberRequestDTO: email
- InvitationResponseDTO: id, email, status, expiresAt
- UpdateMemberRequestDTO: role, salary, customPercentage
- FamilyListItemResponseDTO: id, name, monthlyGoal, memberCount, isAdmin
- FamilyDetailResponseDTO: id, name, monthlyGoal, distributionMode, isAdmin, members, contributions
- FamilyMemberDTO: id, userId, name, role, salary, customPercentage
- FamilyContributionDTO: id, userId, name, amount, date
- AddContributionRequestDTO: amount

---

## Validaciones de consistencia por modo de distribución

### En updateMember (FamilyService)
- EQUITATIVE: no se permite asignar salary ni customPercentage. Todos los miembros pagan lo mismo.
- PROPORTIONAL: no se permite asignar customPercentage. Las contribuciones se calculan según salary.
- CUSTOM: no se permite asignar salary. Las contribuciones se calculan según customPercentage.

### En updateFamily (FamilyService)
- Al cambiar el modo a CUSTOM, se valida que todos los miembros activos tengan customPercentage asignado. Si falta algún miembro, se lanza IllegalStateException con la lista de IDs afectados.

### En calculateContributions (FamilyUnit)
- Modo PROPORTIONAL: si totalSalary es 0, se lanza IllegalStateException con el mensaje "Cannot calculate contributions in PROPORTIONAL mode: no member has a salary assigned or the total sum is 0."

---

## Conexión con el frontend

| Método | Endpoint | Uso |
|--------|----------|-----|
| GET | /api/v1/families | Listar familias |
| POST | /api/v1/families | Crear familia |
| GET | /api/v1/families/:id | Detalle de familia |
| PUT | /api/v1/families/:id | Actualizar familia |
| POST | /api/v1/families/:id/invitations | Invitar miembro |
| PUT | /api/v1/families/:id/members/:memberId | Editar miembro |
| DELETE | /api/v1/families/:id/members/:memberId | Eliminar miembro |
| POST | /api/v1/families/:id/contributions | Añadir aportación |
| POST | /api/v1/families/invitations/:token/accept | Aceptar invitación |