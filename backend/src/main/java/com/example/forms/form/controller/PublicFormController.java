package com.example.forms.form.controller;

import com.example.forms.form.dto.PublicFormResponse;
import com.example.forms.form.dto.SubmitFormRequest;
import com.example.forms.form.dto.SubmitFormResponse;
import com.example.forms.form.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/forms")
@RequiredArgsConstructor
public class PublicFormController {

    private final FormService formService;

    @GetMapping("/{slug}")
    public PublicFormResponse getPublicForm(@PathVariable String slug) {
        return formService.getPublicForm(slug);
    }

    @PostMapping("/{slug}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmitFormResponse submit(@PathVariable String slug, @RequestBody @Valid SubmitFormRequest request) {
        return formService.submitPublicForm(slug, request);
    }
}
