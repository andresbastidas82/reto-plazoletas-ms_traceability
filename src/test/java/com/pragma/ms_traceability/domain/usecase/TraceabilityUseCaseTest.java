package com.pragma.ms_traceability.domain.usecase;

import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.domain.spi.IAuthenticationServicePort;
import com.pragma.ms_traceability.domain.spi.ITraceabilityPersistencePort;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceabilityUseCaseTest {

    @Mock
    private ITraceabilityPersistencePort traceabilityPersistencePort;

    @Mock
    private IAuthenticationServicePort authenticationServicePort;

    @InjectMocks
    private TraceabilityUseCase traceabilityUseCase;

    private Traceability traceability;

    @BeforeEach
    void setUp() {
        traceability = new Traceability("id123", 1L, 2L, 3L, "PREPARING", "READY", LocalDateTime.now().minusHours(1), LocalDateTime.now());
    }

    @Test
    void saveTraceability_whenNewStateIsPending_shouldSetDatePreviousStateFromNewDate() {
        // Arrange
        traceability.setNewState("PENDING");
        traceability.setDatePreviousState(null); // Aseguramos que está nulo al inicio
        LocalDateTime newDate = LocalDateTime.now();
        traceability.setDateNewState(newDate);

        when(traceabilityPersistencePort.saveTraceability(any(Traceability.class))).thenReturn(traceability);

        // Act
        traceabilityUseCase.saveTraceability(traceability);

        // Assert
        // Capturamos el objeto que se pasa al método save para inspeccionarlo
        ArgumentCaptor<Traceability> traceabilityCaptor = ArgumentCaptor.forClass(Traceability.class);
        verify(traceabilityPersistencePort).saveTraceability(traceabilityCaptor.capture());

        Traceability capturedTraceability = traceabilityCaptor.getValue();
        assertEquals(newDate, capturedTraceability.getDatePreviousState());
        verify(traceabilityPersistencePort, never()).getLastTraceabilityByOrderId(anyLong());
    }

    @Test
    void saveTraceability_whenNewStateIsNotPendingAndLastTraceabilityExists_shouldSetDatePreviousStateFromLast() {
        // Arrange
        traceability.setNewState("DELIVERED");
        traceability.setDatePreviousState(null);

        LocalDateTime lastDate = LocalDateTime.now().minusMinutes(30);
        Traceability lastTraceability = new Traceability();
        lastTraceability.setDateNewState(lastDate);

        when(traceabilityPersistencePort.getLastTraceabilityByOrderId(traceability.getOrderId())).thenReturn(lastTraceability);
        when(traceabilityPersistencePort.saveTraceability(any(Traceability.class))).thenReturn(traceability);

        // Act
        traceabilityUseCase.saveTraceability(traceability);

        // Assert
        ArgumentCaptor<Traceability> traceabilityCaptor = ArgumentCaptor.forClass(Traceability.class);
        verify(traceabilityPersistencePort).saveTraceability(traceabilityCaptor.capture());

        Traceability capturedTraceability = traceabilityCaptor.getValue();
        assertEquals(lastDate, capturedTraceability.getDatePreviousState());
        verify(traceabilityPersistencePort, times(1)).getLastTraceabilityByOrderId(traceability.getOrderId());
    }

    @Test
    void saveTraceability_whenNewStateIsNotPendingAndLastTraceabilityNotExists_shouldKeepDatePreviousStateNull() {
        // Arrange
        traceability.setNewState("DELIVERED");
        traceability.setDatePreviousState(null);

        when(traceabilityPersistencePort.getLastTraceabilityByOrderId(traceability.getOrderId())).thenReturn(null);
        when(traceabilityPersistencePort.saveTraceability(any(Traceability.class))).thenReturn(traceability);

        // Act
        traceabilityUseCase.saveTraceability(traceability);

        // Assert
        ArgumentCaptor<Traceability> traceabilityCaptor = ArgumentCaptor.forClass(Traceability.class);
        verify(traceabilityPersistencePort).saveTraceability(traceabilityCaptor.capture());

        Traceability capturedTraceability = traceabilityCaptor.getValue();
        assertNull(capturedTraceability.getDatePreviousState());
        verify(traceabilityPersistencePort, times(1)).getLastTraceabilityByOrderId(traceability.getOrderId());
    }

    @Test
    void getLogsByFilters_shouldSetCustomerIdFromTokenAndCallPersistence() {
        // Arrange
        int page = 0;
        int size = 10;
        Long customerIdFromToken = 12345L;
        Traceability filter = new Traceability(); // Filtro inicial sin customerId

        Page<Traceability> expectedPage = new PageImpl<>(List.of(traceability));

        when(authenticationServicePort.getUserIdOfToken()).thenReturn(customerIdFromToken);
        when(traceabilityPersistencePort.getLogsByFilters(any(Traceability.class), eq(page), eq(size))).thenReturn(expectedPage);

        // Act
        Page<Traceability> resultPage = traceabilityUseCase.getLogsByFilters(filter, page, size);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());

        // Verificamos que el customerId se haya seteado en el filtro antes de pasarlo
        ArgumentCaptor<Traceability> filterCaptor = ArgumentCaptor.forClass(Traceability.class);
        verify(traceabilityPersistencePort).getLogsByFilters(filterCaptor.capture(), eq(page), eq(size));
        assertEquals(customerIdFromToken, filterCaptor.getValue().getCustomerId());

        verify(authenticationServicePort, times(1)).getUserIdOfToken();
    }

    @Test
    void getEmployeePerformanceRanking_shouldCallPersistenceAndReturnResult() {
        // Arrange
        int limit = 5;
        List<EmployeeRankingDTO> expectedRanking = Collections.singletonList(
                new EmployeeRankingDTO(1L, 1, 25.5, Collections.emptyList())
        );

        when(traceabilityPersistencePort.getEmployeePerformanceRanking(limit)).thenReturn(expectedRanking);

        // Act
        List<EmployeeRankingDTO> actualRanking = traceabilityUseCase.getEmployeePerformanceRanking(limit);

        // Assert
        assertNotNull(actualRanking);
        assertEquals(expectedRanking.size(), actualRanking.size());
        assertEquals(expectedRanking.get(0).employeeId(), actualRanking.get(0).employeeId());

        verify(traceabilityPersistencePort, times(1)).getEmployeePerformanceRanking(limit);
    }
}