package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaFamilyUnitRepository extends JpaRepository<FamilyUnitEntity, UUID> {}