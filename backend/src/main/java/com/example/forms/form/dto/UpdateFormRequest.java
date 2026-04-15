package com.example.forms.form.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFormRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 2048) String description
) {
}
