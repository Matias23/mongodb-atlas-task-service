package com.mricotta.task.dto;

public record TaskResponse(
        String id,
        String title,
        String description,
        boolean completed) {
}
