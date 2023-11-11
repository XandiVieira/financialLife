package com.relyon.financiallife.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relyon.financiallife.exception.ErrorsResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            String uri = request.getRequestURI();
            log.warn("User: {} attempted to access the protected URL: {}", username, uri);
        } else {
            log.warn("An anonymous user attempted to access a protected URL: {}", request.getRequestURI());
        }
        buildResponseError(response);
    }

    private static void buildResponseError(HttpServletResponse response) throws IOException {
        log.debug("Building access denied error response");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ObjectMapper mapper = new ObjectMapper();
        ErrorsResponse responseObject = new ErrorsResponse(HttpStatus.FORBIDDEN.value(), "Access denied");
        String json = mapper.writeValueAsString(responseObject);
        PrintWriter writer = response.getWriter();
        writer.write(json);
    }
}