package com.puntomartinez.millete.dataexport.domain.exception;

public class OwnershipException extends RuntimeException {

    private final String errorCode;

    public OwnershipException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}