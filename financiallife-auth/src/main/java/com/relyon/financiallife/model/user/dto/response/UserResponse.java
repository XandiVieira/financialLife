package com.relyon.financiallife.model.user.dto.response;

import com.relyon.financiallife.model.role.dto.RoleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Details")
public class UserResponse {

    @Schema(description = "The user's ID number", example = "544", minLength = 1, maxLength = 19)
    private Long id;

    @Schema(description = "The user's first name", example = "John", minLength = 2, maxLength = 30)
    private String firstName;

    @Schema(description = "The user's last name", example = "Doe", minLength = 2, maxLength = 30)
    private String lastName;

    @Schema(description = "The user's username", example = "johndoe", minLength = 2, maxLength = 30)
    private String username;

    @Schema(description = "The user's date of birth", example = "10/02/1985", minLength = 10, maxLength = 10)
    private String dateOfBirth;

    @Schema(description = "The age of the user", example = "35")
    private Integer age;

    @Schema(description = "The user's CPF", example = "123.456.789-00", minLength = 11, maxLength = 14)
    private String cpf;

    @Schema(description = "The user's cellphone number", example = "(99) 91234-5678", minLength = 14, maxLength = 15)
    private String cellphoneNumber;

    @Schema(description = "The user's email address", example = "johndoe@example.com", minLength = 5, maxLength = 50)
    private String email;

    @Schema(description = "Last user login date and time", example = "30/06/2022 10:49:22")
    private String lastLogin;

    @Schema(description = "Whether the user is enabled or not", example = "true", minLength = 4, maxLength = 5)
    private boolean enabled;

    @Schema(description = "The roles associated with the user", example = "[{\"id\": 1,\"name\": \"admin\"},{\"id\": 2,\"name\": \"user\"}]\n")
    private List<RoleResponse> roles;

    @Schema(description = "The age of the user", example = "35")
    public Integer getAge() {
        return Period.between(LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd/MM/yyyy")), LocalDate.now()).getYears();
    }
}