package com.relyon.financiallife.model.user.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.relyon.financiallife.mapper.serializer.LocalDateDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Details for creating a new user")
public class CreateUserRequest {

    @NotBlank(message = "The firstName cannot be empty")
    @Size(min = 2, max = 50, message = "First name must be between {min} and {max} characters long")
    @Schema(description = "The first name of the user", example = "John", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
    private String firstName;

    @NotBlank(message = "The lastName cannot be empty")
    @Size(min = 2, max = 50, message = "Last name must be between {min} and {max} characters long")
    @Schema(description = "The last name of the user", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
    private String lastName;

    @NotBlank(message = "The username cannot be empty")
    @Size(min = 2, max = 50, message = "Username must be between {min} and {max} characters long")
    @Schema(description = "The username of the user", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
    private String username;

    @NotNull(message = "The date of birth cannot be empty")
    @PastOrPresent(message = "dateOfBirth must be in the past or present")
    @Schema(description = "The user's date of birth", type = "string", pattern = "dd/MM/yyyy", example = "17/02/2020", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 10, maxLength = 10)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dateOfBirth;

    @NotBlank(message = "The CPF cannot be empty")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "The CPF must be in the format XXX.XXX.XXX-XX")
    @Size(min = 11, max = 14, message = "The CPF length must be between {min} and {max} characters")
    @Schema(description = "The user's CPF", example = "123.456.789-00", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 11, maxLength = 14)
    private String cpf;

    @NotBlank(message = "The cellphone number cannot be empty")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "The cellphone number must be in the format (XX) XXXX-XXXX or (XX) XXXXX-XXXX")
    @Size(min = 14, max = 15, message = "The cellphone number length must be between {min} and {max} characters")
    @Schema(description = "The user's cellphone number", example = "(99) 91234-5678", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 14, maxLength = 15)
    private String cellphoneNumber;

    @NotBlank(message = "The email cannot be empty")
    @Size(min = 5, max = 50, message = "Email must be between {min} and {max} characters long")
    @Email(message = "Invalid email format")
    @Schema(description = "The email of the user", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 5, maxLength = 50)
    private String email;

    @Schema(description = "The list of roles assigned to the user", example = "[1, 2]")
    private List<Integer> roles;
}