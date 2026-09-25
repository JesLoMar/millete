package com.puntomartinez.millete.categories.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Category aggregate")
class CategoryTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VALID_NAME = "Food";
    private static final String VALID_COLOR = "#FF5733";
    private static final FixedTimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));

    private Category createValidCategory() {
        return Category.create(TIME, 
                TIME,
                USER_ID,
                VALID_NAME,
                VALID_COLOR,
                new BigDecimal("100.00")
        );
    }

    private Category reconstituteValidCategory(boolean active) {
        return Category.reconstitute(
                UUID.randomUUID(),
                USER_ID,
                VALID_NAME,
                VALID_COLOR,
                new BigDecimal("100.00"),
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-02T10:00:00Z"),
                active
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create a valid active category")
        void shouldCreateValidCategory() {
            Category category = createValidCategory();

            assertThat(category.getId()).isNotNull();
            assertThat(category.getUserId()).isEqualTo(USER_ID);
            assertThat(category.getName()).isEqualTo(VALID_NAME);
            assertThat(category.getColor()).isEqualTo(VALID_COLOR);
            assertThat(category.getBudgetLimit()).isEqualByComparingTo("100.00");
            assertThat(category.getCreatedAt()).isNotNull();
            assertThat(category.getModifiedAt()).isNotNull();
            assertThat(category.getCreatedAt()).isEqualTo(category.getModifiedAt());
            assertThat(category.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should generate different ids for different categories")
        void shouldGenerateDifferentIds() {
            Category first = createValidCategory();
            Category second = createValidCategory();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() ->
                    Category.create(TIME, TIME, null, VALID_NAME, VALID_COLOR, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "   ",
                "123456789012345678901"
        })
        @DisplayName("Should reject invalid name")
        void shouldRejectInvalidName(String name) {
            assertThatThrownBy(() ->
                    Category.create(TIME, TIME, USER_ID, name, VALID_COLOR, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null color")
        void shouldRejectNullColor() {
            assertThatThrownBy(() ->
                    Category.create(TIME, TIME, USER_ID, VALID_NAME, null, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "#GGGGGG",
                "#12345",
                "#1234567",
                "FF5733",
                "#12345G"
        })
        @DisplayName("Should reject invalid color")
        void shouldRejectInvalidColor(String color) {
            assertThatThrownBy(() ->
                    Category.create(TIME, TIME, USER_ID, VALID_NAME, color, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject negative budget limit")
        void shouldRejectNegativeBudgetLimit() {
            assertThatThrownBy(() ->
                    Category.create(TIME, TIME, USER_ID, VALID_NAME, VALID_COLOR, new BigDecimal("-0.01"))
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should allow null budget limit")
        void shouldAllowNullBudgetLimit() {
            Category category = Category.create(TIME, TIME, USER_ID, VALID_NAME, VALID_COLOR, null);

            assertThat(category.getBudgetLimit()).isNull();
        }

        @Test
        @DisplayName("Should allow zero budget limit")
        void shouldAllowZeroBudgetLimit() {
            Category category = Category.create(TIME, TIME, USER_ID, VALID_NAME, VALID_COLOR, BigDecimal.ZERO);

            assertThat(category.getBudgetLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute an existing category")
        void shouldReconstituteCategory() {
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
            Instant modifiedAt = Instant.parse("2024-01-02T12:30:00Z");
            BigDecimal budgetLimit = new BigDecimal("250.50");

            Category category = Category.reconstitute(
                    id,
                    USER_ID,
                    "Groceries",
                    "#00FF00",
                    budgetLimit,
                    createdAt,
                    modifiedAt,
                    false
            );

            assertThat(category.getId()).isEqualTo(id);
            assertThat(category.getUserId()).isEqualTo(USER_ID);
            assertThat(category.getName()).isEqualTo("Groceries");
            assertThat(category.getColor()).isEqualTo("#00FF00");
            assertThat(category.getBudgetLimit()).isEqualByComparingTo(budgetLimit);
            assertThat(category.getCreatedAt()).isEqualTo(createdAt);
            assertThat(category.getModifiedAt()).isEqualTo(modifiedAt);
            assertThat(category.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should reject null id")
        void shouldRejectNullId() {
            Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
            Instant modifiedAt = Instant.parse("2024-01-02T10:00:00Z");

            assertThatThrownBy(() ->
                    Category.reconstitute(
                            null,
                            USER_ID,
                            VALID_NAME,
                            VALID_COLOR,
                            BigDecimal.TEN,
                            createdAt,
                            modifiedAt,
                            true
                    )
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
            Instant modifiedAt = Instant.parse("2024-01-02T10:00:00Z");

            assertThatThrownBy(() ->
                    Category.reconstitute(
                            UUID.randomUUID(),
                            null,
                            VALID_NAME,
                            VALID_COLOR,
                            BigDecimal.TEN,
                            createdAt,
                            modifiedAt,
                            true
                    )
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null created at")
        void shouldRejectNullCreatedAt() {
            assertThatThrownBy(() ->
                    Category.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            VALID_NAME,
                            VALID_COLOR,
                            BigDecimal.TEN,
                            null,
                            Instant.parse("2024-01-02T10:00:00Z"),
                            true
                    )
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null modified at")
        void shouldRejectNullModifiedAt() {
            assertThatThrownBy(() ->
                    Category.reconstitute(
                            UUID.randomUUID(),
                            USER_ID,
                            VALID_NAME,
                            VALID_COLOR,
                            BigDecimal.TEN,
                            Instant.parse("2024-01-01T10:00:00Z"),
                            null,
                            true
                    )
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update valid details")
        void shouldUpdateValidDetails() {
            Category category = reconstituteValidCategory(true);
            Instant previousModifiedAt = category.getModifiedAt();

            category.updateDetails(TIME, "Updated", "#0000FF", new BigDecimal("20.00"));

            assertThat(category.getName()).isEqualTo("Updated");
            assertThat(category.getColor()).isEqualTo("#0000FF");
            assertThat(category.getBudgetLimit()).isEqualByComparingTo("20.00");
            assertThat(category.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "   ",
                "123456789012345678901"
        })
        @DisplayName("Should reject invalid name on update")
        void shouldRejectInvalidNameOnUpdate(String name) {
            Category category = createValidCategory();

            assertThatThrownBy(() ->
                    category.updateDetails(TIME, name, VALID_COLOR, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null color on update")
        void shouldRejectNullColorOnUpdate() {
            Category category = createValidCategory();

            assertThatThrownBy(() ->
                    category.updateDetails(TIME, VALID_NAME, null, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "#GGGGGG",
                "#12345",
                "#1234567",
                "FF5733",
                "#12345G"
        })
        @DisplayName("Should reject invalid color on update")
        void shouldRejectInvalidColorOnUpdate(String color) {
            Category category = createValidCategory();

            assertThatThrownBy(() ->
                    category.updateDetails(TIME, VALID_NAME, color, BigDecimal.TEN)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject negative budget limit on update")
        void shouldRejectNegativeBudgetLimitOnUpdate() {
            Category category = createValidCategory();

            assertThatThrownBy(() ->
                    category.updateDetails(TIME, VALID_NAME, VALID_COLOR, new BigDecimal("-0.01"))
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should allow null budget limit on update")
        void shouldAllowNullBudgetLimitOnUpdate() {
            Category category = createValidCategory();

            category.updateDetails(TIME, VALID_NAME, VALID_COLOR, null);

            assertThat(category.getBudgetLimit()).isNull();
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate an active category")
        void shouldDeactivateActiveCategory() {
            Category category = reconstituteValidCategory(true);
            Instant previousModifiedAt = category.getModifiedAt();

            category.deactivate(TIME);

            assertThat(category.isActive()).isFalse();
            assertThat(category.getModifiedAt()).isAfter(previousModifiedAt);
        }

        @Test
        @DisplayName("Should not change modified at when category is already inactive")
        void shouldNotChangeModifiedAtWhenAlreadyInactive() {
            Category category = reconstituteValidCategory(false);
            Instant previousModifiedAt = category.getModifiedAt();

            category.deactivate(TIME);

            assertThat(category.isActive()).isFalse();
            assertThat(category.getModifiedAt()).isEqualTo(previousModifiedAt);
        }
    }
}