package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class SavingsGoalEntityMapperImpl implements SavingsGoalEntityMapper {

    @Override
    public SavingsGoalEntity toEntity(SavingsGoal domain) {
        if ( domain == null ) {
            return null;
        }

        SavingsGoalEntity savingsGoalEntity = new SavingsGoalEntity();

        savingsGoalEntity.setPriority( priorityToString( domain.getPriority() ) );
        savingsGoalEntity.setId( domain.getId() );
        savingsGoalEntity.setUserId( domain.getUserId() );
        savingsGoalEntity.setName( domain.getName() );
        savingsGoalEntity.setTargetAmount( domain.getTargetAmount() );
        savingsGoalEntity.setCurrentAmount( domain.getCurrentAmount() );
        savingsGoalEntity.setDeadline( domain.getDeadline() );
        savingsGoalEntity.setLink( domain.getLink() );
        savingsGoalEntity.setCreatedAt( domain.getCreatedAt() );
        savingsGoalEntity.setModifiedAt( domain.getModifiedAt() );
        savingsGoalEntity.setActive( domain.isActive() );

        return savingsGoalEntity;
    }
}
