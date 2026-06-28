package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationEntityMapper {

    Notification toDomain(NotificationEntity entity);

    NotificationEntity toEntity(Notification notification);
}
