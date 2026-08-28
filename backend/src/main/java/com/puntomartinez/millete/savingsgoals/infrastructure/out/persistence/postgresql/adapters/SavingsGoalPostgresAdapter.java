package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers.SavingsGoalEntityMapper;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.repository.JpaSavingsGoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SavingsGoalPostgresAdapter implements SavingsGoalRepository {

    private final JpaSavingsGoalRepository jpaRepository;
    private final SavingsGoalEntityMapper mapper;

    public SavingsGoalPostgresAdapter(JpaSavingsGoalRepository jpaRepository, SavingsGoalEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SavingsGoal save(SavingsGoal savingsGoal) {
        SavingsGoalEntity entity = mapper.toEntity(savingsGoal);
        SavingsGoalEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SavingsGoal> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SavingsGoal> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<SavingsGoal> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserIdAndActiveTrue(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SavingsGoal> findAllByUserIdAndStatus(UUID userId, String status) {
        return jpaRepository.findByUserIdAndStatusAndActiveTrue(userId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private Specification<SavingsGoalEntity> buildSpecification(UUID userId, String search, String status) {
        Specification<SavingsGoalEntity> spec = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), true));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status.toUpperCase()));
        }

        return spec;
    }

    @Override
    public List<SavingsGoal> findAllByUserId(UUID userId, int page, int size, String search, String status) {
        Specification<SavingsGoalEntity> spec = buildSpecification(userId, search, status);
        Page<SavingsGoalEntity> result = jpaRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return result.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserIdAndFilters(UUID userId, String search, String status) {
        return jpaRepository.count(buildSpecification(userId, search, status));
    }
}
