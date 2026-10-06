package com.projectexpress.application_service.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }
}