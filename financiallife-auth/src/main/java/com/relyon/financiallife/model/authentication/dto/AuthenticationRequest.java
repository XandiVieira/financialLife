package com.relyon.financiallife.model.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details for authenticating a user")
public class AuthenticationRequest {
    @NotBlank(message = "Email cannot be empty")
    @Size(min = 2, max = 30, message = "Email must be between {min} and {max} characters long")
    @Email(message = "Invalid email format")
    @Schema(description = "The email of the user", example = "johndoe@email.com", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 5, maxLength = 50)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 120, message = "Password must be between {min} and {max} characters long")
    @Schema(description = "The password of the user", example = "mypassword", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 120)
    private String password;
}