package com.icc.itemservice.exception;

public class DuplicateUpcException extends RuntimeException {
    
    public DuplicateUpcException(String message) {
        super(message);
    }
    
    public DuplicateUpcException(String message, Throwable cause) {
        super(message, cause);
    }
}