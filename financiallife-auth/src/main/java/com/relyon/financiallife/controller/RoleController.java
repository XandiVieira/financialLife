package com.relyon.financiallife.controller;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.role.RoleFilters;
import com.relyon.financiallife.exception.ErrorsResponse;
import com.relyon.financiallife.mapper.RoleMapper;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleRequest;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import com.relyon.financiallife.service.PermissionService;
import com.relyon.financiallife.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Requires authentication", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Requires authentication\"}"))),
        @ApiResponse(responseCode = "403", description = "User unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":403,\"message\":\"User unauthorized\"}"))),
        @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)))
})
@Tag(name = "Roles", description = "Operations related to roles")
@Slf4j
@Validated
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    @PostMapping("/")
    @Operation(summary = "Create new roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Roles created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicated key", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class)))
    })
    public ResponseEntity<List<RoleResponse>> createRoles(@RequestBody @Valid @NotEmpty(message = "Must not be empty") List<RoleRequest> rolesRequest) {
        log.info("Creating new roles");
        List<Role> roles = rolesRequest.stream()
                .map(request -> roleMapper.roleRequestToRoleModel(request, permissionService.getAllPermissionsByIds(request.getPermissions())))
                .toList();

        List<Role> createdRoles = roleService.createRoles(roles);

        List<RoleResponse> rolesResponse = createdRoles.stream()
                .map(roleMapper::roleToRoleResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(rolesResponse);
    }

    @GetMapping("/")
    @Operation(summary = "Get all roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles found", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleResponse.class)))),
            @ApiResponse(responseCode = "404", description = "No roles found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"No roles found\"}")))
    })
    public ResponseEntity<Page<RoleResponse>> getAllRoles(@Valid Pagination pagination, @Valid RoleFilters roleFilters, @Valid BaseSort baseSort) {
        log.info("Getting all roles");
        Page<Role> returnedRoles = roleService.getAllRoles(pagination, roleFilters, baseSort);
        Page<RoleResponse> rolesResponsePage = returnedRoles.map(roleMapper::roleToRoleResponse);
        return ResponseEntity.ok().body(rolesResponsePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a role by its ID")
    @Parameter(name = "id", description = "The ID of the role", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Role not found\"}")))
    })
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Integer id) {
        log.info("Getting role with ID {}", id);
        List<Role> returnedRoles = roleService.getAllRolesByIds(Collections.singletonList(id));
        RoleResponse roleResponse = roleMapper.roleToRoleResponse(returnedRoles.get(0));
        return ResponseEntity.ok(roleResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role by its ID")
    @Parameter(name = "id", description = "The ID of the role", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Role not found\"}"))),
            @ApiResponse(responseCode = "409", description = "Duplicated key", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class)))
    })
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Integer id, @RequestBody @Valid RoleRequest roleRequest) {
        log.info("Updating role with ID {} with new name {}", id, roleRequest.getName());
        Role role = roleMapper.roleRequestToRoleModel(roleRequest, permissionService.getAllPermissionsByIds(roleRequest.getPermissions()));
        Role updatedRole = roleService.updateRole(id, role);
        RoleResponse roleResponse = roleMapper.roleToRoleResponse(updatedRole);
        return ResponseEntity.ok(roleResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Role not found\"}")))
    })
    @Parameter(name = "id", description = "The ID of the role", required = true, example = "1")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        log.info("Deleting role with ID {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}