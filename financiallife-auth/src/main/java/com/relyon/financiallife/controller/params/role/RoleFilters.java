package com.relyon.financiallife.controller.params.role;

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
@Schema(name = "roleFilter", description = "Filter for searching roles")
public class RoleFilters extends BaseFilters {

    @Schema(description = "The name of the role", minLength = 1, maxLength = 100, example = "admin")
    @Size(min = 1, max = 100, message = "name filter length must be between {min} and {max}")
    private String name;
}