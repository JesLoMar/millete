package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvestmentEntityMapper {
    InvestmentEntity toEntity(Investment domain);
    Investment toDomain(InvestmentEntity entity);
}