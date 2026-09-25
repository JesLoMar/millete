package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.domain.ports.in.DeleteTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.GetTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.ListTransactionsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.UnassignCategoryFromTransactionsUseCase;
import com.puntomartinez.millete.transactions.domain.ports.in.UpdateTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository.TransactionAggregates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService implements
        RegisterTransactionUseCase,
        ListTransactionsUseCase,
        DeleteTransactionUseCase,
        GetTransactionUseCase,
        UpdateTransactionUseCase,
        UnassignCategoryFromTransactionsUseCase {

    private final TransactionRepository transactionRepository;
    private final TimeProvider timeProvider;
    private final CategoryRepository categoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            TimeProvider timeProvider
    ) {
        this.timeProvider = timeProvider;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public RegisterTransactionUseCase.RegisterTransactionResult register(
            RegisterTransactionUseCase.RegisterTransactionCommand command
    ) {
        validateCategoryOwnership(
                command.categoryId(),
                command.userId()
        );

        Transaction transaction = Transaction.create(
                timeProvider,
                command.userId(),
                command.categoryId(),
                command.amount(),
                command.date(),
                command.type(),
                command.description()
        );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        boolean limitExceeded = false;

        if (command.type() == TransactionType.EXPENSE) {
            limitExceeded = isMonthlyExpenseLimitExceeded(
                    command.userId(),
                    command.date()
            );
        }

        return new RegisterTransactionUseCase.RegisterTransactionResult(
                savedTransaction,
                limitExceeded
        );
    }

    @Override
    @Transactional
    public void unassignCategory(
            UUID categoryId,
            UUID userId
    ) {
        transactionRepository.clearCategoryFromActiveTransactions(
                categoryId,
                userId,
                timeProvider.instantNow()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findAllByUserId(UUID userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return transactionRepository.findAllByUserId(
                userId,
                page,
                size,
                search,
                type,
                startDate,
                endDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserIdAndFilters(
            UUID userId,
            String search,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return transactionRepository.countByUserIdAndFilters(
                userId,
                search,
                type,
                startDate,
                endDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found."
                        )
                );
    }

    @Override
    @Transactional
    public Transaction update(
            UUID id,
            UpdateTransactionUseCase.UpdateTransactionCommand command
    ) {
        Transaction transaction = getByIdAndUserId(
                id,
                command.userId()
        );

        validateCategoryOwnership(
                command.categoryId(),
                command.userId()
        );

        transaction.updateDetails(
                timeProvider,
                command.amount(),
                command.date(),
                command.type(),
                command.description(),
                command.categoryId()
        );

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        Transaction transaction = getByIdAndUserId(id, userId);

        transaction.deactivate(timeProvider);
        transactionRepository.save(transaction);
    }

    private void validateCategoryOwnership(
            UUID categoryId,
            UUID userId
    ) {
        if (categoryId == null) {
            return;
        }

        categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category does not exist or does not belong to you."
                        )
                );
    }

    private boolean isMonthlyExpenseLimitExceeded(
            UUID userId,
            LocalDate date
    ) {
        LocalDate startOfMonth = date.withDayOfMonth(1);

        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        TransactionAggregates aggregates =
                transactionRepository.getAggregatesByUserIdAndDateBetween(
                        userId,
                        startOfMonth,
                        endOfMonth
                );

        BigDecimal totalIncome = aggregates.totalIncome();
        BigDecimal totalExpense = aggregates.totalExpense();

        BigDecimal limit = totalIncome.multiply(
                new BigDecimal("0.70")
        );

        return totalIncome.compareTo(BigDecimal.ZERO) > 0
                && totalExpense.compareTo(limit) > 0;
    }
}