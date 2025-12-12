package com.pragma.ms_traceability.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtService.extractAllClaims(jwt);

                // 1. Extraer la lista de roles
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                if (roles == null) {
                    roles = Collections.emptyList();
                }

                // 2. Extraer el objeto "user" del token
                Map<String, Object> userData = claims.get("user", Map.class);
                if (userData == null) {
                    log.warn("JWT token is missing 'user' object claim.");
                    filterChain.doFilter(request, response);
                    return;
                }

                // 3. Extraer los datos del usuario del objeto anidado
                Long userId = userData.get("id") != null ? Long.valueOf(userData.get("id").toString()) : null;
                String userName = userData.get("name") != null ? userData.get("name").toString() : null;
                Long restaurantId = userData.get("restaurantId") != null ? Long.valueOf(userData.get("restaurantId").toString()) : null;

                // 4. Crear el objeto UserPrincipal con todos los datos
                UserPrincipal principal = new UserPrincipal(userId, userName, userEmail, restaurantId);

                // 5. Convertir roles a autoridades
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // 6. Crear el token de autenticación usando el objeto UserPrincipal
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal, // <-- El principal ahora es un objeto rico en información
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("User '{}' with ID {} and roles {} successfully authenticated.", principal.getName(), principal.getId(), roles);
            }
        } catch (Exception e) {
            log.warn("Invalid JWT Token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
