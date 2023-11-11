package com.relyon.financiallife.model.role.dto;

import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "The response DTO for roles")
public class RoleResponse {

    @Schema(description = "The ID of the role", example = "1", minLength = 1, maxLength = 3)
    private Integer id;

    @Schema(description = "The name of the role", example = "admin", minLength = 1, maxLength = 50)
    private String name;

    @Schema(description = "The permissions associated with the role", example = "[{\"id\": 1,\"name\": \"user:view\"},{\"id\": 2,\"name\": \"role:create\"}]\n")
    private List<PermissionResponse> permissions;
}