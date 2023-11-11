package com.relyon.financiallife.controller.params;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "pagination", description = "Fields for defining pagination")
public class Pagination {

    @Schema(description = "The number of the page wanted", minLength = 1, maxLength = 3, defaultValue = "0", example = "2")
    @Range(min = 0, max = 100, message = "pageNumber must be between {min} than {max}")
    private Integer pageNumber = 0;

    @Schema(description = "The size of the page wanted", minLength = 1, maxLength = 3, defaultValue = "10", example = "50")
    @Range(min = 1, max = 100, message = "pageSize must be between {min} and {max}")
    private Integer pageSize = 10;
}