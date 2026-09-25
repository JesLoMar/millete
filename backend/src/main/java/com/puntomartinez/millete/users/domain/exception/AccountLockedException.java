package com.puntomartinez.millete.users.domain.exception;

import com.puntomartinez.millete.shared.domain.exception.DomainException;
import lombok.Getter;

import java.time.Instant;

@Getter
public class AccountLockedException extends DomainException {

    private final Instant lockTime;
    private final long remainingMinutes;

    public AccountLockedException(Instant lockTime, long remainingMinutes) {
        super(String.format(
                "La cuenta se encuentra temporalmente bloqueada. "
                        + "Inténtalo de nuevo en %d minutos.",
                remainingMinutes
        ));
        this.lockTime = lockTime;
        this.remainingMinutes = remainingMinutes;
    }
}