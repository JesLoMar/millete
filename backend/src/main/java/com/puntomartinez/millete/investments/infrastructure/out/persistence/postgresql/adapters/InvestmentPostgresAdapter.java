package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.InvestmentEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.SpringDataInvestmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    private Specification<InvestmentEntity> buildSpecification(UUID userId, String search, InvestmentType type) {
        Specification<InvestmentEntity> spec = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("assetName")), pattern),
                    cb.like(cb.lower(root.get("ticker")), pattern)
            ));
        }

        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type.name()));
        }

        return spec;
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
        Specification<InvestmentEntity> spec = buildSpecification(userId, null, null);
        return repository.findAll(spec, Sort.by("purchaseDate").descending()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Investment> findAllByUserId(UUID userId, int page, int size, String search, InvestmentType type) {
        Specification<InvestmentEntity> spec = buildSpecification(userId, search, type);
        Page<InvestmentEntity> result = repository.findAll(spec,
                PageRequest.of(page, size, Sort.by("purchaseDate").descending()));
        return result.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndFilters(UUID userId, String search, InvestmentType type) {
        return repository.count(buildSpecification(userId, search, type));
    }
}
