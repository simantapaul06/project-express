package com.projectexpress.application_service.exception;

public class JobServiceUnavailableException extends RuntimeException{

    public JobServiceUnavailableException(String message){
        super(message);
    }
}
