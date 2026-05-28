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
public class FamilyMember {
    private UUID id;
    private UUID familyId;
    private UUID userId;
    private FamilyRole role;
    private BigDecimal salary;
    private BigDecimal customPercentage;

    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public boolean isAdmin() {
        return FamilyRole.ADMIN.equals(this.role);
    }

}