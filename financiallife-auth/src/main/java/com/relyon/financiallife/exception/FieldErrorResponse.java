package com.relyon.financiallife.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldErrorResponse {

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Field", example = "field with error")
    private String field;

    @Schema(description = "Message", example = "Error field description")
    private String message;
}