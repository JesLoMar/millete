package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class InvestmentEntityMapperImpl implements InvestmentEntityMapper {

    @Override
    public InvestmentEntity toEntity(Investment domain) {
        if ( domain == null ) {
            return null;
        }

        InvestmentEntity.InvestmentEntityBuilder investmentEntity = InvestmentEntity.builder();

        investmentEntity.id( domain.getId() );
        investmentEntity.userId( domain.getUserId() );
        investmentEntity.assetName( domain.getAssetName() );
        investmentEntity.ticker( domain.getTicker() );
        investmentEntity.quantity( domain.getQuantity() );
        investmentEntity.purchasePrice( domain.getPurchasePrice() );
        investmentEntity.currentPrice( domain.getCurrentPrice() );
        if ( domain.getType() != null ) {
            investmentEntity.type( domain.getType().name() );
        }
        investmentEntity.purchaseDate( domain.getPurchaseDate() );
        investmentEntity.createdAt( domain.getCreatedAt() );
        investmentEntity.modifiedAt( domain.getModifiedAt() );
        investmentEntity.active( domain.isActive() );

        return investmentEntity.build();
    }
}
