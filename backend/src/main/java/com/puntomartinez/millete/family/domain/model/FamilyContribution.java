package com.puntomartinez.millete.family.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class FamilyContribution {
    private UUID id;
    private UUID familyId;
    private UUID userId;
    private BigDecimal amount;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
}