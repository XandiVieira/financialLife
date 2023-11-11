package com.relyon.financiallife.model.password.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Password redefinition response")
public class PasswordResetResponse {

    @Schema(description = "Password reset feedback", example = "The password reset request was successful", minLength = 1, maxLength = 100)
    private String message;
}