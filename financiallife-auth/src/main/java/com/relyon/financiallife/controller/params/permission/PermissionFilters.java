package com.relyon.financiallife.controller.params.permission;

import com.relyon.financiallife.controller.params.BaseFilters;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "permissionFilter", description = "Filter for searching permissions")
public class PermissionFilters extends BaseFilters {

    @Schema(description = "The name of the permission", minLength = 1, maxLength = 41, example = "user:create")
    @Size(min = 1, max = 100, message = "name filter length must be between {min} and {max}")
    private String name;

    @Schema(description = "The name of the role", example = "manager", minLength = 1, maxLength = 50)
    @Size(min = 1, max = 50, message = "Role name must be between {min} and {max} characters long")
    private String role;
}