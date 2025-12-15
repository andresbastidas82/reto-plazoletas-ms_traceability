package com.pragma.ms_traceability.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Limpia el contexto de seguridad después de cada test para evitar interferencias
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenHeaderIsValid_shouldAuthenticateUser() throws ServletException, IOException {
        // Arrange
        final String jwt = "valid-jwt-token";
        final String authHeader = "Bearer " + jwt;
        final String userEmail = "test@example.com";
        final Long userId = 1L;
        final String userName = "Test User";
        final Long restaurantId = 10L;
        final List<String> roles = List.of("ROLE_EMPLOYEE");

        // 1. Simular el header de la petición
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // 2. Simular la extracción de datos del JWT
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);

        // 3. Crear los claims que el servicio devolvería
        Claims claims = new DefaultClaims();
        claims.put("roles", roles);
        claims.put("user", Map.of(
                "id", userId,
                "name", userName,
                "restaurantId", restaurantId
        ));
        when(jwtService.extractAllClaims(jwt)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // 4. Verificar que la autenticación se estableció en el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        // 5. Verificar que el "principal" es nuestro objeto UserPrincipal con los datos correctos
        assertTrue(authentication.getPrincipal() instanceof UserPrincipal);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals(userId, principal.getId());
        assertEquals(userName, principal.getName());
        assertEquals(userEmail, principal.getEmail());
        assertEquals(restaurantId, principal.getRestaurantId());

        // 6. Verificar que los roles se asignaron correctamente
        assertEquals(1, authentication.getAuthorities().size());
        assertEquals("ROLE_EMPLOYEE", authentication.getAuthorities().iterator().next().getAuthority());

        // 7. Verificar que el filtro continuó la cadena
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenHeaderIsMissing_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Verificar que no se estableció ninguna autenticación
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // Verificar que el filtro continuó la cadena
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenHeaderDoesNotStartWithBearer_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic some-credentials");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenJwtIsInvalid_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        final String jwt = "invalid-jwt-token";
        final String authHeader = "Bearer " + jwt;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        // Simular una excepción al procesar el token
        when(jwtService.extractUsername(jwt)).thenThrow(new RuntimeException("Token expired"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenUserClaimIsMissing_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        final String jwt = "valid-jwt-no-user-claim";
        final String authHeader = "Bearer " + jwt;
        final String userEmail = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);

        // Simular claims sin el objeto "user"
        Claims claims = new DefaultClaims();
        claims.put("roles", List.of("ROLE_ADMIN"));
        when(jwtService.extractAllClaims(jwt)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}