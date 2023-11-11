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
public class SuccessfulAuthenticationResponse {

    @Schema(description = "Access token", example = "eyJhbGcixxxxUzI1Nixx.eyJzdWIiOiJhbGV4YW5kcmUudmllaXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxODA3MTkxODUsImV4cCI6MTY4MDxxxDk4NX0.XXfIrbfxxxxbVc1EZwAWVR12l_rOxxxxx3Pi2mK7OJg", minLength = 164, maxLength = 164)
    private String token;
}