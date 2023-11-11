package com.relyon.financiallife.controller;

import com.relyon.financiallife.exception.ErrorsResponse;
import com.relyon.financiallife.model.authentication.dto.AuthenticationRequest;
import com.relyon.financiallife.model.authentication.dto.SuccessfulAuthenticationResponse;
import com.relyon.financiallife.service.AuthenticationService;
import com.relyon.financiallife.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authentication/")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authenticate User")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true)))
})
@Slf4j
public class AuthenticationController {

    private final AuthenticationService service;
    private final BlacklistService blacklistService;

    @PostMapping("authenticate")
    @Operation(summary = "Authenticates a user and returns an access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User authenticated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Invalid email or password\"}")))
    })
    public ResponseEntity<?> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());
        return service.authenticate(request);
    }

    @PostMapping("logout")
    @Operation(summary = "Revokes a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Token Revoked Successfully", content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Authorization token was not sent properly", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorsResponse.class), examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authorization token was not sent properly\"}")))
    })
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        log.info("Revoking token");
        blacklistService.revokeToken(request);
        return ResponseEntity.noContent().header("Message", "Token was revoked successfully").build();
    }
}