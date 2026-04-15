package com.example.forms.form.dto;

import com.example.forms.form.entity.FormStatus;
import java.time.Instant;
import java.util.List;

public record FormDetailsResponse(
    Long id,
    String title,
    String description,
    FormStatus status,
    String slug,
    String theme,
    boolean acceptingResponses,
    boolean publicAccess,
    Instant createdAt,
    Instant updatedAt,
    List<FormQuestionResponse> questions
) {
}
