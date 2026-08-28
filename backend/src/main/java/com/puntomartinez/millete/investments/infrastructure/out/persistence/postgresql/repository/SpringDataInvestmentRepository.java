package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SpringDataInvestmentRepository extends JpaRepository<InvestmentEntity, UUID>, JpaSpecificationExecutor<InvestmentEntity> {
}
