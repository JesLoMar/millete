package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "planned_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedTransactionEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "frequency_type", nullable = false, length = 20)
    private String frequencyType;

    @Column(name = "frequency_interval", nullable = false)
    private Integer frequencyInterval;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "last_executed_date")
    private LocalDate lastExecutedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(name = "active", nullable = false)
    private boolean active;
}