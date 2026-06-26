package com.puntomartinez.millete.savingsgoals.application.services;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.*;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private UUID userId;
    private UUID goalId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        goalId = UUID.randomUUID();
    }

    private SavingsGoal createActiveGoal() {
        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setUserId(userId);
        goal.setName("Vacaciones");
        goal.setTargetAmount(new BigDecimal("2000.00"));
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(LocalDate.now().plusMonths(6));
        goal.setPriority("HIGH");
        goal.setStatus("ACTIVE");
        goal.setActive(true);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setModifiedAt(LocalDateTime.now());
        return goal;
    }

    @Test
    void create_shouldSaveNewGoal() {
        CreateSavingsGoalCommand command = new CreateSavingsGoalCommand(
                userId, "Vacaciones", new BigDecimal("2000.00"),
                LocalDate.now().plusMonths(6), "HIGH", null
        );

        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        SavingsGoal result = savingsGoalService.create(command);

        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals("Vacaciones", result.getName());
        assertEquals(new BigDecimal("2000.00"), result.getTargetAmount());
        assertEquals(BigDecimal.ZERO, result.getCurrentAmount());
        assertEquals("HIGH", result.getPriority());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.isActive());
    }

    @Test
    void create_shouldUseDefaultPriority_whenNull() {
        CreateSavingsGoalCommand command = new CreateSavingsGoalCommand(
                userId, "Vacaciones", new BigDecimal("2000.00"),
                LocalDate.now().plusMonths(6), null, null
        );

        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        SavingsGoal result = savingsGoalService.create(command);

        assertEquals("MEDIUM", result.getPriority());
    }

    @Test
    void update_shouldUpdateExistingGoal() {
        SavingsGoal existing = createActiveGoal();
        UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                goalId, userId, "Nuevo nombre", new BigDecimal("3000.00"),
                LocalDate.now().plusMonths(12), "LOW", "PAUSED", "http://link.com"
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(existing));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        SavingsGoal result = savingsGoalService.update(command);

        assertEquals("Nuevo nombre", result.getName());
        assertEquals(new BigDecimal("3000.00"), result.getTargetAmount());
        assertEquals("PAUSED", result.getStatus());
        assertEquals("http://link.com", result.getLink());
    }

    @Test
    void update_shouldThrow_whenGoalNotFound() {
        UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                goalId, userId, "Nombre", new BigDecimal("1000.00"),
                null, null, null, null
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.update(command));
    }

    @Test
    void update_shouldThrow_whenGoalInactive() {
        SavingsGoal inactive = createActiveGoal();
        inactive.setActive(false);
        UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                goalId, userId, "Nombre", new BigDecimal("1000.00"),
                null, null, null, null
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(inactive));

        assertThrows(InvalidInputException.class, () -> savingsGoalService.update(command));
    }

    @Test
    void addContribution_shouldIncreaseCurrentAmount() {
        SavingsGoal goal = createActiveGoal();
        AddContributionToGoalCommand command = new AddContributionToGoalCommand(
                goalId, userId, new BigDecimal("500.00")
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        SavingsGoal result = savingsGoalService.addContribution(command);

        assertEquals(new BigDecimal("500.00"), result.getCurrentAmount());
    }

    @Test
    void addContribution_shouldThrow_whenGoalNotFound() {
        AddContributionToGoalCommand command = new AddContributionToGoalCommand(
                goalId, userId, new BigDecimal("100.00")
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.addContribution(command));
    }

    @Test
    void addContribution_shouldThrow_whenGoalInactive() {
        SavingsGoal inactive = createActiveGoal();
        inactive.setActive(false);
        AddContributionToGoalCommand command = new AddContributionToGoalCommand(
                goalId, userId, new BigDecimal("100.00")
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(inactive));

        assertThrows(InvalidInputException.class, () -> savingsGoalService.addContribution(command));
    }

    @Test
    void addContribution_shouldThrow_whenGoalNotActiveStatus() {
        SavingsGoal paused = createActiveGoal();
        paused.setStatus("PAUSED");
        AddContributionToGoalCommand command = new AddContributionToGoalCommand(
                goalId, userId, new BigDecimal("100.00")
        );

        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(paused));

        assertThrows(InvalidInputException.class, () -> savingsGoalService.addContribution(command));
    }

    @Test
    void findByUserId_shouldReturnOnlyActiveGoals() {
        SavingsGoal active1 = createActiveGoal();
        SavingsGoal active2 = createActiveGoal();
        active2.setId(UUID.randomUUID());
        SavingsGoal inactive = createActiveGoal();
        inactive.setId(UUID.randomUUID());
        inactive.setActive(false);

        when(savingsGoalRepository.findAllByUserId(userId)).thenReturn(List.of(active1, active2, inactive));

        List<SavingsGoal> result = savingsGoalService.findByUserId(userId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(SavingsGoal::isActive));
    }

    @Test
    void findByUserIdAndStatus_shouldReturnFilteredActiveGoals() {
        SavingsGoal active = createActiveGoal();
        SavingsGoal paused = createActiveGoal();
        paused.setId(UUID.randomUUID());
        paused.setStatus("PAUSED");

        when(savingsGoalRepository.findAllByUserIdAndStatus(userId, "ACTIVE")).thenReturn(List.of(active));

        List<SavingsGoal> result = savingsGoalService.findByUserIdAndStatus(userId, "ACTIVE");

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    void getByIdAndUserId_shouldReturnActiveGoal() {
        SavingsGoal goal = createActiveGoal();
        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));

        SavingsGoal result = savingsGoalService.getByIdAndUserId(goalId, userId);

        assertEquals(goalId, result.getId());
    }

    @Test
    void getByIdAndUserId_shouldThrow_whenNotFound() {
        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.getByIdAndUserId(goalId, userId));
    }

    @Test
    void getByIdAndUserId_shouldThrow_whenInactive() {
        SavingsGoal inactive = createActiveGoal();
        inactive.setActive(false);
        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(inactive));

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.getByIdAndUserId(goalId, userId));
    }

    @Test
    void deleteByIdAndUserId_shouldDeactivateGoal() {
        SavingsGoal goal = createActiveGoal();
        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        savingsGoalService.deleteByIdAndUserId(goalId, userId);

        ArgumentCaptor<SavingsGoal> captor = ArgumentCaptor.forClass(SavingsGoal.class);
        verify(savingsGoalRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
        assertEquals("CANCELLED", captor.getValue().getStatus());
    }

    @Test
    void deleteByIdAndUserId_shouldThrow_whenNotFound() {
        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> savingsGoalService.deleteByIdAndUserId(goalId, userId));
    }

    @Test
    void deleteByIdAndUserId_shouldThrow_whenAlreadyInactive() {
        SavingsGoal inactive = createActiveGoal();
        inactive.setActive(false);
        when(savingsGoalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(inactive));

        assertThrows(InvalidInputException.class, () -> savingsGoalService.deleteByIdAndUserId(goalId, userId));
    }
}
