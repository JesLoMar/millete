package com.puntomartinez.millete.transactions.domain.ports.in;

import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;

import java.util.UUID;

public interface GetTransactionMetricsUseCase {

    record MetricsCommand(UUID userId, String period) {}

    TransactionMetricsResponseDTO getMetrics(MetricsCommand command);
}