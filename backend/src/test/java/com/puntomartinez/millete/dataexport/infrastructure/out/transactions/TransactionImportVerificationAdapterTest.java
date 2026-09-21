package com.puntomartinez.millete.dataexport.infrastructure.out.transactions;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionImportVerificationAdapter")
class TransactionImportVerificationAdapterTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionImportVerificationAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("verifyImportedTransactions should pass when all categories are resolvable")
    void verifyImportedTransactionsShouldPassWhenAllCategoriesResolvable() {
        UUID categoryId = UUID.randomUUID();

        Transaction tx = Transaction.reconstitute(
                UUID.randomUUID(), userId, categoryId,
                new BigDecimal("50.00"), LocalDateTime.now(),
                Transaction.TransactionType.EXPENSE, "Lunch",
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        Category cat = Category.reconstitute(
                categoryId, userId, "Food", "#FF5733",
                new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(tx));
        when(categoryRepository.findByIdsAndUserId(eq(userId), any()))
                .thenReturn(List.of(cat));

        assertThatCode(() -> adapter.verifyImportedTransactions(userId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("verifyImportedTransactions should handle empty transactions")
    void verifyImportedTransactionsShouldHandleEmptyTransactions() {
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());

        assertThatCode(() -> adapter.verifyImportedTransactions(userId))
                .doesNotThrowAnyException();
    }
}