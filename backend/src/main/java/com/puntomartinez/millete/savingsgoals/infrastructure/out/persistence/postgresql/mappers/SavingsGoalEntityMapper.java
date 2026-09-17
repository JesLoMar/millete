package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SavingsGoalEntityMapper {

    @Mapping(
            target = "priority",
            source = "priority",
            qualifiedByName = "priorityToString"
    )
    SavingsGoalEntity toEntity(SavingsGoal domain);

    default SavingsGoal toDomain(SavingsGoalEntity entity) {
        if (entity == null) {
            return null;
        }
        return SavingsGoal.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getTargetAmount(),
                entity.getCurrentAmount(),
                entity.getDeadline(),
                stringToPriority(entity.getPriority()),
                entity.getLink(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }

    @Named("priorityToString")
    default String priorityToString(GoalPriority priority) {
        return priority != null ? priority.name() : null;
    }

    default GoalPriority stringToPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return GoalPriority.MEDIUM;
        }
        try {
            return GoalPriority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            return GoalPriority.MEDIUM;
        }
    }
}