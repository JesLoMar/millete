package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class GoalInvitationEntityMapperImpl implements GoalInvitationEntityMapper {

    @Override
    public GoalInvitationEntity toEntity(GoalInvitation domain) {
        if ( domain == null ) {
            return null;
        }

        GoalInvitationEntity goalInvitationEntity = new GoalInvitationEntity();

        goalInvitationEntity.setStatus( mapStatusToString( domain.getStatus() ) );
        goalInvitationEntity.setId( domain.getId() );
        goalInvitationEntity.setGoalId( domain.getGoalId() );
        goalInvitationEntity.setInviterUserId( domain.getInviterUserId() );
        goalInvitationEntity.setInvitedUserId( domain.getInvitedUserId() );
        goalInvitationEntity.setExpiresAt( domain.getExpiresAt() );
        goalInvitationEntity.setCreatedAt( domain.getCreatedAt() );
        goalInvitationEntity.setModifiedAt( domain.getModifiedAt() );
        goalInvitationEntity.setActive( domain.isActive() );

        return goalInvitationEntity;
    }
}
