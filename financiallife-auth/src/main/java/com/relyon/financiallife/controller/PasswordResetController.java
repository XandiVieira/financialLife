package com.relyon.financiallife.controller;

import com.relyon.financiallife.exception.ErrorsResponse;
import com.relyon.financiallife.model.password.dto.PasswordResetEmailResponse;
import com.relyon.financiallife.model.password.dto.PasswordResetResponse;
import com.relyon.financiallife.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password-reset")
@RequiredArgsConstructor
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
        @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)))
})
@Tag(name = "Password Reset", description = "Endpoints for resetting user passwords")
@Slf4j
@Validated
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/email-confirmation")
    @Operation(summary = "Sends a password reset email", description = "Sends an email to the user to reset their password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
            @ApiResponse(responseCode = "404", description = "Email not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Email not found\"}")))
    })
    public ResponseEntity<PasswordResetEmailResponse> processForgotPasswordForm(@Parameter(description = "The email address of the user", example = "user@example.com", required = true)
                                                                           @RequestParam("email") @NotBlank(message = "The email cannot be empty") @Email(message = "Invalid email format") @Size(min = 5, max = 50, message = "Email must be between {min} and {max} characters long") String email) {
        log.info("Password reset email sent successfully for email: {}", email);
        return passwordResetService.sendPasswordResetEmail(email);
    }

    @PostMapping(value = "/reset")
    @Operation(summary = "Resets a user's password", description = "Resets the password of the user associated with the given token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Invalid or expired token\"}")))
    })
    public ResponseEntity<PasswordResetResponse> processResetPasswordForm(@Parameter(description = "The token sent in the email to reset the password", example = "some-token", required = true)
                                                                          @RequestHeader("token") @NotBlank(message = "token cannot be empty") String tokenValue,
                                                                          @Parameter(description = "The new password for the user", example = "new-password", required = true)
                                                                          @RequestHeader("password") @NotBlank(message = "password cannot be empty") @Size(min = 8, max = 120, message = "password must be between {min} and {max} characters long") String password,
                                                                          @RequestHeader("passwordConfirmation") @NotBlank(message = "passwordConfirmation cannot be empty") @Size(min = 8, max = 120, message = "passwordConfirmation must be between {min} and {max} characters long") String passwordConfirmation) {
        log.info("Password reset successfully");
        return passwordResetService.resetPassword(tokenValue, password, passwordConfirmation);
    }
}