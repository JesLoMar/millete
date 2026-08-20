package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.*;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements RegisterTransactionUseCase, ListTransactionsUseCase, DeleteTransactionUseCase, GetTransactionUseCase, UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;


    @Override
    public RegisterTransactionResult register(RegisterTransactionCommand command) {
        if (command.categoryId() != null) {
            categoryRepository.findByIdAndUserId(command.categoryId(), command.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category does not exist or does not belong to you."));
        }

        UUID newId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction(
                newId,
                command.userId(),
                command.categoryId(),
                command.amount(),
                command.date(),
                command.type(),
                command.description(),
                now,
                now,
                true
        );

        Transaction savedTransaction = transactionRepository.save(transaction);


        boolean limitExceeded = false;


        if (command.type() == Transaction.TransactionType.EXPENSE) {
            int year = command.date().getYear();
            int month = command.date().getMonthValue();


            List<Transaction> monthTransactions = transactionRepository.findAllByUserId(command.userId()).stream()
                    .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                    .toList();

            BigDecimal totalIncome = monthTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            BigDecimal previousExpenses = monthTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            BigDecimal totalExpense = previousExpenses.add(command.amount());


            BigDecimal limit = totalIncome.multiply(new BigDecimal("0.70"));


            if (totalIncome.compareTo(BigDecimal.ZERO) > 0 && totalExpense.compareTo(limit) > 0) {
                limitExceeded = true;

            }
        }

        return new RegisterTransactionResult(savedTransaction, limitExceeded);
    }


    @Override
    public List<Transaction> findAllByUserId(UUID userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    @Override
    public List<Transaction> findAllByUserId(UUID userId, int page, int size, String search, TransactionType type,
                                               LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findAllByUserId(userId, page, size, search, type, startDate, endDate);
    }

    @Override
    public long countByUserIdAndFilters(UUID userId, String search, TransactionType type,
                                        LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.countByUserIdAndFilters(userId, search, type, startDate, endDate);
    }


    @Override
    public Transaction getByIdAndUserId(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found."));


        if (!transaction.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("You do not have permission to view this transaction.");
        }

        return transaction;
    }


    @Override
    public Transaction update(UUID id, UpdateTransactionCommand command) {

        Transaction transaction = this.getByIdAndUserId(id, command.userId());

        if (command.categoryId() != null) {
            categoryRepository.findByIdAndUserId(command.categoryId(), command.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category does not exist or does not belong to you."));
        }

        transaction.updateDetails(
                command.amount(),
                command.date(),
                command.type(),
                command.description(),
                command.categoryId()
        );

        return transactionRepository.save(transaction);
    }


    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        Transaction transaction = this.getByIdAndUserId(id, userId);

        transaction.deactivate();
        transactionRepository.save(transaction);
    }
}
