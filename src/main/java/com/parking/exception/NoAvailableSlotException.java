package com.parking.exception;

public class NoAvailableSlotException extends RuntimeException {

    public NoAvailableSlotException(String message) {
        super(message);
    }
}


