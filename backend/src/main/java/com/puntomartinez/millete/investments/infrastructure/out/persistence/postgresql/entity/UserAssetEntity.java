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
@Table(name = "user_assets")
@Getter
@Setter
@NoArgsConstructor
public class UserAssetEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "sector_code", length = 100)
    private String sectorCode;

    @Column(name = "sector_display_name", length = 120)
    private String sectorDisplayName;

    @Column(name = "sector_custom", nullable = false)
    private boolean sectorCustom;

    @Column(nullable = false, length = 30)
    private String origin;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;
}