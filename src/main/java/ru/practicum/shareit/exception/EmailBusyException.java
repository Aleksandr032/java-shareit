package ru.practicum.shareit.exception;

public class EmailBusyException extends RuntimeException {
    public EmailBusyException(String message) {
        super(message);
    }
}
