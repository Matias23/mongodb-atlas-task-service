package com.mricotta.task.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String id) {
        super("Task with id %s not found".formatted(id));
    }
}
