package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlannedTransactionEntityMapper {

@Mapping(
        target = "type",
        source = "type",
        qualifiedByName = "mapTransactionTypeToString"
)
@Mapping(
        target = "frequencyType",
        source = "frequencyType",
        qualifiedByName = "mapFrequencyTypeToString"
)
PlannedTransactionEntity toEntity(PlannedTransaction domain);

default PlannedTransaction toDomain(
        PlannedTransactionEntity entity
) {
    if (entity == null) {
        return null;
    }

    return PlannedTransaction.reconstitute(
            entity.getId(),
            entity.getUserId(),
            entity.getCategoryId(),
            entity.getAmount(),
            mapStringToTransactionType(entity.getType()),
            entity.getDescription(),
            mapStringToFrequencyType(entity.getFrequencyType()),
            entity.getFrequencyInterval(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCreatedAt(),
            entity.getModifiedAt(),
            entity.isActive(),
            entity.getLastExecutedDate()
    );
}

@Named("mapTransactionTypeToString")
default String mapTransactionTypeToString(
        TransactionType type
) {
    return type != null
            ? type.name()
            : null;
}

@Named("mapFrequencyTypeToString")
default String mapFrequencyTypeToString(
        FrequencyType type
) {
    return type != null
            ? type.name()
            : null;
}

default TransactionType mapStringToTransactionType(
        String type
) {
    return type != null
            ? TransactionType.valueOf(type)
            : null;
}

default FrequencyType mapStringToFrequencyType(
        String frequencyType
) {
    return frequencyType != null
            ? FrequencyType.valueOf(frequencyType)
            : null;
}
}