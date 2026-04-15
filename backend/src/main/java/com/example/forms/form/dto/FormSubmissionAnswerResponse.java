package com.example.forms.form.dto;

import com.example.forms.form.entity.QuestionType;

public record FormSubmissionAnswerResponse(
    Long questionId,
    String questionTitle,
    QuestionType questionType,
    String value
) {
}
