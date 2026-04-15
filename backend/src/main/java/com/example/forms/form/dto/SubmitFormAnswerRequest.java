package com.example.forms.form.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SubmitFormAnswerRequest(
    @NotNull Long questionId,
    String value,
    List<String> values
) {
}
