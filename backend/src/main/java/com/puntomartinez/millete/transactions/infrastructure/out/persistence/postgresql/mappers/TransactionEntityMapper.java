package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionEntityMapper {

    default TransactionEntity toEntity(Transaction domain) {
        if (domain == null) {
            return null;
        }

        TransactionEntity entity = new TransactionEntity();

        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setAmount(domain.getAmount());
        entity.setDate(domain.getDate());
        entity.setType(domain.getType().name());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setModifiedAt(domain.getModifiedAt());
        entity.setActive(domain.isActive());

        return entity;
    }

    default Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return Transaction.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getAmount(),
                entity.getDate(),
                Transaction.TransactionType.valueOf(entity.getType()),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }
}