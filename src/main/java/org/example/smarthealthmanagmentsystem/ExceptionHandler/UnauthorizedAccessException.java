package org.example.smarthealthmanagmentsystem.ExceptionHandler;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
