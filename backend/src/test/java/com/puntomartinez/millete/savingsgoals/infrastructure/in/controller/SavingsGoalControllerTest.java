package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller;

import com.puntomartinez.millete.savingsgoals.application.services.SavingsGoalService;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalControllerTest {

    @Mock
    private SavingsGoalService savingsGoalService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SavingsGoalController controller;

    private UUID userId;
    private UUID goalId;
    private JwtUser jwtUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        goalId = UUID.randomUUID();
        jwtUser = new JwtUser(userId, "test@example.com", "Test User");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private SavingsGoal createGoal() {
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
    void create_shouldReturnCreatedGoal() {
        SavingsGoal goal = createGoal();
        CreateSavingsGoalRequestDTO request = new CreateSavingsGoalRequestDTO();
        request.setName("Vacaciones");
        request.setTargetAmount(new BigDecimal("2000.00"));
        request.setDeadline(LocalDate.now().plusMonths(6));
        request.setPriority("HIGH");

        when(savingsGoalService.create(any())).thenReturn(goal);

        ResponseEntity<SavingsGoalResponseDTO> response = controller.create(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Vacaciones", response.getBody().getName());
        assertEquals(new BigDecimal("2000.00"), response.getBody().getTargetAmount());
    }

    @Test
    void getAll_shouldReturnListOfGoals() {
        SavingsGoal goal = createGoal();
        when(savingsGoalService.findByUserId(userId)).thenReturn(List.of(goal));

        ResponseEntity<List<SavingsGoalResponseDTO>> response = controller.getAll(authentication, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Vacaciones", response.getBody().getFirst().getName());
    }

    @Test
    void getAll_shouldFilterByStatus_whenProvided() {
        SavingsGoal goal = createGoal();
        when(savingsGoalService.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(List.of(goal));

        ResponseEntity<List<SavingsGoalResponseDTO>> response = controller.getAll(authentication, "active");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getById_shouldReturnGoal() {
        SavingsGoal goal = createGoal();
        when(savingsGoalService.getByIdAndUserId(goalId, userId)).thenReturn(goal);

        ResponseEntity<SavingsGoalResponseDTO> response = controller.getById(goalId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(goalId, response.getBody().getId());
    }

    @Test
    void update_shouldReturnUpdatedGoal() {
        SavingsGoal goal = createGoal();
        goal.setName("Nuevo nombre");
        UpdateSavingsGoalRequestDTO request = new UpdateSavingsGoalRequestDTO();
        request.setName("Nuevo nombre");
        request.setTargetAmount(new BigDecimal("3000.00"));

        when(savingsGoalService.update(any())).thenReturn(goal);

        ResponseEntity<SavingsGoalResponseDTO> response = controller.update(goalId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nuevo nombre", response.getBody().getName());
    }

    @Test
    void addContribution_shouldReturnUpdatedGoal() {
        SavingsGoal goal = createGoal();
        goal.setCurrentAmount(new BigDecimal("500.00"));
        AddContributionRequestDTO request = new AddContributionRequestDTO();
        request.setAmount(new BigDecimal("500.00"));

        when(savingsGoalService.addContribution(any())).thenReturn(goal);

        ResponseEntity<SavingsGoalResponseDTO> response = controller.addContribution(goalId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new BigDecimal("500.00"), response.getBody().getCurrentAmount());
    }

    @Test
    void delete_shouldReturnNoContent() {
        doNothing().when(savingsGoalService).deleteByIdAndUserId(goalId, userId);

        ResponseEntity<Void> response = controller.delete(goalId, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(savingsGoalService).deleteByIdAndUserId(goalId, userId);
    }
}
