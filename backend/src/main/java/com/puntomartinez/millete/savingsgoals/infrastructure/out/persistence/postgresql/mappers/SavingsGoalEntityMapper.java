package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SavingsGoalEntityMapper {
    SavingsGoalEntity toEntity(SavingsGoal domain);
    SavingsGoal toDomain(SavingsGoalEntity entity);
}