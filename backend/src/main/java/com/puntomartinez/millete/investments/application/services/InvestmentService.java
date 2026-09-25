package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.domain.ports.in.DeleteInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListInvestmentsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class InvestmentService implements
        RegisterInvestmentUseCase,
        ListInvestmentsUseCase,
        GetInvestmentUseCase,
        UpdateInvestmentPriceUseCase,
        DeleteInvestmentUseCase,
        UpdateInvestmentUseCase {

    private final InvestmentRepository investmentRepository;
    private final TimeProvider timeProvider;

    public InvestmentService(
            InvestmentRepository investmentRepository,
            TimeProvider timeProvider
    ) {
        this.investmentRepository = investmentRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    public Investment register(RegisterInvestmentCommand command) {
        Investment investment = Investment.create(
                timeProvider,
                command.userId(),
                command.assetName(),
                command.ticker(),
                command.quantity(),
                command.purchasePrice(),
                command.type(),
                command.purchaseDate()
        );

        return investmentRepository.save(investment);
    }

    @Override
    public Investment getById(UUID id, UUID userId) {
        return investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inversión no encontrada."));
    }

    @Override
    public List<Investment> findAllByUserId(UUID userId) {
        return investmentRepository.findAllByUserId(userId);
    }

    @Override
    public List<Investment> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search,
            InvestmentType type) {

        return investmentRepository.findAllByUserId(
                userId,
                page,
                size,
                search,
                type
        );
    }

    @Override
    public long countByUserIdAndFilters(
            UUID userId,
            String search,
            InvestmentType type) {

        return investmentRepository.countByUserIdAndFilters(
                userId,
                search,
                type
        );
    }

    @Override
    public Investment updatePrice(
            UUID id,
            UUID userId,
            BigDecimal newPrice) {

        Investment investment = investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inversión no encontrada."));

        investment.updateCurrentPrice(timeProvider, newPrice);

        return investmentRepository.save(investment);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        Investment investment = investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inversión no encontrada."));

        investment.deactivate(timeProvider);

        investmentRepository.save(investment);
    }

    @Override
    public Investment update(UpdateInvestmentCommand command) {
        Investment investment = investmentRepository
                .findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inversión no encontrada."));

        investment.updateDetails(
                timeProvider,
                command.assetName(),
                command.ticker(),
                command.quantity(),
                command.purchasePrice(),
                command.type(),
                command.purchaseDate()
        );

        return investmentRepository.save(investment);
    }
}
