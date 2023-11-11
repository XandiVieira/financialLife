package com.relyon.financiallife.model.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing a token")
public class FailedAuthenticationResponse {

    @Schema(description = "Error message", example = "Invalid email or password")
    private String errorMessage;

    @Schema(description = "Remaining login attempts before account is blocked.", example = "2")
    private int remainingAttempts;
}