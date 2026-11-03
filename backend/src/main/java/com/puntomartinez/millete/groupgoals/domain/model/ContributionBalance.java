package com.puntomartinez.millete.groupgoals.domain.model;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.math.BigDecimal;

/**
 * Saldo neto de un miembro dentro de una meta grupal.
 *
 * <p>El saldo disponible es lo que el miembro ha aportado menos lo que ya ha
 * retirado. Las retiradas nunca pueden superar ese saldo, de modo que un
 * miembro no pueda sacar dinero aportado por otros.</p>
 */
public final class ContributionBalance {

    private final BigDecimal totalDeposits;
    private final BigDecimal totalWithdrawals;

    private ContributionBalance(
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals
    ) {
        this.totalDeposits = amountOrZero(totalDeposits);
        this.totalWithdrawals = amountOrZero(totalWithdrawals);
    }

    public static ContributionBalance empty() {
        return new ContributionBalance(
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    public static ContributionBalance of(
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals
    ) {
        return new ContributionBalance(
                totalDeposits,
                totalWithdrawals
        );
    }

    public BigDecimal totalDeposits() {
        return totalDeposits;
    }

    public BigDecimal totalWithdrawals() {
        return totalWithdrawals;
    }

    /**
     * Saldo disponible para retirar. Nunca es negativo: si por datos
     * inconsistentes las retiradas superan a los aportes, se considera cero.
     */
    public BigDecimal available() {
        BigDecimal net = totalDeposits.subtract(totalWithdrawals);

        return net.signum() < 0
                ? BigDecimal.ZERO
                : net;
    }

    /**
     * Comprueba que el importe solicitado se puede retirar de este saldo.
     *
     * @throws InvalidInputException si el importe no es positivo o supera el
     *                               saldo disponible.
     */
    public void ensureCanWithdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidInputException(
                    "La cantidad a retirar debe ser mayor que cero."
            );
        }

        if (amount.compareTo(available()) > 0) {
            throw new InvalidInputException(
                    "No se puede retirar más dinero del disponible en el objetivo."
            );
        }
    }

    private static BigDecimal amountOrZero(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }
}
