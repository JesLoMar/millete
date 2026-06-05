package com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.family.domain.model.FamilyMember;
import com.puntomartinez.millete.family.domain.model.FamilyRole;
import com.puntomartinez.millete.family.infrastructure.out.persistence.postgresql.entity.FamilyMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FamilyMemberEntityMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "mapRoleToString")
    FamilyMemberEntity toEntity(FamilyMember domain);

    @Mapping(target = "role", source = "role", qualifiedByName = "mapStringToRole")
    FamilyMember toDomain(FamilyMemberEntity entity);

    @Named("mapRoleToString")
    default String mapRoleToString(FamilyRole role) {
        return role != null ? role.name() : null;
    }

    @Named("mapStringToRole")
    default FamilyRole mapStringToRole(String role) {
        return role != null ? FamilyRole.valueOf(role) : null;
    }
}