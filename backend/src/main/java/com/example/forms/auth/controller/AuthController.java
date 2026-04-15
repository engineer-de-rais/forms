package com.example.forms.auth.controller;

import com.example.forms.auth.dto.AuthResponse;
import com.example.forms.auth.dto.LoginRequest;
import com.example.forms.auth.dto.RegisterRequest;
import com.example.forms.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and user onboarding endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register user",
        description = "Creates a new user and returns JWT token",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "User registration payload",
            content = @Content(
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(
                    name = "register-request",
                    value = """
                        {
                          \"email\": \"anna@example.com\",
                          \"password\": \"StrongPass123\",
                          \"name\": \"Anna\"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created",
            content = @Content(
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "register-response",
                    value = """
                        {
                          \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\",
                          \"userId\": 1,
                          \"email\": \"anna@example.com\",
                          \"name\": \"Anna\"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed or email already exists")
    })
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticates user credentials and returns JWT token",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "User login payload",
            content = @Content(
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    name = "login-request",
                    value = """
                        {
                          \"email\": \"anna@example.com\",
                          \"password\": \"StrongPass123\"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authenticated",
            content = @Content(
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "login-response",
                    value = """
                        {
                          \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\",
                          \"userId\": 1,
                          \"email\": \"anna@example.com\",
                          \"name\": \"Anna\"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid credentials or validation failed")
    })
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
