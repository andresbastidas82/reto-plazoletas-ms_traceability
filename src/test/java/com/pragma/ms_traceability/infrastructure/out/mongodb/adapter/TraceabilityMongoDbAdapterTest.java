package com.pragma.ms_traceability.infrastructure.out.mongodb.adapter;

import com.pragma.ms_traceability.domain.model.Traceability;
import com.pragma.ms_traceability.infrastructure.out.mongodb.document.TraceabilityDocument;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.EmployeeRankingDTO;
import com.pragma.ms_traceability.infrastructure.out.mongodb.dto.OrderRankingDTO;
import com.pragma.ms_traceability.infrastructure.out.mongodb.mapper.ITraceabilityDocumentMapper;
import com.pragma.ms_traceability.infrastructure.out.mongodb.repository.ITraceabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceabilityMongoDbAdapterTest {

    @Mock
    private ITraceabilityRepository traceabilityRepository;

    @Mock
    private ITraceabilityDocumentMapper traceabilityDocumentMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TraceabilityMongoDbAdapter traceabilityMongoDbAdapter;

    private Traceability traceability;
    private TraceabilityDocument traceabilityDocument;

    @BeforeEach
    void setUp() {
        traceability = new Traceability("id123", 1L, 2L, 3L, "PENDING", "PREPARING", LocalDateTime.now(), LocalDateTime.now());
        traceabilityDocument = new TraceabilityDocument(); // Los datos se pueden setear si es necesario
        traceabilityDocument.setId("id123");
        traceabilityDocument.setOrderId(1L);
    }

    @Test
    void saveTraceability_shouldMapAndSaveDocument() {
        // Arrange
        when(traceabilityDocumentMapper.toDocument(any(Traceability.class))).thenReturn(traceabilityDocument);
        when(traceabilityRepository.save(any(TraceabilityDocument.class))).thenReturn(traceabilityDocument);
        when(traceabilityDocumentMapper.toModel(any(TraceabilityDocument.class))).thenReturn(traceability);

        // Act
        Traceability result = traceabilityMongoDbAdapter.saveTraceability(traceability);

        // Assert
        assertNotNull(result);
        assertEquals(traceability.getId(), result.getId());
        verify(traceabilityDocumentMapper, times(1)).toDocument(traceability);
        verify(traceabilityRepository, times(1)).save(traceabilityDocument);
        verify(traceabilityDocumentMapper, times(1)).toModel(traceabilityDocument);
    }

    @Test
    void getLastTraceabilityByOrderId_whenFound_shouldReturnTraceability() {
        // Arrange
        Long orderId = 1L;
        when(traceabilityRepository.findTopByOrderIdOrderByDateNewStateDesc(orderId)).thenReturn(Optional.of(traceabilityDocument));
        when(traceabilityDocumentMapper.toModel(traceabilityDocument)).thenReturn(traceability);

        // Act
        Traceability result = traceabilityMongoDbAdapter.getLastTraceabilityByOrderId(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(traceability.getOrderId(), result.getOrderId());
        verify(traceabilityRepository, times(1)).findTopByOrderIdOrderByDateNewStateDesc(orderId);
        verify(traceabilityDocumentMapper, times(1)).toModel(traceabilityDocument);
    }

    /*@Test
    void getLastTraceabilityByOrderId_whenNotFound_shouldReturnNull() {
        // Arrange
        Long orderId = 99L;
        when(traceabilityRepository.findTopByOrderIdOrderByDateNewStateDesc(orderId)).thenReturn(Optional.empty());

        // Act
        Traceability result = traceabilityMongoDbAdapter.getLastTraceabilityByOrderId(orderId);

        // Assert
        assertNull(result);
        verify(traceabilityRepository, times(1)).findTopByOrderIdOrderByDateNewStateDesc(orderId);
        verify(traceabilityDocumentMapper, never()).toModel(any());
    }*/

    @Test
    void getLogsByFilters_shouldBuildQueryAndReturnPage() {
        // Arrange
        int page = 0;
        int size = 10;
        Traceability filter = new Traceability();
        filter.setCustomerId(3L);

        List<TraceabilityDocument> documents = List.of(traceabilityDocument);
        long totalElements = 1L;

        when(mongoTemplate.count(any(Query.class), eq(TraceabilityDocument.class))).thenReturn(totalElements);
        when(mongoTemplate.find(any(Query.class), eq(TraceabilityDocument.class))).thenReturn(documents);
        when(traceabilityDocumentMapper.toModel(any(TraceabilityDocument.class))).thenReturn(traceability);

        // Act
        Page<Traceability> resultPage = traceabilityMongoDbAdapter.getLogsByFilters(filter, page, size);

        // Assert
        assertNotNull(resultPage);
        assertEquals(totalElements, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals(traceability.getId(), resultPage.getContent().get(0).getId());

        verify(mongoTemplate, times(1)).count(any(Query.class), eq(TraceabilityDocument.class));
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(TraceabilityDocument.class));
        verify(traceabilityDocumentMapper, times(1)).toModel(traceabilityDocument);
    }

    @Test
    void getEmployeePerformanceRanking_shouldExecuteAggregationAndReturnRankedList() {
        // Arrange
        int limit = 5;
        // 1. Simular la respuesta de la agregación de MongoDB (sin el ranking final)
        List<OrderRankingDTO> orders = List.of(new OrderRankingDTO(101L, 25.0));
        EmployeeRankingDTO dtoFromMongo = new EmployeeRankingDTO(1L, null, 25.5, orders);
        List<EmployeeRankingDTO> mongoResults = List.of(dtoFromMongo);

        // 2. Mockear la clase de resultados de la agregación
        AggregationResults<EmployeeRankingDTO> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(mongoResults);

        // 3. Mockear la llamada a mongoTemplate.aggregate
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("traceability_logs"), eq(EmployeeRankingDTO.class)))
                .thenReturn(aggregationResults);

        // Act
        List<EmployeeRankingDTO> finalResult = traceabilityMongoDbAdapter.getEmployeePerformanceRanking(limit);

        // Assert
        assertNotNull(finalResult);
        assertEquals(1, finalResult.size());

        // 4. Verificar que el ranking se aplicó correctamente en Java
        EmployeeRankingDTO rankedResult = finalResult.get(0);
        assertEquals(1L, rankedResult.employeeId());
        assertEquals(1, rankedResult.ranking()); // El ranking debe ser 1
        assertEquals(25.5, rankedResult.averageTime());
        assertEquals(1, rankedResult.orders().size());

        verify(mongoTemplate, times(1)).aggregate(any(Aggregation.class), eq("traceability_logs"), eq(EmployeeRankingDTO.class));
    }
}