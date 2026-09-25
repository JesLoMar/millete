package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:16+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class NotificationEntityMapperImpl implements NotificationEntityMapper {

    @Override
    public NotificationEntity toEntity(Notification domain) {
        if ( domain == null ) {
            return null;
        }

        NotificationEntity notificationEntity = new NotificationEntity();

        notificationEntity.setId( domain.getId() );
        notificationEntity.setUserId( domain.getUserId() );
        notificationEntity.setType( domain.getType() );
        notificationEntity.setTitle( domain.getTitle() );
        notificationEntity.setMessage( domain.getMessage() );
        Map<String, Object> map = domain.getMetadata();
        if ( map != null ) {
            notificationEntity.setMetadata( new LinkedHashMap<String, Object>( map ) );
        }
        notificationEntity.setRead( domain.isRead() );
        notificationEntity.setActionRequired( domain.isActionRequired() );
        notificationEntity.setActionedAt( domain.getActionedAt() );
        notificationEntity.setCreatedAt( domain.getCreatedAt() );
        notificationEntity.setExpiresAt( domain.getExpiresAt() );
        notificationEntity.setActive( domain.isActive() );

        return notificationEntity;
    }
}
