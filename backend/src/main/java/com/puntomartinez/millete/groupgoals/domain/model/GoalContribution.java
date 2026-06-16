package com.puntomartinez.millete.groupgoals.domain.model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class GoalContribution {
    private UUID id;
    private UUID goalId;
    private UUID userId;
    private BigDecimal amount;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
}