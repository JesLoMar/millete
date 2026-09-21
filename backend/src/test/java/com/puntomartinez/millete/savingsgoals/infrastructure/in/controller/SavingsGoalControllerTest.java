package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.AddContributionToGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.AddContributionToGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.CreateSavingsGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.CreateSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.DeleteSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.GetSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.ListSavingsGoalsUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.UpdateSavingsGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.UpdateSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.WithdrawFromGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.WithdrawFromGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.AddContributionRequestDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.CreateSavingsGoalRequestDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.SavingsGoalResponseDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.UpdateSavingsGoalRequestDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.WithdrawRequestDTO;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalController")
class SavingsGoalControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private CreateSavingsGoalUseCase createSavingsGoalUseCase;

    @Mock
    private UpdateSavingsGoalUseCase updateSavingsGoalUseCase;

    @Mock
    private AddContributionToGoalUseCase addContributionToGoalUseCase;

    @Mock
    private WithdrawFromGoalUseCase withdrawFromGoalUseCase;

    @Mock
    private ListSavingsGoalsUseCase listSavingsGoalsUseCase;

    @Mock
    private GetSavingsGoalUseCase getSavingsGoalUseCase;

    @Mock
    private DeleteSavingsGoalUseCase deleteSavingsGoalUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SavingsGoalController controller;

    private void mockAuthenticatedUser() {
        JwtUser jwtUser = new JwtUser(USER_ID, "username", "user@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private SavingsGoal validGoal() {
        return SavingsGoal.create(
                USER_ID,
                "Vacation",
                new BigDecimal("1000.00"),
                LocalDate.now().plusDays(30),
                GoalPriority.MEDIUM,
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create savings goal with default MEDIUM priority when priority is null")
        void shouldCreateSavingsGoalWithDefaultPriority() {
            mockAuthenticatedUser();

            CreateSavingsGoalRequestDTO request = new CreateSavingsGoalRequestDTO(
                    "Vacation",
                    new BigDecimal("1000.00"),
                    LocalDate.now().plusDays(30),
                    null,
                    null
            );
            SavingsGoal goal = validGoal();

            when(createSavingsGoalUseCase.create(any(CreateSavingsGoalCommand.class)))
                    .thenReturn(goal);

            ResponseEntity<SavingsGoalResponseDTO> response =
                    controller.create(request, authentication);

            ArgumentCaptor<CreateSavingsGoalCommand> captor =
                    ArgumentCaptor.forClass(CreateSavingsGoalCommand.class);
            verify(createSavingsGoalUseCase).create(captor.capture());

            CreateSavingsGoalCommand command = captor.getValue();

            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.name()).isEqualTo("Vacation");
            assertThat(command.targetAmount()).isEqualByComparingTo("1000.00");
            assertThat(command.priority()).isEqualTo(GoalPriority.MEDIUM);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(goal.getId());
        }

        @Test
        @DisplayName("Should create savings goal with explicit priority")
        void shouldCreateSavingsGoalWithExplicitPriority() {
            mockAuthenticatedUser();

            CreateSavingsGoalRequestDTO request = new CreateSavingsGoalRequestDTO(
                    "Vacation",
                    new BigDecimal("1000.00"),
                    null,
                    GoalPriority.HIGH,
                    "https://example.com"
            );
            SavingsGoal goal = validGoal();

            when(createSavingsGoalUseCase.create(any(CreateSavingsGoalCommand.class)))
                    .thenReturn(goal);

            controller.create(request, authentication);

            ArgumentCaptor<CreateSavingsGoalCommand> captor =
                    ArgumentCaptor.forClass(CreateSavingsGoalCommand.class);
            verify(createSavingsGoalUseCase).create(captor.capture());

            assertThat(captor.getValue().priority()).isEqualTo(GoalPriority.HIGH);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @ParameterizedTest
        @ValueSource(ints = {-1, -100})
        @DisplayName("Should reject negative page")
        void shouldRejectNegativePage(int page) {
            assertThatThrownBy(() -> controller.getAll(authentication, page, 10, null))
                    .isInstanceOf(InvalidInputException.class);

            verifyNoInteractions(listSavingsGoalsUseCase);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 201})
        @DisplayName("Should reject invalid page size")
        void shouldRejectInvalidPageSize(int size) {
            assertThatThrownBy(() -> controller.getAll(authentication, 0, size, null))
                    .isInstanceOf(InvalidInputException.class);

            verifyNoInteractions(listSavingsGoalsUseCase);
        }

        @Test
        @DisplayName("Should throw when requested page is out of range")
        void shouldThrowWhenRequestedPageIsOutOfRange() {
            mockAuthenticatedUser();

            when(listSavingsGoalsUseCase.countByUserIdAndFilters(USER_ID, null))
                    .thenReturn(0L);

            assertThatThrownBy(() -> controller.getAll(authentication, 1, 10, null))
                    .isInstanceOf(InvalidInputException.class);

            verify(listSavingsGoalsUseCase, never())
                    .findByUserId(any(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("Should return single page result")
        void shouldReturnSinglePageResult() {
            mockAuthenticatedUser();

            SavingsGoal goal = validGoal();

            when(listSavingsGoalsUseCase.countByUserIdAndFilters(USER_ID, "vacation"))
                    .thenReturn(1L);
            when(listSavingsGoalsUseCase.findByUserId(USER_ID, 0, 10, "vacation"))
                    .thenReturn(List.of(goal));

            ResponseEntity<PaginatedResponseDTO<SavingsGoalResponseDTO>> response =
                    controller.getAll(authentication, 0, 10, "vacation");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            PaginatedResponseDTO<SavingsGoalResponseDTO> body = response.getBody();

            assertThat(body.content()).hasSize(1);
            assertThat(body.content().get(0).id()).isEqualTo(goal.getId());
            assertThat(body.currentPage()).isZero();
            assertThat(body.totalPages()).isEqualTo(1);
            assertThat(body.totalElements()).isEqualTo(1L);
            assertThat(body.size()).isEqualTo(10);
            assertThat(body.first()).isTrue();
            assertThat(body.last()).isTrue();
        }

        @Test
        @DisplayName("Should return first page of multiple pages")
        void shouldReturnFirstPageOfMultiplePages() {
            mockAuthenticatedUser();

            SavingsGoal goal = validGoal();

            when(listSavingsGoalsUseCase.countByUserIdAndFilters(USER_ID, null))
                    .thenReturn(25L);
            when(listSavingsGoalsUseCase.findByUserId(USER_ID, 0, 10, null))
                    .thenReturn(List.of(goal));

            ResponseEntity<PaginatedResponseDTO<SavingsGoalResponseDTO>> response =
                    controller.getAll(authentication, 0, 10, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            PaginatedResponseDTO<SavingsGoalResponseDTO> body = response.getBody();

            assertThat(body.currentPage()).isZero();
            assertThat(body.totalPages()).isEqualTo(3);
            assertThat(body.totalElements()).isEqualTo(25L);
            assertThat(body.first()).isTrue();
            assertThat(body.last()).isFalse();
        }

        @Test
        @DisplayName("Should return last page of multiple pages")
        void shouldReturnLastPageOfMultiplePages() {
            mockAuthenticatedUser();

            SavingsGoal goal = validGoal();

            when(listSavingsGoalsUseCase.countByUserIdAndFilters(USER_ID, null))
                    .thenReturn(25L);
            when(listSavingsGoalsUseCase.findByUserId(USER_ID, 2, 10, null))
                    .thenReturn(List.of(goal));

            ResponseEntity<PaginatedResponseDTO<SavingsGoalResponseDTO>> response =
                    controller.getAll(authentication, 2, 10, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            PaginatedResponseDTO<SavingsGoalResponseDTO> body = response.getBody();

            assertThat(body.currentPage()).isEqualTo(2);
            assertThat(body.totalPages()).isEqualTo(3);
            assertThat(body.totalElements()).isEqualTo(25L);
            assertThat(body.first()).isFalse();
            assertThat(body.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("Should return savings goal when found")
        void shouldReturnSavingsGoalWhenFound() {
            mockAuthenticatedUser();

            SavingsGoal goal = validGoal();
            UUID goalId = goal.getId();

            when(getSavingsGoalUseCase.getByIdAndUserId(goalId, USER_ID))
                    .thenReturn(goal);

            ResponseEntity<SavingsGoalResponseDTO> response =
                    controller.getById(goalId, authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(goalId);
            assertThat(response.getBody().name()).isEqualTo("Vacation");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update savings goal and return 200")
        void shouldUpdateSavingsGoalAndReturnOk() {
            mockAuthenticatedUser();

            UUID goalId = UUID.randomUUID();
            UpdateSavingsGoalRequestDTO request = new UpdateSavingsGoalRequestDTO(
                    "Updated",
                    new BigDecimal("2000.00"),
                    null,
                    GoalPriority.HIGH,
                    null
            );
            SavingsGoal goal = validGoal();

            when(updateSavingsGoalUseCase.update(any(UpdateSavingsGoalCommand.class)))
                    .thenReturn(goal);

            ResponseEntity<SavingsGoalResponseDTO> response =
                    controller.update(goalId, request, authentication);

            ArgumentCaptor<UpdateSavingsGoalCommand> captor =
                    ArgumentCaptor.forClass(UpdateSavingsGoalCommand.class);
            verify(updateSavingsGoalUseCase).update(captor.capture());

            UpdateSavingsGoalCommand command = captor.getValue();

            assertThat(command.id()).isEqualTo(goalId);
            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.name()).isEqualTo("Updated");
            assertThat(command.targetAmount()).isEqualByComparingTo("2000.00");
            assertThat(command.priority()).isEqualTo(GoalPriority.HIGH);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("addContribution")
    class AddContribution {

        @Test
        @DisplayName("Should add contribution and return 200")
        void shouldAddContributionAndReturnOk() {
            mockAuthenticatedUser();

            UUID goalId = UUID.randomUUID();
            AddContributionRequestDTO request = new AddContributionRequestDTO(new BigDecimal("100.00"));
            SavingsGoal goal = validGoal();

            when(addContributionToGoalUseCase.addContribution(any(AddContributionToGoalCommand.class)))
                    .thenReturn(goal);

            ResponseEntity<SavingsGoalResponseDTO> response =
                    controller.addContribution(goalId, request, authentication);

            ArgumentCaptor<AddContributionToGoalCommand> captor =
                    ArgumentCaptor.forClass(AddContributionToGoalCommand.class);
            verify(addContributionToGoalUseCase).addContribution(captor.capture());

            AddContributionToGoalCommand command = captor.getValue();

            assertThat(command.goalId()).isEqualTo(goalId);
            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.amount()).isEqualByComparingTo("100.00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("Should withdraw amount and return 200")
        void shouldWithdrawAmountAndReturnOk() {
            mockAuthenticatedUser();

            UUID goalId = UUID.randomUUID();
            WithdrawRequestDTO request = new WithdrawRequestDTO(new BigDecimal("50.00"));
            SavingsGoal goal = validGoal();

            when(withdrawFromGoalUseCase.withdraw(any(WithdrawFromGoalCommand.class)))
                    .thenReturn(goal);

            ResponseEntity<SavingsGoalResponseDTO> response =
                    controller.withdraw(goalId, request, authentication);

            ArgumentCaptor<WithdrawFromGoalCommand> captor =
                    ArgumentCaptor.forClass(WithdrawFromGoalCommand.class);
            verify(withdrawFromGoalUseCase).withdraw(captor.capture());

            WithdrawFromGoalCommand command = captor.getValue();

            assertThat(command.goalId()).isEqualTo(goalId);
            assertThat(command.userId()).isEqualTo(USER_ID);
            assertThat(command.amount()).isEqualByComparingTo("50.00");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should delete savings goal and return 204")
        void shouldDeleteSavingsGoalAndReturnNoContent() {
            mockAuthenticatedUser();

            UUID goalId = UUID.randomUUID();

            ResponseEntity<Void> response = controller.delete(goalId, authentication);

            verify(deleteSavingsGoalUseCase).deleteByIdAndUserId(goalId, USER_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
        }
    }
}