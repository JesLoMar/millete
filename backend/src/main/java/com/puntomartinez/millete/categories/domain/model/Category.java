package com.puntomartinez.millete.categories.domain.model;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Category {
    private UUID id;
    private UUID userId;
    private String name;
    private String color;
    private BigDecimal budgetLimit;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public Category(UUID userId, String name, String color, BigDecimal budgetLimit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("El color debe ser un hexadecimal válido (ej: #FF5733)");
        }
        if (budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El límite de presupuesto no puede ser negativo");
        }

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.color = color;
        this.budgetLimit = budgetLimit;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.active = true;
    }

    public Category(UUID id, UUID userId, String name, String color,
                    BigDecimal budgetLimit, LocalDateTime createdAt,
                    LocalDateTime modifiedAt, boolean active) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.color = color;
        this.budgetLimit = budgetLimit;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public void updateDetails(String nombre, String color, BigDecimal budgetLimit) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("El color debe ser un hexadecimal válido");
        }
        if (budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El límite de presupuesto no puede ser negativo");
        }

        this.name = nombre;
        this.color = color;
        this.budgetLimit = budgetLimit;
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }
}