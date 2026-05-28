package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionEntityMapper {
    TransactionEntity toEntity(Transaction domain);
    Transaction toDomain(TransactionEntity entity);
}