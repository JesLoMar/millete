package com.puntomartinez.millete.shared.domain.exception;

public class ResourceAlreadyExistsException extends DomainException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
