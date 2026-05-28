package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.InvestmentEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.SpringDataInvestmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvestmentPostgresAdapter implements InvestmentRepository {

    private final SpringDataInvestmentRepository repository;
    private final InvestmentEntityMapper mapper;

    public InvestmentPostgresAdapter(SpringDataInvestmentRepository repository, InvestmentEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Investment save(Investment investment) {
        InvestmentEntity entity = mapper.toEntity(investment);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Investment> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Investment> findAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}