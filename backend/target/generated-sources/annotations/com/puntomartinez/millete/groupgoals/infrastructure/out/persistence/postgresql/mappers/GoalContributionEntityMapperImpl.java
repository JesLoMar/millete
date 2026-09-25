package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:16+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class GoalContributionEntityMapperImpl implements GoalContributionEntityMapper {

    @Override
    public GoalContributionEntity toEntity(GoalContribution domain) {
        if ( domain == null ) {
            return null;
        }

        GoalContributionEntity goalContributionEntity = new GoalContributionEntity();

        goalContributionEntity.setType( mapTypeToString( domain.getType() ) );
        goalContributionEntity.setId( domain.getId() );
        goalContributionEntity.setGoalId( domain.getGoalId() );
        goalContributionEntity.setUserId( domain.getUserId() );
        goalContributionEntity.setAmount( domain.getAmount() );
        goalContributionEntity.setDate( domain.getDate() );
        goalContributionEntity.setCreatedAt( domain.getCreatedAt() );
        goalContributionEntity.setModifiedAt( domain.getModifiedAt() );
        goalContributionEntity.setActive( domain.isActive() );

        return goalContributionEntity;
    }
}
