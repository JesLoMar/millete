package com.puntomartinez.millete.groupgoals.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoalMember {
    private UUID id;
    private UUID goalId;
    private UUID userId;
    private GoalRole role;
    private BigDecimal salary;
    private BigDecimal customPercentage;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public boolean isAdmin() {
        return GoalRole.ADMIN.equals(this.role);
    }
}