package com.relyon.financiallife.controller.params.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "rolesData", description = "Filter by roles related fields")
public class RolesData {

    @Schema(description = "The roles assigned to the user", minLength = 1, maxLength = 100, example = "admin,user")
    @Size(min = 1, max = 100, message = "roles length must be between {min} and {max}")
    private String roles;

    @Schema(description = "Whether the search by roles is inclusive or not",
            minLength = 4, maxLength = 5, defaultValue = "true", example = "false")
    private boolean rolesSearchInclusive = true;
}