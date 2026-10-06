package com.projectexpress.api.profile_service.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Login/Register must work without JWT
                        .requestMatchers("/api/auth/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception

                        // No JWT / invalid JWT
                        .authenticationEntryPoint(
                                (request, response, authException) -> {

                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );
                                    response.setContentType("application/json");

                                    response.getWriter().write("""
                                            {
                                                "status": 401,
                                                "error": "Unauthorized",
                                                "message": "Authentication is required to access this resource"
                                            }
                                            """);
                                }
                        )

                        // Authenticated, but insufficient authority
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {

                                    response.setStatus(
                                            HttpServletResponse.SC_FORBIDDEN
                                    );
                                    response.setContentType("application/json");

                                    response.getWriter().write("""
                                            {
                                                "status": 403,
                                                "error": "Forbidden",
                                                "message": "You do not have permission to access this resource"
                                            }
                                            """);
                                }
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
