package com.relyon.financiallife.controller.params.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.relyon.financiallife.mapper.serializer.LocalDateDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "userFilter", description = "Filter for searching users")
public class UserFilters {

    @Schema(description = "The first name of the user", minLength = 1, maxLength = 50, example = "John")
    @Size(min = 1, max = 50, message = "firstName length must be between {min} and {max}")
    private String firstName;

    @Schema(description = "The last name of the user", minLength = 1, maxLength = 50, example = "Doe")
    @Size(min = 1, max = 50, message = "lastName length must be between {min} and {max}")
    private String lastName;

    @Schema(description = "The username of the user", minLength = 1, maxLength = 50, example = "johndoe")
    @Size(min = 1, max = 50, message = "username length must be between {min} and {max}")
    private String username;

    @Schema(description = "The date of birth of the user", format = "date", example = "01/01/1997", implementation = LocalDate.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @PastOrPresent(message = "dateOfBirth must be in the past or present")
    private LocalDate dateOfBirth;

    @Schema(description = "The CPF number of the user", minLength = 1, maxLength = 11, example = "12345678901")
    @Size(min = 1, max = 11, message = "cpf length must be between {min} and {max}")
    private String cpf;

    @Schema(description = "The cellphone number of the user", minLength = 1, maxLength = 11, example = "11987654321")
    @Size(min = 1, max = 11, message = "cellphoneNumber length must be between {min} and {max}")
    private String cellphoneNumber;

    @Schema(description = "The email address of the user", format = "email", minLength = 1, maxLength = 50, example = "johndoe@example.com")
    @Email(message = "email must be a valid email address")
    @Size(min = 1, max = 50, message = "email length must be between {min} and {max}")
    private String email;

    @Schema(description = "The creator of the user record", minLength = 1, maxLength = 50, example = "email@example.com")
    @Size(min = 1, max = 50, message = "createdBy length must be between {min} and {max}")
    private String createdBy;

    @Schema(description = "The last modifier of the user record", minLength = 1, maxLength = 50, example = "email@example.com")
    @Size(min = 1, max = 50, message = "lastModifiedBy length must be between {min} and {max}")
    private String lastModifiedBy;

    @Schema(description = "Whether the user is enabled or not", example = "true")
    private Boolean enabled = true;

    @Schema(hidden = true)
    private RolesData rolesData = new RolesData();

    @Schema(description = "The roles assigned to the user", minLength = 1, maxLength = 100, example = "admin,user")
    public void setRoles(String roles) {
        if (this.rolesData == null) {
            this.rolesData = new RolesData();
        }
        this.rolesData.setRoles(roles);
    }

    @Schema(description = "Whether the search by roles is inclusive or not",
            minLength = 4, maxLength = 5, defaultValue = "true", example = "false")
    public void setRolesOperator(boolean rolesOperator) {
        if (this.rolesData == null) {
            this.rolesData = new RolesData();
        }
        this.rolesData.setRolesSearchInclusive(rolesOperator);
    }
}