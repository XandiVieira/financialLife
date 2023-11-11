package com.relyon.financiallife.model.password.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Password redefinition response")
public class PasswordResetEmailResponse {

    @Schema(description = "Password reset feedback", example = "The password reset request was successful", minLength = 1, maxLength = 100)
    private String message;

    @Schema(description = "Expiration time for password redefinition block", example = "2023-06-14T10:30:00Z")
    private LocalDateTime passwordRedefinitionBlockExpirationTime;
}