package com.example.forms.form.controller;

import com.example.forms.form.dto.CreateFormRequest;
import com.example.forms.form.dto.FormResponse;
import com.example.forms.form.dto.UpdateFormRequest;
import com.example.forms.form.service.FormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Forms", description = "Form management endpoints")
public class FormController {

    private final FormService formService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create form",
        description = "Creates a new form for the owner defined by X-User-Id",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Form creation payload",
            content = @Content(
                schema = @Schema(implementation = CreateFormRequest.class),
                examples = @ExampleObject(
                    name = "create-form-request",
                    value = """
                        {
                          \"title\": \"Customer Feedback\",
                          \"description\": \"Quarterly satisfaction survey\"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Form created",
            content = @Content(
                schema = @Schema(implementation = FormResponse.class),
                examples = @ExampleObject(
                    name = "create-form-response",
                    value = """
                        {
                          \"id\": 10,
                          \"title\": \"Customer Feedback\",
                          \"description\": \"Quarterly satisfaction survey\",
                          \"status\": \"DRAFT\",
                          \"slug\": \"ab12cd34ef56\",
                          \"createdAt\": \"2026-04-15T10:00:00Z\",
                          \"updatedAt\": \"2026-04-15T10:00:00Z\"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public FormResponse create(
        @Parameter(description = "Owner user id", required = true, example = "1")
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody @Valid CreateFormRequest request
    ) {
        return formService.create(userId, request);
    }

    @GetMapping
    @Operation(summary = "List owner forms", description = "Returns all forms for owner defined by X-User-Id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Forms returned",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = FormResponse.class)),
                examples = @ExampleObject(
                    name = "list-forms-response",
                    value = """
                        [
                          {
                            \"id\": 10,
                            \"title\": \"Customer Feedback\",
                            \"description\": \"Quarterly satisfaction survey\",
                            \"status\": \"DRAFT\",
                            \"slug\": \"ab12cd34ef56\",
                            \"createdAt\": \"2026-04-15T10:00:00Z\",
                            \"updatedAt\": \"2026-04-15T10:00:00Z\"
                          }
                        ]
                        """
                )
            ))
    })
    public List<FormResponse> list(
        @Parameter(description = "Owner user id", required = true, example = "1")
        @RequestHeader("X-User-Id") Long userId
    ) {
        return formService.findAllForOwner(userId);
    }

    @GetMapping("/{formId}")
    @Operation(summary = "Get form by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Form found",
            content = @Content(
                schema = @Schema(implementation = FormResponse.class),
                examples = @ExampleObject(
                    name = "get-form-response",
                    value = """
                        {
                          \"id\": 10,
                          \"title\": \"Customer Feedback\",
                          \"description\": \"Quarterly satisfaction survey\",
                          \"status\": \"PUBLISHED\",
                          \"slug\": \"ab12cd34ef56\",
                          \"createdAt\": \"2026-04-15T10:00:00Z\",
                          \"updatedAt\": \"2026-04-15T10:30:00Z\"
                        }
                        """
                )
            )),
        @ApiResponse(responseCode = "404", description = "Form not found")
    })
    public FormResponse get(
        @Parameter(description = "Form id", required = true, example = "10")
        @PathVariable Long formId
    ) {
        return formService.findById(formId);
    }

    @PatchMapping("/{formId}")
    @Operation(
        summary = "Update form",
        description = "Updates form title and description",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Form update payload",
            content = @Content(
                schema = @Schema(implementation = UpdateFormRequest.class),
                examples = @ExampleObject(
                    name = "update-form-request",
                    value = """
                        {
                          \"title\": \"Customer Feedback Q2\",
                          \"description\": \"Updated survey for Q2\"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Form updated",
            content = @Content(schema = @Schema(implementation = FormResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "Form not found")
    })
    public FormResponse update(
        @Parameter(description = "Form id", required = true, example = "10")
        @PathVariable Long formId,
        @RequestBody @Valid UpdateFormRequest request
    ) {
        return formService.update(formId, request);
    }

    @PostMapping("/{formId}/publish")
    @Operation(summary = "Publish form")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Form published",
            content = @Content(schema = @Schema(implementation = FormResponse.class))),
        @ApiResponse(responseCode = "404", description = "Form not found")
    })
    public FormResponse publish(
        @Parameter(description = "Form id", required = true, example = "10")
        @PathVariable Long formId
    ) {
        return formService.publish(formId);
    }

    @PostMapping("/{formId}/unpublish")
    @Operation(summary = "Unpublish form")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Form moved to draft",
            content = @Content(schema = @Schema(implementation = FormResponse.class))),
        @ApiResponse(responseCode = "404", description = "Form not found")
    })
    public FormResponse unpublish(
        @Parameter(description = "Form id", required = true, example = "10")
        @PathVariable Long formId
    ) {
        return formService.unpublish(formId);
    }
}
