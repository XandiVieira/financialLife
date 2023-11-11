package com.relyon.financiallife.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class WebSecurityConfiguration implements WebMvcConfigurer {

    @Value("${cors.allowed-origin}")
    private String allowedOrigin;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String ADMIN = "ADMIN";
    public static final String PATH_PARAM = "/api/v1/";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String usersPath = PATH_PARAM + "users/**";
        String rolesPath = PATH_PARAM + "roles/**";
        String permissionsPath = PATH_PARAM + "permissions/**";

        http.cors(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable)
                .headers(header -> header.addHeaderWriter((request, response) -> response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type")))
                .headers(header -> header.addHeaderWriter((request, response) -> response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/swagger-resources", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/authentication/authenticate").permitAll()
                        .requestMatchers("/api/v1/password-reset/**").permitAll()

                        .requestMatchers(HttpMethod.GET, usersPath).hasAuthority("user:view")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/").hasAuthority("user:create")
                        .requestMatchers(HttpMethod.PUT, usersPath).hasAuthority("user:update")
                        .requestMatchers(HttpMethod.DELETE, usersPath).hasAuthority("user:delete")

                        .requestMatchers(HttpMethod.GET, rolesPath).hasAuthority("role:view")
                        .requestMatchers(HttpMethod.POST, "/api/v1/roles/").hasAuthority("role:create")
                        .requestMatchers(HttpMethod.PUT, rolesPath).hasAuthority("role:update")
                        .requestMatchers(HttpMethod.DELETE, rolesPath).hasAuthority("role:delete")

                        .requestMatchers(HttpMethod.GET, permissionsPath).hasAuthority("permission:view")
                        .requestMatchers(HttpMethod.POST, "/api/v1/permissions/").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, permissionsPath).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, permissionsPath).hasRole(ADMIN)

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}