package com.relyon.financiallife.exception;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ErrorsResponse {

    @Schema(description = "400")
    private int status;

    @Schema(description = "Message", example = "Descriptive message")
    private String message;

    @Schema(description = "Errors found.")
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private List<FieldErrorResponse> errors;

    public ErrorsResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}