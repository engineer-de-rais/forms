package com.example.forms.form.dto;

import com.example.forms.form.entity.FormStatus;
import java.util.List;

public record PublicFormResponse(
    Long id,
    String title,
    String description,
    String slug,
    FormStatus status,
    String theme,
    boolean acceptingResponses,
    List<FormQuestionResponse> questions
) {
}
