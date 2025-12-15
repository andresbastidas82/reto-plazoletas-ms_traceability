package com.pragma.ms_traceability.infrastructure.security.adapter;

import com.pragma.ms_traceability.infrastructure.security.PrincipalProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthAdapterTest {

    @Mock
    private PrincipalProvider principalProvider;

    @InjectMocks
    private AuthAdapter authAdapter;

    @Test
    void getUserIdOfToken_shouldReturnUserIdFromProvider() {
        // Arrange
        Long expectedUserId = 123L;
        when(principalProvider.getUserId()).thenReturn(expectedUserId);

        // Act
        Long actualUserId = authAdapter.getUserIdOfToken();

        // Assert
        assertEquals(expectedUserId, actualUserId);
        verify(principalProvider, times(1)).getUserId();
    }

    @Test
    void getNameOfToken_shouldReturnUserNameFromProvider() {
        // Arrange
        String expectedName = "Test User";
        when(principalProvider.getUserName()).thenReturn(expectedName);

        // Act
        String actualName = authAdapter.getNameOfToken();

        // Assert
        assertEquals(expectedName, actualName);
        verify(principalProvider, times(1)).getUserName();
    }

    @Test
    void getEmailOfToken_shouldReturnUserEmailFromProvider() {
        // Arrange
        String expectedEmail = "test@example.com";
        when(principalProvider.getUserEmail()).thenReturn(expectedEmail);

        // Act
        String actualEmail = authAdapter.getEmailOfToken();

        // Assert
        assertEquals(expectedEmail, actualEmail);
        verify(principalProvider, times(1)).getUserEmail();
    }

    @Test
    void getRestaurantIdOfToken_shouldReturnRestaurantIdFromProvider() {
        // Arrange
        Long expectedRestaurantId = 456L;
        when(principalProvider.getRestaurantId()).thenReturn(expectedRestaurantId);

        // Act
        Long actualRestaurantId = authAdapter.getRestaurantIdOfToken();

        // Assert
        assertEquals(expectedRestaurantId, actualRestaurantId);
        verify(principalProvider, times(1)).getRestaurantId();
    }
}