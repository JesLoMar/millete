package com.puntomartinez.millete.plannedtransactions.domain.model;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PlannedTransaction aggregate")
class PlannedTransactionTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("100.00");
    private static final String VALID_DESCRIPTION = "Monthly rent";
    private static final LocalDate VALID_START_DATE = LocalDate.now();

    private PlannedTransaction createValid() {
        return PlannedTransaction.create(
                USER_ID,
                CATEGORY_ID,
                VALID_AMOUNT,
                TransactionType.EXPENSE,
                VALID_DESCRIPTION,
                FrequencyType.MONTHS,
                1,
                VALID_START_DATE,
                null
        );
    }

    private PlannedTransaction reconstituteValid(boolean active) {
        return PlannedTransaction.reconstitute(
                UUID.randomUUID(),
                USER_ID,
                CATEGORY_ID,
                VALID_AMOUNT,
                TransactionType.EXPENSE,
                VALID_DESCRIPTION,
                FrequencyType.MONTHS,
                1,
                VALID_START_DATE,
                null,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0),
                active,
                null,
                0
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create a valid active planned transaction")
        void shouldCreateValidPlannedTransaction() {
            PlannedTransaction ptx = createValid();

            assertThat(ptx.getId()).isNotNull();
            assertThat(ptx.getUserId()).isEqualTo(USER_ID);
            assertThat(ptx.getCategoryId()).isEqualTo(CATEGORY_ID);
            assertThat(ptx.getAmount()).isEqualByComparingTo(VALID_AMOUNT);
            assertThat(ptx.getType()).isEqualTo(TransactionType.EXPENSE);
            assertThat(ptx.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(ptx.getFrequencyType()).isEqualTo(FrequencyType.MONTHS);
            assertThat(ptx.getFrequencyInterval()).isEqualTo(1);
            assertThat(ptx.getStartDate()).isEqualTo(VALID_START_DATE);
            assertThat(ptx.getEndDate()).isNull();
            assertThat(ptx.getLastExecutedDate()).isNull();
            assertThat(ptx.getFailureCount()).isZero();
            assertThat(ptx.isActive()).isTrue();
            assertThat(ptx.getCreatedAt()).isNotNull();
            assertThat(ptx.getModifiedAt()).isNotNull();
            assertThat(ptx.getCreatedAt()).isEqualTo(ptx.getModifiedAt());
        }

        @Test
        @DisplayName("Should allow null category id")
        void shouldAllowNullCategoryId() {
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, null, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            );

            assertThat(ptx.getCategoryId()).isNull();
        }

        @Test
        @DisplayName("Should allow null end date")
        void shouldAllowNullEndDate() {
            PlannedTransaction ptx = createValid();

            assertThat(ptx.getEndDate()).isNull();
        }

        @Test
        @DisplayName("Should allow end date")
        void shouldAllowEndDate() {
            LocalDate endDate = VALID_START_DATE.plusMonths(12);
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, endDate
            );

            assertThat(ptx.getEndDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    null, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, null, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive amount")
        void shouldRejectNonPositiveAmount(String amount) {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, new BigDecimal(amount),
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, null,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank description")
        void shouldRejectBlankDescription(String description) {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    description, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject description exceeding max length")
        void shouldRejectDescriptionExceedingMaxLength() {
            String longDescription = "A".repeat(51);

            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    longDescription, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow description at max length")
        void shouldAllowDescriptionAtMaxLength() {
            String maxDescription = "A".repeat(50);

            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    maxDescription, FrequencyType.MONTHS, 1,
                    VALID_START_DATE, null
            );

            assertThat(ptx.getDescription()).isEqualTo(maxDescription);
        }

        @Test
        @DisplayName("Should reject null frequency type")
        void shouldRejectNullFrequencyType() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, null, 1,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null frequency interval")
        void shouldRejectNullFrequencyInterval() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, null,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Should reject non-positive frequency interval")
        void shouldRejectNonPositiveFrequencyInterval(int interval) {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, interval,
                    VALID_START_DATE, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null start date")
        void shouldRejectNullStartDate() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    null, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject end date before start date")
        void shouldRejectEndDateBeforeStartDate() {
            LocalDate startDate = LocalDate.now().plusDays(10);
            LocalDate endDate = LocalDate.now().plusDays(5);

            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    startDate, endDate
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute an existing planned transaction")
        void shouldReconstitutePlannedTransaction() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
            LocalDate lastExecuted = LocalDate.of(2024, 2, 1);

            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    id, USER_ID, CATEGORY_ID,
                    new BigDecimal("200.00"),
                    TransactionType.INCOME,
                    "Salary",
                    FrequencyType.MONTHS,
                    1,
                    VALID_START_DATE,
                    null,
                    createdAt,
                    modifiedAt,
                    false,
                    lastExecuted,
                    2
            );

            assertThat(ptx.getId()).isEqualTo(id);
            assertThat(ptx.getUserId()).isEqualTo(USER_ID);
            assertThat(ptx.getAmount()).isEqualByComparingTo("200.00");
            assertThat(ptx.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(ptx.isActive()).isFalse();
            assertThat(ptx.getLastExecutedDate()).isEqualTo(lastExecuted);
            assertThat(ptx.getFailureCount()).isEqualTo(2);
            assertThat(ptx.getCreatedAt()).isEqualTo(createdAt);
            assertThat(ptx.getModifiedAt()).isEqualTo(modifiedAt);
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() -> PlannedTransaction.reconstitute(
                    null, USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null,
                    LocalDateTime.now(), LocalDateTime.now(), true, null, 0
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null created at on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            assertThatThrownBy(() -> PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null,
                    null, LocalDateTime.now(), true, null, 0
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null modified at on reconstitute")
        void shouldRejectNullModifiedAtOnReconstitute() {
            assertThatThrownBy(() -> PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null,
                    LocalDateTime.now(), null, true, null, 0
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update all provided fields")
        void shouldUpdateAllProvidedFields() {
            PlannedTransaction ptx = reconstituteValid(true);
            UUID newCategoryId = UUID.randomUUID();
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            ptx.updateDetails(
                    new BigDecimal("300.00"),
                    TransactionType.INCOME,
                    "Updated description",
                    FrequencyType.WEEKS,
                    2,
                    newCategoryId
            );

            assertThat(ptx.getAmount()).isEqualByComparingTo("300.00");
            assertThat(ptx.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(ptx.getDescription()).isEqualTo("Updated description");
            assertThat(ptx.getFrequencyType()).isEqualTo(FrequencyType.WEEKS);
            assertThat(ptx.getFrequencyInterval()).isEqualTo(2);
            assertThat(ptx.getCategoryId()).isEqualTo(newCategoryId);
            assertThat(ptx.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should allow setting category id to null on update")
        void shouldAllowSettingCategoryIdToNull() {
            PlannedTransaction ptx = reconstituteValid(true);

            ptx.updateDetails(
                    VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1, null
            );

            assertThat(ptx.getCategoryId()).isNull();
        }

        @Test
        @DisplayName("Should reject invalid amount on update")
        void shouldRejectInvalidAmountOnUpdate() {
            PlannedTransaction ptx = createValid();

            assertThatThrownBy(() -> ptx.updateDetails(
                    BigDecimal.ZERO, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1, CATEGORY_ID
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject invalid description on update")
        void shouldRejectInvalidDescriptionOnUpdate() {
            PlannedTransaction ptx = createValid();

            assertThatThrownBy(() -> ptx.updateDetails(
                    VALID_AMOUNT, TransactionType.EXPENSE,
                    "", FrequencyType.MONTHS, 1, CATEGORY_ID
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject invalid frequency type on update")
        void shouldRejectInvalidFrequencyTypeOnUpdate() {
            PlannedTransaction ptx = createValid();

            assertThatThrownBy(() -> ptx.updateDetails(
                    VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, null, 1, CATEGORY_ID
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject invalid frequency interval on update")
        void shouldRejectInvalidFrequencyIntervalOnUpdate() {
            PlannedTransaction ptx = createValid();

            assertThatThrownBy(() -> ptx.updateDetails(
                    VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 0, CATEGORY_ID
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("markAsExecuted")
    class MarkAsExecuted {

        @Test
        @DisplayName("Should set last executed date and update modified at")
        void shouldSetLastExecutedDateAndUpdateModifiedAt() {
            PlannedTransaction ptx = reconstituteValid(true);
            LocalDate executionDate = LocalDate.now();
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            ptx.markAsExecuted(executionDate);

            assertThat(ptx.getLastExecutedDate()).isEqualTo(executionDate);
            assertThat(ptx.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should reject null execution date")
        void shouldRejectNullExecutionDate() {
            PlannedTransaction ptx = createValid();

            assertThatThrownBy(() -> ptx.markAsExecuted(null))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject execution date before start date")
        void shouldRejectExecutionDateBeforeStartDate() {
            LocalDate startDate = LocalDate.now().plusDays(10);
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    startDate, null
            );

            assertThatThrownBy(() -> ptx.markAsExecuted(startDate.minusDays(1)))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject execution date after end date")
        void shouldRejectExecutionDateAfterEndDate() {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(10);
            PlannedTransaction ptx = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT, TransactionType.EXPENSE,
                    VALID_DESCRIPTION, FrequencyType.MONTHS, 1,
                    startDate, endDate
            );

            assertThatThrownBy(() -> ptx.markAsExecuted(endDate.plusDays(1)))
                    .isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("incrementFailureCount")
    class IncrementFailureCount {

        @Test
        @DisplayName("Should increment failure count and update modified at")
        void shouldIncrementFailureCount() {
            PlannedTransaction ptx = reconstituteValid(true);
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            boolean shouldDeactivate = ptx.incrementFailureCount();

            assertThat(ptx.getFailureCount()).isEqualTo(1);
            assertThat(shouldDeactivate).isFalse();
            assertThat(ptx.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should return true when max consecutive failures is reached")
        void shouldReturnTrueWhenMaxFailuresReached() {
            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null,
                    LocalDateTime.now(), LocalDateTime.now(), true, null,
                    PlannedTransaction.MAX_CONSECUTIVE_FAILURES - 1
            );

            boolean shouldDeactivate = ptx.incrementFailureCount();

            assertThat(ptx.getFailureCount())
                    .isEqualTo(PlannedTransaction.MAX_CONSECUTIVE_FAILURES);
            assertThat(shouldDeactivate).isTrue();
        }

        @Test
        @DisplayName("Should return true when exceeding max consecutive failures")
        void shouldReturnTrueWhenExceedingMaxFailures() {
            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null,
                    LocalDateTime.now(), LocalDateTime.now(), true, null,
                    PlannedTransaction.MAX_CONSECUTIVE_FAILURES
            );

            boolean shouldDeactivate = ptx.incrementFailureCount();

            assertThat(shouldDeactivate).isTrue();
        }
    }

    @Nested
    @DisplayName("resetFailureCount")
    class ResetFailureCount {

        @Test
        @DisplayName("Should reset failure count to zero and update modified at")
        void shouldResetFailureCountToZero() {
            PlannedTransaction ptx = PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    FrequencyType.MONTHS, 1, VALID_START_DATE, null,
                    LocalDateTime.now(), LocalDateTime.now(), true, null, 2
            );
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            ptx.resetFailureCount();

            assertThat(ptx.getFailureCount()).isZero();
            assertThat(ptx.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not update modified at when failure count is already zero")
        void shouldNotUpdateModifiedAtWhenAlreadyZero() {
            PlannedTransaction ptx = createValid();
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            ptx.resetFailureCount();

            assertThat(ptx.getFailureCount()).isZero();
            assertThat(ptx.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate an active planned transaction")
        void shouldDeactivateActivePlannedTransaction() {
            PlannedTransaction ptx = reconstituteValid(true);
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            ptx.deactivate();

            assertThat(ptx.isActive()).isFalse();
            assertThat(ptx.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not change modified at when already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            PlannedTransaction ptx = reconstituteValid(false);
            LocalDateTime previousModifiedAt = ptx.getModifiedAt();

            ptx.deactivate();

            assertThat(ptx.isActive()).isFalse();
            assertThat(ptx.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }
}