package com.relyon.financiallife.controller.params;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "sort", description = "Field for sorting searches")
public class BaseSort {
    @Schema(description = "The sorting criteria", minLength = 1, maxLength = 100, example = "createdAt,-createdBy", defaultValue = "createdAt")
    @Size(min = 1, max = 100, message = "sort length must be between {min} and {max}")
    private String sort = "createdAt";
}