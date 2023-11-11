package com.relyon.financiallife.model.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Credentials for user authentication")
public class LoginRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 50, message = "Email length must be between {min} and {max} characters")
    @Schema(description = "User email", example = "johndoe@example.com", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 5, maxLength = 50)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 120, message = "Password length must be between {min} and {max} characters")
    @Schema(description = "User password", example = "my$trongPassw0rd", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 120)
    private String password;
}