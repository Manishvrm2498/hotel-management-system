package com.university.project.hotelmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record AIChatRequest(
        @NotBlank(message = "Prompt is required")
        String prompt
) {
}
