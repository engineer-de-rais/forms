package com.example.forms.form.dto;

import jakarta.validation.Valid;
import java.util.List;

public record SubmitFormRequest(
    @Valid List<SubmitFormAnswerRequest> answers
) {
}
