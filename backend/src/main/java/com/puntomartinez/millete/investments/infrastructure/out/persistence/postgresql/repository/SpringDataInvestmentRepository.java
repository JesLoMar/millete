package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataInvestmentRepository extends JpaRepository<InvestmentEntity, UUID> {
    List<InvestmentEntity> findAllByUserId(UUID userId);
    Page<InvestmentEntity> findAllByUserIdAndActiveTrue(UUID userId, Pageable pageable);
    long countByUserIdAndActiveTrue(UUID userId);
}
