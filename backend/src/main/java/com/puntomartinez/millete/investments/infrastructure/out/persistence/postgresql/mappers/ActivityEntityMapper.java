package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.ActivityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ActivityEntityMapper {

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ActivityEntity toEntity(Activity domain) {
        if (domain == null) {
            return null;
        }

        ActivityEntity entity = new ActivityEntity();

        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setType(domain.getType().name());

        AssetReference reference = domain.getAssetReference();

        entity.setAssetReferenceKind(
                reference == null
                        ? null
                        : reference.kind().name()
        );

        entity.setAssetReferenceId(
                reference == null
                        ? null
                        : reference.id()
        );

        entity.setOccurredAt(domain.getOccurredAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setModifiedAt(domain.getModifiedAt());
        entity.setOrderingKey(domain.getOrderingKey());
        entity.setDetails(
                serializeDetails(domain.getDetails())
        );
        entity.setComment(domain.getComment());
        entity.setLinkedTransactionId(
                domain.getLinkedTransactionId()
        );

        return entity;
    }

    public Activity toDomain(ActivityEntity entity) {
        if (entity == null) {
            return null;
        }

        ActivityType type = ActivityType.valueOf(
                entity.getType()
        );

        AssetReference assetReference =
                readAssetReference(
                        entity.getAssetReferenceKind(),
                        entity.getAssetReferenceId()
                );

        ActivityDetails details =
                deserializeDetails(
                        type,
                        entity.getDetails()
                );

        return Activity.reconstitute(
                entity.getId(),
                entity.getUserId(),
                type,
                assetReference,
                entity.getOccurredAt(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.getOrderingKey(),
                details,
                entity.getComment(),
                entity.getLinkedTransactionId()
        );
    }

    private String serializeDetails(
            ActivityDetails details
    ) {
        if (details == null) {
            throw new IllegalStateException(
                    "Una Activity debe tener details."
            );
        }

        try {
            return objectMapper.writeValueAsString(
                    details
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudieron serializar los detalles de la Activity.",
                    exception
            );
        }
    }

    private ActivityDetails deserializeDetails(
            ActivityType type,
            String json
    ) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException(
                    "Una Activity debe tener details."
            );
        }

        try {
            JsonNode node = objectMapper.readTree(json);

            Class<? extends ActivityDetails> detailsType =
                    detailsType(type, node);

            return objectMapper.treeToValue(
                    node,
                    detailsType
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "No se pudieron deserializar los detalles "
                            + "de la Activity de tipo "
                            + type
                            + ".",
                    exception
            );
        }
    }

    private Class<? extends ActivityDetails> detailsType(
            ActivityType type,
            JsonNode node
    ) {
        return switch (type) {

            case BUY, SELL ->
                    TradeActivityDetails.class;

            case DIVIDEND -> {
                if (node.has("quantity")
                        && node.has("referenceUnitPrice")) {

                    yield DividendUnitsDetails.class;
                }

                yield CashActivityDetails.class;
            }

            case DEPOSIT,
                    WITHDRAW,
                    OPENING_CASH ->

                    CashActivityDetails.class;

            case SPLIT ->
                    SplitActivityDetails.class;

            case EXCHANGE ->
                    ExchangeActivityDetails.class;

            case OPENING_POSITION ->
                    OpeningPositionDetails.class;
        };
    }

    private AssetReference readAssetReference(
            String kind,
            UUID id
    ) {
        if (kind == null || id == null) {
            return null;
        }

        return switch (
                AssetReferenceKind.valueOf(kind)
        ) {
            case SHARED ->
                    AssetReference.shared(id);

            case USER ->
                    AssetReference.user(id);
        };
    }
}