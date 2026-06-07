package com.puntomartinez.millete.transactions.infrastructure.in.controller.dto;

import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal amount,
        LocalDateTime date,
        TransactionType type,
        String description,
        boolean alertLimitExceeded,
        boolean active
) {}