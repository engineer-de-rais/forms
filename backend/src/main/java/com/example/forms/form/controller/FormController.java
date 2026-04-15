package com.example.forms.form.controller;

import com.example.forms.form.dto.CreateFormRequest;
import com.example.forms.form.dto.FormDetailsResponse;
import com.example.forms.form.dto.FormResponse;
import com.example.forms.form.dto.FormResultsResponse;
import com.example.forms.form.dto.FormSubmissionDetailsResponse;
import com.example.forms.form.dto.FormSubmissionItemResponse;
import com.example.forms.form.dto.UpdateFormRequest;
import com.example.forms.form.dto.UpdateFormStructureRequest;
import com.example.forms.form.service.FormService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public FormResponse get(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        return formService.findById(userId, formId);
    }

    @PatchMapping("/{formId}")
    public FormResponse update(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId,
        @RequestBody @Valid UpdateFormRequest request
    ) {
        return formService.update(userId, formId, request);
    }

    @PostMapping("/{formId}/publish")
    public FormResponse publish(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        return formService.publish(userId, formId);
    }

    @PostMapping("/{formId}/unpublish")
    public FormResponse unpublish(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        return formService.unpublish(userId, formId);
    }

    @GetMapping("/{formId}/structure")
    public FormDetailsResponse getStructure(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        return formService.getStructure(userId, formId);
    }

    @PutMapping("/{formId}/structure")
    public FormDetailsResponse updateStructure(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId,
        @RequestBody @Valid UpdateFormStructureRequest request
    ) {
        return formService.updateStructure(userId, formId, request);
    }

    @GetMapping("/{formId}/responses")
    public List<FormSubmissionItemResponse> getResponses(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        return formService.getSubmissions(userId, formId);
    }

    @GetMapping("/{formId}/responses/{submissionId}")
    public FormSubmissionDetailsResponse getResponseDetails(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId,
        @PathVariable Long submissionId
    ) {
        return formService.getSubmissionDetails(userId, formId, submissionId);
    }

    @GetMapping("/{formId}/results")
    public FormResultsResponse getResults(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        return formService.getResults(userId, formId);
    }

    @GetMapping("/{formId}/responses.csv")
    public ResponseEntity<byte[]> exportResponsesCsv(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long formId
    ) {
        String csv = formService.exportSubmissionsCsv(userId, formId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"form-" + formId + "-responses.csv\"")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
