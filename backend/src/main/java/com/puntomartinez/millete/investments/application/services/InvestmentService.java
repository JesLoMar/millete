package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.in.ListInvestmentsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvestmentService implements RegisterInvestmentUseCase, ListInvestmentsUseCase, UpdateInvestmentPriceUseCase {

    private final InvestmentRepository investmentRepository;

    public InvestmentService(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    // =======================================================
    // REGISTRAR INVERSIÓN
    // =======================================================
    @Override
    public Investment register(RegisterInvestmentCommand command) {
        UUID newId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Investment investment = new Investment(
                newId,
                command.userId(),
                command.assetName(),
                command.ticker(),
                command.quantity(),
                command.purchasePrice(),
                command.purchasePrice(), // Al comprar, el precio actual es el precio de compra
                command.type(),
                command.purchaseDate(),
                now,
                now,
                true
        );

        return investmentRepository.save(investment);
    }

    // =======================================================
    // LISTAR INVERSIONES
    // =======================================================
    @Override
    public List<Investment> findAllByUserId(UUID userId) {
        return investmentRepository.findAllByUserId(userId);
    }

    // =======================================================
    // ACTUALIZAR PRECIO DE MERCADO
    // =======================================================
    @Override
    public Investment updatePrice(UUID id, UUID userId, BigDecimal newPrice) {
        // 1. Buscamos la inversión en la base de datos
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inversión no encontrada."));

        // 2. Seguridad Anti-IDOR: Verificamos que el usuario sea el dueño
        if (!investment.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para actualizar esta inversión.");
        }

        // 3. Actualizamos el precio usando la lógica de nuestro modelo de Dominio
        investment.updateCurrentPrice(newPrice);

        // 4. Guardamos los cambios
        return investmentRepository.save(investment);
    }
}