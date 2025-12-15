package com.pragma.ms_traceability.application.handler.impl;

import com.pragma.ms_traceability.application.dto.GenericResponse;
import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.application.mapper.ITraceabilityRequestMapper;
import com.pragma.ms_traceability.domain.api.ITraceabilityServicePort;
import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceabilityHandlerTest {

    @Mock
    private ITraceabilityServicePort traceabilityServicePort;

    @Mock
    private ITraceabilityRequestMapper traceabilityRequestMapper;

    @InjectMocks
    private TraceabilityHandler traceabilityHandler;

    private TraceabilityRequest traceabilityRequest;
    private Traceability traceability;

    @BeforeEach
    void setUp() {
        // Objeto común para varios tests
        traceabilityRequest = new TraceabilityRequest(1L, 2L, 3L, "PENDING", "PREPARING", LocalDateTime.now());
        traceability = new Traceability("id123", 1L, 2L, 3L, "PENDING", "PREPARING", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void saveTraceability_shouldSaveAndReturnSuccessResponse() {
        // Arrange (Organizar)
        // 1. Definir el comportamiento de los mocks
        when(traceabilityRequestMapper.toTraceability(any(TraceabilityRequest.class))).thenReturn(traceability);
        // No necesitamos mockear el retorno de saveTraceability porque el handler no lo usa,
        // pero sí necesitamos asegurarnos de que se llame.

        // Act (Actuar)
        // 2. Llamar al metodo que se está probando
        GenericResponse response = traceabilityHandler.saveTraceability(traceabilityRequest);

        // Assert (Afirmar)
        // 3. Verificar los resultados y las interacciones
        assertNotNull(response);
        assertEquals("CREATED", response.getCode());
        assertEquals(HttpStatus.CREATED.value(), response.getHttpStatus());
        assertEquals("Traceability saved successfully", response.getMessage());

        // Verificar que los mocks fueron llamados como se esperaba
        verify(traceabilityRequestMapper, times(1)).toTraceability(traceabilityRequest);
        verify(traceabilityServicePort, times(1)).saveTraceability(traceability);
    }

    @Test
    void getLogsByOrderId_shouldReturnPageOfLogs() {
        // Arrange (Organizar)
        int page = 0;
        int size = 10;

        // 1. Preparar los datos de entrada y salida esperados
        LogsResponse logsResponse = new LogsResponse(1L, "PENDING", "PREPARING", LocalDateTime.now().toString(), LocalDateTime.now().toString(), BigDecimal.TEN, null, null);
        Page<Traceability> traceabilityPage = new PageImpl<>(List.of(traceability));

        // 2. Definir el comportamiento de los mocks
        when(traceabilityRequestMapper.toTraceability(any(TraceabilityRequest.class))).thenReturn(traceability);
        when(traceabilityServicePort.getLogsByFilters(traceability, page, size)).thenReturn(traceabilityPage);
        when(traceabilityRequestMapper.toLogsResponse(any(Traceability.class))).thenReturn(logsResponse);

        // Act (Actuar)
        // 3. Llamar al metodo que se está probando
        Page<LogsResponse> resultPage = traceabilityHandler.getLogsByOrderId(traceabilityRequest, page, size);

        // Assert (Afirmar)
        // 4. Verificar los resultados
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(logsResponse.getOrderId(), resultPage.getContent().get(0).getOrderId());

        // Verificar que los mocks fueron llamados
        verify(traceabilityRequestMapper, times(1)).toTraceability(traceabilityRequest);
        verify(traceabilityServicePort, times(1)).getLogsByFilters(traceability, page, size);
        // El .map en la Page se encarga de llamar a toLogsResponse, verificamos que se llamó una vez por cada elemento en la página
        verify(traceabilityRequestMapper, times(1)).toLogsResponse(traceability);
    }

    @Test
    void getPerformanceRanking_shouldReturnListOfRankings() {
        // Arrange (Organizar)
        int size = 5;
        List<EmployeeRankingDTO> expectedRanking = Collections.singletonList(
                new EmployeeRankingDTO(1L, 1, 25.5, Collections.emptyList())
        );

        // 1. Definir el comportamiento del mock
        when(traceabilityServicePort.getEmployeePerformanceRanking(size)).thenReturn(expectedRanking);

        // Act (Actuar)
        // 2. Llamar al metodo que se está probando
        List<EmployeeRankingDTO> actualRanking = traceabilityHandler.getPerformanceRanking(size);

        // Assert (Afirmar)
        // 3. Verificar los resultados
        assertNotNull(actualRanking);
        assertEquals(expectedRanking.size(), actualRanking.size());
        assertEquals(expectedRanking.get(0).employeeId(), actualRanking.get(0).employeeId());

        // Verificar que el mock fue llamado
        verify(traceabilityServicePort, times(1)).getEmployeePerformanceRanking(size);
    }
}