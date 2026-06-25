package com.puntomartinez.millete.shared.domain.exception;

public class InvalidInputException extends DomainException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
