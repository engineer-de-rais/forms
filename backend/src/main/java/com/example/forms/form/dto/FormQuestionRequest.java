package com.example.forms.form.dto;

import com.example.forms.form.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FormQuestionRequest(
    Integer section,
    @NotBlank @Size(max = 500) String title,
    @Size(max = 2048) String description,
    @NotNull QuestionType type,
    boolean required,
    @Valid List<FormQuestionOptionRequest> options,
    Integer linearMin,
    Integer linearMax,
    @Size(max = 255) String linearMinLabel,
    @Size(max = 255) String linearMaxLabel
) {
}
