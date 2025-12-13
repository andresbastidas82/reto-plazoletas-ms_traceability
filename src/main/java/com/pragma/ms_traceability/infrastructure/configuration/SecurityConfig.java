package com.pragma.ms_traceability.infrastructure.configuration;

import com.pragma.ms_traceability.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String ROLE_OWNER = "OWNER";
    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF: Esencial para APIs REST stateless.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Manejo de excepciones: Usar nuestro EntryPoint para errores 401.
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))

                // 3. Gestión de sesión: No crear ni usar sesiones HTTP.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Definir reglas de autorización para las rutas.
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas sin autenticación.
                        .requestMatchers("/api/v1/traceability/performance-ranking").hasAnyRole(ROLE_OWNER, ROLE_ADMIN)

                        .requestMatchers("/api/v1/traceability/save").hasAnyRole(ROLE_EMPLOYEE, ROLE_OWNER, ROLE_CUSTOMER, ROLE_ADMIN)
                        .requestMatchers("/api/v1/traceability/logs-customer").hasAnyRole(ROLE_CUSTOMER, ROLE_EMPLOYEE, ROLE_OWNER, ROLE_ADMIN)

                        // Cualquier otra petición requiere que el usuario esté autenticado.
                        .anyRequest().authenticated()
                )

                // 5. Añadir nuestro filtro JWT antes del filtro de autenticación estándar.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
