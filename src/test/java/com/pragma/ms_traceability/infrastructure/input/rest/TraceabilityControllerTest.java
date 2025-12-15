package com.pragma.ms_traceability.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.ms_traceability.application.dto.GenericResponse;
import com.pragma.ms_traceability.application.dto.LogsResponse;
import com.pragma.ms_traceability.application.dto.TraceabilityRequest;
import com.pragma.ms_traceability.application.handler.ITraceabilityHandler;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.OrderRankingDTO;
import com.pragma.ms_traceability.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// --- FIN DE LOS IMPORTS ---
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraceabilityController.class)
class TraceabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITraceabilityHandler traceabilityHandler;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void saveTraceability_shouldReturnGenericResponse() throws Exception {
        // Arrange
        TraceabilityRequest request = new TraceabilityRequest(1L, 2L, 3L, "PENDING", "PREPARING", LocalDateTime.now());
        GenericResponse response = GenericResponse.builder()
                .code("CREATED")
                .httpStatus(HttpStatus.CREATED.value())
                .message("Traceability saved successfully")
                .build();

        when(traceabilityHandler.saveTraceability(any(TraceabilityRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/traceability/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // <-- 2. AÑADE ESTO PARA DESHABILITAR CSRF EN LA PETICIÓN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.message").value("Traceability saved successfully"));
    }

    @Test
    @WithMockUser
    void getLogsByOrderId_shouldReturnPageOfLogs() throws Exception {
        // Arrange
        TraceabilityRequest request = new TraceabilityRequest(1L, null, 3L, null, null, null);
        LogsResponse logsResponse = new LogsResponse(1L, "PENDING", "PREPARING", LocalDateTime.now().toString(), LocalDateTime.now().toString(), 2L, 3L);
        Page<LogsResponse> responsePage = new PageImpl<>(List.of(logsResponse));

        when(traceabilityHandler.getLogsByOrderId(any(TraceabilityRequest.class), anyInt(), anyInt())).thenReturn(responsePage);

        // Act & Assert
        mockMvc.perform(post("/api/v1/traceability/logs-customer")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())) // <-- 2. AÑADE ESTO TAMBIÉN AQUÍ
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value(1L))
                .andExpect(jsonPath("$.content[0].previousState").value("PENDING"));
    }

    @Test
    @WithMockUser
    void getPerformanceRanking_shouldReturnListOfRankings() throws Exception {
        // Arrange
        int size = 5;
        List<OrderRankingDTO> orders = List.of(new OrderRankingDTO(101L, 25.0));
        List<EmployeeRankingDTO> rankingList = Collections.singletonList(
                new EmployeeRankingDTO(1L, 1, 25.5, orders)
        );

        when(traceabilityHandler.getPerformanceRanking(size)).thenReturn(rankingList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/traceability/performance-ranking")
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeId").value(1L))
                .andExpect(jsonPath("$[0].ranking").value(1))
                .andExpect(jsonPath("$[0].averageTime").value(25.5));
    }
}