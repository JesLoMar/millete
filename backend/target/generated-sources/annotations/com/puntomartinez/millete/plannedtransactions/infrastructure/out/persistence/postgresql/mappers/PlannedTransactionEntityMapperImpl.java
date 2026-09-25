package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class PlannedTransactionEntityMapperImpl implements PlannedTransactionEntityMapper {

    @Override
    public PlannedTransactionEntity toEntity(PlannedTransaction domain) {
        if ( domain == null ) {
            return null;
        }

        PlannedTransactionEntity plannedTransactionEntity = new PlannedTransactionEntity();

        plannedTransactionEntity.setType( mapTransactionTypeToString( domain.getType() ) );
        plannedTransactionEntity.setFrequencyType( mapFrequencyTypeToString( domain.getFrequencyType() ) );
        plannedTransactionEntity.setId( domain.getId() );
        plannedTransactionEntity.setUserId( domain.getUserId() );
        plannedTransactionEntity.setCategoryId( domain.getCategoryId() );
        plannedTransactionEntity.setAmount( domain.getAmount() );
        plannedTransactionEntity.setDescription( domain.getDescription() );
        plannedTransactionEntity.setFrequencyInterval( domain.getFrequencyInterval() );
        plannedTransactionEntity.setStartDate( domain.getStartDate() );
        plannedTransactionEntity.setEndDate( domain.getEndDate() );
        plannedTransactionEntity.setLastExecutedDate( domain.getLastExecutedDate() );
        plannedTransactionEntity.setCreatedAt( domain.getCreatedAt() );
        plannedTransactionEntity.setModifiedAt( domain.getModifiedAt() );
        plannedTransactionEntity.setActive( domain.isActive() );
        plannedTransactionEntity.setFailureCount( domain.getFailureCount() );

        return plannedTransactionEntity;
    }
}
