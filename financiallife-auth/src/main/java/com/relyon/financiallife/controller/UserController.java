package com.relyon.financiallife.controller;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.user.UserFilters;
import com.relyon.financiallife.exception.ErrorsResponse;
import com.relyon.financiallife.mapper.UserMapper;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.dto.request.CreateUserRequest;
import com.relyon.financiallife.model.user.dto.request.UpdateUserRequest;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import com.relyon.financiallife.service.RoleService;
import com.relyon.financiallife.service.UserService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Requires authentication", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Requires authentication\"}"))),
        @ApiResponse(responseCode = "403", description = "User unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":403,\"message\":\"User unauthorized\"}"))),
        @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)))
})
@Tag(name = "Users", description = "Operations related to users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleService roleService;

    @PostMapping("/")
    @Operation(summary = "Create user", description = "Creates a new user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicated key", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class)))
    })
    public ResponseEntity<UserResponse> createUser(@Parameter(description = "User details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUserRequest.class))) @RequestBody @Valid CreateUserRequest createUserRequest) {
        log.info("Creating user: {}", createUserRequest.getEmail());
        User user = userMapper.createUserRequestToUserModel(createUserRequest, roleService.getAllRolesByIds(createUserRequest.getRoles()));
        User updatedUser = userService.createUser(user);
        UserResponse userResponse = userMapper.userToUserResponse(updatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/")
    @Operation(summary = "Get all users", description = "Returns a list of all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "404", description = "No users found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"No users found\"}")))
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(@Valid Pagination pagination, @Valid UserFilters userFilters, @Valid BaseSort userSort) {
        log.info("Getting all users");
        Page<User> returnedUsersPage = userService.getAllUsers(pagination, userFilters, userSort);
        Page<UserResponse> userResponsePage = returnedUsersPage.map(userMapper::userToUserResponse);
        return ResponseEntity.ok().body(userResponsePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns a user by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"User not found\"}")))
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable @Parameter(description = "User ID", example = "1") Long id) {
        log.info("Getting user with ID: {}", id);
        User returnedUser = userService.getUserById(id);
        UserResponse userResponse = userMapper.userToUserResponse(returnedUser);
        return ResponseEntity.ok().body(userResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"User not found\"}"))),
            @ApiResponse(responseCode = "409", description = "Duplicated key", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class)))
    })
    public ResponseEntity<UserResponse> updateUser(@PathVariable @Parameter(description = "User ID", example = "1") Long id, @Parameter(description = "Updated user details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateUserRequest.class))) @RequestBody @Valid UpdateUserRequest updateUserRequest) {
        log.info("Updating user with ID: {}", id);
        User user = userMapper.updateUserRequestToUserModel(updateUserRequest, roleService.getAllRolesByIds(updateUserRequest.getRoles()));
        User updatedUser = userService.updateUser(id, user);
        UserResponse userResponse = userMapper.userToUserResponse(updatedUser);
        return ResponseEntity.ok().body(userResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":404,\"message\":\"User not found\"}")))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable @Parameter(description = "User ID", example = "1") Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}