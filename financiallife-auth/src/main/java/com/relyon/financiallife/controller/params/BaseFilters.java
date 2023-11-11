package com.relyon.financiallife.controller.params;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "baseFilters", description = "The basic/standard filters")
public class BaseFilters {

    @Schema(description = "The username of the user who created the record", minLength = 1, maxLength = 50, example = "email@example.com")
    @Size(min = 1, max = 50, message = "createdBy length must be between {min} and {max}")
    private String createdBy;

    @Schema(description = "The username of the user who last modified the record", minLength = 1, maxLength = 50, example = "email@example.com")
    @Size(min = 1, max = 50, message = "lastModifiedBy length must be between {min} and {max}")
    private String lastModifiedBy;
}