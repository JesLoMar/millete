package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class GoalMemberEntityMapperImpl implements GoalMemberEntityMapper {

    @Override
    public GoalMemberEntity toEntity(GoalMember goalMember) {
        if ( goalMember == null ) {
            return null;
        }

        GoalMemberEntity.GoalMemberEntityBuilder goalMemberEntity = GoalMemberEntity.builder();

        goalMemberEntity.id( goalMember.getId() );
        goalMemberEntity.goalId( goalMember.getGoalId() );
        goalMemberEntity.userId( goalMember.getUserId() );
        if ( goalMember.getRole() != null ) {
            goalMemberEntity.role( goalMember.getRole().name() );
        }
        goalMemberEntity.salary( goalMember.getSalary() );
        goalMemberEntity.customPercentage( goalMember.getCustomPercentage() );
        goalMemberEntity.joinedAt( goalMember.getJoinedAt() );
        goalMemberEntity.createdAt( goalMember.getCreatedAt() );
        goalMemberEntity.modifiedAt( goalMember.getModifiedAt() );
        goalMemberEntity.active( goalMember.isActive() );

        return goalMemberEntity.build();
    }
}
