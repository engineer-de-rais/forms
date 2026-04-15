package com.example.forms.form.dto;

import java.time.Instant;

public record FormSubmissionItemResponse(
    Long id,
    Instant submittedAt
) {
}
