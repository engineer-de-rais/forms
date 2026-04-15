package com.example.forms.form.dto;

import java.time.Instant;

public record SubmitFormResponse(
    Long submissionId,
    Instant submittedAt
) {
}
