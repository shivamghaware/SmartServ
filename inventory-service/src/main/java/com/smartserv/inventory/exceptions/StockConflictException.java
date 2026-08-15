package com.smartserv.inventory.exceptions;

public class StockConflictException extends RuntimeException {
    public StockConflictException(String message) {
        super(message);
    }
}
