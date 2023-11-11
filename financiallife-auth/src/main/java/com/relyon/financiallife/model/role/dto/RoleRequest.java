package com.relyon.financiallife.model.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Details for creating a new role")
public class RoleRequest {
    @NotBlank(message = "Role name cannot be empty")
    @Size(min = 1, max = 50, message = "Role name must be between {min} and {max} characters long")
    @Schema(description = "The name of the role", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 50)
    private String name;

    @Schema(description = "The list of permissions assigned to the role", example = "[1, 2]")
    private List<Integer> permissions;

    public RoleRequest(String name) {
        this.name = name;
    }
}