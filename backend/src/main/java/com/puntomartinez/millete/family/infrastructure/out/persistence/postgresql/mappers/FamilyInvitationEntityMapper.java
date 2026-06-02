package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.FamilyInvitation;
import com.puntomartinez.millete.family.domain.model.InvitationStatus;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyInvitationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FamilyInvitationEntityMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    FamilyInvitationEntity toEntity(FamilyInvitation domain);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStringToStatus")
    FamilyInvitation toDomain(FamilyInvitationEntity entity);

    @Named("mapStatusToString")
    default String mapStatusToString(InvitationStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapStringToStatus")
    default InvitationStatus mapStringToStatus(String status) {
        return status != null ? InvitationStatus.valueOf(status) : null;
    }
}