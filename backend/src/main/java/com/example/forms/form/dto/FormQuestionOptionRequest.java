package com.example.forms.form.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FormQuestionOptionRequest(
    @NotBlank @Size(max = 500) String value,
    Integer goToSection
) {
}
