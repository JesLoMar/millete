package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:16+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class GoalUnitEntityMapperImpl implements GoalUnitEntityMapper {

    @Override
    public GoalUnitEntity toEntity(GoalUnit goalUnit) {
        if ( goalUnit == null ) {
            return null;
        }

        GoalUnitEntity.GoalUnitEntityBuilder goalUnitEntity = GoalUnitEntity.builder();

        goalUnitEntity.id( goalUnit.getId() );
        goalUnitEntity.name( goalUnit.getName() );
        goalUnitEntity.monthlyTarget( goalUnit.getMonthlyTarget() );
        if ( goalUnit.getDistributionMode() != null ) {
            goalUnitEntity.distributionMode( goalUnit.getDistributionMode().name() );
        }
        goalUnitEntity.createdAt( goalUnit.getCreatedAt() );
        goalUnitEntity.modifiedAt( goalUnit.getModifiedAt() );
        goalUnitEntity.active( goalUnit.isActive() );

        return goalUnitEntity.build();
    }
}
