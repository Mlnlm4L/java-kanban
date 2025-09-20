package ru.practikum.exception;

public class TaskTimeConflictException extends RuntimeException {
    public TaskTimeConflictException(String message) {
        super(message);
    }
}
