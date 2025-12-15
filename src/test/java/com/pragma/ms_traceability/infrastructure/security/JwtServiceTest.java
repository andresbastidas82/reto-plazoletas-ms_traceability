package com.pragma.ms_traceability.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    // Usaremos una clave secreta consistente para generar y validar tokens en los tests.
    // Debe ser lo suficientemente larga para el algoritmo HS256.
    private final String SECRET_KEY = "dGhpcy1pcy1hLXZlcnktbG9uZy1hbmQtc2VjdXJlLXNlY3JldC1rZXktZm9yLXRlc3Rpbmc=";

    private String validToken;
    private String expiredToken;
    private final String testUsername = "test@example.com";

    @BeforeEach
    void setUp() {
        // Inyectamos la clave secreta en la instancia de JwtService antes de cada test.
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);

        // Generamos tokens para usar en los tests
        validToken = generateTestToken(new HashMap<>(), testUsername, 1000 * 60 * 15); // 15 minutos de validez
        expiredToken = generateTestToken(new HashMap<>(), testUsername, -1000 * 60); // Expirado hace 1 minuto
    }

    @Test
    @DisplayName("Debería extraer el nombre de usuario correctamente de un token válido")
    void extractUsername_shouldReturnCorrectUsername() {
        // Arrange & Act
        String extractedUsername = jwtService.extractUsername(validToken);

        // Assert
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    @DisplayName("Debería extraer todos los claims de un token válido")
    void extractAllClaims_shouldReturnAllClaims() {
        // Arrange & Act
        Claims claims = jwtService.extractAllClaims(validToken);

        // Assert
        assertNotNull(claims);
        assertEquals(testUsername, claims.getSubject());
    }

    @Test
    @DisplayName("Debería validar un token cuando el nombre de usuario coincide y no está expirado")
    void isTokenValid_whenTokenIsValidAndUsernameMatches_shouldReturnTrue() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(testUsername);

        // Act
        boolean isValid = jwtService.isTokenValid(validToken, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("No debería validar un token cuando el nombre de usuario no coincide")
    void isTokenValid_whenUsernameDoesNotMatch_shouldReturnFalse() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("another-user@example.com");

        // Act
        boolean isValid = jwtService.isTokenValid(validToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    /**
     * Helper para generar tokens JWT para los tests.
     * @param extraClaims Claims adicionales a incluir.
     * @param username El sujeto del token.
     * @param expirationInMillis Duración de la validez del token en milisegundos.
     * @return Un string con el token JWT.
     */
    private String generateTestToken(Map<String, Object> extraClaims, String username, long expirationInMillis) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}