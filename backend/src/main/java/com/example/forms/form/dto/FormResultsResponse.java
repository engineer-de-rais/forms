package com.example.forms.form.dto;

import java.util.List;

public record FormResultsResponse(
    Long formId,
    long totalResponses,
    List<QuestionResultsResponse> questions
) {
}
