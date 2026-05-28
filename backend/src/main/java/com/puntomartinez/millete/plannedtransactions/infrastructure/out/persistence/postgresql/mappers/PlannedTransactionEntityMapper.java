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

    @Mapping(target = "type", source = "type", qualifiedByName = "mapTransactionTypeToString")
    @Mapping(target = "frequencyType", source = "frequencyType", qualifiedByName = "mapFrequencyTypeToString")
    PlannedTransactionEntity toEntity(PlannedTransaction domain);

    @Mapping(target = "type", source = "type", qualifiedByName = "mapStringToTransactionType")
    @Mapping(target = "frequencyType", source = "frequencyType", qualifiedByName = "mapStringToFrequencyType")
    PlannedTransaction toDomain(PlannedTransactionEntity entity);

    @Named("mapTransactionTypeToString")
    default String mapTransactionTypeToString(TransactionType type) {
        return type != null ? type.name() : null;
    }

    @Named("mapFrequencyTypeToString")
    default String mapFrequencyTypeToString(FrequencyType type) {
        return type != null ? type.name() : null;
    }

    @Named("mapStringToTransactionType")
    default TransactionType mapStringToTransactionType(String type) {
        return type != null ? TransactionType.valueOf(type) : null;
    }

    @Named("mapStringToFrequencyType")
    default FrequencyType mapStringToFrequencyType(String type) {
        return type != null ? FrequencyType.valueOf(type) : null;
    }
}