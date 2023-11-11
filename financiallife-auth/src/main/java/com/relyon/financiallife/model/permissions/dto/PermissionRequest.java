package com.relyon.financiallife.model.permissions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Permission details")
public class PermissionRequest {
    @NotBlank(message = "The name of the resource cannot be empty")
    @Size(min = 2, max = 20, message = "Permission name length must be between {min} and {max} characters long")
    @Schema(description = "The resource of the permission", example = "user", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 20)
    private String resource;

    @NotBlank(message = "The action of the permission cannot be empty")
    @Size(min = 2, max = 20, message = "Permission name length must be between {min} and {max} characters long")
    @Schema(description = "The action of the permission", example = "read", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 20)
    private String action;
}