package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "investment_activities")
@Getter
@Setter
@NoArgsConstructor
public class ActivityEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "type", updatable = false, nullable = false, length = 30)
    private String type;

    @Column(
            name = "asset_reference_kind",
            updatable = false,
            length = 10
    )
    private String assetReferenceKind;

    @Column(
            name = "asset_reference_id",
            updatable = false
    )
    private UUID assetReferenceId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(name = "ordering_key", nullable = false)
    private long orderingKey;

    @Column(
            name = "details",
            nullable = false,
            columnDefinition = "JSONB"
    )
    private String details;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "linked_transaction_id")
    private UUID linkedTransactionId;
}