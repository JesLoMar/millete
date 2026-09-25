package com.puntomartinez.millete.users.infrastructure.in.controller.advice;

import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import com.puntomartinez.millete.users.domain.exception.AccountLockedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class UserExceptionHandler {

    private final TimeProvider timeProvider;

    public UserExceptionHandler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountLockedException(
            AccountLockedException ex,
            HttpServletRequest request) {

        log.warn("Cuenta bloqueada: {}", ex.getMessage());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                timeProvider.instantNow(),
                HttpStatus.LOCKED.value(),
                HttpStatus.LOCKED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);
    }
}