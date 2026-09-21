package com.puntomartinez.millete.savingsgoals.domain.model;

import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SavingsGoal aggregate")
class SavingsGoalTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VALID_NAME = "Vacation";
    private static final BigDecimal VALID_TARGET = new BigDecimal("1000.00");
    private static final LocalDate VALID_DEADLINE = LocalDate.now().plusDays(30);

    private SavingsGoal createValidGoal() {
        return SavingsGoal.create(
                USER_ID,
                VALID_NAME,
                VALID_TARGET,
                VALID_DEADLINE,
                GoalPriority.HIGH,
                "https://example.com"
        );
    }

    private SavingsGoal reconstituteValidGoal(boolean active) {
        return SavingsGoal.reconstitute(
                UUID.randomUUID(),
                USER_ID,
                VALID_NAME,
                VALID_TARGET,
                BigDecimal.ZERO,
                VALID_DEADLINE,
                GoalPriority.MEDIUM,
                null,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0),
                active
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create a valid active savings goal")
        void shouldCreateValidSavingsGoal() {
            SavingsGoal goal = createValidGoal();

            assertThat(goal.getId()).isNotNull();
            assertThat(goal.getUserId()).isEqualTo(USER_ID);
            assertThat(goal.getName()).isEqualTo(VALID_NAME);
            assertThat(goal.getTargetAmount()).isEqualByComparingTo(VALID_TARGET);
            assertThat(goal.getCurrentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(goal.getDeadline()).isEqualTo(VALID_DEADLINE);
            assertThat(goal.getPriority()).isEqualTo(GoalPriority.HIGH);
            assertThat(goal.getLink()).isEqualTo("https://example.com");
            assertThat(goal.getCreatedAt()).isNotNull();
            assertThat(goal.getModifiedAt()).isNotNull();
            assertThat(goal.getCreatedAt()).isEqualTo(goal.getModifiedAt());
            assertThat(goal.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should generate different ids for different goals")
        void shouldGenerateDifferentIds() {
            SavingsGoal first = createValidGoal();
            SavingsGoal second = createValidGoal();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        @DisplayName("Should default priority to MEDIUM when priority is null")
        void shouldDefaultPriorityToMediumWhenNull() {
            SavingsGoal goal = SavingsGoal.create(
                    USER_ID,
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_DEADLINE,
                    null,
                    null
            );

            assertThat(goal.getPriority()).isEqualTo(GoalPriority.MEDIUM);
        }

        @Test
        @DisplayName("Should allow null deadline")
        void shouldAllowNullDeadline() {
            SavingsGoal goal = SavingsGoal.create(
                    USER_ID,
                    VALID_NAME,
                    VALID_TARGET,
                    null,
                    GoalPriority.LOW,
                    null
            );

            assertThat(goal.getDeadline()).isNull();
        }

        @Test
        @DisplayName("Should allow null link")
        void shouldAllowNullLink() {
            SavingsGoal goal = SavingsGoal.create(
                    USER_ID,
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_DEADLINE,
                    GoalPriority.LOW,
                    null
            );

            assertThat(goal.getLink()).isNull();
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            null,
                            VALID_NAME,
                            VALID_TARGET,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject invalid name")
        void shouldRejectInvalidName(String name) {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            name,
                            VALID_TARGET,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject name exceeding max length")
        void shouldRejectNameExceedingMaxLength() {
            String longName = "A".repeat(101);

            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            longName,
                            VALID_TARGET,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null target amount")
        void shouldRejectNullTargetAmount() {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            VALID_NAME,
                            null,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive target amount")
        void shouldRejectNonPositiveTargetAmount(String amount) {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            VALID_NAME,
                            new BigDecimal(amount),
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject deadline not after today")
        void shouldRejectDeadlineNotAfterToday() {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            LocalDate.now(),
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject deadline in the past")
        void shouldRejectDeadlineInThePast() {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            LocalDate.now().minusDays(1),
                            GoalPriority.LOW,
                            null
                    )
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute an existing savings goal")
        void shouldReconstituteSavingsGoal() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 12, 30);

            SavingsGoal goal = SavingsGoal.reconstitute(
                    id,
                    USER_ID,
                    "Car",
                    new BigDecimal("5000.00"),
                    new BigDecimal("1500.00"),
                    VALID_DEADLINE,
                    GoalPriority.HIGH,
                    "https://example.com",
                    createdAt,
                    modifiedAt,
                    false
            );

            assertThat(goal.getId()).isEqualTo(id);
            assertThat(goal.getUserId()).isEqualTo(USER_ID);
            assertThat(goal.getName()).isEqualTo("Car");
            assertThat(goal.getTargetAmount()).isEqualByComparingTo("5000.00");
            assertThat(goal.getCurrentAmount()).isEqualByComparingTo("1500.00");
            assertThat(goal.getDeadline()).isEqualTo(VALID_DEADLINE);
            assertThat(goal.getPriority()).isEqualTo(GoalPriority.HIGH);
            assertThat(goal.getLink()).isEqualTo("https://example.com");
            assertThat(goal.getCreatedAt()).isEqualTo(createdAt);
            assertThat(goal.getModifiedAt()).isEqualTo(modifiedAt);
            assertThat(goal.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should reject null id")
        void shouldRejectNullId() {
            assertThatThrownBy(() ->
                    SavingsGoal.reconstitute(
                            null,
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            BigDecimal.ZERO,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null created at")
        void shouldRejectNullCreatedAt() {
            assertThatThrownBy(() ->
                    SavingsGoal.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            BigDecimal.ZERO,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null,
                            null,
                            LocalDateTime.now(),
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null modified at")
        void shouldRejectNullModifiedAt() {
            assertThatThrownBy(() ->
                    SavingsGoal.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            BigDecimal.ZERO,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null,
                            LocalDateTime.now(),
                            null,
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject negative current amount")
        void shouldRejectNegativeCurrentAmount() {
            assertThatThrownBy(() ->
                    SavingsGoal.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            new BigDecimal("-0.01"),
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null priority")
        void shouldRejectNullPriority() {
            assertThatThrownBy(() ->
                    SavingsGoal.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            BigDecimal.ZERO,
                            VALID_DEADLINE,
                            null,
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update all provided fields")
        void shouldUpdateAllProvidedFields() {
            SavingsGoal goal = reconstituteValidGoal(true);
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.updateDetails(
                    "Updated",
                    new BigDecimal("2000.00"),
                    LocalDate.now().plusDays(60),
                    GoalPriority.HIGH,
                    "https://updated.com"
            );

            assertThat(goal.getName()).isEqualTo("Updated");
            assertThat(goal.getTargetAmount()).isEqualByComparingTo("2000.00");
            assertThat(goal.getDeadline()).isEqualTo(LocalDate.now().plusDays(60));
            assertThat(goal.getPriority()).isEqualTo(GoalPriority.HIGH);
            assertThat(goal.getLink()).isEqualTo("https://updated.com");
            assertThat(goal.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not update null fields")
        void shouldNotUpdateNullFields() {
            SavingsGoal goal = reconstituteValidGoal(true);
            String originalName = goal.getName();
            BigDecimal originalTarget = goal.getTargetAmount();
            LocalDate originalDeadline = goal.getDeadline();
            GoalPriority originalPriority = goal.getPriority();

            goal.updateDetails(null, null, null, null, null);

            assertThat(goal.getName()).isEqualTo(originalName);
            assertThat(goal.getTargetAmount()).isEqualByComparingTo(originalTarget);
            assertThat(goal.getDeadline()).isEqualTo(originalDeadline);
            assertThat(goal.getPriority()).isEqualTo(originalPriority);
        }

        @Test
        @DisplayName("Should set link to null when link is blank")
        void shouldSetLinkToNullWhenBlank() {
            SavingsGoal goal = SavingsGoal.create(
                    USER_ID,
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_DEADLINE,
                    GoalPriority.LOW,
                    "https://example.com"
            );

            goal.updateDetails(null, null, null, null, "   ");

            assertThat(goal.getLink()).isNull();
        }

        @Test
        @DisplayName("Should reject invalid name on update")
        void shouldRejectInvalidNameOnUpdate() {
            SavingsGoal goal = createValidGoal();

            assertThatThrownBy(() ->
                    goal.updateDetails("A".repeat(101), null, null, null, null)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject non-positive target amount on update")
        void shouldRejectNonPositiveTargetAmountOnUpdate() {
            SavingsGoal goal = createValidGoal();

            assertThatThrownBy(() ->
                    goal.updateDetails(null, BigDecimal.ZERO, null, null, null)
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject deadline not after today on update")
        void shouldRejectDeadlineNotAfterTodayOnUpdate() {
            SavingsGoal goal = createValidGoal();

            assertThatThrownBy(() ->
                    goal.updateDetails(null, null, LocalDate.now(), null, null)
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("addContribution")
    class AddContribution {

        @Test
        @DisplayName("Should add contribution to current amount")
        void shouldAddContribution() {
            SavingsGoal goal = createValidGoal();
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.addContribution(new BigDecimal("100.00"));

            assertThat(goal.getCurrentAmount()).isEqualByComparingTo("100.00");
            assertThat(goal.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should accumulate multiple contributions")
        void shouldAccumulateMultipleContributions() {
            SavingsGoal goal = createValidGoal();

            goal.addContribution(new BigDecimal("50.00"));
            goal.addContribution(new BigDecimal("75.50"));

            assertThat(goal.getCurrentAmount()).isEqualByComparingTo("125.50");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive contribution")
        void shouldRejectNonPositiveContribution(String amount) {
            SavingsGoal goal = createValidGoal();

            assertThatThrownBy(() ->
                    goal.addContribution(new BigDecimal(amount))
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null contribution")
        void shouldRejectNullContribution() {
            SavingsGoal goal = createValidGoal();

            assertThatThrownBy(() -> goal.addContribution(null))
                    .isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("Should withdraw amount from current amount")
        void shouldWithdrawAmount() {
            SavingsGoal goal = createValidGoal();
            goal.addContribution(new BigDecimal("100.00"));
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.withdraw(new BigDecimal("30.00"));

            assertThat(goal.getCurrentAmount()).isEqualByComparingTo("70.00");
            assertThat(goal.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive withdrawal")
        void shouldRejectNonPositiveWithdrawal(String amount) {
            SavingsGoal goal = createValidGoal();
            goal.addContribution(new BigDecimal("100.00"));

            assertThatThrownBy(() -> goal.withdraw(new BigDecimal(amount)))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null withdrawal")
        void shouldRejectNullWithdrawal() {
            SavingsGoal goal = createValidGoal();
            goal.addContribution(new BigDecimal("100.00"));

            assertThatThrownBy(() -> goal.withdraw(null))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject withdrawal exceeding current amount")
        void shouldRejectWithdrawalExceedingCurrentAmount() {
            SavingsGoal goal = createValidGoal();
            goal.addContribution(new BigDecimal("50.00"));

            assertThatThrownBy(() -> goal.withdraw(new BigDecimal("50.01")))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow withdrawal equal to current amount")
        void shouldAllowWithdrawalEqualToCurrentAmount() {
            SavingsGoal goal = createValidGoal();
            goal.addContribution(new BigDecimal("50.00"));

            goal.withdraw(new BigDecimal("50.00"));

            assertThat(goal.getCurrentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate an active goal")
        void shouldDeactivateActiveGoal() {
            SavingsGoal goal = reconstituteValidGoal(true);
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.deactivate();

            assertThat(goal.isActive()).isFalse();
            assertThat(goal.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not change modified at when already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            SavingsGoal goal = reconstituteValidGoal(false);
            LocalDateTime previousModifiedAt = goal.getModifiedAt();

            goal.deactivate();

            assertThat(goal.isActive()).isFalse();
            assertThat(goal.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }

    @Nested
    @DisplayName("link normalization and validation")
    class LinkValidation {

        @ParameterizedTest
        @CsvSource({
                "example.com, https://example.com",
                "www.example.com, https://www.example.com",
                "http://example.com, http://example.com",
                "https://example.com, https://example.com",
                "HTTPS://EXAMPLE.COM, HTTPS://EXAMPLE.COM",
                "https://example.com/path?q=1, https://example.com/path?q=1"
        })
        @DisplayName("Should normalize and accept valid links")
        void shouldNormalizeAndAcceptValidLinks(String input, String expected) {
            SavingsGoal goal = SavingsGoal.create(
                    USER_ID,
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_DEADLINE,
                    GoalPriority.LOW,
                    input
            );

            assertThat(goal.getLink()).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should set link to null when link is blank")
        void shouldSetLinkToNullWhenBlank() {
            SavingsGoal goal = SavingsGoal.create(
                    USER_ID,
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_DEADLINE,
                    GoalPriority.LOW,
                    "   "
            );

            assertThat(goal.getLink()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ftp://example.com",
                "https://",
                "http://[invalid",
                "not a url with spaces"
        })
        @DisplayName("Should reject invalid links")
        void shouldRejectInvalidLinks(String link) {
            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            link
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject link exceeding max length")
        void shouldRejectLinkExceedingMaxLength() {
            String longLink = "https://" + "a".repeat(493);

            assertThatThrownBy(() ->
                    SavingsGoal.create(
                            USER_ID,
                            VALID_NAME,
                            VALID_TARGET,
                            VALID_DEADLINE,
                            GoalPriority.LOW,
                            longLink
                    )
            ).isInstanceOf(InvalidInputException.class);
        }
    }
}