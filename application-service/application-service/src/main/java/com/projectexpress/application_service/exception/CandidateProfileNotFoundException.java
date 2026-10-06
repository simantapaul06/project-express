package com.projectexpress.application_service.exception;

public class CandidateProfileNotFoundException extends RuntimeException {

    public CandidateProfileNotFoundException(String message) {
        super(message);
    }
}