package com.example.forms.form.dto;

import java.time.Instant;
import java.util.List;

public record FormSubmissionDetailsResponse(
    Long id,
    Instant submittedAt,
    List<FormSubmissionAnswerResponse> answers
) {
}
