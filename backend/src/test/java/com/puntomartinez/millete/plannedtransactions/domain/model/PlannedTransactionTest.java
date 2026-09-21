package com.puntomartinez.millete.plannedtransactions.domain.model;

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
    private static final String VALID_DESCRIPTION = "Rent";

    private PlannedTransaction createValid() {
        return PlannedTransaction.create(
                USER_ID,
                CATEGORY_ID,
                VALID_AMOUNT,
                TransactionType.EXPENSE,
                VALID_DESCRIPTION,
                PlannedTransaction.FrequencyType.MONTHS,
                1,
                LocalDate.now(),
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
                PlannedTransaction.FrequencyType.MONTHS,
                1,
                LocalDate.now(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
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
            PlannedTransaction pt = createValid();
            assertThat(pt.getId()).isNotNull();
            assertThat(pt.getUserId()).isEqualTo(USER_ID);
            assertThat(pt.getCategoryId()).isEqualTo(CATEGORY_ID);
            assertThat(pt.getAmount()).isEqualByComparingTo(VALID_AMOUNT);
            assertThat(pt.getType()).isEqualTo(TransactionType.EXPENSE);
            assertThat(pt.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(pt.getFrequencyType()).isEqualTo(PlannedTransaction.FrequencyType.MONTHS);
            assertThat(pt.getFrequencyInterval()).isEqualTo(1);
            assertThat(pt.isActive()).isTrue();
            assertThat(pt.getLastExecutedDate()).isNull();
            assertThat(pt.getFailureCount()).isZero();
            assertThat(pt.getCreatedAt()).isNotNull();
            assertThat(pt.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should generate different ids for different planned transactions")
        void shouldGenerateDifferentIds() {
            PlannedTransaction first = createValid();
            PlannedTransaction second = createValid();
            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        @DisplayName("Should allow null category id")
        void shouldAllowNullCategoryId() {
            PlannedTransaction pt = PlannedTransaction.create(
                    USER_ID, null, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );
            assertThat(pt.getCategoryId()).isNull();
        }

        @Test
        @DisplayName("Should allow end date")
        void shouldAllowEndDate() {
            LocalDate end = LocalDate.now().plusMonths(6);
            PlannedTransaction pt = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), end
            );
            assertThat(pt.getEndDate()).isEqualTo(end);
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    null, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, null,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive amount")
        void shouldRejectNonPositiveAmount(String amount) {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, new BigDecimal(amount),
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    null, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank description")
        void shouldRejectBlankDescription(String description) {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, description,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject description exceeding 50 chars")
        void shouldRejectDescriptionExceeding50Chars() {
            String longDescription = "A".repeat(51);
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, longDescription,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow description at exactly 50 chars")
        void shouldAllowDescriptionAt50Chars() {
            String maxDescription = "A".repeat(50);
            PlannedTransaction pt = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, maxDescription,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null
            );
            assertThat(pt.getDescription()).hasSize(50);
        }

        @Test
        @DisplayName("Should reject null frequency type")
        void shouldRejectNullFrequencyType() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    null, 1,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null frequency interval")
        void shouldRejectNullFrequencyInterval() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, null,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Should reject non-positive frequency interval")
        void shouldRejectNonPositiveFrequencyInterval(int interval) {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, interval,
                    LocalDate.now(), null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null start date")
        void shouldRejectNullStartDate() {
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    null, null
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject end date before start date")
        void shouldRejectEndDateBeforeStartDate() {
            LocalDate start = LocalDate.now().plusDays(10);
            LocalDate end = LocalDate.now().plusDays(5);
            assertThatThrownBy(() -> PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    start, end
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {
        @Test
        @DisplayName("Should reconstitute existing planned transaction")
        void shouldReconstitute() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
            LocalDate lastExecuted = LocalDate.now().minusDays(30);
            PlannedTransaction pt = PlannedTransaction.reconstitute(
                    id, USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null,
                    createdAt, modifiedAt, true, lastExecuted, 2
            );
            assertThat(pt.getId()).isEqualTo(id);
            assertThat(pt.getLastExecutedDate()).isEqualTo(lastExecuted);
            assertThat(pt.getFailureCount()).isEqualTo(2);
            assertThat(pt.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullId() {
            assertThatThrownBy(() -> PlannedTransaction.reconstitute(
                    null, USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null,
                    LocalDateTime.now(), LocalDateTime.now(), true, null, 0
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null created at on reconstitute")
        void shouldRejectNullCreatedAt() {
            assertThatThrownBy(() -> PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null,
                    null, LocalDateTime.now(), true, null, 0
            )).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null modified at on reconstitute")
        void shouldRejectNullModifiedAt() {
            assertThatThrownBy(() -> PlannedTransaction.reconstitute(
                    UUID.randomUUID(), USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    LocalDate.now(), null,
                    LocalDateTime.now(), null, true, null, 0
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {
        @Test
        @DisplayName("Should update all details")
        void shouldUpdateAllDetails() {
            PlannedTransaction pt = reconstituteValid(true);
            LocalDateTime previousModifiedAt = pt.getModifiedAt();
            UUID newCategoryId = UUID.randomUUID();
            pt.updateDetails(
                    new BigDecimal("200.00"),
                    TransactionType.INCOME,
                    "Updated",
                    PlannedTransaction.FrequencyType.WEEKS,
                    2,
                    newCategoryId
            );
            assertThat(pt.getAmount()).isEqualByComparingTo("200.00");
            assertThat(pt.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(pt.getDescription()).isEqualTo("Updated");
            assertThat(pt.getFrequencyType()).isEqualTo(PlannedTransaction.FrequencyType.WEEKS);
            assertThat(pt.getFrequencyInterval()).isEqualTo(2);
            assertThat(pt.getCategoryId()).isEqualTo(newCategoryId);
            assertThat(pt.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }

        @Test
        @DisplayName("Should allow setting category id to null on update")
        void shouldAllowNullCategoryIdOnUpdate() {
            PlannedTransaction pt = createValid();
            pt.updateDetails(
                    VALID_AMOUNT, TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1, null
            );
            assertThat(pt.getCategoryId()).isNull();
        }

        @Test
        @DisplayName("Should reject invalid amount on update")
        void shouldRejectInvalidAmountOnUpdate() {
            PlannedTransaction pt = createValid();
            assertThatThrownBy(() -> pt.updateDetails(
                    BigDecimal.ZERO, TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1, null
            )).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("markAsExecuted")
    class MarkAsExecuted {
        @Test
        @DisplayName("Should mark as executed with valid date")
        void shouldMarkAsExecuted() {
            PlannedTransaction pt = createValid();
            LocalDateTime previousModifiedAt = pt.getModifiedAt();
            LocalDate executionDate = LocalDate.now();
            pt.markAsExecuted(executionDate);
            assertThat(pt.getLastExecutedDate()).isEqualTo(executionDate);
            assertThat(pt.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }

        @Test
        @DisplayName("Should reject null execution date")
        void shouldRejectNullExecutionDate() {
            PlannedTransaction pt = createValid();
            assertThatThrownBy(() -> pt.markAsExecuted(null))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject execution date before start date")
        void shouldRejectExecutionBeforeStartDate() {
            LocalDate start = LocalDate.now().plusDays(10);
            PlannedTransaction pt = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    start, null
            );
            assertThatThrownBy(() -> pt.markAsExecuted(LocalDate.now()))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject execution date after end date")
        void shouldRejectExecutionAfterEndDate() {
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusDays(10);
            PlannedTransaction pt = PlannedTransaction.create(
                    USER_ID, CATEGORY_ID, VALID_AMOUNT,
                    TransactionType.EXPENSE, VALID_DESCRIPTION,
                    PlannedTransaction.FrequencyType.MONTHS, 1,
                    start, end
            );
            assertThatThrownBy(() -> pt.markAsExecuted(end.plusDays(1)))
                    .isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("incrementFailureCount")
    class IncrementFailureCount {
        @Test
        @DisplayName("Should increment failure count and return false below max")
        void shouldIncrementAndReturnFalse() {
            PlannedTransaction pt = createValid();
            boolean shouldDeactivate = pt.incrementFailureCount();
            assertThat(pt.getFailureCount()).isEqualTo(1);
            assertThat(shouldDeactivate).isFalse();
            assertThat(pt.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should return true when reaching MAX_CONSECUTIVE_FAILURES")
        void shouldReturnTrueAtMax() {
            PlannedTransaction pt = createValid();
            for (int i = 0; i < PlannedTransaction.MAX_CONSECUTIVE_FAILURES - 1; i++) {
                pt.incrementFailureCount();
            }
            boolean shouldDeactivate = pt.incrementFailureCount();
            assertThat(shouldDeactivate).isTrue();
            assertThat(pt.getFailureCount())
                    .isEqualTo(PlannedTransaction.MAX_CONSECUTIVE_FAILURES);
        }
    }

    @Nested
    @DisplayName("resetFailureCount")
    class ResetFailureCount {
        @Test
        @DisplayName("Should reset failure count when above zero")
        void shouldResetWhenAboveZero() {
            PlannedTransaction pt = createValid();
            pt.incrementFailureCount();
            pt.incrementFailureCount();
            LocalDateTime beforeReset = pt.getModifiedAt();
            pt.resetFailureCount();
            assertThat(pt.getFailureCount()).isZero();
        }

        @Test
        @DisplayName("Should not modify when already zero")
        void shouldNotModifyWhenAlreadyZero() {
            PlannedTransaction pt = createValid();
            LocalDateTime beforeReset = pt.getModifiedAt();
            pt.resetFailureCount();
            assertThat(pt.getFailureCount()).isZero();
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {
        @Test
        @DisplayName("Should deactivate an active planned transaction")
        void shouldDeactivateActive() {
            PlannedTransaction pt = reconstituteValid(true);
            LocalDateTime previousModifiedAt = pt.getModifiedAt();
            pt.deactivate();
            assertThat(pt.isActive()).isFalse();
            assertThat(pt.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }
    }
}