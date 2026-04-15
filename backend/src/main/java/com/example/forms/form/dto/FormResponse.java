package com.example.forms.form.dto;

import com.example.forms.form.entity.FormStatus;
import java.time.Instant;

public record FormResponse(
    Long id,
    String title,
    String description,
    FormStatus status,
    String slug,
    Instant createdAt,
    Instant updatedAt
) {
}
