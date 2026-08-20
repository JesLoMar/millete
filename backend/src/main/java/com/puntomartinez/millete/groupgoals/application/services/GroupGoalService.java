package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.*;
import com.puntomartinez.millete.groupgoals.domain.ports.in.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.*;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsActionedUseCase;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupGoalService implements
        CreateGoalUnitUseCase,
        CalculateContributionsUseCase,
        AcceptInvitationUseCase,
        DeleteGoalUnitUseCase {

    private final GoalUnitRepository goalUnitRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalInvitationRepository goalInvitationRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final UserRepository userRepository;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final MarkNotificationAsActionedUseCase markNotificationAsActionedUseCase;
    private final NotificationRepository notificationRepository;

    public GroupGoalService(
            GoalUnitRepository goalUnitRepository,
            GoalMemberRepository goalMemberRepository,
            GoalInvitationRepository goalInvitationRepository,
            GoalContributionRepository goalContributionRepository,
            UserRepository userRepository,
            CreateNotificationUseCase createNotificationUseCase,
            MarkNotificationAsActionedUseCase markNotificationAsActionedUseCase,
            NotificationRepository notificationRepository) {
        this.goalUnitRepository = goalUnitRepository;
        this.goalMemberRepository = goalMemberRepository;
        this.goalInvitationRepository = goalInvitationRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.userRepository = userRepository;
        this.createNotificationUseCase = createNotificationUseCase;
        this.markNotificationAsActionedUseCase = markNotificationAsActionedUseCase;
        this.notificationRepository = notificationRepository;
    }

    // El DistributionMode llega ya validado desde el DTO (enum, no String):
    // Jackson rechaza valores inválidos con un 400 genérico antes de llegar aquí.
    @Override
    public GoalUnit createGoalUnit(UUID adminUserId, String name, BigDecimal monthlyTarget, DistributionMode distributionMode) {
        GoalUnit goalUnit = new GoalUnit();
        goalUnit.setId(UUID.randomUUID());
        goalUnit.setName(name);
        goalUnit.setMonthlyTarget(monthlyTarget);
        goalUnit.setDistributionMode(distributionMode);
        goalUnit.setCreatedAt(LocalDateTime.now());
        goalUnit.setModifiedAt(LocalDateTime.now());
        goalUnit.setActive(true);

        goalUnit = goalUnitRepository.save(goalUnit);

        GoalMember adminMember = new GoalMember();
        adminMember.setId(UUID.randomUUID());
        adminMember.setGoalId(goalUnit.getId());
        adminMember.setUserId(adminUserId);
        adminMember.setRole(GoalRole.ADMIN);
        adminMember.setSalary(BigDecimal.ZERO);
        adminMember.setJoinedAt(LocalDateTime.now());
        adminMember.setCreatedAt(LocalDateTime.now());
        adminMember.setModifiedAt(LocalDateTime.now());
        adminMember.setActive(true);

        goalMemberRepository.save(adminMember);
        log.info("Goal unit created: {} (ID: {}) by user {}", name, goalUnit.getId(), adminUserId);
        return goalUnit;
    }

    @Override
    public Map<UUID, BigDecimal> calculateContributions(UUID goalId, UUID callerId) {
        GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, callerId)
                .filter(GoalMember::isActive)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));

        GoalUnit goalUnit = goalUnitRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal Unit not found"));
        List<GoalMember> members = goalMemberRepository.findByGoalId(goalId);
        goalUnit.setMembers(members);
        return goalUnit.calculateContributions();
    }

    public GoalListPage getGoalsByUserId(UUID userId, int page, int size) {
        long totalElements = goalUnitRepository.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int safePage = Math.min(page, Math.max(0, totalPages - 1));

        List<GoalUnit> goals = goalUnitRepository.findByUserId(userId, safePage, size);

        // Una sola consulta para los miembros (activos) de TODAS las metas de la
        // página, agrupados en memoria por goalId. Antes: 2 consultas por meta (N+1).
        List<UUID> goalIds = goals.stream().map(GoalUnit::getId).toList();
        Map<UUID, List<GoalMember>> membersByGoal = goalMemberRepository.findByGoalIdIn(goalIds).stream()
                .collect(Collectors.groupingBy(GoalMember::getGoalId));

        List<GoalListItemResponseDTO> result = new ArrayList<>();
        for (GoalUnit goal : goals) {
            // findByGoalIdIn devuelve solo miembros activos (AndActiveTrue en el repo JPA)
            List<GoalMember> members = membersByGoal.getOrDefault(goal.getId(), List.of());

            GoalMember membership = members.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
            if (membership == null) continue;

            result.add(new GoalListItemResponseDTO(
                    goal.getId(),
                    goal.getName(),
                    goal.getMonthlyTarget(),
                    members.size(),
                    membership.isAdmin()));
        }

        result.sort((a, b) -> {
            if (a.isAdmin() && !b.isAdmin()) return -1;
            if (!a.isAdmin() && b.isAdmin()) return 1;
            return a.name().compareTo(b.name());
        });

        return new GoalListPage(result, totalElements, totalPages);
    }

    public record GoalListPage(List<GoalListItemResponseDTO> goals, long totalElements, int totalPages) {}

    public GoalDetailResponseDTO getGoalDetail(UUID goalId, UUID userId) {
        GoalUnit goal = goalUnitRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        List<GoalMember> allMembers = goalMemberRepository.findByGoalId(goalId);
        boolean isMember = allMembers.stream().anyMatch(m -> m.getUserId().equals(userId) && m.isActive());

        if (!isMember) {
            throw new ForbiddenOperationException("You do not have access to this goal");
        }

        boolean isAdmin = allMembers.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isAdmin() && m.isActive());

        List<GoalMemberDTO> memberDTOs = allMembers.stream()
                .filter(GoalMember::isActive)
                .map(m -> {
                    String memberName = userRepository.findById(m.getUserId())
                            .map(u -> u.getUsername() != null ? u.getUsername() : u.getEmail())
                            .orElse("Member");
                    return new GoalMemberDTO(
                            m.getId(), m.getUserId(), memberName,
                            m.getRole().name(), m.getSalary(), m.getCustomPercentage());
                })
                .toList();

        Map<UUID, BigDecimal> contributionTotals = goalContributionRepository.findByGoalId(goalId).stream()
                .collect(Collectors.groupingBy(
                        GoalContribution::getUserId,
                        Collectors.reducing(BigDecimal.ZERO, GoalContribution::getAmount, BigDecimal::add)));

        return new GoalDetailResponseDTO(
                goal.getId(), goal.getName(), goal.getMonthlyTarget(),
                goal.getDistributionMode().name(), isAdmin,
                memberDTOs, List.of(), contributionTotals);
    }

    public ContributionHistoryPage getContributionHistory(UUID goalId, UUID userId, int page, int size) {
        GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .filter(GoalMember::isActive)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));

        long totalElements = goalContributionRepository.countByGoalId(goalId);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int safePage = Math.min(page, Math.max(0, totalPages - 1));

        List<GoalContribution> contributions = goalContributionRepository.findByGoalId(goalId, safePage, size);
        List<GoalContributionDTO> contributionDTOs = contributions.stream()
                .map(c -> {
                    String userName = userRepository.findById(c.getUserId())
                            .map(u -> u.getUsername() != null ? u.getUsername() : u.getEmail())
                            .orElse("Member");
                    return new GoalContributionDTO(c.getId(), c.getUserId(), userName, c.getAmount(), c.getDate());
                })
                .toList();

        return new ContributionHistoryPage(contributionDTOs, totalElements, totalPages);
    }

    public record ContributionHistoryPage(List<GoalContributionDTO> contributions, long totalElements, int totalPages) {}

    public void updateMember(UUID goalId, UUID memberId, UUID userId, UpdateMemberRequestDTO request) {
        GoalMember requester = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Only administrators can edit members");
        }

        GoalMember member = goalMemberRepository.findById(memberId)
                .filter(m -> m.getGoalId().equals(goalId))
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this goal"));


        // GoalRole llega como enum validado desde el DTO (comparación por referencia)
        if (request.getRole() == GoalRole.MEMBER && member.isAdmin()) {
            long activeAdmins = goalMemberRepository.findByGoalId(goalId).stream()
                    .filter(GoalMember::isActive)
                    .filter(GoalMember::isAdmin)
                    .count();
            if (activeAdmins <= 1) {
                throw new ForbiddenOperationException("No puedes dejar la meta grupal sin administradores.");
            }
        }

        if (request.getRole() != null) member.setRole(request.getRole());
        if (request.getSalary() != null) member.setSalary(request.getSalary());
        if (request.getCustomPercentage() != null) member.setCustomPercentage(request.getCustomPercentage());

        member.setModifiedAt(LocalDateTime.now());
        goalMemberRepository.save(member);
        log.info("Member {} updated in goal {} by admin {}", memberId, goalId, userId);
    }

    public void updateGoal(UUID goalId, UUID userId, UpdateGoalRequestDTO request) {
        GoalMember requester = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Only administrators can edit the goal");
        }

        GoalUnit goal = goalUnitRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        DistributionMode oldMode = goal.getDistributionMode();

        if (request.getName() != null) goal.setName(request.getName());

        if (request.getMonthlyTarget() != null) goal.setMonthlyTarget(request.getMonthlyTarget());

        if (request.getDistributionMode() != null) {
            // Sin valueOf: el enum ya viene validado del DTO
            DistributionMode newMode = request.getDistributionMode();
            if (newMode == DistributionMode.CUSTOM && oldMode != DistributionMode.CUSTOM) {
                List<GoalMember> members = goalMemberRepository.findByGoalId(goalId);
                List<GoalMember> activeMembers = members.stream().filter(GoalMember::isActive).toList();
                List<GoalMember> membersWithoutPercentage = activeMembers.stream()
                        .filter(m -> m.getCustomPercentage() == null).toList();

                if (!membersWithoutPercentage.isEmpty()) {
                    int totalMembers = activeMembers.size();
                    BigDecimal equalShare = new BigDecimal("100.00")
                            .divide(new BigDecimal(totalMembers), 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal sum = equalShare.multiply(new BigDecimal(membersWithoutPercentage.size() - 1));
                    BigDecimal lastShare = new BigDecimal("100.00").subtract(sum);

                    for (int i = 0; i < membersWithoutPercentage.size(); i++) {
                        GoalMember m = membersWithoutPercentage.get(i);
                        m.setCustomPercentage(i == membersWithoutPercentage.size() - 1 ? lastShare : equalShare);
                        m.setModifiedAt(LocalDateTime.now());
                        goalMemberRepository.save(m);
                    }
                    log.info("Auto-assigned customPercentage to {} members for CUSTOM mode", membersWithoutPercentage.size());
                }
            }
            goal.setDistributionMode(newMode);
            log.info("Goal {} distribution mode changed from {} to {}", goalId, oldMode, newMode);
        }

        goal.setModifiedAt(LocalDateTime.now());
        goalUnitRepository.save(goal);
    }

    public void deleteMember(UUID goalId, UUID memberId, UUID userId) {
        GoalMember requester = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Only administrators can delete members");
        }

        GoalMember member = goalMemberRepository.findById(memberId)
                .filter(m -> m.getGoalId().equals(goalId))
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this goal"));


        if (member.isAdmin()) {
            long activeAdmins = goalMemberRepository.findByGoalId(goalId).stream()
                    .filter(GoalMember::isActive)
                    .filter(GoalMember::isAdmin)
                    .count();
            if (activeAdmins <= 1) {
                throw new ForbiddenOperationException("No puedes dejar la meta grupal sin administradores.");
            }
        }

        member.setActive(false);
        member.setModifiedAt(LocalDateTime.now());
        goalMemberRepository.save(member);
        log.info("Member {} removed from goal {} by admin {}", memberId, goalId, userId);
    }

    public void addContribution(UUID goalId, UUID userId, AddContributionRequestDTO request) {
        GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .filter(GoalMember::isActive)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));

        GoalContribution contribution = new GoalContribution();
        contribution.setId(UUID.randomUUID());
        contribution.setGoalId(goalId);
        contribution.setUserId(userId);
        contribution.setAmount(request.getAmount());
        contribution.setDate(LocalDateTime.now());
        contribution.setCreatedAt(LocalDateTime.now());
        contribution.setModifiedAt(LocalDateTime.now());
        contribution.setActive(true);

        goalContributionRepository.save(contribution);
        log.info("Contribution of {} added to goal {} by user {}", request.getAmount(), goalId, userId);
    }

    public GoalInvitation inviteMember(UUID goalId, UUID inviterUserId, String identifier) {
        GoalMember requester = goalMemberRepository.findByGoalIdAndUserId(goalId, inviterUserId)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Only administrators can invite members");
        }

        User invitedUser = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier));

        UUID invitedUserId = invitedUser.getId();

        Optional<GoalMember> existingMember = goalMemberRepository.findByGoalIdAndUserId(goalId, invitedUserId);
        if (existingMember.isPresent() && existingMember.get().isActive()) {
            throw new ResourceAlreadyExistsException("User is already a member of this goal");
        }

        Optional<GoalInvitation> existingInvitation = goalInvitationRepository
                .findByGoalIdAndInvitedUserIdAndStatus(goalId, invitedUserId, InvitationStatus.PENDING);
        if (existingInvitation.isPresent()) {
            throw new ResourceAlreadyExistsException("There is already a pending invitation for this user");
        }

        GoalInvitation invitation = new GoalInvitation();
        invitation.setId(UUID.randomUUID());
        invitation.setGoalId(goalId);
        invitation.setInviterUserId(inviterUserId);
        invitation.setInvitedUserId(invitedUserId);
        invitation.setEmail(invitedUser.getEmail());
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setModifiedAt(LocalDateTime.now());
        invitation.setActive(true);

        invitation = goalInvitationRepository.save(invitation);

        GoalUnit goalUnit = goalUnitRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        User inviter = userRepository.findById(inviterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        String inviterName = inviter.getUsername() != null ? inviter.getUsername() : inviter.getEmail();
        Map<String, Object> metadata = Map.of(
                "goalId", goalId.toString(),
                "invitationId", invitation.getId().toString(),
                "goalName", goalUnit.getName(),
                "inviterName", inviterName
        );

        createNotificationUseCase.create(new CreateNotificationUseCase.CreateNotificationCommand(
                invitedUserId,
                NotificationType.GOAL_INVITATION,
                "Nueva invitación a meta grupal",
                inviterName + " te ha invitado a unirte a \"" + goalUnit.getName() + "\".",
                metadata,
                true,
                invitation.getExpiresAt()
        ));

        log.info("User {} invited user {} to goal {}", inviterUserId, invitedUserId, goalId);
        return invitation;
    }

    public List<GoalInvitation> getPendingInvitations(UUID userId) {
        return goalInvitationRepository.findByInvitedUserIdAndStatus(userId, InvitationStatus.PENDING);
    }

    @Override
    public GoalInvitation acceptInvitation(UUID userId, UUID invitationId) {
        GoalInvitation invitation = goalInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new ForbiddenOperationException("This invitation is not for you");
        }

        if (!invitation.isAcceptable()) {
            throw new InvalidInputException("The invitation is not valid or has expired");
        }

        goalMemberRepository.findByGoalIdAndUserId(invitation.getGoalId(), userId)
                .ifPresent(m -> {
                    if (m.isActive()) throw new ResourceAlreadyExistsException("You are already a member of this goal");
                });

        GoalMember member = goalMemberRepository.findByGoalIdAndUserId(invitation.getGoalId(), userId)
                .orElseGet(() -> {
                    GoalMember newMember = new GoalMember();
                    newMember.setId(UUID.randomUUID());
                    newMember.setGoalId(invitation.getGoalId());
                    newMember.setUserId(userId);
                    newMember.setRole(GoalRole.MEMBER);
                    newMember.setSalary(BigDecimal.ZERO);
                    newMember.setCreatedAt(LocalDateTime.now());
                    return newMember;
                });

        member.setActive(true);
        member.setJoinedAt(LocalDateTime.now());
        member.setModifiedAt(LocalDateTime.now());

        goalMemberRepository.save(member);
        invitation.markAsAccepted();
        goalInvitationRepository.save(invitation);
        markInvitationNotificationAsActioned(userId, invitationId);
        log.info("User {} accepted invitation to goal {}", userId, invitation.getGoalId());
        return invitation;
    }

    public void rejectInvitation(UUID userId, UUID invitationId) {
        GoalInvitation invitation = goalInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new ForbiddenOperationException("This invitation is not for you");
        }

        if (!InvitationStatus.PENDING.equals(invitation.getStatus())) {
            throw new InvalidInputException("This invitation can no longer be rejected");
        }

        invitation.markAsRejected();
        goalInvitationRepository.save(invitation);
        markInvitationNotificationAsActioned(userId, invitationId);
        log.info("User {} rejected invitation to goal {}", userId, invitation.getGoalId());
    }

    private void markInvitationNotificationAsActioned(UUID userId, UUID invitationId) {
        List<Notification> notifications = notificationRepository.findActiveByUserIdAndTypeAndMetadataValue(
                userId,
                NotificationType.GOAL_INVITATION.name(),
                "invitationId",
                invitationId.toString()
        );

        for (Notification notification : notifications) {
            markNotificationAsActionedUseCase.markAsActioned(userId, notification.getId());
        }
    }

    @Override
    public void deleteGoalUnit(UUID goalId, UUID userId) {
        GoalMember requester = goalMemberRepository.findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this goal"));
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Only administrators can delete the goal");
        }

        GoalUnit goal = goalUnitRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.isActive()) {
            throw new InvalidInputException("Goal is already deleted");
        }

        goal.setActive(false);
        goal.setModifiedAt(LocalDateTime.now());
        goalUnitRepository.save(goal);

        List<GoalMember> activeMembers = goalMemberRepository.findByGoalId(goalId).stream()
                .filter(GoalMember::isActive)
                .peek(m -> {
                    m.setActive(false);
                    m.setModifiedAt(LocalDateTime.now());
                })
                .toList();

        activeMembers.forEach(goalMemberRepository::save);
        log.info("Goal {} deleted by admin {}. {} members deactivated.", goalId, userId, activeMembers.size());
    }
}