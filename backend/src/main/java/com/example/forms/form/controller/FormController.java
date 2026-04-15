package com.example.forms.form.controller;

import com.example.forms.form.dto.CreateFormRequest;
import com.example.forms.form.dto.FormResponse;
import com.example.forms.form.dto.UpdateFormRequest;
import com.example.forms.form.service.FormService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FormResponse create(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody @Valid CreateFormRequest request
    ) {
        return formService.create(userId, request);
    }

    @GetMapping
    public List<FormResponse> list(@RequestHeader("X-User-Id") Long userId) {
        return formService.findAllForOwner(userId);
    }

    @GetMapping("/{formId}")
    public FormResponse get(@PathVariable Long formId) {
        return formService.findById(formId);
    }

    @PatchMapping("/{formId}")
    public FormResponse update(
        @PathVariable Long formId,
        @RequestBody @Valid UpdateFormRequest request
    ) {
        return formService.update(formId, request);
    }

    @PostMapping("/{formId}/publish")
    public FormResponse publish(@PathVariable Long formId) {
        return formService.publish(formId);
    }

    @PostMapping("/{formId}/unpublish")
    public FormResponse unpublish(@PathVariable Long formId) {
        return formService.unpublish(formId);
    }
}
