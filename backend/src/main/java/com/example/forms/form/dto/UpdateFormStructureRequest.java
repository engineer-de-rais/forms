package com.example.forms.form.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateFormStructureRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 2048) String description,
    @NotBlank @Size(max = 64) String theme,
    boolean acceptingResponses,
    boolean publicAccess,
    @Valid List<FormQuestionRequest> questions
) {
}
