package com.puntomartinez.millete.investments.domain.model;

import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Investment aggregate")
class InvestmentTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String VALID_ASSET_NAME = "Apple Inc.";
    private static final String VALID_TICKER = "AAPL";
    private static final BigDecimal VALID_QUANTITY = new BigDecimal("10");
    private static final BigDecimal VALID_PURCHASE_PRICE = new BigDecimal("150.00");
    private static final LocalDateTime VALID_PURCHASE_DATE = LocalDateTime.now().minusDays(30);

    private Investment createValidStock() {
        return Investment.create(
                USER_ID,
                VALID_ASSET_NAME,
                VALID_TICKER,
                VALID_QUANTITY,
                VALID_PURCHASE_PRICE,
                InvestmentType.STOCK,
                VALID_PURCHASE_DATE
        );
    }

    private Investment reconstituteValid(boolean active) {
        return Investment.reconstitute(
                UUID.randomUUID(),
                USER_ID,
                VALID_ASSET_NAME,
                VALID_TICKER,
                VALID_QUANTITY,
                VALID_PURCHASE_PRICE,
                new BigDecimal("180.00"),
                InvestmentType.STOCK,
                VALID_PURCHASE_DATE,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0),
                active
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create valid investment with current price equal to purchase price")
        void shouldCreateValidInvestment() {
            Investment investment = createValidStock();

            assertThat(investment.getId()).isNotNull();
            assertThat(investment.getUserId()).isEqualTo(USER_ID);
            assertThat(investment.getAssetName()).isEqualTo(VALID_ASSET_NAME);
            assertThat(investment.getTicker()).isEqualTo(VALID_TICKER);
            assertThat(investment.getQuantity()).isEqualByComparingTo(VALID_QUANTITY);
            assertThat(investment.getPurchasePrice()).isEqualByComparingTo(VALID_PURCHASE_PRICE);
            assertThat(investment.getCurrentPrice()).isEqualByComparingTo(VALID_PURCHASE_PRICE);
            assertThat(investment.getType()).isEqualTo(InvestmentType.STOCK);
            assertThat(investment.getPurchaseDate()).isEqualTo(VALID_PURCHASE_DATE);
            assertThat(investment.isActive()).isTrue();
            assertThat(investment.getCreatedAt()).isNotNull();
            assertThat(investment.getModifiedAt()).isNotNull();
            assertThat(investment.getCreatedAt()).isEqualTo(investment.getModifiedAt());
        }

        @Test
        @DisplayName("Should generate different ids for different investments")
        void shouldGenerateDifferentIds() {
            Investment first = createValidStock();
            Investment second = createValidStock();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        @DisplayName("Should reject null user id")
        void shouldRejectNullUserId() {
            assertThatThrownBy(() -> Investment.create(
                    null, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    InvestmentType.STOCK, VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should reject blank asset name")
        void shouldRejectBlankAssetName(String assetName) {
            assertThatThrownBy(() -> Investment.create(
                    USER_ID, assetName, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    InvestmentType.STOCK, VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive quantity")
        void shouldRejectNonPositiveQuantity(String quantity) {
            assertThatThrownBy(() -> Investment.create(
                    USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    new BigDecimal(quantity), VALID_PURCHASE_PRICE,
                    InvestmentType.STOCK, VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-100.00"})
        @DisplayName("Should reject non-positive purchase price")
        void shouldRejectNonPositivePurchasePrice(String price) {
            assertThatThrownBy(() -> Investment.create(
                    USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, new BigDecimal(price),
                    InvestmentType.STOCK, VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> Investment.create(
                    USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    null, VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null purchase date")
        void shouldRejectNullPurchaseDate() {
            assertThatThrownBy(() -> Investment.create(
                    USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    InvestmentType.STOCK, null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(value = InvestmentType.class, names = {"STOCK", "CRYPTO", "FUND"})
        @DisplayName("Should reject null ticker for types that require it")
        void shouldRejectNullTickerForTypesThatRequireIt(InvestmentType type) {
            assertThatThrownBy(() -> Investment.create(
                    USER_ID, VALID_ASSET_NAME, null,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    type, VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(value = InvestmentType.class, names = {"REAL_ESTATE", "OTHER"})
        @DisplayName("Should allow null ticker for types that do not require it")
        void shouldAllowNullTickerForTypesThatDoNotRequireIt(InvestmentType type) {
            Investment investment = Investment.create(
                    USER_ID, VALID_ASSET_NAME, null,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    type, VALID_PURCHASE_DATE
            );

            assertThat(investment.getTicker()).isNull();
        }

        @Test
        @DisplayName("Should normalize ticker to uppercase")
        void shouldNormalizeTickerToUppercase() {
            Investment investment = Investment.create(
                    USER_ID, VALID_ASSET_NAME, "aapl",
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    InvestmentType.STOCK, VALID_PURCHASE_DATE
            );

            assertThat(investment.getTicker()).isEqualTo("AAPL");
        }

        @Test
        @DisplayName("Should trim ticker whitespace")
        void shouldTrimTickerWhitespace() {
            Investment investment = Investment.create(
                    USER_ID, VALID_ASSET_NAME, "  AAPL  ",
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    InvestmentType.STOCK, VALID_PURCHASE_DATE
            );

            assertThat(investment.getTicker()).isEqualTo("AAPL");
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("Should reconstitute existing investment")
        void shouldReconstituteExistingInvestment() {
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

            Investment investment = Investment.reconstitute(
                    id, USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    new BigDecimal("180.00"),
                    InvestmentType.STOCK, VALID_PURCHASE_DATE,
                    createdAt, modifiedAt, false
            );

            assertThat(investment.getId()).isEqualTo(id);
            assertThat(investment.getCurrentPrice()).isEqualByComparingTo("180.00");
            assertThat(investment.isActive()).isFalse();
            assertThat(investment.getCreatedAt()).isEqualTo(createdAt);
            assertThat(investment.getModifiedAt()).isEqualTo(modifiedAt);
        }

        @Test
        @DisplayName("Should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            assertThatThrownBy(() -> Investment.reconstitute(
                    null, USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    VALID_PURCHASE_PRICE, InvestmentType.STOCK,
                    VALID_PURCHASE_DATE,
                    LocalDateTime.now(), LocalDateTime.now(), true
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject negative current price on reconstitute")
        void shouldRejectNegativeCurrentPriceOnReconstitute() {
            assertThatThrownBy(() -> Investment.reconstitute(
                    UUID.randomUUID(), USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    new BigDecimal("-1.00"), InvestmentType.STOCK,
                    VALID_PURCHASE_DATE,
                    LocalDateTime.now(), LocalDateTime.now(), true
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null created at on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            assertThatThrownBy(() -> Investment.reconstitute(
                    UUID.randomUUID(), USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    VALID_PURCHASE_PRICE, InvestmentType.STOCK,
                    VALID_PURCHASE_DATE,
                    null, LocalDateTime.now(), true
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null modified at on reconstitute")
        void shouldRejectNullModifiedAtOnReconstitute() {
            assertThatThrownBy(() -> Investment.reconstitute(
                    UUID.randomUUID(), USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    VALID_PURCHASE_PRICE, InvestmentType.STOCK,
                    VALID_PURCHASE_DATE,
                    LocalDateTime.now(), null, true
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update all details and modify modifiedAt")
        void shouldUpdateAllDetails() {
            Investment investment = createValidStock();
            LocalDateTime previousModifiedAt = investment.getModifiedAt();

            investment.updateDetails(
                    "Tesla Inc.", "TSLA",
                    new BigDecimal("5"), new BigDecimal("200.00"),
                    InvestmentType.STOCK, LocalDateTime.now()
            );

            assertThat(investment.getAssetName()).isEqualTo("Tesla Inc.");
            assertThat(investment.getTicker()).isEqualTo("TSLA");
            assertThat(investment.getQuantity()).isEqualByComparingTo("5");
            assertThat(investment.getPurchasePrice()).isEqualByComparingTo("200.00");
            assertThat(investment.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }

        @Test
        @DisplayName("Should reject invalid data on update")
        void shouldRejectInvalidDataOnUpdate() {
            Investment investment = createValidStock();

            assertThatThrownBy(() -> investment.updateDetails(
                    null, VALID_TICKER, VALID_QUANTITY,
                    VALID_PURCHASE_PRICE, InvestmentType.STOCK,
                    VALID_PURCHASE_DATE
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("calculations")
    class Calculations {

        @Test
        @DisplayName("Should calculate invested capital correctly")
        void shouldCalculateInvestedCapital() {
            Investment investment = createValidStock();

            assertThat(investment.getInvestedCapital())
                    .isEqualByComparingTo("1500.00");
        }

        @Test
        @DisplayName("Should calculate current value correctly")
        void shouldCalculateCurrentValue() {
            Investment investment = reconstituteValid(true);

            assertThat(investment.getCurrentValue())
                    .isEqualByComparingTo("1800.00");
        }

        @Test
        @DisplayName("Should calculate profit correctly")
        void shouldCalculateProfit() {
            Investment investment = reconstituteValid(true);

            assertThat(investment.getProfitOrLoss())
                    .isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("Should calculate loss correctly")
        void shouldCalculateLoss() {
            Investment investment = Investment.reconstitute(
                    UUID.randomUUID(), USER_ID, VALID_ASSET_NAME, VALID_TICKER,
                    VALID_QUANTITY, VALID_PURCHASE_PRICE,
                    new BigDecimal("120.00"),
                    InvestmentType.STOCK, VALID_PURCHASE_DATE,
                    LocalDateTime.now(), LocalDateTime.now(), true
            );

            assertThat(investment.getProfitOrLoss())
                    .isEqualByComparingTo("-300.00");
        }

        @Test
        @DisplayName("Should calculate ROI percentage correctly")
        void shouldCalculateRoiPercentage() {
            Investment investment = reconstituteValid(true);

            BigDecimal roi = investment.getReturnOnInvestmentPercentage();

            assertThat(roi).isEqualByComparingTo("20.00");
        }
    }

    @Nested
    @DisplayName("updateCurrentPrice")
    class UpdateCurrentPrice {

        @Test
        @DisplayName("Should update current price and modify modifiedAt")
        void shouldUpdateCurrentPrice() {
            Investment investment = createValidStock();
            LocalDateTime previousModifiedAt = investment.getModifiedAt();

            investment.updateCurrentPrice(new BigDecimal("200.00"));

            assertThat(investment.getCurrentPrice())
                    .isEqualByComparingTo("200.00");
            assertThat(investment.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }

        @Test
        @DisplayName("Should reject null price")
        void shouldRejectNullPrice() {
            Investment investment = createValidStock();

            assertThatThrownBy(() -> investment.updateCurrentPrice(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject negative price")
        void shouldRejectNegativePrice() {
            Investment investment = createValidStock();

            assertThatThrownBy(() -> investment.updateCurrentPrice(new BigDecimal("-10.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate investment and modify modifiedAt")
        void shouldDeactivateInvestment() {
            Investment investment = reconstituteValid(true);
            LocalDateTime previousModifiedAt = investment.getModifiedAt();

            investment.deactivate();

            assertThat(investment.isActive()).isFalse();
            assertThat(investment.getModifiedAt()).isAfterOrEqualTo(previousModifiedAt);
        }
    }
}