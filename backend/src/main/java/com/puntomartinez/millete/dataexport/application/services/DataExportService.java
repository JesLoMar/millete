package com.puntomartinez.millete.dataexport.application.services;

import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.dataexport.domain.model.ExportVersion;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.PlannedTransactionRepository;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class DataExportService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final InvestmentRepository investmentRepository;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    public DataExportService(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            PlannedTransactionRepository plannedTransactionRepository,
            InvestmentRepository investmentRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.investmentRepository = investmentRepository;
    }

    public UserDataSnapshot exportAllUserData(UUID userId) {
        log.info("Exportando datos para usuario: {}", userId);

        UserDataSnapshot snapshot = new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        ExportVersion.CURRENT.toString(),
                        LocalDateTime.now(),
                        userId,
                        appVersion
                ),
                categoryRepository.findByIdUsuario(userId),
                transactionRepository.findAllByUserId(userId),
                plannedTransactionRepository.findAllByUserId(userId),
                investmentRepository.findAllByUserId(userId)
        );

        log.info("Exportación completada. v{}", ExportVersion.CURRENT);
        return snapshot;
    }
}