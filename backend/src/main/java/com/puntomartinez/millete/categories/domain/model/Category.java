package com.puntomartinez.millete.categories.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Category {

    private final UUID id;
    private final UUID userId;
    private String name;
    private String color;
    private BigDecimal budgetLimit;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    private Category(
            UUID id,
            UUID userId,
            String name,
            String color,
            BigDecimal budgetLimit,
            Instant createdAt,
            Instant modifiedAt,
            boolean active
    ) {
        validateId(id);
        validateUserId(userId);
        validateName(name);
        validateColor(color);
        validateBudgetLimit(budgetLimit);
        validateCreatedAt(createdAt);
        validateModifiedAt(modifiedAt);

        this.id = id;
        this.userId = userId;
        this.name = name;
        this.color = color;
        this.budgetLimit = budgetLimit;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
    }

    public static Category create(
            TimeProvider timeProvider,
            UUID userId,
            String name,
            String color,
            BigDecimal budgetLimit
    ) {
        Instant now = timeProvider.now();

        return new Category(
                UUID.randomUUID(),
                userId,
                name,
                color,
                budgetLimit,
                now,
                now,
                true
        );
    }

    public static Category reconstitute(
            UUID id,
            UUID userId,
            String name,
            String color,
            BigDecimal budgetLimit,
            Instant createdAt,
            Instant modifiedAt,
            boolean active
    ) {
        return new Category(
                id,
                userId,
                name,
                color,
                budgetLimit,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateDetails(
            TimeProvider timeProvider,
            String name,
            String color,
            BigDecimal budgetLimit
    ) {
        validateName(name);
        validateColor(color);
        validateBudgetLimit(budgetLimit);

        this.name = name;
        this.color = color;
        this.budgetLimit = budgetLimit;
        this.modifiedAt = timeProvider.now();
    }

    public void deactivate(TimeProvider timeProvider) {
        if (!this.active) {
            return;
        }

        this.active = false;
        this.modifiedAt = timeProvider.now();
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador de la categoría es obligatorio"
            );
        }
    }

    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio"
            );
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio"
            );
        }

        if (name.length() > 20) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría no puede superar los 20 caracteres"
            );
        }
    }

    private static void validateColor(String color) {
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException(
                    "El color debe ser un hexadecimal válido (ej: #FF5733)"
            );
        }
    }

    private static void validateBudgetLimit(BigDecimal budgetLimit) {
        if (budgetLimit != null
                && budgetLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El límite de presupuesto no puede ser negativo"
            );
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }
    }

    private static void validateModifiedAt(Instant modifiedAt) {
        if (modifiedAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de modificación es obligatoria"
            );
        }
    }
}

/*
5. Las invariantes viven dentro del agregado

Por ejemplo:

validateName(name);
validateColor(color);
validateBudgetLimit(budgetLimit);

Ya no dependemos únicamente de que el controller haya puesto @Valid.

Eso es importante porque mañana podríamos crear una categoría desde:

REST
importación
otro caso de uso
un test
un proceso batch

y el dominio seguirá protegiéndose.

Una decisión importante: userId

He mantenido:

private final UUID userId;

porque en el modelo de negocio una categoría pertenece a un usuario y no debería poder cambiar de propietario mediante una actualización normal.

Pero esto no impide la importación.

Para importar datos de otro usuario, posteriormente tendremos un mecanismo explícito que cree/reconstituya la categoría con el userId del usuario destino.

Es mucho mejor que hacer:

category.setUserId(loggedInUserId);

porque eso era una mutación arbitraria del agregado.
*/
