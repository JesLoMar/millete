package com.puntomartinez.millete.transactions.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transaction aggregate")
class TransactionTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("100.00");
    private static final LocalDateTime VALID_DATE = Instant.now();
    private static final String VALID_DESCRIPTION = "Groceries";

    private Transaction createValidTransaction() {
        return Transaction.create(
                USER_ID,
                CATEGORY_ID,
                VALID_AMOUNT,
                VALID_DATE,
                TransactionType.EXPENSE,
                VALID_DESCRIPTION
        );
    }

    private Transaction reconstituteValidTransaction(boolean active) {
        return Transaction.reconstitute(
                UUID.randomUUID(),
                USER_ID,
                CATEGORY_ID,
                VALID_AMOUNT,
                VALID_DATE,
                TransactionType.EXPENSE,
                VALID_DESCRIPTION,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0),
                active
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create a valid active transaction")
        void shouldCreateValidTransaction() {
            Transaction transaction = createValidTransaction();

            assertThat(transaction.getId()).isNotNull();
            assertThat(transaction.getUserId()).isEqualTo(USER_ID);
            assertThat(transaction.getCategoryId()).isEqualTo(CATEGORY_ID);
            assertThat(transaction.getAmount()).isEqualByComparingTo(VALID_AMOUNT);
            assertThat(transaction.getDate()).isEqualTo(VALID_DATE);
            assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
            assertThat(transaction.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(transaction.getCreatedAt()).isNotNull();
            assertThat(transaction.getModifiedAt()).isNotNull();
            assertThat(transaction.getCreatedAt()).isEqualTo(transaction.getModifiedAt());
            assertThat(transaction.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should generate different ids for different transactions")
        void shouldGenerateDifferentIds() {
            Transaction first = createValidTransaction();
            Transaction second = createValidTransaction();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        @DisplayName("Should allow null category id")
        void shouldAllowNullCategoryId() {
            Transaction transaction = Transaction.create(
                    USER_ID,
                    null,
                    VALID_AMOUNT,
                    VALID_DATE,
                    TransactionType.EXPENSE,
                    VALID_DESCRIPTION
            );

            assertThat(transaction.getCategoryId()).isNull();
        }

        @Test
        @DisplayName("Should allow INCOME type")
        void shouldAllowIncomeType() {
            Transaction transaction = Transaction.create(
                    USER_ID,
                    null,
                    VALID_AMOUNT,
                    VALID_DATE,
                    TransactionType.INCOME,
                    VALID_DESCRIPTION
            );

            assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() ->
                    Transaction.create(
                            null,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            assertThatThrownBy(() ->
                    Transaction.create(
                            USER_ID,
                            CATEGORY_ID,
                            null,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive amount")
        void shouldRejectNonPositiveAmount(String amount) {
            assertThatThrownBy(() ->
                    Transaction.create(
                            USER_ID,
                            CATEGORY_ID,
                            new BigDecimal(amount),
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null date")
        void shouldRejectNullDate() {
            assertThatThrownBy(() ->
                    Transaction.create(
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            null,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() ->
                    Transaction.create(
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            null,
                            VALID_DESCRIPTION
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank description")
        void shouldRejectBlankDescription(String description) {
            assertThatThrownBy(() ->
                    Transaction.create(
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            description
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject description exceeding max length")
        void shouldRejectDescriptionExceedingMaxLength() {
            String longDescription = "A".repeat(51);

            assertThatThrownBy(() ->
                    Transaction.create(
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            longDescription
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should allow description at max length")
        void shouldAllowDescriptionAtMaxLength() {
            String maxDescription = "A".repeat(50);

            Transaction transaction = Transaction.create(
                    USER_ID,
                    CATEGORY_ID,
                    VALID_AMOUNT,
                    VALID_DATE,
                    TransactionType.EXPENSE,
                    maxDescription
            );

            assertThat(transaction.getDescription()).isEqualTo(maxDescription);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute an existing transaction")
        void shouldReconstituteTransaction() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 12, 30);

            Transaction transaction = Transaction.reconstitute(
                    id,
                    USER_ID,
                    CATEGORY_ID,
                    new BigDecimal("250.50"),
                    VALID_DATE,
                    TransactionType.INCOME,
                    "Salary",
                    createdAt,
                    modifiedAt,
                    false
            );

            assertThat(transaction.getId()).isEqualTo(id);
            assertThat(transaction.getUserId()).isEqualTo(USER_ID);
            assertThat(transaction.getCategoryId()).isEqualTo(CATEGORY_ID);
            assertThat(transaction.getAmount()).isEqualByComparingTo("250.50");
            assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(transaction.getDescription()).isEqualTo("Salary");
            assertThat(transaction.getCreatedAt()).isEqualTo(createdAt);
            assertThat(transaction.getModifiedAt()).isEqualTo(modifiedAt);
            assertThat(transaction.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

            assertThatThrownBy(() ->
                    Transaction.reconstitute(
                            null,
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION,
                            createdAt,
                            modifiedAt,
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null created at on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            assertThatThrownBy(() ->
                    Transaction.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION,
                            null,
                            Instant.now(),
                            true
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject null modified at on reconstitute")
        void shouldRejectNullModifiedAtOnReconstitute() {
            assertThatThrownBy(() ->
                    Transaction.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            CATEGORY_ID,
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION,
                            Instant.now(),
                            null,
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
            Transaction transaction = reconstituteValidTransaction(true);
            UUID newCategoryId = UUID.randomUUID();
            LocalDateTime previousModifiedAt = transaction.getModifiedAt();

            transaction.updateDetails(
                    new BigDecimal("200.00"),
                    Instant.now(),
                    TransactionType.INCOME,
                    "Updated",
                    newCategoryId
            );

            assertThat(transaction.getAmount()).isEqualByComparingTo("200.00");
            assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
            assertThat(transaction.getDescription()).isEqualTo("Updated");
            assertThat(transaction.getCategoryId()).isEqualTo(newCategoryId);
            assertThat(transaction.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should allow setting category id to null on update")
        void shouldAllowSettingCategoryIdToNullOnUpdate() {
            Transaction transaction = reconstituteValidTransaction(true);

            transaction.updateDetails(
                    VALID_AMOUNT,
                    VALID_DATE,
                    TransactionType.EXPENSE,
                    VALID_DESCRIPTION,
                    null
            );

            assertThat(transaction.getCategoryId()).isNull();
        }

        @Test
        @DisplayName("Should reject invalid amount on update")
        void shouldRejectInvalidAmountOnUpdate() {
            Transaction transaction = createValidTransaction();

            assertThatThrownBy(() ->
                    transaction.updateDetails(
                            BigDecimal.ZERO,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            VALID_DESCRIPTION,
                            CATEGORY_ID
                    )
            ).isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should reject invalid description on update")
        void shouldRejectInvalidDescriptionOnUpdate() {
            Transaction transaction = createValidTransaction();

            assertThatThrownBy(() ->
                    transaction.updateDetails(
                            VALID_AMOUNT,
                            VALID_DATE,
                            TransactionType.EXPENSE,
                            "",
                            CATEGORY_ID
                    )
            ).isInstanceOf(InvalidInputException.class);
        }
    }

    @Nested
    @DisplayName("unassignCategory")
    class UnassignCategory {

        @Test
        @DisplayName("Should set category id to null and update modified at")
        void shouldSetCategoryIdToNullAndUpdateModifiedAt() {
            Transaction transaction = reconstituteValidTransaction(true);
            LocalDateTime previousModifiedAt = transaction.getModifiedAt();

            transaction.unassignCategory();

            assertThat(transaction.getCategoryId()).isNull();
            assertThat(transaction.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not change modified at when category is already null")
        void shouldNotChangeModifiedAtWhenCategoryIsNull() {
            Transaction transaction = Transaction.create(
                    USER_ID,
                    null,
                    VALID_AMOUNT,
                    VALID_DATE,
                    TransactionType.EXPENSE,
                    VALID_DESCRIPTION
            );
            LocalDateTime previousModifiedAt = transaction.getModifiedAt();

            transaction.unassignCategory();

            assertThat(transaction.getCategoryId()).isNull();
            assertThat(transaction.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate an active transaction")
        void shouldDeactivateActiveTransaction() {
            Transaction transaction = reconstituteValidTransaction(true);
            LocalDateTime previousModifiedAt = transaction.getModifiedAt();

            transaction.deactivate();

            assertThat(transaction.isActive()).isFalse();
            assertThat(transaction.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not change modified at when already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            Transaction transaction = reconstituteValidTransaction(false);
            LocalDateTime previousModifiedAt = transaction.getModifiedAt();

            transaction.deactivate();

            assertThat(transaction.isActive()).isFalse();
            assertThat(transaction.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }
}