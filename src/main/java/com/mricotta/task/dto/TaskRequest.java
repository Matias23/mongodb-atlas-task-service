package com.mricotta.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @NotNull Boolean completed) {
}
