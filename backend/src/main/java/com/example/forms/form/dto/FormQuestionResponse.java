package com.example.forms.form.dto;

import com.example.forms.form.entity.QuestionType;
import java.util.List;

public record FormQuestionResponse(
    Long id,
    int position,
    int section,
    String title,
    String description,
    QuestionType type,
    boolean required,
    List<FormQuestionOptionResponse> options,
    Integer linearMin,
    Integer linearMax,
    String linearMinLabel,
    String linearMaxLabel
) {
}
