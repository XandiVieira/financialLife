package com.relyon.financiallife.model.permissions.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "The response DTO for permissions")
public class PermissionResponse {

    @Schema(description = "The ID of the permission", example = "1", minLength = 1, maxLength = 3)
    private Integer id;

    @Schema(description = "The name of the permission", example = "user:view", minLength = 1, maxLength = 41)
    private String name;

    @JsonIgnore
    @Schema(description = "The resource in the permission", example = "user", minLength = 1, maxLength = 20, hidden = true)
    public String getResource() {
        int separatorIndex = name.indexOf(":");
        return separatorIndex == -1 ? name : name.substring(0, separatorIndex);
    }

    @JsonIgnore
    @Schema(description = "The action in the permission", example = "view", minLength = 1, maxLength = 20, hidden = true)
    public String getAction() {
        int separatorIndex = name.indexOf(":");
        return separatorIndex == -1 ? "" : name.substring(separatorIndex + 1);
    }
}