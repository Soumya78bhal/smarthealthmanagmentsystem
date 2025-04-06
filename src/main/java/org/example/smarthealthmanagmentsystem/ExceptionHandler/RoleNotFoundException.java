package org.example.smarthealthmanagmentsystem.ExceptionHandler;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
