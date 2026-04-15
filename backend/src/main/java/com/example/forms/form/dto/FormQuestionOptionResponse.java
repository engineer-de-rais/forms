package com.example.forms.form.dto;

public record FormQuestionOptionResponse(
    Long id,
    int position,
    String value,
    Integer goToSection
) {
}
