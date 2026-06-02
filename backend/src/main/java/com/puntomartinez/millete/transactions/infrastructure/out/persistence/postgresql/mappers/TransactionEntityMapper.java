package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionEntityMapper {
    TransactionEntity toEntity(Transaction domain);
    Transaction toDomain(TransactionEntity entity);
}