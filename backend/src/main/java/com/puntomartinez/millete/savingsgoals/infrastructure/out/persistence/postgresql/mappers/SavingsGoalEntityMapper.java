package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SavingsGoalEntityMapper {

SavingsGoalEntity toEntity(SavingsGoal domain);

default SavingsGoal toDomain(SavingsGoalEntity entity) {
    return SavingsGoal.reconstitute(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            entity.getTargetAmount(),
            entity.getCurrentAmount(),
            entity.getDeadline(),
            entity.getPriority(),
            entity.getLink(),
            entity.getCreatedAt(),
            entity.getModifiedAt(),
            entity.isActive()
    );
}
}