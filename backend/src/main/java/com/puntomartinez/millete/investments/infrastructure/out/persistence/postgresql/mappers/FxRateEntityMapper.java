package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.FxRateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FxRateEntityMapper {

    @Mapping(
            target = "baseCurrency",
            source = "baseCurrency.value"
    )
    @Mapping(
            target = "quoteCurrency",
            source = "quoteCurrency.value"
    )
    FxRateEntity toEntity(FxRate domain);

    default FxRate toDomain(FxRateEntity entity) {
        if (entity == null) {
            return null;
        }

        return new FxRate(
                entity.getId(),
                CurrencyCode.of(entity.getBaseCurrency()),
                CurrencyCode.of(entity.getQuoteCurrency()),
                entity.getTimestamp(),
                entity.getRate(),
                entity.getSource(),
                entity.getFetchedAt()
        );
    }
}