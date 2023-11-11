package com.relyon.financiallife.controller;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.permission.PermissionFilters;
import com.relyon.financiallife.exception.ErrorsResponse;
import com.relyon.financiallife.mapper.PermissionMapper;
import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.permissions.dto.PermissionRequest;
import com.relyon.financiallife.model.permissions.dto.PermissionResponse;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import com.relyon.financiallife.service.PermissionService;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Requires authentication", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Requires authentication\"}"))),
        @ApiResponse(responseCode = "403", description = "User unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":403,\"message\":\"User unauthorized\"}"))),
        @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)))
})
@Tag(name = "Permissions", description = "Operations related to permissions")
@Slf4j
@Validated
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @PostMapping("/")
    @Operation(summary = "Create new permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Permissions created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicated key", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class)))
    })
    public ResponseEntity<List<PermissionResponse>> createPermissions(@RequestBody @Valid @NotEmpty(message = "Must not be empty") List<PermissionRequest> permissionsRequest) {
        log.info("Converting permissions");
        List<Permission> permissions = permissionsRequest.stream()
                .map(permissionMapper::permissionRequestToPermissionModel)
                .toList();

        List<Permission> createdPermissions = permissionService.createPermissions(permissions);

        List<PermissionResponse> permissionsResponse = createdPermissions.stream().map(permissionMapper::permissionToPermissionResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionsResponse);
    }

    @GetMapping("/")
    @Operation(summary = "Get all permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions found", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PermissionResponse.class)))),
            @ApiResponse(responseCode = "404", description = "No permissions found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"No permissions found\"}")))
    })
    public ResponseEntity<Page<PermissionResponse>> getAllPermissions(@Valid Pagination pagination, @Valid PermissionFilters permissionFilters, @Valid BaseSort baseSort) {
        log.info("Getting all permissions");
        Page<Permission> returnedPermissions = permissionService.getAllPermissions(pagination, permissionFilters, baseSort);
        Page<PermissionResponse> permissionsResponsePage = returnedPermissions.map(permissionMapper::permissionToPermissionResponse);
        return ResponseEntity.ok().body(permissionsResponsePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a permission by its ID")
    @Parameter(name = "id", description = "The ID of the permission", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Permission not found\"}")))
    })
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Integer id) {
        log.info("Getting permission with ID {}", id);
        List<Permission> returnedPermissions = permissionService.getAllPermissionsByIds(Collections.singletonList(id));
        List<PermissionResponse> permissionResponse = returnedPermissions.stream().map(permissionMapper::permissionToPermissionResponse).toList();
        return ResponseEntity.ok().body(permissionResponse.get(0));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a permission by its ID")
    @Parameter(name = "id", description = "The ID of the permission", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Permission not found\"}"))),
            @ApiResponse(responseCode = "409", description = "Duplicated key", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class)))
    })
    public ResponseEntity<PermissionResponse> updatePermission(@PathVariable Integer id, @RequestBody @Valid PermissionRequest permissionRequest) {
        log.info("Updating permission with ID {} with new name {}", id, (permissionRequest.getResource() + ":" + permissionRequest.getAction()));
        Permission permission = permissionMapper.permissionRequestToPermissionModel(permissionRequest);
        Permission updatedPermission = permissionService.updatePermission(id, permission);
        PermissionResponse permissionResponse = permissionMapper.permissionToPermissionResponse(updatedPermission);
        return ResponseEntity.ok().body(permissionResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Permission deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Permission not found\"}")))
    })
    @Parameter(name = "id", description = "The ID of the permission", required = true, example = "1")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer id) {
        log.info("Deleting permission with ID {}", id);
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}