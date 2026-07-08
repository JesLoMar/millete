package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.in.ListInvestmentsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
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
                command.purchasePrice(),
                command.type(),
                command.purchaseDate(),
                now,
                now,
                true
        );

        return investmentRepository.save(investment);
    }


    @Override
    public List<Investment> findAllByUserId(UUID userId) {
        return investmentRepository.findAllByUserId(userId);
    }

    @Override
    public List<Investment> findAllByUserId(UUID userId, int page, int size) {
        return investmentRepository.findAllByUserId(userId, page, size);
    }

    @Override
    public long countActiveByUserId(UUID userId) {
        return investmentRepository.countByUserIdAndActiveTrue(userId);
    }


    @Override
    public Investment updatePrice(UUID id, UUID userId, BigDecimal newPrice) {

        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inversión no encontrada."));


        if (!investment.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("No tienes permiso para actualizar esta inversión.");
        }


        investment.updateCurrentPrice(newPrice);


        return investmentRepository.save(investment);
    }
}
