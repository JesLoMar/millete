package com.puntomartinez.millete.transactions.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.*;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements RegisterTransactionUseCase, ListTransactionsUseCase, DeleteTransactionUseCase, GetTransactionUseCase, UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    // =======================================================
    // REGISTRAR CON GUARDIÁN DE PRESUPUESTO (Historia 6)
    // =======================================================
    @Override
    public RegisterTransactionResult register(RegisterTransactionCommand command) {
        if (command.categoryId() != null) {
            categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category does not exist."));
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

        // ==========================================
        // 🛡️ LÓGICA DEL GUARDIÁN DEL PRESUPUESTO
        // ==========================================
        boolean limitExceeded = false;

        // Only check if the newly inserted transaction is an EXPENSE
        if (command.type() == Transaction.TransactionType.EXPENSE) {
            int year = command.date().getYear();
            int month = command.date().getMonthValue();

            // Retrieve this month's transactions (this DOES NOT include the one just created)
            List<Transaction> monthTransactions = transactionRepository.findAllByUserId(command.userId()).stream()
                    .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                    .toList();

            BigDecimal totalIncome = monthTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sum expenses already in the DB
            BigDecimal previousExpenses = monthTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ADD CURRENT EXPENSE to get the real total
            BigDecimal totalExpense = previousExpenses.add(command.amount());

            // Set the limit to 70% of income
            BigDecimal limit = totalIncome.multiply(new BigDecimal("0.70"));

            // If there is income and total expenses exceed 70%
            if (totalIncome.compareTo(BigDecimal.ZERO) > 0 && totalExpense.compareTo(limit) > 0) {
                limitExceeded = true;
                // NOTE: In the future, we would send an event for a Push Notification or Email here
            }
        }

        return new RegisterTransactionResult(savedTransaction, limitExceeded);
    }

    // =======================================================
    // LISTAR: Filtramos estrictamente por el usuario actual
    // =======================================================
    @Override
    public List<Transaction> findAllByUserId(UUID userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    // =======================================================
    // BUSCAR: Validamos que la transacción sea del usuario
    // =======================================================
    @Override
    public Transaction getByIdAndUserId(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));

        // Security check (Anti-IDOR)
        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have permission to view this transaction.");
        }

        return transaction;
    }

    // =======================================================
    // ACTUALIZAR: Solo si el userId coincide
    // =======================================================
    @Override
    public Transaction update(UUID id, UpdateTransactionCommand command) {
        // Reuse retrieval logic with user validation
        Transaction transaction = this.getByIdAndUserId(id, command.userId());

        if (command.categoryId() != null) {
            categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category does not exist."));
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

    // =======================================================
    // ELIMINAR (SOFT DELETE): Solo si el userId coincide
    // =======================================================
    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        Transaction transaction = this.getByIdAndUserId(id, userId);

        transaction.deactivate();
        transactionRepository.save(transaction);
    }
}