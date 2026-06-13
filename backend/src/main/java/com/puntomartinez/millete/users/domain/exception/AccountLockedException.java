package com.puntomartinez.millete.users.domain.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AccountLockedException extends RuntimeException {

    private final LocalDateTime lockTime;
    private final long remainingMinutes;

    public AccountLockedException(LocalDateTime lockTime, long remainingMinutes) {
        super(String.format("La cuenta se encuentra temporalmente bloqueada. Inténtalo de nuevo en %d minutos.", remainingMinutes));
        this.lockTime = lockTime;
        this.remainingMinutes = remainingMinutes;
    }
}