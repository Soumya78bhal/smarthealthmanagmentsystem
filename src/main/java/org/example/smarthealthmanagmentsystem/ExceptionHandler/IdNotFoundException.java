package org.example.smarthealthmanagmentsystem.ExceptionHandler;

public class IdNotFoundException extends RuntimeException {
    public IdNotFoundException( String msg) {
        super(msg);
    }
}
